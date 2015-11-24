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
import urbosenti.core.device.model.Agent;
import urbosenti.core.device.model.AgentCommunicationLanguage;
import urbosenti.core.device.model.AgentMessage;
import urbosenti.core.device.model.AgentType;
import urbosenti.core.device.model.CommunicativeAct;
import urbosenti.core.device.model.Content;
import urbosenti.core.device.model.Conversation;
import urbosenti.core.device.model.DataType;
import urbosenti.core.device.model.Direction;
import urbosenti.core.device.model.InteractionModel;
import urbosenti.core.device.model.InteractionType;
import urbosenti.core.device.model.Parameter;
import urbosenti.core.device.model.PossibleContent;
import urbosenti.core.device.model.State;
import urbosenti.core.events.Action;
import urbosenti.core.events.Event;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class AgentTypeDAO {

	private SQLiteDatabase database;

    public static final String TABLE_NAME = "agent_types";  
    public static final String COLUMN_ID = "id"; 
    public static final String COLUMN_DESCRIPTION = "description";
    
    public AgentTypeDAO(Object context) {
    	this.database = (SQLiteDatabase) context;
    }

    public AgentType get(int id) throws SQLException {
        AgentType agentType = null;
        String sql = "SELECT id, description "
                + "FROM agent_types "
                + "WHERE id = ?;";
        
        Cursor cursor = this.database.rawQuery(sql,
        		new String[]{String.valueOf(id)});
        
        while (cursor.moveToNext()) {
            agentType = new AgentType();
            agentType.setId(cursor.getInt(cursor.getColumnIndex("id")));
            agentType.setDescription(cursor.getString(cursor.getColumnIndex("description")));
        }        
        return agentType;
    }

    public void insert(AgentType type) throws SQLException {
        //String sql = "INSERT INTO agent_types (id, description) "
        //        + "VALUES (?,?);";
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, type.getId());
        values.put(COLUMN_DESCRIPTION, type.getDescription());
        
        this.database.insertOrThrow(TABLE_NAME, null, values);

        if (DeveloperSettings.SHOW_DAO_SQL){
        	Log.d("SQL_DEBUG","INSERT INTO agent_types (id, description) "
                + " VALUES (" + type.getId() + ",'" + type.getDescription() + "');");
        }
    }

    public void insertInteraction(InteractionModel interaction) throws SQLException {
        String sql = "INSERT INTO interactions (id,description,agent_type_id, communicative_act_id, interaction_type_id, direction_id, interaction_id) "
                + " VALUES (?,?,?,?,?,?,?);";
        ContentValues values = new ContentValues();
        values.put("id", interaction.getId());
        values.put("description", interaction.getDescription());
        values.put("agent_type_id", interaction.getAgentType().getId());
        values.put("communicative_act_id", interaction.getCommunicativeAct().getId());
        values.put("interaction_type_id", interaction.getInteractionType().getId());
        values.put("direction_id", interaction.getDirection().getId());
        values.put("interaction_id", (interaction.getPrimaryInteraction() == null) ? -1 : interaction.getPrimaryInteraction().getId());

        this.database.insertOrThrow("interactions", null, values);
        
        if (DeveloperSettings.SHOW_DAO_SQL){
            Log.d("SQL_DEBUG","INSERT INTO interactions (id,description,agent_type_id,communicative_act_id, interaction_type_id, direction_id, interaction_id) "
                + " VALUES (" + interaction.getId() + ",'" + interaction.getDescription() + "'," + interaction.getAgentType().getId() + ","
                + interaction.getCommunicativeAct().getId() + "," + interaction.getInteractionType().getId() + "," + interaction.getDirection().getId() + ","
                + ((interaction.getPrimaryInteraction() == null) ? -1 : interaction.getPrimaryInteraction().getId()) + ");");
        }
    }

    public void insertPossibleStateContents(State state) throws SQLException {
        /*String sql = "INSERT INTO possible_agent_state_contents (possible_value, default_value, agent_state_id) "
                + " VALUES (?,?,?);";*/
        
        if (state.getPossibleContents() != null) {
            for (PossibleContent possibleContent : state.getPossibleContents()) {
            	ContentValues values = new ContentValues();
            	values.put("possible_value", (String) Content.parseContent(state.getDataType(),possibleContent.getValue()));
            	values.put("default_value", possibleContent.isIsDefault());
            	values.put("agent_state_id", state.getId());
            	
            	state.setId((int)(long)this.database.insertOrThrow("possible_agent_state_contents", null, values));
            	
                if (DeveloperSettings.SHOW_DAO_SQL){
                    Log.d("SQL_DEBUG","INSERT INTO possible_agent_state_contents (id,possible_value, default_value, agent_state_id) "
                        + " VALUES (" + possibleContent.getId() + "," + Content.parseContent(state.getDataType(),possibleContent.getValue()) + "," + possibleContent.isIsDefault() + "," + state.getId() + ");");
                }
            }
        }
    }

    public void insertState(State state) throws SQLException {
        //String sql = "INSERT INTO agent_states (id,description,agent_type_id,data_type_id,superior_limit,inferior_limit,initial_value) "
        //        + " VALUES (?,?,?,?,?,?,?);";
        
        ContentValues values = new ContentValues();
        // trata o tipo de dado do estado
        state.setSuperiorLimit(Content.parseContent(state.getDataType(), state.getSuperiorLimit()));
        state.setInferiorLimit(Content.parseContent(state.getDataType(), state.getInferiorLimit()));
        state.setInitialValue(Content.parseContent(state.getDataType(), state.getInitialValue()));
        values.put("id", state.getId());
        values.put("description", state.getDescription());
        values.put("agent_type_id", state.getAgentType().getId());
        values.put("data_type_id", state.getDataType().getId());
        values.put("superior_limit", String.valueOf(state.getSuperiorLimit()));
        values.put("inferior_limit", String.valueOf(state.getInferiorLimit()));
        values.put("initial_value", String.valueOf(state.getInitialValue()));
        
        this.database.insertOrThrow("agent_states", null, values);
        
        if (DeveloperSettings.SHOW_DAO_SQL){
            Log.d("SQL_DEBUG","INSERT INTO agent_states (id,description,agent_type_id,data_type_id,superior_limit,inferior_limit,initial_value) "
                + " VALUES (" + state.getId() + ",'" + state.getDescription() + "'," + state.getAgentType().getId() + "," + state.getDataType().getId()
                + ",'" + state.getSuperiorLimit() + "','" + state.getInferiorLimit() + "','" + state.getInitialValue() + "');");
        }
    }

    public void insertParameters(InteractionModel interaction) throws SQLException {
        //String sql = "INSERT INTO interaction_parameters (description,optional,label,superior_limit,inferior_limit,initial_value,agent_state_id,data_type_id,interaction_id) "
        //        + " VALUES (?,?,?,?,?,?,?,?,?);";
        
        if (interaction.getParameters() != null) {
            for (Parameter parameter : interaction.getParameters()) {
            	ContentValues values = new ContentValues();
                // trata o tipo de dado do estado
                parameter.setSuperiorLimit(Content.parseContent(parameter.getDataType(), parameter.getSuperiorLimit()));
                parameter.setInferiorLimit(Content.parseContent(parameter.getDataType(), parameter.getInferiorLimit()));
                parameter.setInitialValue(Content.parseContent(parameter.getDataType(), parameter.getInitialValue()));
                // valores
                values.put("description", parameter.getDescription());
                values.put("optional", parameter.isOptional());
                values.put("label", parameter.getLabel());
                values.put("superior_limit", String.valueOf(parameter.getSuperiorLimit()));
                values.put("inferior_limit", String.valueOf(parameter.getInferiorLimit()));
                values.put("initial_value", String.valueOf(parameter.getInitialValue()));
                values.put("agent_state_id", (parameter.getRelatedState() == null) ? -1 : parameter.getRelatedState().getId());
                values.put("data_type_id", parameter.getDataType().getId());
                values.put("interaction_id", interaction.getId());
                // executar
                parameter.setId((int)(long)this.database.insertOrThrow("interaction_parameters", null, values));
                
                if (DeveloperSettings.SHOW_DAO_SQL){
                    Log.d("SQL_DEBUG","INSERT INTO interaction_parameters (description,optional,label,superior_limit,inferior_limit,initial_value,agent_state_id,data_type_id,interaction_id) "
                        + " VALUES (" + parameter.getId() + ",'" + parameter.getDescription() + "','" + parameter.getLabel() + "','" + parameter.getSuperiorLimit()
                        + "','" + parameter.getInferiorLimit() + "','" + parameter.getInitialValue() + "'," + ((parameter.getRelatedState() == null) ? -1 : parameter.getRelatedState().getId())
                        + "," + parameter.getDataType().getId() + "," + interaction.getId() + ");");
                }
            }
        }
    }

    public void insertPossibleParameterContents(Parameter parameter) throws SQLException {
        //String sql = "INSERT INTO possible_interaction_contents (possible_value, default_value, interaction_parameter_id) "
        //       + " VALUES (?,?,?);";
        
        if (parameter.getPossibleContents() != null) {
            for (PossibleContent possibleContent : parameter.getPossibleContents()) {
            	// valores
            	ContentValues values = new ContentValues();
            	values.put("possible_value", String.valueOf(Content.parseContent(parameter.getDataType(),possibleContent.getValue())));
            	values.put("default_value", possibleContent.isIsDefault());
            	values.put("interaction_parameter_id", parameter.getId());
            	// executar
            	possibleContent.setId((int)(long)this.database.insertOrThrow("possible_interaction_contents", null, values));
                
                if (DeveloperSettings.SHOW_DAO_SQL){
                    Log.d("SQL_DEBUG","INSERT INTO possible_interaction_contents (id,possible_value, default_value, interaction_parameter_id) "
                        + " VALUES (" + possibleContent.getId() + "," + Content.parseContent(parameter.getDataType(),possibleContent.getValue()) + "," + possibleContent.isIsDefault() + "," + parameter.getId() + ");");
                }
            }
        }
    }

    public Content getCurrentContentValue(State state) throws SQLException {
        Content content = null;
        String sql = "SELECT id, reading_value, reading_time "
                + " FROM agent_state_contents "
                + " WHERE agent_state_id = ? ORDER BY id DESC LIMIT 1;";
        
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
        //String sql = "INSERT INTO agent_state_contents (reading_value,reading_time,agent_state_id) "
        //        + " VALUES (?,?,?);";
        // values
        ContentValues values = new ContentValues();
        values.put("reading_value", String.valueOf(Content.parseContent(state.getDataType(), state.getContent().getValue())));
        values.put("reading_time", state.getContent().getTime().getTime());
        values.put("agent_state_id", state.getId());
        // insere e retorna o ID
        state.getContent().setId((int)(long)this.database.insertOrThrow("agent_state_contents", null, values));

        if (DeveloperSettings.SHOW_DAO_SQL) {
            Log.d("SQL_DEBUG","INSERT INTO agent_state_contents (id,reading_value,reading_time,agent_state_id) "
                    + " VALUES (" + state.getContent().getId() + ",'" + Content.parseContent(state.getDataType(), state.getContent().getValue()) + "',"
                    + ",'" + state.getContent().getTime().getTime() + "'," + "," + state.getId() + ");");
        }
    }

    public Content getCurrentContentValue(Parameter parameter) throws SQLException {
        Content content = null;
        String sql = "SELECT messages.id as content_id, reading_value, reading_time, message_id, message_time, interaction_id, conversation_id, "
                + " interaction_id, created_time,  finished_time, agent_id\n"
                + " FROM interaction_contents, messages, conversations\n"
                + " WHERE interaction_parameter_id = ? AND messages.id = message_id AND conversation_id = conversations.id \n"
                + " ORDER BY messages.id DESC LIMIT 1;";
        // consulta
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(parameter.getId())});
       
        if (cursor.moveToFirst()) {
        	content = new Content();
            // pegar o valor atual
            content.setId(cursor.getInt(cursor.getColumnIndex("content_id")));
            content.setTime(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("reading_time")))));
            content.setValue(Content.parseContent(parameter.getDataType(), cursor.getString(cursor.getColumnIndex("reading_value"))));
            content.setMessage(new AgentMessage());
            content.getMessage().setId(cursor.getInt(cursor.getColumnIndex("message_id")));
            content.getMessage().setTime(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("message_time")))));
        }
        return content;
    }

    public void insertContent(Parameter parameter) throws SQLException {
        //String sql = "INSERT INTO interaction_contents (reading_value,reading_time,interaction_parameter_id,message_id) "
         //       + " VALUES (?,?,?,?);";
        ContentValues values = new ContentValues();
    	values.put("reading_value", String.valueOf(Content.parseContent(parameter.getDataType(), parameter.getContent().getValue())));
    	values.put("reading_time", parameter.getContent().getTime().getTime());
    	values.put("interaction_parameter_id", parameter.getId());
    	values.put("message_id", parameter.getContent().getMessage().getId());
    	// executar
    	parameter.getContent().setId((int)(long)this.database.insertOrThrow("interaction_contents", null, values));
    	
        if (DeveloperSettings.SHOW_DAO_SQL) {
            Log.d("SQL_DEBUG","INSERT INTO interaction_contents (id,reading_value,reading_time,interaction_parameter_id,message_id) "
                    + " VALUES (" + parameter.getContent().getId() + ",'" + Content.parseContent(parameter.getDataType(), parameter.getContent().getValue()) + "',"
                    + ",'" + parameter.getContent().getTime().getTime() + "'," + "," + parameter.getId() + "," + parameter.getContent().getMessage().getId() + ");");
        }
    }

    List<InteractionModel> getAgentInteractions(AgentType agentType) throws SQLException {
        List<InteractionModel> interactions = new ArrayList();
        InteractionModel interaction = null;
        String sql = "SELECT interactions.id as id, interactions.description as interaction_desc, communicative_act_id, communicative_acts.description as act_desc,  \n"
                + "              interaction_type_id, interaction_types.description as type_desc, direction_id, interaction_directions.description as direction_desc, "
                + "              interaction_id, agent_communication_language_id, agent_communication_languages.description as language_description\n"
                + " FROM interactions, communicative_acts, interaction_types, interaction_directions, agent_communication_languages\n"
                + " WHERE agent_type_id = ? AND communicative_act_id = communicative_acts.id AND interaction_types.id = interaction_type_id "
                + " AND direction_id = interaction_directions.id and agent_communication_languages.id = agent_communication_language_id;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(agentType.getId())});

        while (cursor.moveToNext()) {
            interaction = new InteractionModel();
            interaction.setId(cursor.getInt(cursor.getColumnIndex("id")));
            interaction.setDescription(cursor.getString(cursor.getColumnIndex("interaction_desc")));
            interaction.setCommunicativeAct(
                    new CommunicativeAct(
                    		cursor.getInt(cursor.getColumnIndex("communicative_act_id")),
                    		cursor.getString(cursor.getColumnIndex("act_desc")),
                            new AgentCommunicationLanguage(
                            		cursor.getInt(cursor.getColumnIndex("agent_communication_language_id")), 
                            		cursor.getString(cursor.getColumnIndex("language_description")))));
            interaction.setDirection(new Direction(
            		cursor.getInt(cursor.getColumnIndex("direction_id")), 
            		cursor.getString(cursor.getColumnIndex("direction_desc"))));
            interaction.setAgentType(agentType);
            interaction.setInteractionType(new InteractionType(
            		cursor.getInt(cursor.getColumnIndex("interaction_type_id")), 
            		cursor.getString(cursor.getColumnIndex("type_desc"))));
            if (cursor.getInt(cursor.getColumnIndex("interaction_id")) > 0) {
                interaction.setPrimaryInteraction(
                		this.getPrimaryInteraction(cursor.getInt(cursor.getColumnIndex("interaction_id"))));
            }
            interaction.setParameters(this.getInteractionParameters(interaction));
            interactions.add(interaction);
        }
        return interactions;
    }

    List<State> getAgentStates(AgentType agentType) throws SQLException {
        List<State> states = new ArrayList();
        State state = null;
        String sql = "SELECT agent_states.id as state_id, agent_states.description as state_desc, "
                + " superior_limit, inferior_limit, \n"
                + " agent_states.initial_value, data_type_id, data_types.initial_value as data_initial_value, "
                + " data_types.description as data_desc\n"
                + " FROM agent_states, data_types\n"
                + " WHERE agent_type_id = ? and data_types.id = data_type_id;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(agentType.getId())});
        
        while (cursor.moveToNext()) {
            state = new State();
            state.setId(cursor.getInt(cursor.getColumnIndex("state_id")));
            state.setDescription(cursor.getString(cursor.getColumnIndex("state_desc")));
            DataType type = new DataType();
            type.setId(cursor.getInt(cursor.getColumnIndex("data_type_id")));
            type.setDescription(cursor.getString(cursor.getColumnIndex("data_desc")));
            state.setDataType(type);
            type.setInitialValue(Content.parseContent(state.getDataType(),cursor.getString(cursor.getColumnIndex("data_initial_value"))));
            state.setInferiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("inferior_limit"))));
            state.setSuperiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("superior_limit"))));
            state.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("initial_value"))));
            state.setUserCanChange(false);
            
            state.setPossibleContent(this.getPossibleStateContents(state));
            // pegar o valor atual
            Content c = this.getCurrentContentValue(state);
            if (c != null) { // se c for nulo deve usar os valores iniciais, senÃ£o adiciona o conteÃºdo no estado
                state.setContent(c);
            }
            states.add(state);
        }
        return states;
    }

    private InteractionModel getPrimaryInteraction(int id) throws SQLException {
        InteractionModel interaction = null;
        String sql = "SELECT agent_types.description as agent_type_description, interactions.description as interaction_desc, communicative_act_id, communicative_acts.description as act_desc,  \n"
                + "              interaction_type_id, interaction_types.description as type_desc, direction_id, interaction_directions.description as direction_desc, "
                + "              interaction_id, agent_communication_language_id, agent_communication_languages.description as language_description, agent_type_id\n"
                + " FROM interactions, communicative_acts, interaction_types, interaction_directions, agent_communication_languages, agent_types \n"
                + " WHERE interactions.id = ? AND communicative_act_id = communicative_acts.id AND interaction_types.id = interaction_type_id "
                + " AND direction_id = interaction_directions.id AND agent_communication_languages.id = agent_communication_language_id AND agent_types.id = agent_type_id;";
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            interaction = new InteractionModel();
            interaction.setId(id);
            interaction.setDescription(cursor.getString(cursor.getColumnIndex("interaction_desc")));
            interaction.setCommunicativeAct(
                    new CommunicativeAct(
                    		cursor.getInt(cursor.getColumnIndex("communicative_act_id")),
                    		cursor.getString(cursor.getColumnIndex("act_desc")),
                            new AgentCommunicationLanguage(cursor.getInt(cursor.getColumnIndex("agent_communication_language_id")), cursor.getString(cursor.getColumnIndex("language_description")))));
            interaction.setDirection(new Direction(cursor.getInt(cursor.getColumnIndex("direction_id")), cursor.getString(cursor.getColumnIndex("direction_desc"))));
            interaction.setAgentType(new AgentType(cursor.getInt(cursor.getColumnIndex("agent_type_id")), cursor.getString(cursor.getColumnIndex("agent_type_description"))));
            interaction.setInteractionType(new InteractionType(cursor.getInt(cursor.getColumnIndex("interaction_type_id")), cursor.getString(cursor.getColumnIndex("type_desc"))));
            interaction.setParameters(this.getInteractionParameters(interaction));
        }
        return interaction;
    }

    private List<Parameter> getInteractionParameters(InteractionModel interaction) throws SQLException {
        List<Parameter> parameters = new ArrayList();
        Parameter parameter = null;
        String sql = "SELECT interaction_parameters.id as id, label, interaction_parameters.description as parameter_desc, \n"
                + "                optional, superior_limit, inferior_limit, agent_state_id,\n"
                + "                interaction_parameters.initial_value, data_type_id, data_types.initial_value as data_initial_value,\n"
                + "                data_types.description as data_desc\n"
                + "                FROM interaction_parameters, data_types\n"
                + "                WHERE interaction_id = ? and data_types.id = data_type_id;";
        // consulta
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(interaction.getId())});
        
        while (cursor.moveToNext()) {
            parameter = new Parameter();
            parameter.setId(cursor.getInt(cursor.getColumnIndex("id")));
            parameter.setDescription(cursor.getString(cursor.getColumnIndex("parameter_desc")));
            parameter.setLabel(cursor.getString(cursor.getColumnIndex("label")));
            DataType type = new DataType();
            type.setId(cursor.getInt(cursor.getColumnIndex("data_type_id")));
            type.setDescription(cursor.getString(cursor.getColumnIndex("data_desc")));
            type.setInitialValue(cursor.getString(cursor.getColumnIndex("data_initial_value")));
            parameter.setDataType(type);
            parameter.setInferiorLimit(Content.parseContent(type, cursor.getString(cursor.getColumnIndex("inferior_limit"))));
            parameter.setSuperiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("superior_limit"))));
            parameter.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("initial_value"))));
            parameter.setOptional(cursor.getInt(cursor.getColumnIndex("optional")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("agent_state_id")) > 0) {
                parameter.setRelatedState(this.getInteractionState(cursor.getInt(cursor.getColumnIndex("agent_state_id"))));
            }
            
            // pegar o valor atual
            Content c = this.getCurrentContentValue(parameter);
            if (c != null) { // se c for nulo deve usar os valores iniciais, senÃ£o adiciona o conteÃºdo no estado
                parameter.setContent(c);
            }
            parameters.add(parameter);
        }
        return parameters;
    }

    private State getInteractionState(int id) throws SQLException {
        State state = null;
        String sql = "SELECT agent_states.id as state_id, agent_states.description as state_desc, \n"
                + "                superior_limit, inferior_limit, \n"
                + "                agent_states.initial_value, data_type_id, data_types.initial_value as data_initial_value,  \n"
                + "                data_types.description as data_desc \n"
                + "                FROM agent_states, data_types \n"
                + "                WHERE agent_states.id = ? and data_types.id = data_type_id;";
        // consulta
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(id)});
       
        if (cursor.moveToNext()) {
            state = new State();
            state.setId(cursor.getInt(cursor.getColumnIndex("state_id")));
            state.setDescription(cursor.getString(cursor.getColumnIndex("state_desc")));
            DataType type = new DataType();
            type.setId(cursor.getInt(cursor.getColumnIndex("data_type_id")));
            type.setDescription(cursor.getString(cursor.getColumnIndex("data_desc")));
            type.setInitialValue(cursor.getString(cursor.getColumnIndex("data_initial_value")));
            state.setDataType(type);
            state.setInferiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("inferior_limit"))));
            state.setSuperiorLimit(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("superior_limit"))));
            state.setInitialValue(Content.parseContent(type,cursor.getString(cursor.getColumnIndex("initial_value"))));
            state.setUserCanChange(false);
            state.setPossibleContent(this.getPossibleStateContents(state));
            // pegar o valor atual
            Content c = this.getCurrentContentValue(state);
            if (c != null) { // se c for nulo deve usar os valores iniciais, senÃ£o adiciona o conteÃºdo no estado
                state.setContent(c);
            }
        }
        return state;
    }

    private List<PossibleContent> getPossibleStateContents(State state) throws SQLException {
        List<PossibleContent> possibleContents = new ArrayList();
        String sql = " SELECT id, possible_value, default_value "
                + " FROM possible_agent_state_contents\n"
                + " WHERE agent_state_id = ?;";
        
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

    public List<AgentType> getAgentTypes() throws SQLException{
        List<AgentType> agentTypes = new ArrayList();
        String sql = " SELECT id, description FROM agent_types ";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{});
        
        while (cursor.moveToNext()) {
            agentTypes.add(new AgentType(
            		cursor.getInt(cursor.getColumnIndex("id")), 
            		cursor.getString(cursor.getColumnIndex("description"))));
        }
        return agentTypes;
    }
    
    List<Conversation> getAgentConversations(Agent agent) throws SQLException {
        List<Conversation> conversations = new ArrayList();
        Conversation conversation = null;
        String sql = " SELECT id, created_time, finished_time "
                + " FROM conversations\n"
                + " WHERE agent_id = ?;";
        // consulta
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(agent.getId())});
        
        while (cursor.moveToNext()) {
            conversation = new Conversation();
            conversation.setId(cursor.getInt(cursor.getColumnIndex("id")));
            if (!cursor.isNull(cursor.getColumnIndex("finished_time"))) {
                conversation.setFinishedTime( new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("finished_time")))));
            }
            conversation.setMessages(this.getAgentMessages(conversation));
            conversations.add(conversation);
        }
        return conversations;
    }

    private List<AgentMessage> getAgentMessages(Conversation conversation) throws SQLException {
        List<AgentMessage> messages = new ArrayList();
        AgentMessage message = null;
        String sql = " SELECT id, message_time,interaction_id "
                + " FROM messages\n"
                + " WHERE conversation_id = ?;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(conversation.getId())});
  
        while (cursor.moveToNext()) {
            message = new AgentMessage();
            message.setId(cursor.getInt(cursor.getColumnIndex("id")));
            message.setTime(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("message_time")))));
            if (cursor.getInt(cursor.getColumnIndex("interaction_id")) > 0) {
                message.setPreviousInteraction(this.getInteraction(cursor.getInt(cursor.getColumnIndex("interaction_id"))));
            }
            message.setContents(this.getInteractionParameterContents(message));
            messages.add(message);
        }
        return messages;
    }

    private InteractionModel getInteraction(int id) throws SQLException {
        InteractionModel interaction = null;
        String sql = "SELECT agent_types.description as agent_type_description, interactions.description as interaction_desc, communicative_act_id, communicative_acts.description as act_desc,  \n"
                + "              interaction_type_id, interaction_types.description as type_desc, direction_id, interaction_directions.description as direction_desc, "
                + "              interaction_id, agent_communication_language_id, agent_communication_languages.description as language_description, agent_type_id\n"
                + " FROM interactions, communicative_acts, interaction_types, interaction_directions, agent_communication_languages, agent_types \n"
                + " WHERE interactions.id = ? AND communicative_act_id = communicative_acts.id AND interaction_types.id = interaction_type_id "
                + " AND direction_id = interaction_directions.id AND agent_communication_languages.id = agent_communication_language_id AND agent_types.id = agent_type_id;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(id)});

        if (cursor.moveToNext()) {
            interaction = new InteractionModel();
            interaction.setId(id);
            interaction.setDescription(cursor.getString(cursor.getColumnIndex("interaction_desc")));
            interaction.setCommunicativeAct(
                    new CommunicativeAct(
                            cursor.getInt(cursor.getColumnIndex("communicative_act_id")),
                            cursor.getString(cursor.getColumnIndex("act_desc")),
                            new AgentCommunicationLanguage(cursor.getInt(cursor.getColumnIndex("agent_communication_language_id")), cursor.getString(cursor.getColumnIndex("language_description")))));
            interaction.setDirection(new Direction(cursor.getInt(cursor.getColumnIndex("direction_id")), cursor.getString(cursor.getColumnIndex("direction_desc"))));
            interaction.setAgentType(new AgentType(cursor.getInt(cursor.getColumnIndex("agent_type_id")), cursor.getString(cursor.getColumnIndex("agent_type_description"))));
            interaction.setInteractionType(new InteractionType(cursor.getInt(cursor.getColumnIndex("interaction_type_id")), cursor.getString(cursor.getColumnIndex("type_desc"))));
            if (cursor.getInt(cursor.getColumnIndex("interaction_id")) > 0) {
                interaction.setPrimaryInteraction(this.getPrimaryInteraction(cursor.getInt(cursor.getColumnIndex("interaction_id"))));
            }
            interaction.setParameters(this.getInteractionParameters(interaction));
        }
        return interaction;
    }

    private List<Content> getInteractionParameterContents(AgentMessage message) throws SQLException {
        List<Content> contents = new ArrayList();
        Content content = null;
        String sql = " SELECT id, reading_value, reading_time, interaction_parameter_id "
                + " FROM interaction_contents \n"
                + " WHERE message_id = ?;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(message.getId())});
        
        while (cursor.moveToNext()) {
            content = new Content();
            content.setId(cursor.getInt(cursor.getColumnIndex("id")));
            if (!cursor.isNull(cursor.getColumnIndex("reading_time"))) {
                content.setTime(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("reading_time")))));
            }
            content.setParameter(this.getParameter(content));
            content.setValue(cursor.getString(cursor.getColumnIndex("reading_value")));
            content.setMessage(message);
            contents.add(content);
        }
        return contents;
    }

    private Parameter getParameter(Content content) throws SQLException {
        Parameter parameter = null;
        String sql = "SELECT interaction_parameters.id as interation_id, label, interaction_parameters.description as parameter_desc, \n"
                + "                optional, superior_limit, inferior_limit, agent_state_id,\n"
                + "                interaction_parameters.initial_value, data_type_id, data_types.initial_value as data_initial_value,\n"
                + "                data_types.description as data_desc\n"
                + "                FROM interaction_parameters, data_types, interaction_contents \n"
                + "                WHERE interaction_contents.id = ? and data_types.id = data_type_id;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(content.getId())});

        while (cursor.moveToNext()) {
            parameter = new Parameter();
            parameter.setId(cursor.getInt(cursor.getColumnIndex("interation_id")));
            parameter.setDescription(cursor.getString(cursor.getColumnIndex("parameter_desc")));
            parameter.setLabel(cursor.getString(cursor.getColumnIndex("label")));
            DataType type = new DataType();
            type.setId(cursor.getInt(cursor.getColumnIndex("data_type_id")));
            type.setDescription(cursor.getString(cursor.getColumnIndex("data_desc")));
            type.setInitialValue(cursor.getString(cursor.getColumnIndex("data_initial_value")));
            parameter.setInferiorLimit(Content.parseContent(type, cursor.getString(cursor.getColumnIndex("inferior_limit"))));
            parameter.setSuperiorLimit(Content.parseContent(type, cursor.getString(cursor.getColumnIndex("superior_limit"))));
            parameter.setInitialValue(Content.parseContent(type, cursor.getString(cursor.getColumnIndex("initial_value"))));
            parameter.setOptional(cursor.getInt(cursor.getColumnIndex("optional")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("agent_state_id")) > 0) {
                parameter.setRelatedState(this.getInteractionState(cursor.getInt(cursor.getColumnIndex("agent_state_id"))));
            }
            parameter.setContent(content);
            parameter.setDataType(type);
        }
        return parameter;
    }

    public InteractionModel getInteractionModel(Event event) throws SQLException {
        return getInteraction(event.getId());
    }

    public InteractionModel getInteractionModel(int interactionId) throws SQLException {
        return getInteraction(interactionId);
    }
    
    public List<Parameter> getInteractionParameterContents(Action action) throws SQLException {
        return getInteractionParameterContents(action.getDataBaseId());
    }
    
    public List<Parameter> getInteractionParameterContents(int dataBaseEventId) throws SQLException {
        List<Parameter> parameters = new ArrayList();
        Parameter parameter;
        Content content;
        String sql = "interaction_contents.id as content_id, reading_value, reading_time, interaction_parameters.id as parameter_id, "
                + " label, interaction_parameters.description as parameter_desc, optional, superior_limit, "
                + " inferior_limit, agent_state_id, interaction_parameters.initial_value, data_type_id, "
                + " data_types.initial_value as data_initial_value, data_types.description as data_desc "
                + " FROM interaction_contents, interaction_parameters, data_types "
                + " WHERE generated_event_id = 1  AND interaction_parameter_id = interaction_parameters.id AND data_types.id = data_type_id "
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
            parameter.setInferiorLimit(cursor.getString(cursor.getColumnIndex("inferior_limit")));
            parameter.setSuperiorLimit(cursor.getString(cursor.getColumnIndex("superior_limit")));
            parameter.setInitialValue(cursor.getString(cursor.getColumnIndex("initial_value")));
            parameter.setOptional(cursor.getInt(cursor.getColumnIndex("optional")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("agent_state_id")) > 0) {
                parameter.setRelatedState(getInteractionState(
                        cursor.getInt(cursor.getColumnIndex("agent_state_id"))));
            }
            // pega o valor utilizado na fun��o
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
                + " FROM possible_interaction_contents\n"
                + " WHERE interaction_parameter_id = ?;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(parameter.getId())});
        
        while (cursor.moveToNext()) {
            possibleContents.add(new PossibleContent(cursor.getInt(cursor.getColumnIndex("id")), Content
                    .parseContent(parameter.getDataType(),
                            cursor.getString(cursor.getColumnIndex("possible_value"))), cursor
                    .getInt(cursor.getColumnIndex("default_value")) > 0));
        }
        
        return possibleContents;
    }
}
