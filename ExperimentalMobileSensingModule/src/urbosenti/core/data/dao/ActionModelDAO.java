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
import urbosenti.adaptation.ExecutionPlan;
import urbosenti.core.data.DataManager;
import urbosenti.core.device.model.ActionModel;
import urbosenti.core.device.model.Content;
import urbosenti.core.device.model.DataType;
import urbosenti.core.device.model.Entity;
import urbosenti.core.device.model.FeedbackAnswer;
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
public class ActionModelDAO {

	private DataManager dataManager;
	private SQLiteDatabase database;

    public ActionModelDAO(Object context) {
    	this.database = (SQLiteDatabase) context;
        this.dataManager = null;
    }
    
    public ActionModelDAO(Object context,DataManager dataManager) {
    	this.database = (SQLiteDatabase) context;
        this.dataManager = dataManager;
    }

    public void insert(ActionModel action) throws SQLException {
        //String sql = "INSERT INTO actions (model_id,description,has_feedback,entity_id) "
        //        + " VALUES (?,?,?,?);";
        action.setModelId(action.getId());
        ContentValues values = new ContentValues();
        values.put("model_id", action.getId());
        values.put("description", action.getDescription());
        values.put("has_feedback", action.isHasFeedback());
        values.put("entity_id", action.getEntity().getId());
        
        action.setId((int)(long)this.database.insertOrThrow("actions", null, values));
        
        if (DeveloperSettings.SHOW_DAO_SQL) {
        	Log.d("SQL_DEBUG","INSERT INTO actions (id,model_id,description,has_feedback,entity_id) "
                            + " VALUES ("
                            + action.getId()
                            + ","
                            + action.getModelId()
                            + ",'"
                            + action.getDescription()
                            + "',"
                            + action.isHasFeedback()
                            + ","
                            + action.getEntity().getId() + ");");
        }
    }

    public void insertFeedbackAnswers(ActionModel action) throws SQLException {
        //String sql = "INSERT INTO possible_action_contents (description, action_id) "
        //        + " VALUES (?,?);";
        
        if (action.getFeedbackAnswers() != null) {
            for (FeedbackAnswer feedbackAnswer : action.getFeedbackAnswers()) {
            	ContentValues values = new ContentValues();
                values.put("description", feedbackAnswer.getDescription());
                values.put("action_id", action.getId());
                
                feedbackAnswer.setId((int)(long)this.database.insertOrThrow("possible_action_contents", null, values));
                
                if (DeveloperSettings.SHOW_DAO_SQL) {
                	Log.d("SQL_DEBUG","INSERT INTO possible_action_contents (id,description, action_id) "
                                    + " VALUES ("
                                    + feedbackAnswer.getId()
                                    + ",'"
                                    + feedbackAnswer.getDescription()
                                    + "'," + action.getId() + ");");
                }
            }
        }
    }

    public void insertParameters(ActionModel action) throws SQLException {
        //String sql = "INSERT INTO action_parameters (description,optional,label,superior_limit,inferior_limit,initial_value,entity_state_id,data_type_id,action_id) "
        //        + " VALUES (?,?,?,?,?,?,?,?,?);";
        
        for (Parameter parameter : action.getParameters()) {
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
            values.put("inferior_limit",  String.valueOf(parameter.getInferiorLimit()));
            values.put("initial_value",  String.valueOf(parameter.getInitialValue()));
            values.put("entity_state_id", (parameter.getRelatedState() == null) ? -1 : parameter.getRelatedState().getId());
            values.put("data_type_id", parameter.getDataType().getId());
            values.put("action_id", action.getId());
            // executar
            parameter.setId((int)(long)this.database.insertOrThrow("action_parameters", null, values));
        	
            if (DeveloperSettings.SHOW_DAO_SQL) {
            	Log.d("SQL_DEBUG","INSERT INTO action_parameters (id,description,optional,label,superior_limit,inferior_limit,initial_value,entity_state_id,data_type_id,event_id) "
                                + " VALUES ("
                                + parameter.getId()
                                + ",'"
                                + parameter.getDescription()
                                + "',"
                                + parameter.isOptional()
                                + ",'"
                                + parameter.getLabel()
                                + "','"
                                + parameter.getSuperiorLimit()
                                + "','"
                                + parameter.getInferiorLimit()
                                + "','"
                                + parameter.getInitialValue()
                                + "',"
                                + ((parameter.getRelatedState() == null) ? -1
                                        : parameter.getRelatedState().getId())
                                + ","
                                + parameter.getDataType().getId()
                                + ","
                                + action.getId() + ");");
            }

        }
    }

    /**
     *
     * @param parameter
     * @throws SQLException
     */
    public void insertPossibleParameterContents(Parameter parameter)
            throws SQLException {
        //String sql = "INSERT INTO possible_action_contents (possible_value, default_value, action_parameter_id) "
        //        + " VALUES (?,?,?);";

        if (parameter.getPossibleContents() != null) {
            for (PossibleContent possibleContent : parameter
                    .getPossibleContents()) {
            	ContentValues values = new ContentValues();
                values.put("possible_value", String.valueOf(Content.parseContent(parameter.getDataType(), possibleContent.getValue())));
                values.put("default_value", possibleContent.isIsDefault());
                values.put("action_parameter_id", parameter.getId());
                
                possibleContent.setId((int)(long)this.database.insertOrThrow("possible_action_contents", null, values));
            	
                if (DeveloperSettings.SHOW_DAO_SQL) {
                	Log.d("SQL_DEBUG","INSERT INTO possible_action_contents (id,possible_value, default_value, event_parameter_id) "
                                    + " VALUES ("
                                    + possibleContent.getId()
                                    + ","
                                    + Content.parseContent(
                                            parameter.getDataType(),
                                            possibleContent.getValue())
                                    + ","
                                    + possibleContent.isIsDefault()
                                    + ","
                                    + parameter.getId() + ");");
                }
            }
        }
    }

    public Content getCurrentContentValue(Parameter parameter)
            throws SQLException {
        Content content = null;
        String sql = "SELECT id, reading_value, reading_time, score "
                + " FROM action_contents "
                + " WHERE action_parameter_id = ? ORDER BY id DESC LIMIT 1;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(parameter.getId())});
        
        if (cursor.moveToFirst()) {
            content = new Content();
            // pegar o valor atual
            content.setId(cursor.getInt(cursor.getColumnIndex("id")));
            content.setTime(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("reading_time")))));
            content.setValue(Content.parseContent(parameter.getDataType(), cursor.getString(cursor.getColumnIndex("reading_value"))));
            content.setScore(cursor.getDouble(cursor.getColumnIndex("score")));
        }
        return content;
    }

    public void insertContent(Parameter parameter, Action action) throws SQLException {
        //String sql = "INSERT INTO action_contents (reading_value,reading_time,action_parameter_id,score,generated_action_id) "
        //        + " VALUES (?,?,?,?,?);";
        
        ContentValues values = new ContentValues();
    	values.put("reading_value",  String.valueOf(Content.parseContent(parameter.getDataType(), parameter.getContent().getValue())));
    	values.put("reading_time", parameter.getContent().getTime().getTime());
    	values.put("action_parameter_id", parameter.getId());
    	values.put("generated_action_id", action.getDataBaseId());
    	values.put("score", parameter.getContent().getScore());
    	
    	// executar
    	parameter.getContent().setId((int)(long)this.database.insertOrThrow("action_contents", null, values));
        
        if (DeveloperSettings.SHOW_DAO_SQL) {
        	Log.d("SQL_DEBUG","INSERT INTO action_contents (id,reading_value,reading_time,action_parameter_id,score) "
                            + " VALUES ("
                            + parameter.getContent().getId()
                            + ",'"
                            + parameter.getContent().getValue()
                            + "',"
                            + ",'"
                            + parameter.getContent().getTime().getTime()
                            + "'," + parameter.getId() + ");");
        }
    }

    List<ActionModel> getEntityActionModels(Entity entity) throws SQLException {
        List<ActionModel> actions = new ArrayList();
        ActionModel action = null;
        String sql = "SELECT id, model_id, description, has_feedback "
                + "FROM actions\n" + "WHERE entity_id = ? ;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(entity.getId())});
        
        while (cursor.moveToNext()) {
            action = new ActionModel();
            action.setId(cursor.getInt(cursor.getColumnIndex("id")));
            action.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            action.setHasFeedback(cursor.getInt(cursor.getColumnIndex("has_feedback")) > 0);
            action.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            action.setEntity(entity);
            action.setFeedbackAnswers(this.getActionFeedbackAnswers(action));
            action.setParameters(this.getActionParameters(action));
            actions.add(action);
        }
        return actions;
    }

    ActionModel getActionModel(int id) throws SQLException {
        ActionModel action = null;
        String sql = "SELECT actions.id, actions.model_id, actions.description, has_feedback, entities.description as entity_description, entity_id "
                + "FROM actions, entities "
                + "WHERE actions.id = ? AND entity_id = entities.id;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(id)});
        
        while (cursor.moveToNext()) {
            action = new ActionModel();
            action.setId(cursor.getInt(cursor.getColumnIndex("id")));
            action.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            action.setHasFeedback(cursor.getInt(cursor.getColumnIndex("has_feedback")) > 0);
            action.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            action.setEntity(new Entity());
            action.getEntity().setDescription(
                    cursor.getString(cursor.getColumnIndex("entity_description")));
            action.getEntity().setId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            action.setFeedbackAnswers(this.getActionFeedbackAnswers(action));
            action.setParameters(this.getActionParameters(action));
        }
        return action;
    }

    ActionModel getActionModel(int modelId, int entityModelId, int componentId) throws SQLException {
        ActionModel action = null;
        String sql = "SELECT actions.id, actions.model_id, actions.description, has_feedback, entities.description as entity_description, entity_id "
                + "FROM actions, entities "
                + " WHERE actions.model_id = ? AND entities.model_id = ? AND component_id = ? AND entity_id = entities.id; ";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(modelId),
        		String.valueOf(entityModelId),
        		String.valueOf(componentId)
        });

        while (cursor.moveToNext()) {
            action = new ActionModel();
            action.setId(cursor.getInt(cursor.getColumnIndex("id")));
            action.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            action.setHasFeedback(cursor.getInt(cursor.getColumnIndex("has_feedback")) > 0);
            action.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            action.setEntity(new Entity());
            action.getEntity().setDescription(
                    cursor.getString(cursor.getColumnIndex("entity_description")));
            action.getEntity().setId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            action.setFeedbackAnswers(this.getActionFeedbackAnswers(action));
            action.setParameters(this.getActionParameters(action));
        }
        return action;
    }

    private List<FeedbackAnswer> getActionFeedbackAnswers(ActionModel action)
            throws SQLException {
        List<FeedbackAnswer> answers = new ArrayList();
        FeedbackAnswer answer = null;
        String sql = "SELECT id, description "
                + " FROM action_feedback_answer\n" + " WHERE action_id = ? ;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(action.getId())});
        
        while (cursor.moveToNext()) {
            answer = new FeedbackAnswer();
            answer.setId(cursor.getInt(cursor.getColumnIndex("id")));
            answer.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            answers.add(answer);
        }
        return answers;
    }

    private List<Parameter> getActionParameters(ActionModel action)
            throws SQLException {
        List<Parameter> parameters = new ArrayList();
        Parameter parameter = null;
        String sql = "SELECT action_parameters.id as parameter_id, label, action_parameters.description as parameter_desc, \n"
                + "                optional, superior_limit, inferior_limit, entity_state_id,\n"
                + "                action_parameters.initial_value, data_type_id, data_types.initial_value as data_initial_value,\n"
                + "                data_types.description as data_desc\n"
                + "                FROM action_parameters, data_types\n"
                + "                WHERE action_id = ? and data_types.id = data_type_id;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(action.getId())});
        
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
        	
            // pegar o valor atual
            Content c = this.getCurrentContentValue(parameter);
            if (c != null) { // se c for nulo deve usar os valores iniciais,
                // senão adiciona o conteúdo no estado
                parameter.setContent(c);
            }
            parameter.setPossibleContents(this.getPossibleContents(parameter));
            parameters.add(parameter);
        }
        return parameters;
    }

    ActionModel getActionState(State entityState) throws SQLException {
        ActionModel action = null;
        String sql = "SELECT action_id, actions.model_id as action_model_id, actions.description as action_desc, has_feedback, entity_id, "
                + " action_parameters.id as parameter_id, action_parameters.description as parameter_desc, label, optional, superior_limit, "
                + " inferior_limit, action_parameters.initial_value, data_type_id, data_types.description as data_type_desc, "
                + " data_types.initial_value as type_initial_value "
                + " FROM actions, action_parameters, data_types "
                + " WHERE entity_state_id = ?  AND action_id = actions.id AND data_type_id = data_types.id; ";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(entityState.getId())});

        if (cursor.moveToNext()) {
            action = new ActionModel();
            action.setId(cursor.getInt(cursor.getColumnIndex("action_id")));
            action.setDescription(cursor.getString(cursor.getColumnIndex("action_desc")));
            action.setHasFeedback(cursor.getInt(cursor.getColumnIndex("has_feedback")) > 0);
            action.setModelId(cursor.getInt(cursor.getColumnIndex("action_model_id")));
            action.setEntity(new Entity());
            action.getEntity().setId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            action.setFeedbackAnswers(this.getActionFeedbackAnswers(action));
            action.getParameters().add(
                    new Parameter(cursor.getString(cursor.getColumnIndex("label")), new DataType(cursor
                                    .getInt(cursor.getColumnIndex("data_type_id")), cursor
                                    .getString(cursor.getColumnIndex("data_type_desc")))));
            action.getParameters().get(0).setId(cursor.getInt(cursor.getColumnIndex("parameter_id")));
            action.getParameters()
                    .get(0)
                    .setInitialValue(
                            Content.parseContent(action.getParameters().get(0)
                                    .getDataType(),
                                    cursor.getString(cursor.getColumnIndex("initial_value"))));
            action.getParameters()
                    .get(0)
                    .setContent(
                            getCurrentContentValue(action.getParameters()
                                    .get(0)));
            action.getParameters().get(0).setRelatedState(entityState);
            action.getParameters()
                    .get(0)
                    .setSuperiorLimit(
                            Content.parseContent(action.getParameters().get(0)
                                    .getDataType(),
                                    cursor.getString(cursor.getColumnIndex("superior_limit"))));
            action.getParameters()
                    .get(0)
                    .setInferiorLimit(
                            Content.parseContent(action.getParameters().get(0)
                                    .getDataType(),
                                    cursor.getString(cursor.getColumnIndex("inferior_limit"))));
            action.getParameters()
                    .get(0)
                    .setPossibleContents(
                            this.getPossibleContents(action.getParameters()
                                    .get(0)));
        }
        return action;
    }

    private List<PossibleContent> getPossibleContents(Parameter parameter)
            throws SQLException {
        List<PossibleContent> possibleContents = new ArrayList();
        String sql = " SELECT id, possible_value, default_value "
                + " FROM possible_action_contents\n"
                + " WHERE action_parameter_id = ?;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(parameter.getId())});
        
        while (cursor.moveToNext()) {
            possibleContents.add(new PossibleContent(cursor.getInt(cursor.getColumnIndex("id")), Content
                    .parseContent(parameter.getDataType(),
                            cursor.getString(cursor.getColumnIndex("possible_value"))), cursor
                    .getInt(cursor.getColumnIndex("default_value")) > 0));
        }
        return possibleContents;
    }

    void insertAction(FeedbackAnswer response, Event event, Action actionToExecute, ExecutionPlan ep) throws SQLException {
        //String sql = "INSERT INTO generated_actions (action_model_id, entity_id, component_id, parameters, "
        //        + " response_time, feedback_id, feedback_description, action_type, execution_plan_id, event_id, event_type) "
        //        + " VALUES (?,?,?,?,?,?,?,?,?,?,?);";
        ContentValues values = new ContentValues();
        values.put("action_model_id", actionToExecute.getId());
        values.put("entity_id", actionToExecute.getTargetEntityId());
        values.put("component_id", actionToExecute.getTargetComponentId());
        values.put("parameters", actionToExecute.getParameters().toString());
        values.put("response_time", response.getTime().getTime());
        values.put("feedback_id", response.getId());
        values.put("feedback_description", response.getDescription());
        values.put("action_type", actionToExecute.getActionType());
        values.put("execution_plan_id", ep.getId());
        values.put("event_id", event.getDatabaseId());
        values.put("event_type", event.getEventType());

        actionToExecute.setDataBaseId((int)(long)this.database.insertOrThrow("generated_actions", null, values));
    }

    public ArrayList<Action> getActions(int actionModelId, int entityId, int componentId) throws SQLException {
        ArrayList<Action> actions = new ArrayList<Action>();
        Action action;
        FeedbackAnswer feedbackAnswer;
        String sql = "SELECT id, action_model_id, entity_id, component_id, action_type, response_time, feedback_id "
                + " FROM generated_actions WHERE action_model_id = ? AND entity_id = ? AND component_id = ?;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(actionModelId),
        		String.valueOf(entityId),
        		String.valueOf(componentId)
        });

        while (cursor.moveToNext()) {
            action = new Action();
            feedbackAnswer = new FeedbackAnswer(cursor.getInt(cursor.getColumnIndex("feedback_id")));
            feedbackAnswer.setTime(new Date(cursor.getLong(cursor.getColumnIndex("response_time"))));
            action.setId(cursor.getInt(cursor.getColumnIndex("action_model_id")));
            action.setDataBaseId(cursor.getInt(cursor.getColumnIndex("id")));
            action.setActionType(cursor.getInt(cursor.getColumnIndex("action_type")));
            action.setFeedbackAnswer(feedbackAnswer);
            action.setTargetEntityId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            action.setTargetComponentId(cursor.getInt(cursor.getColumnIndex("component_id")));
            actions.add(action);
        }
        return actions;
    }

    public ArrayList<Action> getActions(int feedbackId) throws SQLException {
        ArrayList<Action> actions = new ArrayList<Action>();
        Action action;
        FeedbackAnswer feedbackAnswer;
        String sql = "SELECT  id, action_model_id, entity_id, component_id, action_type, response_time, feedback_id "
                + " FROM generated_actions "
                + " WHERE feedback_id = ? ORDER BY id;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(feedbackId)});
        
        while (cursor.moveToNext()) {
        	action = new Action();
            feedbackAnswer = new FeedbackAnswer(cursor.getInt(cursor.getColumnIndex("feedback_id")));
            feedbackAnswer.setTime(new Date(cursor.getLong(cursor.getColumnIndex("response_time"))));
            action.setId(cursor.getInt(cursor.getColumnIndex("action_model_id")));
            action.setDataBaseId(cursor.getInt(cursor.getColumnIndex("id")));
            action.setActionType(cursor.getInt(cursor.getColumnIndex("action_type")));
            action.setFeedbackAnswer(feedbackAnswer);
            action.setTargetEntityId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            action.setTargetComponentId(cursor.getInt(cursor.getColumnIndex("component_id")));
            actions.add(action);
        }
        return actions;
    }

    public ArrayList<Action> getActions(int feedbackId, Date startDate, Date lastDate) throws SQLException {
        return this.getActions(feedbackId, startDate.getTime(), lastDate.getTime());
    }

    public ArrayList<Action> getActions(int feedbackId, long startDate, long lastDate) throws SQLException {
        ArrayList<Action> actions = new ArrayList<Action>();
        Action action;
        FeedbackAnswer feedbackAnswer;
        String sql = "SELECT  id, action_model_id, entity_id, component_id, action_type, response_time, feedback_id "
                + " FROM generated_actions "
                + " WHERE feedback_id = ? AND response_time >= ? AND response_time <= ?  ORDER BY id;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(feedbackId),
        		String.valueOf(startDate),
        		String.valueOf(lastDate)
        });

        while (cursor.moveToNext()) {
        	action = new Action();
            feedbackAnswer = new FeedbackAnswer(cursor.getInt(cursor.getColumnIndex("feedback_id")));
            feedbackAnswer.setTime(new Date(cursor.getLong(cursor.getColumnIndex("response_time"))));
            action.setId(cursor.getInt(cursor.getColumnIndex("action_model_id")));
            action.setDataBaseId(cursor.getInt(cursor.getColumnIndex("id")));
            action.setActionType(cursor.getInt(cursor.getColumnIndex("action_type")));
            action.setFeedbackAnswer(feedbackAnswer);
            action.setTargetEntityId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            action.setTargetComponentId(cursor.getInt(cursor.getColumnIndex("component_id")));
            actions.add(action);
        }
        return actions;
    }

    public ArrayList<Action> getActionsFeedbackErrors(Date startDate, Date lastDate) throws SQLException {
        return getActionsFeedbackErros(startDate.getTime(), lastDate.getTime());
    }

    public ArrayList<Action> getActionsFeedbackErros(long startDate, long lastDate) throws SQLException {
        ArrayList<Action> actions = new ArrayList<Action>();
        Action action;
        FeedbackAnswer feedbackAnswer;
        String sql = "SELECT  id, action_model_id, entity_id, component_id, action_type, response_time, feedback_id "
                + " FROM generated_actions "
                + " WHERE feedback_id <> ? AND response_time >= ? AND response_time <= ?  ORDER BY id;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(FeedbackAnswer.ACTION_RESULT_WAS_SUCCESSFUL),
        		String.valueOf(startDate),
        		String.valueOf(lastDate)
        });
        
        while (cursor.moveToNext()) {
        	action = new Action();
            feedbackAnswer = new FeedbackAnswer(cursor.getInt(cursor.getColumnIndex("feedback_id")));
            feedbackAnswer.setTime(new Date(cursor.getLong(cursor.getColumnIndex("response_time"))));
            action.setId(cursor.getInt(cursor.getColumnIndex("action_model_id")));
            action.setDataBaseId(cursor.getInt(cursor.getColumnIndex("id")));
            action.setActionType(cursor.getInt(cursor.getColumnIndex("action_type")));
            action.setFeedbackAnswer(feedbackAnswer);
            action.setTargetEntityId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            action.setTargetComponentId(cursor.getInt(cursor.getColumnIndex("component_id")));
            actions.add(action);
        }
        return actions;
    }

    public ArrayList<Action> getActions(int actionModelId, int entityId, int componentId, Date startDate, Date lastDate) throws SQLException {
        return this.getActions(actionModelId, entityId, componentId, startDate.getTime(), lastDate.getTime());
    }

    public ArrayList<Action> getActions(int actionModelId, int entityId, int componentId, long startDate, long lastDate) throws SQLException {
        ArrayList<Action> actions = new ArrayList<Action>();
        Action action;
        FeedbackAnswer feedbackAnswer;
        String sql = "SELECT id, action_model_id, entity_id, component_id, action_type, response_time, feedback_id "
                + " FROM generated_actions WHERE action_model_id = ? AND entity_id = ? AND component_id = ?"
                + " AND response_time >= ? AND response_time <= ? ORDER BY id;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(actionModelId),
        		String.valueOf(entityId),
        		String.valueOf(componentId),
        		String.valueOf(startDate),
        		String.valueOf(lastDate)
        });

        while (cursor.moveToNext()) {
        	action = new Action();
            feedbackAnswer = new FeedbackAnswer(cursor.getInt(cursor.getColumnIndex("feedback_id")));
            feedbackAnswer.setTime(new Date(cursor.getLong(cursor.getColumnIndex("response_time"))));
            action.setId(cursor.getInt(cursor.getColumnIndex("action_model_id")));
            action.setDataBaseId(cursor.getInt(cursor.getColumnIndex("id")));
            action.setActionType(cursor.getInt(cursor.getColumnIndex("action_type")));
            action.setFeedbackAnswer(feedbackAnswer);
            action.setTargetEntityId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            action.setTargetComponentId(cursor.getInt(cursor.getColumnIndex("component_id")));
            actions.add(action);
        }
        return actions;
    }

    public Action getAction(int actionId) throws SQLException {
        Action action = null;
        FeedbackAnswer feedbackAnswer;
        String sql = "SELECT id, action_model_id, entity_id, component_id, action_type, response_time, feedback_id "
                + " FROM generated_actions WHERE id = ? ;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(actionId)});
        
        while (cursor.moveToNext()) {
            action = new Action();
            feedbackAnswer = new FeedbackAnswer(cursor.getInt(cursor.getColumnIndex("feedback_id")));
            feedbackAnswer.setTime(new Date(cursor.getLong(cursor.getColumnIndex("response_time"))));
            action.setId(cursor.getInt(cursor.getColumnIndex("action_model_id")));
            action.setDataBaseId(cursor.getInt(cursor.getColumnIndex("id")));
            action.setActionType(cursor.getInt(cursor.getColumnIndex("action_type")));
            action.setFeedbackAnswer(feedbackAnswer);
            action.setTargetEntityId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            action.setTargetComponentId(cursor.getInt(cursor.getColumnIndex("component_id")));
        }
        return action;
    }

    public List<Parameter> getActionParameterContents(Action action) throws SQLException {
        return getActionParameterContents(action.getDataBaseId());
    }

    public List<Parameter> getActionParameterContents(int dataBaseActionId) throws SQLException {
        List<Parameter> parameters = new ArrayList();
        Parameter parameter;
        Content content;
        String sql = "SELECT action_contents.id as content_id, reading_value, reading_time, score,"
                + "     action_parameters.id as parameter_id, label, action_parameters.description as parameter_desc, "
                + "     optional, superior_limit, inferior_limit, entity_state_id, action_parameters.initial_value, "
                + "     data_type_id, data_types.initial_value as data_initial_value, data_types.description as data_desc "
                + " FROM action_contents, action_parameters, data_types WHERE generated_action_id = ? "
                + " AND action_parameter_id = action_parameters.id AND data_types.id = data_type_id "
                + " ORDER BY generated_action_id ;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(dataBaseActionId)});
        
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
            content.setScore(cursor.getDouble(cursor.getColumnIndex("score")));
            parameter.setContent(content);

            parameter.setPossibleContents(this.getPossibleContents(parameter));
            parameters.add(parameter);
        }
        return parameters;
    }
}
