/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.data.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import urbosenti.core.data.DataManager;
import urbosenti.core.device.BaseComponentManager;
import urbosenti.core.device.ComponentManager;
import urbosenti.core.device.model.Content;
import urbosenti.core.device.model.DataType;
import urbosenti.core.device.model.Entity;
import urbosenti.core.device.model.EventModel;
import urbosenti.core.device.model.EventTarget;
import urbosenti.core.device.model.Implementation;
import urbosenti.core.device.model.Parameter;
import urbosenti.core.device.model.PossibleContent;
import urbosenti.core.device.model.TargetOrigin;
import urbosenti.core.events.Action;
import urbosenti.core.events.Event;
import urbosenti.core.events.SystemEvent;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class EventModelDAO {

	private SQLiteDatabase database;
	private DataManager dataManager;

    public EventModelDAO(Object context) {
    	this.database = (SQLiteDatabase) context;
    	this.dataManager = null;
    }
    
    public EventModelDAO(Object context, DataManager dataManager) {
    	this.database = (SQLiteDatabase) context;
    	this.dataManager = dataManager;
    }

    public void insert(EventModel event) throws SQLException {
        //String sql = "INSERT INTO events (model_id,description,synchronous,implementation_type_id,entity_id) "
        //        + " VALUES (?,?,?,?,?);";
        event.setModelId(event.getId());
        ContentValues values = new ContentValues();
        values.put("model_id", event.getId());
        values.put("description", event.getDescription());
        values.put("synchronous", event.isSynchronous());
        values.put("implementation_type_id", event.getImplementation().getId());
        values.put("entity_id", event.getEntity().getId());
        
        event.setId((int)(long)this.database.insertOrThrow("events", null, values));
        
        if (DeveloperSettings.SHOW_DAO_SQL) {
        	Log.d("SQL_DEBUG","INSERT INTO events (id,model_id,description,synchronous,implementation_type_id,entity_id) "
                    + " VALUES (" + event.getId() + "," + event.getModelId() + ",'" + event.getDescription() + "'," + event.isSynchronous() + ","
                    + event.getImplementation().getId() + "," + event.getEntity().getId() + ");");
        }
    }

    public void insertParameters(EventModel event) throws SQLException {
       // String sql = "INSERT INTO event_parameters (description,optional,parameter_label,superior_limit,inferior_limit,initial_value,entity_state_id,data_type_id,event_id) "
        //        + " VALUES (?,?,?,?,?,?,?,?,?);";
        
        if (event.getParameters() != null) {
            for (Parameter parameter : event.getParameters()) {
            	ContentValues values = new ContentValues();
                // trata o tipo de dado do estado
                parameter.setSuperiorLimit(Content.parseContent(parameter.getDataType(), parameter.getSuperiorLimit()));
                parameter.setInferiorLimit(Content.parseContent(parameter.getDataType(), parameter.getInferiorLimit()));
                parameter.setInitialValue(Content.parseContent(parameter.getDataType(), parameter.getInitialValue()));
                // valores
                values.put("description", parameter.getDescription());
                values.put("optional", parameter.isOptional());
                values.put("parameter_label", parameter.getLabel());
                values.put("superior_limit", String.valueOf(parameter.getSuperiorLimit()));
                values.put("inferior_limit", String.valueOf(parameter.getInferiorLimit()));
                values.put("initial_value", String.valueOf(parameter.getInitialValue()));
                values.put("entity_state_id", (parameter.getRelatedState() == null) ? -1 : parameter.getRelatedState().getId());
                values.put("data_type_id", parameter.getDataType().getId());
                values.put("event_id", event.getId());
                // executar
                parameter.setId((int)(long)this.database.insertOrThrow("event_parameters", null, values));
                
                if (DeveloperSettings.SHOW_DAO_SQL) {
                    Log.d("SQL_DEBUG","INSERT INTO event_parameters (id,description,optional,parameter_label,superior_limit,inferior_limit,initial_value,entity_state_id,data_type_id,event_id) "
                            + " VALUES (" + parameter.getId() + ",'" + parameter.getDescription() + "','" + parameter.getLabel() + "','" + parameter.getSuperiorLimit()
                            + "','" + parameter.getInferiorLimit() + "','" + parameter.getInitialValue() + "'," + ((parameter.getRelatedState() == null) ? -1 : parameter.getRelatedState().getId())
                            + "," + parameter.getDataType().getId() + "," + event.getId() + ");");
                }

            }
        }
    }

    /**
     *
     * @param parameter
     * @throws SQLException
     */
    public void insertPossibleParameterContents(Parameter parameter) throws SQLException {
        //String sql = "INSERT INTO possible_event_contents (possible_value, default_value, event_parameter_id) "
        //        + " VALUES (?,?,?);";
        
        if (parameter.getPossibleContents() != null) {
            for (PossibleContent possibleContent : parameter.getPossibleContents()) {
            	// valores
            	ContentValues values = new ContentValues();
            	values.put("possible_value", String.valueOf(Content.parseContent(parameter.getDataType(),possibleContent.getValue())));
            	values.put("default_value", possibleContent.isIsDefault());
            	values.put("event_parameter_id", parameter.getId());
            	// executar
            	possibleContent.setId((int)(long)this.database.insertOrThrow("possible_event_contents", null, values));
            	
                if (DeveloperSettings.SHOW_DAO_SQL) {
                    Log.d("SQL_DEBUG","INSERT INTO possible_event_contents (id,possible_value, default_value, event_parameter_id) "
                            + " VALUES (" + possibleContent.getId() + "," + Content.parseContent(parameter.getDataType(), possibleContent.getValue()) + "," + possibleContent.isIsDefault() + "," + parameter.getId() + ");");
                }
            }
        }
    }

    public void insertTargets(EventModel event) throws SQLException {
        //String sql = "INSERT INTO event_targets_origins (event_id,target_origin_id,mandatory) "
        //        + " VALUES (?,?,?);";
        
        for (EventTarget target : event.getTargets()) {  
            ContentValues values = new ContentValues();
        	values.put("event_id", event.getId());
        	values.put("target_origin_id", target.getTarget().getId());
        	values.put("mandatory", target.isMandatory());
        	// executar
        	this.database.insertOrThrow("event_targets_origins", null, values);
            
            if (DeveloperSettings.SHOW_DAO_SQL) {
                Log.d("SQL_DEBUG","INSERT INTO event_targets_origins (event_id,target_origin_id,mandatory) "
                        + " VALUES (" + event.getId() + "," + target.getTarget().getId() + "," + target.isMandatory() + ");");
            }
        }
    }

    public Content getCurrentContentValue(Parameter parameter) throws SQLException {
        Content content = null;
        String sql = "SELECT id, reading_value, reading_time "
                + "FROM event_contents\n"
                + "WHERE event_parameter_id = ? ORDER BY id DESC LIMIT 1;";
        
        // consulta
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(parameter.getId())});
       
        if (cursor.moveToFirst()) {
        	content = new Content();
            // pegar o valor atual
            content.setId(cursor.getInt(cursor.getColumnIndex("id")));
            content.setTime(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("reading_time")))));
            content.setValue(Content.parseContent(parameter.getDataType(), cursor.getString(cursor.getColumnIndex("reading_value"))));
        }
        return content;
    }

    public void insertContent(Parameter parameter, Event event) throws SQLException {
        //String sql = "INSERT INTO event_contents (reading_value,reading_time,event_parameter_id, generated_event_id) "
        //        + " VALUES (?,?,?,?);";
        
        ContentValues values = new ContentValues();
    	values.put("reading_value", String.valueOf(Content.parseContent(parameter.getDataType(), parameter.getContent().getValue())));
    	values.put("reading_time", parameter.getContent().getTime().getTime());
    	values.put("event_parameter_id", parameter.getId());
    	values.put("generated_event_id", event.getDatabaseId());
    	// executar
    	parameter.getContent().setId((int)(long)this.database.insertOrThrow("event_contents", null, values));

        if (DeveloperSettings.SHOW_DAO_SQL) {
            Log.d("SQL_DEBUG","INSERT INTO event_contents (id,reading_value,reading_time,event_parameter_id) "
                    + " VALUES (" + parameter.getContent().getId() + ",'" + Content.parseContent(parameter.getDataType(), parameter.getContent().getValue()) + "',"
                    + ",'" + parameter.getContent().getTime().getTime() + "'," + "," + parameter.getId() + ");");
        }
    }

    public List<EventModel> getEntityEventModels(Entity entity) throws SQLException {
        List<EventModel> events = new ArrayList();
        EventModel event = null;
        String sql = "SELECT events.id as event_id, model_id, events.description as event_description, synchronous, "
                + " implementation_type_id, implementation_types.description as implementation_description "
                + " FROM events, implementation_types\n"
                + " WHERE entity_id = ? AND implementation_type_id = implementation_types.id;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(entity.getId())});
        
        while (cursor.moveToNext()) {
            event = new EventModel();
            event.setId(cursor.getInt(cursor.getColumnIndex("event_id")));
            event.setDescription(cursor.getString(cursor.getColumnIndex("event_description")));
            event.setSynchronous(cursor.getInt(cursor.getColumnIndex("synchronous")) > 0);
            event.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            event.setEntity(entity);
            event.setImplementation(new Implementation(
            		cursor.getInt(cursor.getColumnIndex("implementation_type_id")), 
            		cursor.getString(cursor.getColumnIndex("implementation_description"))));
            event.setTargets(this.getEventTargets(event));
            event.setParameters(this.getEventParameters(event));
            events.add(event);
        }
        return events;
    }

    private List<EventTarget> getEventTargets(EventModel event) throws SQLException {
        List<EventTarget> targets = new ArrayList();
        EventTarget target = null;
        String sql = "SELECT description, mandatory, target_origin_id "
                + " FROM event_targets_origins, targets_origins \n"
                + " WHERE event_id = ? AND target_origin_id = targets_origins.id;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(event.getId())});

        while (cursor.moveToNext()) {
            target = new EventTarget();
            target.setTarget(new TargetOrigin(cursor.getInt(cursor.getColumnIndex("target_origin_id")), cursor.getString(cursor.getColumnIndex("description"))));
            target.setMandatory(cursor.getInt(cursor.getColumnIndex("mandatory")) > 0);
            target.setEvent(event);
            targets.add(target);
        }
        return targets;
    }

    private List<Parameter> getEventParameters(EventModel event) throws SQLException {
        List<Parameter> parameters = new ArrayList();
        Parameter parameter = null;
        String sql = "SELECT event_parameters.id as parameter_id, parameter_label, event_parameters.description as parameter_desc, \n"
                + "                optional, superior_limit, inferior_limit, entity_state_id,\n"
                + "                event_parameters.initial_value, data_type_id, data_types.initial_value as data_initial_value,\n"
                + "                data_types.description as data_desc\n"
                + "                FROM event_parameters, data_types\n"
                + "                WHERE event_id = ? and data_types.id = data_type_id;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(event.getId())});
        
        while (cursor.moveToNext()) {
        	parameter = new Parameter();
            parameter.setId(cursor.getInt(cursor.getColumnIndex("parameter_id")));
            parameter.setDescription(cursor.getString(cursor.getColumnIndex("parameter_desc")));
            parameter.setLabel(cursor.getString(cursor.getColumnIndex("parameter_label")));
            DataType type = new DataType();
            type.setId(cursor.getInt(cursor.getColumnIndex("data_type_id")));
            type.setDescription(cursor.getString(cursor.getColumnIndex("data_desc")));
            type.setInitialValue(cursor.getString(cursor.getColumnIndex("data_initial_value")));
            parameter.setDataType(type);
            parameter.setInferiorLimit(Content.parseContent(type, cursor.getString(cursor.getColumnIndex("inferior_limit"))));
            parameter.setSuperiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("superior_limit"))));
            parameter.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("initial_value"))));
            parameter.setOptional(cursor.getInt(cursor.getColumnIndex("optional")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("entity_state_id")) > 0) {
            	if(this.dataManager==null){
            		EntityStateDAO dao = new EntityStateDAO(this.database);
            		parameter.setRelatedState(dao.getState(cursor.getInt(cursor.getColumnIndex("entity_state_id"))));
            	} else {
            		parameter.setRelatedState(this.dataManager.getEntityStateDAO()
            				.getState(cursor.getInt(cursor.getColumnIndex("entity_state_id"))));
            	}                
            }
        	
            // pegar o valor atual
            Content c = this.getCurrentContentValue(parameter);
            if (c != null) { // se c for nulo deve usar os valores iniciais, senão adiciona o conteúdo no estado
                parameter.setContent(c);
            }
            parameters.add(parameter);
        }
        return parameters;
    }

    public EventModel get(int modelId, int entityModelId, int componentId) throws SQLException {
        EventModel event = null;
        String sql = "SELECT events.id as event_id, events.model_id, events.description as event_description, synchronous, \n"
                + "                implementation_type_id, implementation_types.description as implementation_description \n"
                + "                FROM events, implementation_types, entities, components \n"
                + "                WHERE events.model_id = ? AND entities.model_id = ? AND components.id = ? "
                + "                AND implementation_type_id = implementation_types.id AND entity_id = entities.id\n"
                + "                AND component_id = components.id;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(modelId),
        		String.valueOf(entityModelId),
        		String.valueOf(componentId)
        });

        if (cursor.moveToNext()) {
            event = new EventModel();
            event.setId(cursor.getInt(cursor.getColumnIndex("event_id")));
            event.setDescription(cursor.getString(cursor.getColumnIndex("event_description")));
            event.setSynchronous(cursor.getInt(cursor.getColumnIndex("synchronous")) > 0);
            event.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            event.setImplementation(new Implementation(cursor.getInt(cursor.getColumnIndex("implementation_type_id")), cursor.getString(cursor.getColumnIndex("implementation_description"))));
            event.setTargets(this.getEventTargets(event));
            event.setParameters(this.getEventParameters(event));
        }
        return event;
    }

    public EventModel get(int id) throws SQLException {
        EventModel event = null;
        String sql = "SELECT events.id as event_id, events.model_id, events.description as event_description, synchronous, \n"
                + "                implementation_type_id, implementation_types.description as implementation_description \n"
                + "                FROM events, implementation_types "
                + "                WHERE id = ? AND implementation_type_id = implementation_types.id;";
        // consulta
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(id)});
        
        if (cursor.moveToNext()) {
        	event = new EventModel();
            event.setId(cursor.getInt(cursor.getColumnIndex("event_id")));
            event.setDescription(cursor.getString(cursor.getColumnIndex("event_description")));
            event.setSynchronous(cursor.getInt(cursor.getColumnIndex("synchronous")) > 0);
            event.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            event.setImplementation(new Implementation(cursor.getInt(cursor.getColumnIndex("implementation_type_id")), cursor.getString(cursor.getColumnIndex("implementation_description"))));
            event.setTargets(this.getEventTargets(event));
            event.setParameters(this.getEventParameters(event));
        }
        return event;
    }

    /**
     * Gera o evento e salva o valor do parâmetros. Se algum parâmetro
     * obrigatório estiver com valor nulo é gerada uma exceção.
     *
     * @param event
     * @param eventModel
     * @throws SQLException
     * @throws Exception
     */
    public void insert(Event event, EventModel eventModel) throws SQLException, Exception {
        // Criar evento
        //String sql = "INSERT INTO generated_events (event_id,entity_id,component_id,time,timeout,event_type) "
        //        + " VALUES (?,?,?,?,?,?);";
        ContentValues values = new ContentValues();
        values.put("event_id", eventModel.getId());
        values.put("entity_id", event.getEntityId());
        values.put("component_id", event.getComponentManager().getComponentId());
        values.put("time", event.getTime().getTime());
        values.put("timeout", event.getTimeout());
        values.put("event_type", event.getEventType());

        event.setDatabaseId((int)(long)this.database.insertOrThrow("generated_events", null, values));
        
        // adicionar conteúdos dos parâmetros
        for (Parameter p : eventModel.getParameters()) {
            if (event.getParameters().get(p.getLabel()) == null && !p.isOptional()) {
                throw new Exception("Parameter " + p.getLabel() + " from the event " + eventModel.getDescription() + " id " + eventModel.getId()
                        + " was not found. Such parameter is not optional!");
            } else {
                if (event.getParameters().get(p.getLabel()) != null) {
                    p.setContent(new Content(
                            Content.parseContent(p.getDataType(), event.getParameters().get(p.getLabel())),
                            event.getTime()));
                    this.insertContent(p, event);
                }
            }
        }
    }

    public Date getLastEventTime(int eventModelId, int entityModelId, int componentModelId) throws SQLException, Exception {
        Date eventTime = null;
        String sql = "SELECT time FROM generated_events "
                + " WHERE event_id = ? AND entity_id = ? AND component_id = ? ORDER BY time DESC "
                + " LIMIT 1;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(eventModelId),
        		String.valueOf(entityModelId),
        		String.valueOf(componentModelId)
        });
        
        if (cursor.moveToNext()) {
            eventTime = new Date(cursor.getLong(cursor.getColumnIndex("time")));
        } else {
            throw new SQLException("Select time from generated_events table failed.");
        }
        return eventTime;
    }

    public Content getLastEventContentByLabelAndValue(Object value, String label, int eventModelId, int entityModelId, int componentModelId) throws SQLException, Exception {
        Content content = null;
        String sql = " SELECT event_contents.id as id, reading_value, time "
                + " FROM  generated_events, event_contents , event_parameters, events "
                + " WHERE reading_value = ? AND parameter_label = ? AND events.model_id = ? AND generated_events.entity_id = ? AND component_id = ? "
                + " AND generated_event_id = generated_events.id AND event_parameter_id = event_parameters.id AND events.id = generated_events.event_id "
                + " ORDER BY time DESC LIMIT 1;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(value),
        		String.valueOf(label),
        		String.valueOf(eventModelId),
        		String.valueOf(entityModelId),
        		String.valueOf(componentModelId)
        });

        if (cursor.moveToNext()) {
            content = new Content();
            content.setTime(new Date(cursor.getLong(cursor.getColumnIndex("time"))));
            content.setId(cursor.getInt(cursor.getColumnIndex("id")));
            content.setValue(value);
        }
        return content;
    }

    public Event getEvent(Action action, BaseComponentManager bcm) throws SQLException{
        return getEvent(action.getDataBaseId(),bcm);
    }

    public Event getEvent(int dataBaseActionId,BaseComponentManager bcm) throws SQLException {
        Event event = null;
        String sql = " SELECT generated_events.id, generated_events.event_id, generated_events.entity_id, generated_events.component_id, generated_events.time, "
                + " timeout, generated_events.event_type, description, synchronous "
                + " FROM generated_events, generated_actions, events "
                + " WHERE generated_actions.id = ? AND events.id = generated_events.event_id AND generated_actions.event_id = generated_events.id;";
        
        // consulta
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(dataBaseActionId)});

        if (cursor.moveToNext()) {
            for(ComponentManager cm : bcm.getComponentManagers()){
                if(cursor.getInt(cursor.getColumnIndex("component_id"))==cm.getComponentId()){
                    event = new SystemEvent(cm);
                }
            }          
            event.setId(cursor.getInt(cursor.getColumnIndex("event_id")));
            event.setDatabaseId(cursor.getInt(cursor.getColumnIndex("id")));
            event.setEventType(cursor.getInt(cursor.getColumnIndex("event_type")));
            event.setEntityId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            event.setTime(new Date(cursor.getLong(cursor.getColumnIndex("time"))));
            event.setTimeout(cursor.getInt(cursor.getColumnIndex("timeout")));
            event.setName(cursor.getString(cursor.getColumnIndex("description")));
            event.setSynchronous(cursor.getInt(cursor.getColumnIndex("synchronous")) > 0 );
        }
        return event;
    }

    public List<Parameter> getEventParameterContents(Action action) throws SQLException {
        return getEventParameterContents(action.getDataBaseId());
    }
    
    public List<Parameter> getEventParameterContents(int dataBaseEventId) throws SQLException {
        List<Parameter> parameters = new ArrayList();
        Parameter parameter;
        Content content;
        String sql = "SELECT event_contents.id as content_id, reading_value, reading_time, event_parameters.id as parameter_id, "
                + " parameter_label as label, event_parameters.description as parameter_desc, optional, superior_limit, "
                + " inferior_limit, entity_state_id, event_parameters.initial_value, data_type_id, "
                + " data_types.initial_value as data_initial_value, data_types.description as data_desc "
                + " FROM event_contents, event_parameters, data_types "
                + " WHERE generated_event_id = ?  AND event_parameter_id = event_parameters.id AND data_types.id = data_type_id "
                + " ORDER BY generated_event_id ; ";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(dataBaseEventId)});
        
        while (cursor.moveToNext()) {
        	parameter = new Parameter();
            parameter.setId(cursor.getInt(cursor.getColumnIndex("parameter_id")));
            parameter.setDescription(cursor.getString(cursor.getColumnIndex("parameter_desc")));
            parameter.setLabel(cursor.getString(cursor.getColumnIndex("label")));
            DataType type = new DataType();
            type.setId(cursor.getInt(cursor.getColumnIndex("data_type_id")));
            type.setDescription(cursor.getString(cursor.getColumnIndex("data_desc")));
            type.setInitialValue(cursor.getString(cursor.getColumnIndex("data_initial_value")));
            parameter.setDataType(type);
            // trata o tipo de dado do estado
            parameter.setSuperiorLimit(Content.parseContent(
                    parameter.getDataType(), parameter.getSuperiorLimit()));
            parameter.setInferiorLimit(Content.parseContent(
                    parameter.getDataType(), parameter.getInferiorLimit()));
            parameter.setInitialValue(Content.parseContent(
                    parameter.getDataType(), parameter.getInitialValue()));
            parameter.setInferiorLimit(Content.parseContent(type, cursor.getString(cursor.getColumnIndex("inferior_limit"))));
            parameter.setSuperiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("superior_limit"))));
            parameter.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("initial_value"))));
            parameter.setOptional(cursor.getInt(cursor.getColumnIndex("optional")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("entity_state_id")) > 0) {
            	if(this.dataManager==null){
            		EntityStateDAO dao = new EntityStateDAO(this.database);
            		parameter.setRelatedState(dao.getState(cursor.getInt(cursor.getColumnIndex("entity_state_id"))));
            	} else {
            		parameter.setRelatedState(this.dataManager.getEntityStateDAO()
            				.getState(cursor.getInt(cursor.getColumnIndex("entity_state_id"))));
            	}                
            }
            // pega o valor utilizado na ação
            content = new Content();
            content.setId(cursor.getInt(cursor.getColumnIndex("content_id")));
            content.setTime(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("reading_time")))));
            content.setValue(Content.parseContent(parameter.getDataType(),
                    cursor.getString(cursor.getColumnIndex("reading_value"))));
            parameter.setContent(content);

            parameter.setPossibleContents(this.getPossibleContents(parameter));
            parameters.add(parameter);
        }
        return parameters;
    }
    
    private List<PossibleContent> getPossibleContents(Parameter parameter)
            throws SQLException {
        List<PossibleContent> possibleContents = new ArrayList();
        String sql = " SELECT id, possible_value, default_value "
                + " FROM possible_event_contents\n"
                + " WHERE event_parameter_id = ?;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(parameter.getId())});
        
        while (cursor.moveToNext()) {
            possibleContents.add(new PossibleContent(cursor.getInt(cursor.getColumnIndex("id")), Content
                    .parseContent(parameter.getDataType(),
                            cursor.getString(cursor.getColumnIndex("possible_value"))), cursor
                    .getInt(cursor.getColumnIndex("default_value")) > 0));
        }
        return possibleContents;
    }
    
    public int getLastGeneratedEventId() throws SQLException{
        Integer id = 0;
        Cursor cursor = this.database.rawQuery("SELECT id FROM generated_events ORDER BY id DESC LIMIT 1;",null);
        if(cursor.moveToNext()){
            id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        return id;
    }

    private int getGeneratedIdByTime(int databaseEventId, Date time) throws SQLException {
        Integer id = 0;
        Cursor cursor = this.database.rawQuery("SELECT id FROM generated_events WHERE time = ? AND event_id = ? ;",
        		new String[]{
        				String.valueOf(time.getTime()),
        				String.valueOf(databaseEventId)
        		});
        if(cursor.moveToNext()){
            id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        return id;
    }
}
