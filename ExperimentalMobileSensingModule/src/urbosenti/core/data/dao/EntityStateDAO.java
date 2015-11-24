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
public class EntityStateDAO {

	public static final String TABLE_NAME = "entity_states";  
    public static final String COLUMN_ID = "id"; 
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_USER_CAN_CHANGE = "user_can_change";
    public static final String COLUMN_INSTANCE_STATE = "instance_state";
    public static final String COLUMN_ENTITY_ID = "entity_id";
    public static final String COLUMN_DATA_TYPE = "data_type_id";
    public static final String COLUMN_SUPERIOR_LIMIT = "superior_limit";
    public static final String COLUMN_INFERIOR_LIMIT = "inferior_limit";
    public static final String COLUMN_INITIAL_VALUE = "initial_value";
    public static final String COLUMN_MODEL_ID = "model_id";
	private SQLiteDatabase database;


    public EntityStateDAO(Object context) {
    	this.database = (SQLiteDatabase) context;
    }

    public void insert(State state) throws SQLException {
        //String sql = "INSERT INTO entity_states (description,user_can_change,instance_state,entity_id,data_type_id,superior_limit,inferior_limit,initial_value,model_id) "
        //        + " VALUES (?,?,?,?,?,?,?,?,?);";
        state.setModelId(state.getId());
        ContentValues values = new ContentValues();
        values.put(COLUMN_DESCRIPTION, state.getDescription());
        values.put(COLUMN_USER_CAN_CHANGE, state.isUserCanChange());
        values.put(COLUMN_INSTANCE_STATE, state.isStateInstance());
        values.put(COLUMN_ENTITY_ID, state.getEntity().getId());
        values.put(COLUMN_DATA_TYPE, state.getDataType().getId());
        values.put(COLUMN_SUPERIOR_LIMIT, String.valueOf(state.getSuperiorLimit()));
        values.put(COLUMN_INFERIOR_LIMIT, String.valueOf(state.getInferiorLimit()));
        values.put(COLUMN_INITIAL_VALUE, String.valueOf(state.getInitialValue()));
        values.put(COLUMN_MODEL_ID, state.getId());
        
        state.setId((int)(long)this.database.insertOrThrow(TABLE_NAME, null, values));
        
        if (DeveloperSettings.SHOW_DAO_SQL) {
        	Log.d("SQL_DEBUG","INSERT INTO entity_states (id,description,user_can_change,instance_state,entity_id,data_type_id,superior_limit,inferior_limit,initial_value,model_id)  "
                    + " VALUES (" + state.getId() + ",'" + state.getDescription() + "'," + state.isUserCanChange() + "," + state.isStateInstance() + "," + state.getEntity().getId()
                    + "," + state.getDataType().getId() + ",'" + state.getSuperiorLimit() + "','" + state.getInferiorLimit() + "','" + state.getInitialValue() + "'," + state.getModelId() + ");");
        }
    }

    public void insertPossibleContents(State state) throws SQLException {
        //String sql = "INSERT INTO possible_entity_contents (possible_value, default_value, entity_state_id) "
        //        + " VALUES (?,?,?);";
        ContentValues values = new ContentValues();        	
        for (PossibleContent possibleContent : state.getPossibleContents()) {
            values.put("possible_value", String.valueOf(possibleContent.getValue()));
            values.put("default_value", possibleContent.isIsDefault());
            values.put("entity_state_id", state.getId());
            
            possibleContent.setId((int)(long)this.database.insertOrThrow("possible_entity_contents", null, values));

            if (DeveloperSettings.SHOW_DAO_SQL) {
                Log.d("SQL_DEBUG","INSERT INTO possible_entity_contents (id,possible_value, default_value, entity_state_id) "
                        + " VALUES (" + possibleContent.getId() + "," + possibleContent.getValue() + "," + possibleContent.isIsDefault() + "," + state.getId() + ");");
            }
        }
    }

    public List<State> getEntityStateModels(Entity entity) throws SQLException {
        List<State> states = new ArrayList();
        State state = null;
        String sql = "SELECT entity_states.id as state_id, model_id, entity_states.description as state_desc, "
                + "user_can_change, instance_state, superior_limit, inferior_limit, \n"
                + "entity_states.initial_value, data_type_id, data_types.initial_value as data_initial_value, "
                + "data_types.description as data_desc\n"
                + "FROM entity_states, data_types\n"
                + "WHERE entity_id = ? AND data_types.id = data_type_id AND instance_state = 0 ORDER BY model_id;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(entity.getId())});
        
        while (cursor.moveToNext()) {
        	state = new State();
        	state.setId(cursor.getInt(cursor.getColumnIndex("state_id")));
            state.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            state.setDescription(cursor.getString(cursor.getColumnIndex("state_desc")));
            DataType type = new DataType();
            type.setId(cursor.getInt(cursor.getColumnIndex("data_type_id")));
            type.setDescription(cursor.getString(cursor.getColumnIndex("data_desc")));
            type.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("data_initial_value"))));
            state.setDataType(type);
            state.setInferiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("inferior_limit"))));
            state.setSuperiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("superior_limit"))));
            state.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("initial_value"))));
            state.setStateInstance(cursor.getInt(cursor.getColumnIndex("instance_state")) > 0);
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

    public List<State> getInitialModelInstanceStates(Entity entity) throws SQLException {
        List<State> states = new ArrayList();
        State state = null;
        String sql = "SELECT entity_states.id as state_id, model_id, entity_states.description as state_desc, "
                + "user_can_change, instance_state, superior_limit, inferior_limit, \n"
                + "entity_states.initial_value, data_type_id, data_types.initial_value as data_initial_value, "
                + "data_types.description as data_desc\n"
                + "FROM entity_states, data_types\n"
                + "WHERE entity_id = ? AND data_types.id = data_type_id AND instance_state = 1 ORDER BY model_id;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(entity.getId())});
        
        while (cursor.moveToNext()) {
            state = new State();
            state.setId(cursor.getInt(cursor.getColumnIndex("state_id")));
            state.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            state.setDescription(cursor.getString(cursor.getColumnIndex("state_desc")));
            DataType type = new DataType();
            type.setId(cursor.getInt(cursor.getColumnIndex("data_type_id")));
            type.setDescription(cursor.getString(cursor.getColumnIndex("data_desc")));
            type.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("data_initial_value"))));
            state.setDataType(type);
            state.setInferiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("inferior_limit"))));
            state.setSuperiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("superior_limit"))));
            state.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("initial_value"))));
            state.setStateInstance(cursor.getInt(cursor.getColumnIndex("instance_state")) > 0);
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

    public List<State> getAllEntityAndInitialModelInstanceStates(Entity entity) throws SQLException {
        List<State> states = new ArrayList();
        State state = null;
        String sql = "SELECT entity_states.id as state_id, model_id, entity_states.description as state_desc, "
                + "user_can_change, instance_state, superior_limit, inferior_limit, \n"
                + "entity_states.initial_value, data_type_id, data_types.initial_value as data_initial_value, "
                + "data_types.description as data_desc\n"
                + "FROM entity_states, data_types\n"
                + "WHERE entity_id = ? AND data_types.id = data_type_id ORDER BY model_id;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(entity.getId())});
        
        while (cursor.moveToNext()) {
        	state = new State();
            state.setId(cursor.getInt(cursor.getColumnIndex("state_id")));
            state.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            state.setDescription(cursor.getString(cursor.getColumnIndex("state_desc")));
            DataType type = new DataType();
            type.setId(cursor.getInt(cursor.getColumnIndex("data_type_id")));
            type.setDescription(cursor.getString(cursor.getColumnIndex("data_desc")));
            type.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("data_initial_value"))));
            state.setDataType(type);
            state.setInferiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("inferior_limit"))));
            state.setSuperiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("superior_limit"))));
            state.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("initial_value"))));
            state.setStateInstance(cursor.getInt(cursor.getColumnIndex("instance_state")) > 0);
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
                + " FROM possible_entity_contents\n"
                + " WHERE entity_state_id = ?;";
        
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

    public Content getCurrentContentValue(State state) throws SQLException {
        Content content = null;
        String sql = "SELECT id, reading_value, reading_time, monitored_user_instance_id "
                + "FROM entity_state_contents\n"
                + "WHERE entity_state_id = ? ORDER BY id DESC LIMIT 1;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(state.getId())});
        
        if (cursor.moveToFirst()) {
            content = new Content();
            // pegar o valor atual
            content.setId(cursor.getInt(cursor.getColumnIndex("id")));
            content.setTime(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("reading_time")))));
            content.setValue(Content.parseContent(state.getDataType(), cursor.getString(cursor.getColumnIndex("reading_value"))));
            if (cursor.getInt(cursor.getColumnIndex("monitored_user_instance_id")) > 0) {
                Instance i = new Instance();
                i.setId(cursor.getInt(cursor.getColumnIndex("monitored_user_instance_id")));
                content.setMonitoredInstance(i);
            }
        }   
        return content;
    }

    public void insertContent(State state) throws SQLException {
        //String sql = "INSERT INTO entity_state_contents (reading_value,reading_time,monitored_user_instance_id,entity_state_id) "
        //        + " VALUES (?,?,?,?);";
        ContentValues values = new ContentValues();
        values.put("reading_value", String.valueOf(Content.parseContent(state.getDataType(), state.getContent().getValue())));
        values.put("reading_time", (state.getContent().getTime() == null) ? System.currentTimeMillis() : state.getContent().getTime().getTime());
        values.put("entity_state_id", state.getId());
        if (state.getContent().getMonitoredInstance() != null) {
        	values.put("monitored_user_instance_id", state.getContent().getMonitoredInstance().getId());
        }
        
        // insere e retorna o ID
        state.getContent().setId((int)(long)this.database.insertOrThrow("entity_state_contents", null, values));
        
        if (DeveloperSettings.SHOW_DAO_SQL) {
            Log.d("SQL_DEBUG","INSERT INTO entity_state_contents (id, reading_value,reading_time,monitored_user_instance_id,entity_state_id)   "
                    + " VALUES (" + state.getContent().getId() + ",'" + state.getContent().getValue() + "',"
                    + ((state.getContent().getMonitoredInstance() != null) ? state.getContent().getMonitoredInstance().getId() : "null")
                    + ",'" + state.getContent().getTime().getTime() + "'," + "," + state.getId() + ");");
        }
    }

    State getState(int id) throws SQLException {
        State state = null;
        String sql = "SELECT entity_states.id as state_id, model_id, entity_states.description as state_desc, \n"
                + "                user_can_change, instance_state, superior_limit, inferior_limit, \n"
                + "                entity_states.initial_value, data_type_id, data_types.initial_value as data_initial_value,  \n"
                + "                data_types.description as data_desc \n"
                + "                FROM entity_states, data_types \n"
                + "                WHERE entity_states.id = ? and data_types.id = data_type_id;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(id)});
        
        if (cursor.moveToNext()) {
        	state = new State();
            state.setId(cursor.getInt(cursor.getColumnIndex("state_id")));
            state.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            state.setDescription(cursor.getString(cursor.getColumnIndex("state_desc")));
            DataType type = new DataType();
            type.setId(cursor.getInt(cursor.getColumnIndex("data_type_id")));
            type.setDescription(cursor.getString(cursor.getColumnIndex("data_desc")));
            type.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("data_initial_value"))));
            state.setDataType(type);
            state.setInferiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("inferior_limit"))));
            state.setSuperiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("superior_limit"))));
            state.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("initial_value"))));
            state.setStateInstance(cursor.getInt(cursor.getColumnIndex("instance_state")) > 0);
            state.setUserCanChange(cursor.getInt(cursor.getColumnIndex("user_can_change")) > 0);
        	
            state.setPossibleContent(this.getPossibleStateContents(state));
            // pegar o valor atual
            Content c = this.getCurrentContentValue(state);
            if (c != null) { // se c for nulo deve usar os valores iniciais, senão adiciona o conteúdo no estado
                state.setContent(c);
            }
        }
        return state;
    }

    public State getEntityState(int componentId, int entityModelId, int stateModelId) throws SQLException {
        State state = null;
        String sql = "SELECT entity_states.id as state_id, entity_states.description as state_desc, "
                + "               user_can_change, instance_state, superior_limit, inferior_limit,  "
                + "                entity_states.initial_value, data_type_id, data_types.initial_value as data_initial_value, "
                + "                data_types.description as data_desc "
                + "              FROM entities, entity_states, data_types "
                + "              WHERE entity_states.model_id = ?  AND entities.model_id = ? AND component_id = ? "
                + "               AND data_types.id = data_type_id AND entity_id = entities.id;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(stateModelId),
        		String.valueOf(entityModelId),
        		String.valueOf(componentId)
        });
 
        while (cursor.moveToNext()) {
        	state = new State();
            state.setId(cursor.getInt(cursor.getColumnIndex("state_id")));
            state.setModelId(stateModelId);
            state.setDescription(cursor.getString(cursor.getColumnIndex("state_desc")));
            DataType type = new DataType();
            type.setId(cursor.getInt(cursor.getColumnIndex("data_type_id")));
            type.setDescription(cursor.getString(cursor.getColumnIndex("data_desc")));
            type.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("data_initial_value"))));
            state.setDataType(type);
            state.setInferiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("inferior_limit"))));
            state.setSuperiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("superior_limit"))));
            state.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("initial_value"))));
            state.setStateInstance(cursor.getInt(cursor.getColumnIndex("instance_state")) > 0);
            state.setUserCanChange(cursor.getInt(cursor.getColumnIndex("user_can_change")) > 0);
            
            state.setPossibleContent(this.getPossibleStateContents(state));
            // pegar o valor atual
            Content c = this.getCurrentContentValue(state);
            if (c != null) { // se c for nulo deve usar os valores iniciais, senão adiciona o conteúdo no estado
                state.setContent(c);
            }
        }
        return state;
    }

    /**
     * Recebe por parâmetro uma instância de usuário e retorna todos os estados
     * que podem ser alterados pelo usuário que não são de instâncias. Esses
     * estados retornam com o valor atual da última vez que o usuário alterou o
     * conteúdo. Caso este não exista retorna o valor inicial. OBS.: Retorna
     * todos os dados até os componentes a partir da visão do estado.
     *
     * @param instance
     * @return
     * @throws java.sql.SQLException
     */
    public List<State> getUserEntityStates(Instance instance) throws SQLException {
        List<State> states = new ArrayList();
        State state = null;
        String sql = "SELECT entity_states.id as state_id, entity_states.description as state_desc, user_can_change, instance_state, superior_limit, "
                + " entity_states.model_id as state_model_id, inferior_limit, entity_states.initial_value, data_type_id, data_types.initial_value as data_initial_value, "
                + " data_types.description as data_desc, entity_id, entities.description as entity_desc, component_id, components.description as component_desc, "
                + " code_class, entities.model_id as entity_model_id, entity_type_id, entity_types.description as entity_type_desc\n "
                + " FROM entities, entity_states, data_types, components, entity_types\n"
                + " WHERE user_can_change = 1 AND instance_state = 0 AND data_types.id = data_type_id AND entity_id = entities.id AND component_id = components.id "
                + " AND entity_type_id = entity_types.id;";
        
        Cursor cursor = this.database.rawQuery(sql,	null);
        
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
            state.setStateInstance(cursor.getInt(cursor.getColumnIndex("instance_state")) > 0);
            state.setUserCanChange(cursor.getInt(cursor.getColumnIndex("user_can_change")) > 0);
        	
            Entity entity = new Entity();
            entity.setDescription(cursor.getString(cursor.getColumnIndex("entity_desc")));
            entity.setId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            entity.setModelId(cursor.getInt(cursor.getColumnIndex("entity_model_id")));
            entity.setEntityType(new EntityType(cursor.getInt(cursor.getColumnIndex("entity_type_id")), cursor.getString(cursor.getColumnIndex("entity_type_desc"))));
            entity.setComponent(new Component(cursor.getInt(cursor.getColumnIndex("component_id")), cursor.getString(cursor.getColumnIndex("component_desc")), cursor.getString(cursor.getColumnIndex("code_class"))));
            state.setEntity(entity);
            
            state.setPossibleContent(this.getPossibleStateContents(state));
            // pegar o valor atual
            Content c = this.getCurrentInstanceContentValue(state,instance);
            if (c != null) { // se c for nulo deve usar os valores iniciais, senão adiciona o conteúdo no estado
                state.setContent(c);
            }
            states.add(state);
        }
        return states;
    }
    
    private Content getCurrentInstanceContentValue(State state, Instance instance) throws SQLException {
        Content content = null;
        String sql = "SELECT id, reading_value, reading_time "
                + " FROM entity_state_contents "
                + " WHERE entity_state_id = ? AND monitored_user_instance_id = ? "
                + " ORDER BY id DESC "
                + " LIMIT 1; ";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{
    		String.valueOf(state.getId()),
    		String.valueOf(instance.getId())
    	});
        
        if (cursor.moveToFirst()) {
            content = new Content();
            // pegar o valor atual
            content.setId(cursor.getInt(cursor.getColumnIndex("id")));
            content.setTime(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("reading_time")))));
            content.setValue(Content.parseContent(state.getDataType(), cursor.getString(cursor.getColumnIndex("reading_value"))));
            content.setMonitoredInstance(instance);
        }   
        return content;
    }

    void deleteUserContents(Instance instance) throws SQLException {
        //String sql = "DELETE FROM entity_state_contents WHERE monitored_user_instance_id = ? ;";
                
        this.database.delete(
        		"entity_state_contents", 
        		" monitored_user_instance_id = ? ", 
        		new String[]{String.valueOf(instance.getId())});
        
    }
}
