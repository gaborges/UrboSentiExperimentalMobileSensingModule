/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import urbosenti.core.device.model.Component;
import urbosenti.core.device.model.Content;
import urbosenti.core.device.model.DataType;
import urbosenti.core.device.model.Entity;
import urbosenti.core.device.model.EntityType;
import urbosenti.core.device.model.Instance;
import urbosenti.core.device.model.PossibleContent;
import urbosenti.core.device.model.State;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class InstanceDAO {

	private SQLiteDatabase database;

    public InstanceDAO(Object context) {
    	this.database = (SQLiteDatabase) context;
    }

    public void insert(Instance instance) throws SQLException {
        //String sql = "INSERT INTO instances (description,representative_class,entity_id,model_id) "
        //        + " VALUES (?,?,?,?);";
        
        instance.setModelId(instance.getId());
        ContentValues values = new ContentValues();
        values.put("description", instance.getDescription());
        values.put("representative_class", instance.getRepresentativeClass());
        values.put("entity_id", instance.getEntity().getId());
        if (instance.getId() > 0) {
        	values.put("model_id", instance.getId());
        }

        instance.setId((int)(long)this.database.insertOrThrow("instances", null, values));
        
        if (DeveloperSettings.SHOW_DAO_SQL) {
        	Log.d("SQL_DEBUG","INSERT INTO instances (id,description,representative_class, entity_id, model_id) "
                    + " VALUES (" + instance.getId() + ",'" + instance.getDescription() + "','" + instance.getRepresentativeClass() + "',"
                    + instance.getEntity().getId() + "," + ((instance.getModelId() > 0) ? instance.getModelId() : null) + ");");
        }
    }

    public void insertState(State state, Instance instance) throws SQLException {
        //String sql = "INSERT INTO instance_states (description,user_can_change,instance_id,data_type_id,superior_limit,inferior_limit,initial_value,state_model_id) "
        //        + " VALUES (?,?,?,?,?,?,?,?);";
        if (state.getModelId() < 1) {
            state.setModelId(state.getId());
        }
        state.setSuperiorLimit(Content.parseContent(state.getDataType(), state.getSuperiorLimit()));
        state.setInferiorLimit(Content.parseContent(state.getDataType(), state.getInferiorLimit()));
        state.setInitialValue(Content.parseContent(state.getDataType(), state.getInitialValue()));
        ContentValues values = new ContentValues();
        values.put("description", state.getDescription());
        values.put("user_can_change", state.isUserCanChange());
        values.put("instance_id", instance.getId());
        values.put("data_type_id", state.getDataType().getId());
        values.put("superior_limit", String.valueOf(state.getSuperiorLimit()));
        values.put("inferior_limit", String.valueOf(state.getInferiorLimit()));
        values.put("initial_value", String.valueOf(state.getInitialValue()));
        values.put("state_model_id", state.getModelId());
        
        state.setId((int)(long)this.database.insertOrThrow("instance_states", null, values));
        
        if (DeveloperSettings.SHOW_DAO_SQL) {
            Log.d("SQL_DEBUG","INSERT INTO instance_states (id,description,user_can_change,instance_id,data_type_id,superior_limit,inferior_limit,initial_value,state_model_id) "
                    + " VALUES (" + state.getId() + ",'" + state.getDescription() + "'," + state.isUserCanChange() + "," + instance.getId()
                    + "," + state.getDataType().getId() + ",'" + state.getSuperiorLimit() + "','" + state.getInferiorLimit() + "','" + state.getInitialValue() + "'," + state.getModelId() + ");");
        }
    }

    public void insertPossibleStateContents(State state, Instance instance) throws SQLException {
        //String sql = "INSERT INTO possible_instance_contents (possible_value, default_value, instance_state_id) "
        //        + " VALUES (?,?,?);";
        
        if (state.getPossibleContents() != null) {
            for (PossibleContent possibleContent : state.getPossibleContents()) {
            	ContentValues values = new ContentValues();
            	values.put("possible_value", String.valueOf(Content.parseContent(state.getDataType(),possibleContent.getValue())));
            	values.put("default_value", possibleContent.isIsDefault());
            	values.put("instance_state_id", state.getId());
            	
            	state.setId((int)(long)this.database.insertOrThrow("possible_instance_contents", null, values));
            	
            	if (DeveloperSettings.SHOW_DAO_SQL) {
                    Log.d("SQL_DEBUG","INSERT INTO possible_instance_contents (id,possible_value, default_value, instance_state_id) "
                            + " VALUES (" + possibleContent.getId() + "," + Content.parseContent(state.getDataType(), possibleContent.getValue()) + "," + possibleContent.isIsDefault() + "," + state.getId() + ");");
                }
            }
        }
    }

    public Content getCurrentContentValue(State state) throws SQLException {
        Content content = null;
        String sql = "SELECT id, reading_value, reading_time "
                + "FROM instance_state_contents\n"
                + "WHERE instance_state_id = ? ORDER BY id DESC LIMIT 1;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(state.getId())});
        
        if (cursor.moveToFirst()) {
            content = new Content();
            // pegar o valor atual
            content.setId(cursor.getInt(cursor.getColumnIndex("id")));
            content.setTime(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("reading_time")))));
            content.setValue(Content.parseContent(state.getDataType(), cursor.getString(cursor.getColumnIndex("reading_value"))));
        }
        return content;
    }

    public void insertContent(State state) throws SQLException {
        //String sql = "INSERT INTO instance_state_contents (reading_value,reading_time,instance_state_id) "
        //        + " VALUES (?,?,?);";
        
        ContentValues values = new ContentValues();
        values.put("reading_value", String.valueOf(Content.parseContent(state.getDataType(), state.getContent().getValue())));
        values.put("reading_time", (state.getContent().getTime() == null) ? System.currentTimeMillis() : state.getContent().getTime().getTime());
        values.put("instance_state_id", state.getId());
        
        // insere e retorna o ID
        state.getContent().setId((int)(long)this.database.insertOrThrow("instance_state_contents", null, values));
        
        if (DeveloperSettings.SHOW_DAO_SQL) {
            Log.d("SQL_DEBUG","INSERT INTO instance_state_contents (id,reading_value,reading_time,instance_state_id) "
                    + " VALUES (" + state.getContent().getId() + ",'" + Content.parseContent(state.getDataType(), state.getContent().getValue()) + "',"
                    + "'" + state.getContent().getTime().getTime() + "'," + state.getId() + ");");
        }
    }

    public List<Instance> getEntityInstanceModels(Entity entity) throws SQLException {
        List<Instance> instances = new ArrayList();
        Instance instance = null;
        String sql = "SELECT id,  description, model_id, representative_class "
                + " FROM instances "
                + " WHERE model_id = ? ;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(entity.getId())});

        while (cursor.moveToNext()) {
            instance = new Instance();
            instance.setId(cursor.getInt(cursor.getColumnIndex("id")));
            instance.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            instance.setRepresentativeClass(cursor.getString(cursor.getColumnIndex("representative_class")));
            instance.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            instance.setEntity(entity);
            instance.setStates(this.getInstanceStates(instance));
            instances.add(instance);
        }
        return instances;
    }

    public List<State> getInstanceStates(Instance instance) throws SQLException {
        List<State> states = new ArrayList();
        State state = null;
        String sql = "SELECT instance_states.id as state_id, instance_states.description as state_desc, state_model_id, "
                + " user_can_change, superior_limit, inferior_limit, \n"
                + " instance_states.initial_value, data_type_id, data_types.initial_value as data_initial_value, "
                + " data_types.description as data_desc\n"
                + " FROM instance_states, data_types\n "
                + " WHERE instance_id = ? and data_types.id = data_type_id ORDER BY state_model_id;";
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(instance.getId())});
        
        while (cursor.moveToNext()) {
        	state = new State();
            state.setId(cursor.getInt(cursor.getColumnIndex("state_id")));
            state.setModelId(cursor.getInt(cursor.getColumnIndex("state_model_id")));
            state.setDescription(cursor.getString(cursor.getColumnIndex("state_desc")));
            DataType type = new DataType();
            type.setId(cursor.getInt(cursor.getColumnIndex("data_type_id")));
            type.setDescription(cursor.getString(cursor.getColumnIndex("data_desc")));
            type.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("data_initial_value"))));
            state.setDataType(type);
            state.setInferiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("inferior_limit"))));
            state.setSuperiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("superior_limit"))));
            state.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("initial_value"))));
            state.setStateInstance(true);
            state.setUserCanChange(cursor.getInt(cursor.getColumnIndex("user_can_change")) > 0);        	

            state.setPossibleContent(this.getPossibleStateContents(state));
            // pegar o valor atual
            Content c = this.getCurrentContentValue(state);
            if (c != null) { // se c for nulo deve usar os valores iniciais, senão adiciona o conteúdo no estado
                state.setContent(c);
            }
            states.add(state);
        }
        return states;
    }

    public List<PossibleContent> getPossibleStateContents(State state) throws SQLException {
        List<PossibleContent> possibleContents = new ArrayList();
        String sql = " SELECT id, possible_value, default_value "
                + " FROM possible_instance_contents\n"
                + " WHERE instance_state_id = ?;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(state.getId())});
        
        while (cursor.moveToNext()) {
            possibleContents.add(
                    new PossibleContent(
                            cursor.getInt(cursor.getColumnIndex("id")),
                            Content.parseContent(state.getDataType(), cursor.getString(cursor.getColumnIndex("possible_value"))),
                            cursor.getInt(cursor.getColumnIndex("default_value")) > 0));
        }
        return possibleContents;
    }

    public Instance getInstance(int id) throws SQLException {
        Instance instance = null;
        String sql = "SELECT instances.description as instance_desc, representative_class, entity_id, entities.description, model_id as entity_model_id, \n"
                + " instances.model_id as model_id, entity_type_id, entity_types.description as type_desc, component_id, components.description as comp_desc, code_class\n"
                + " FROM instances, entities,entity_types, components \n"
                + " WHERE  instances.id = ? AND entities.id = entity_id AND entity_types.id = entity_type_id AND components.id = component_id;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(id)});
        
        while (cursor.moveToNext()) {
            instance = new Instance();
            instance.setId(id);
            instance.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            instance.setRepresentativeClass(cursor.getString(cursor.getColumnIndex("representative_class")));
            instance.setEntity(new Entity());
            instance.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            instance.getEntity().setId(cursor.getInt(cursor.getColumnIndex("id")));
            instance.getEntity().setDescription(cursor.getString(cursor.getColumnIndex("instance_desc")));
            instance.getEntity().setModelId(cursor.getInt(cursor.getColumnIndex("entity_model_id")));
            instance.getEntity().setEntityType(
                    new EntityType(cursor.getInt(cursor.getColumnIndex("entity_type_id")), cursor.getString(cursor.getColumnIndex("type_desc"))));
            instance.getEntity().setComponent(
                    new Component(cursor.getInt(cursor.getColumnIndex("component_id")), 
                    		cursor.getString(cursor.getColumnIndex("comp_desc")), 
                    		cursor.getString(cursor.getColumnIndex("code_class"))));
            instance.setStates(this.getInstanceStates(instance));
        }
        return instance;
    }

    public Instance getInstance(int modelId, int entityModelId, int componentId) throws SQLException {
        Instance instance = null;
        String sql = "SELECT instances.description as instance_desc, representative_class, entity_id, entities.description as entity_desc, instances.model_id, "
                + " entity_type_id, entity_types.description as type_desc, component_id, components.description as comp_desc, code_class, instances.id as instance_id "
                + " FROM instances, entities,entity_types, components "
                + " WHERE instances.model_id = ? AND entities.model_id = ? AND entities.id = entity_id AND entity_types.id = entity_type_id AND components.id = component_id AND component_id = ? "
                + " ORDER BY instances.model_id;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(modelId),
        		String.valueOf(entityModelId),
        		String.valueOf(componentId)
        });
        
        while (cursor.moveToNext()) {
        	instance = new Instance();
            instance.setId(cursor.getInt(cursor.getColumnIndex("instance_id")));
            instance.setDescription(cursor.getString(cursor.getColumnIndex("instance_desc")));
            instance.setRepresentativeClass(cursor.getString(cursor.getColumnIndex("representative_class")));
            instance.setEntity(new Entity());
            instance.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            instance.getEntity().setId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            instance.getEntity().setDescription(cursor.getString(cursor.getColumnIndex("entity_desc")));
            instance.getEntity().setModelId(entityModelId);
            instance.getEntity().setEntityType(
                    new EntityType(cursor.getInt(cursor.getColumnIndex("entity_type_id")), cursor.getString(cursor.getColumnIndex("type_desc"))));
            instance.getEntity().setComponent(
                    new Component(cursor.getInt(cursor.getColumnIndex("component_id")), 
                    		cursor.getString(cursor.getColumnIndex("comp_desc")), 
                    		cursor.getString(cursor.getColumnIndex("code_class"))
                    	));
            instance.setStates(this.getInstanceStates(instance));
        }
        return instance;
    }

    int getEntityInstanceCount(Entity entity) throws SQLException {
        int count = 0;
        String sql = "SELECT count(*) FROM instances WHERE entity_id = ?;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(entity.getId())});
        
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        } else {
            throw new SQLException("Failed to counting the instances.");
        }
        return count;
    }

    Instance getInstanceByStateContent(Object contentValue, int stateModelId, int entityModelId, int componentId) throws SQLException {
        Instance instance = null;
        String sql = "SELECT instances.description as instance_desc, representative_class, instances.entity_id, entities.description as entity_desc, code_class, "
                + " instances.model_id, instance_id, entity_type_id, entity_types.description as type_desc, component_id, components.description as comp_desc "
                + "                 FROM instances, entities,entity_types, components , instance_states, instance_state_contents "
                + "                 WHERE state_model_id = ? AND entities.model_id = ? AND component_id = ? AND reading_value = ? "
                + "                  AND entities.id = instances.entity_id AND entity_types.id = entity_type_id "
                + "                  AND components.id = component_id AND instance_states.id = instance_state_id "
                + "                  AND instance_id = instances.id "
                + "                 ORDER BY instance_state_contents.id DESC LIMIT 1;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(stateModelId),
        		String.valueOf(entityModelId),
        		String.valueOf(componentId),
        		String.valueOf(contentValue)
        });

        if (cursor.moveToNext()) {
        	instance = new Instance();
            instance.setId(cursor.getInt(cursor.getColumnIndex("instance_id")));
            instance.setDescription(cursor.getString(cursor.getColumnIndex("instance_desc")));
            instance.setRepresentativeClass(cursor.getString(cursor.getColumnIndex("representative_class")));
            instance.setEntity(new Entity());
            instance.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            instance.getEntity().setId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            instance.getEntity().setDescription(cursor.getString(cursor.getColumnIndex("entity_desc")));
            instance.getEntity().setModelId(entityModelId);
            instance.getEntity().setEntityType(
                    new EntityType(cursor.getInt(cursor.getColumnIndex("entity_type_id")), cursor.getString(cursor.getColumnIndex("type_desc"))));
            instance.getEntity().setComponent(
                    new Component(cursor.getInt(cursor.getColumnIndex("component_id")), 
                    		cursor.getString(cursor.getColumnIndex("comp_desc")), 
                    		cursor.getString(cursor.getColumnIndex("code_class"))));
            instance.setStates(this.getInstanceStates(instance));
        }
        return instance;
    }

    /**
     * Recebe por parâmetro uma instância de usuário e retorna todos os estados
     * que podem ser alterados pelo usuário em cada interface. Esses estados
     * retornam com o valor atual da última vez que o usuário alterou o
     * conteúdo. Caso este não exista retorna o valor inicial. OBS.: Retorna
     * todos os dados até os componentes a partir da visão do estado.
     *
     * @param instance
     * @return
     */
    public List<State> getUserInstanceStates(Instance instance, Instance userInstance) throws SQLException {
        List<State> states = new ArrayList();
        State state = null;
        String sql = "SELECT instance_states.id as state_id, instance_states.description as state_desc, user_can_change, superior_limit, "
                + " state_model_id, inferior_limit, instance_states.initial_value, data_type_id, data_types.initial_value as data_initial_value, "
                + " data_types.description as data_desc "
                + " FROM instance_states, data_types \n"
                + " WHERE user_can_change = 1 AND data_types.id = data_type_id; ";
        
        Cursor cursor = this.database.rawQuery(sql, null);

        while (cursor.moveToNext()) {
        	state = new State();
            state.setId(cursor.getInt(cursor.getColumnIndex("state_id")));
            state.setModelId(cursor.getInt(cursor.getColumnIndex("state_model_id")));
            state.setDescription(cursor.getString(cursor.getColumnIndex("state_desc")));
            DataType type = new DataType();
            type.setId(cursor.getInt(cursor.getColumnIndex("data_type_id")));
            type.setDescription(cursor.getString(cursor.getColumnIndex("data_desc")));
            type.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("data_initial_value"))));
            state.setDataType(type);
            state.setInferiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("inferior_limit"))));
            state.setSuperiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("superior_limit"))));
            state.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("initial_value"))));
            state.setStateInstance(true);
            state.setUserCanChange(cursor.getInt(cursor.getColumnIndex("user_can_change")) > 0);

            state.setPossibleContent(this.getPossibleStateContents(state));
            // pegar o valor atual
            Content c = this.getCurrentUserInstanceContentValue(state, userInstance);
            if (c != null) { // se c for nulo deve usar os valores iniciais, senão adiciona o conteúdo no estado
                state.setContent(c);
            }
            states.add(state);
        }
        return states;
    }

    /**
     * Recebe por parâmetro uma instância de usuário e retorna todas as
     * instâncias que possuem estados que podem ser alterados pelo usuário,
     * juntamente com esses estados e seu valor atual em relação ao usuário.
     * Esses estados retornam com o valor atual da última vez que o usuário
     * alterou o conteúdo. Caso este não exista retorna o valor inicial. OBS.:
     * Retorna todos os dados até os componentes a partir da visão do estado.
     *
     * @param userInstance
     * @return
     */
    public List<Instance> getUserInstances(Instance userInstance) throws SQLException {
        List<Instance> instances = new ArrayList();
        Instance instance = null;
        // Buscar todas as instâncias que possuam algum estado que pode ser alterado pelo usuário
        String sql = "SELECT instances.id as instance_id,  instances.description as instance_desc, instances.model_id as instance_model_id, "
                + " representative_class, (SELECT count(*) FROM instance_states WHERE user_can_change == 1  AND instance_id = instances.id) as count, "
                + " entity_id, entities.description as entity_desc, component_id, components.description as component_desc, code_class, "
                + " entities.model_id as entity_model_id, entity_type_id, entity_types.description as entity_type_desc\n"
                + "    FROM instances, entities, components, entity_types \n"
                + "    WHERE (SELECT count(*) FROM instance_states WHERE user_can_change == 1  AND instance_id = instances.id) > 0 "
                + " AND entity_id = entities.id AND component_id = components.id AND entity_type_id = entity_types.id AND instance_id = instances.id;";

        Cursor cursor = this.database.rawQuery(sql, null);
        
        while (cursor.moveToNext()) {
        	instance = new Instance();
            instance.setId(cursor.getInt(cursor.getColumnIndex("instance_id")));
            instance.setDescription(cursor.getString(cursor.getColumnIndex("instance_desc")));
            instance.setRepresentativeClass(cursor.getString(cursor.getColumnIndex("representative_class")));
            instance.setModelId(cursor.getInt(cursor.getColumnIndex("instance_model_id")));
            instance.setEntity(new Entity());
            instance.getEntity().setId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            instance.getEntity().setDescription(cursor.getString(cursor.getColumnIndex("entity_desc")));
            instance.getEntity().setModelId(cursor.getInt(cursor.getColumnIndex("entity_model_id")));
            instance.getEntity().setEntityType(
                    new EntityType(cursor.getInt(cursor.getColumnIndex("entity_type_id")), cursor.getString(cursor.getColumnIndex("type_desc"))));
            instance.getEntity().setComponent(
                    new Component(cursor.getInt(cursor.getColumnIndex("component_id")), 
                    		cursor.getString(cursor.getColumnIndex("comp_desc")), 
                    		cursor.getString(cursor.getColumnIndex("code_class"))));
            instance.setStates(this.getUserInstanceStates(instance, userInstance));
            instances.add(instance);
        }
        return instances;
    }

    private Content getCurrentUserInstanceContentValue(State state, Instance userInstance) throws SQLException {
        Content content = null;
        String sql = "SELECT id, reading_value, reading_time "
                + " FROM instance_state_contents "
                + " WHERE instance_state_id = ? AND monitored_user_instance_id = ? "
                + " ORDER BY id DESC "
                + " LIMIT 1; ";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(state.getId()),
        		String.valueOf(userInstance.getId())
        	});
        
        if (cursor.moveToFirst()) {
            content = new Content();
            // pegar o valor atual
            content.setId(cursor.getInt(cursor.getColumnIndex("id")));
            content.setTime(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("reading_time")))));
            content.setValue(Content.parseContent(state.getDataType(), cursor.getString(cursor.getColumnIndex("reading_value"))));
            content.setMonitoredInstance(userInstance);
        }
        return content;
    }

    void deleteInstance(Instance instance) throws SQLException {
        //String sql = "DELETE * FROM entity_state_contents WHERE monitored_user_instance_id = ? ;";
        
        this.database.delete(
        		"entity_state_contents", 
        		" monitored_user_instance_id = ? ", 
        		new String[]{String.valueOf(instance.getId())});
    }

    void deleteInstanceStates(Instance instance) throws SQLException {
        //String sql = "DELETE * FROM entity_states WHERE instance_id = ? ;";
        this.database.delete(
        		"entity_states", 
        		" instance_id = ? ", 
        		new String[]{String.valueOf(instance.getId())});
    }

    void deleteInstanceStateContents(Instance instance) throws SQLException {
        for (State state : instance.getStates()) {
            //String sql = "DELETE * FROM instance_state_contents WHERE instance_state_id = ? ;";
            this.database.delete(
            		"instance_state_contents", 
            		" instance_state_id = ? ", 
            		new String[]{String.valueOf(state.getId())});
        }
    }

    void deleteUserInstanceContents(Instance instance) throws SQLException {
        //String sql = "DELETE * FROM instance_state_contents WHERE monitored_user_instance_id = ? ;";
        this.database.delete(
        		"instance_state_contents", 
        		" monitored_user_instance_id = ? ", 
        		new String[]{String.valueOf(instance.getId())});
    }
    
    public List<Instance> getEntityInstances(int entityModelId, int componentId) throws SQLException {
        Instance instance = null;
        List<Instance> instances = new ArrayList();
        String sql = "SELECT instances.description as instance_desc, representative_class, entity_id, entities.description as entity_desc, instances.model_id,\n"
                + " entity_type_id, entity_types.description as type_desc, component_id, components.description as comp_desc, code_class, instances.id as instance_id\n"
                + " FROM instances, entities,entity_types, components \n"
                + " WHERE entities.model_id = ? AND entities.id = entity_id AND entity_types.id = entity_type_id AND components.id = component_id AND component_id = ?"
                + " ORDER BY instances.model_id;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(entityModelId),
        		String.valueOf(componentId)
        	});

        while (cursor.moveToNext()) {
        	instance = new Instance();
            instance.setId(cursor.getInt(cursor.getColumnIndex("instance_id")));
            instance.setDescription(cursor.getString(cursor.getColumnIndex("instance_desc")));
            instance.setRepresentativeClass(cursor.getString(cursor.getColumnIndex("representative_class")));
            instance.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            instance.setEntity(new Entity());
            instance.getEntity().setId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            instance.getEntity().setDescription(cursor.getString(cursor.getColumnIndex("entity_desc")));
            instance.getEntity().setModelId(entityModelId);
            instance.getEntity().setEntityType(
                    new EntityType(cursor.getInt(cursor.getColumnIndex("entity_type_id")), cursor.getString(cursor.getColumnIndex("type_desc"))));
            instance.getEntity().setComponent(
                    new Component(cursor.getInt(cursor.getColumnIndex("component_id")), 
                    		cursor.getString(cursor.getColumnIndex("comp_desc")), 
                    		cursor.getString(cursor.getColumnIndex("code_class"))));
            instance.setStates(this.getInstanceStates(instance));
            instances.add(instance);
        }
        return instances;
    }
    
    public Content getBeforeCurrentContentValue(int stateModelId, int instanceModelId, int entityModelId, int componentModelId) throws SQLException {
        Content content = null;
        String sql = "SELECT instance_state_contents.id as id, reading_value, reading_time, data_type_id " +
"                FROM instance_state_contents, instance_states, instances, entities\n" +
"                WHERE state_model_id = ? AND instances.model_id = ? AND entities.model_id = ? AND component_id = ?" +
"                AND entity_id = entities.id AND instance_state_id == instance_states.id AND instance_id = instances.id " +
"                AND data_types.id = data_type_id ORDER BY id DESC LIMIT 2;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(stateModelId),
        		String.valueOf(instanceModelId),
        		String.valueOf(entityModelId),
        		String.valueOf(componentModelId)
        	});
        
        // somente o último é que interessa
        if (cursor.moveToNext()) {
            cursor.moveToNext();
            content = new Content();
            // pegar o valor atual
            content.setId(cursor.getInt(cursor.getColumnIndex("id")));
            content.setTime(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("reading_time")))));
            content.setValue(Content.parseContent(new DataType(cursor.getInt(cursor.getColumnIndex("data_type_id")), ""), cursor.getString(cursor.getColumnIndex("reading_value"))));
        }
        return content;
    }

}
