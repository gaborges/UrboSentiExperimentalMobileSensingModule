/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.data.dao;

import java.sql.SQLException;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import urbosenti.core.device.model.Agent;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class AgentDAO {

	public static final String TABLE_NAME = "agents";  
    public static final String COLUMN_ID = "id"; 
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_AGENT_TYPE_ID = "agent_type_id";
    public static final String COLUMN_SERVICE_ID = "service_id";
    public static final String COLUMN_LAYER = "layer";
	private SQLiteDatabase database;

    public AgentDAO(Object context) {
    	this.database = (SQLiteDatabase) context;
    }

    public void insert(Agent agent) throws SQLException {
        //String sql = "INSERT INTO agents ( address, agent_type_id, service_id, layer) "
        //        + "VALUES (?,?,?,?);";
    	ContentValues values = new ContentValues();
        values.put(COLUMN_ADDRESS, agent.getRelativeAddress());
        values.put(COLUMN_AGENT_TYPE_ID, agent.getAgentType().getId());
        values.put(COLUMN_SERVICE_ID, agent.getService().getId());
        values.put(COLUMN_LAYER, agent.getLayer());
        
        this.database.insertOrThrow(TABLE_NAME, null, values);
        
        if (DeveloperSettings.SHOW_DAO_SQL) {
        	Log.d("SQL_DEBUG","INSERT INTO agents (id, address, agent_type_id, service_id, layer)  "
                    + " VALUES (" + agent.getId() + ",'" + agent.getAddress() + "'," + agent.getAgentType().getId() + "," + agent.getService().getId() + "," + agent.getLayer() + ");");
        }
    }

}
