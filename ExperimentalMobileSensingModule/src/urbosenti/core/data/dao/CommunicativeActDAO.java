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
import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import urbosenti.core.device.model.AgentCommunicationLanguage;
import urbosenti.core.device.model.CommunicativeAct;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class CommunicativeActDAO {

	public static final String TABLE_NAME = "communicative_acts";  
    public static final String COLUMN_ID = "id"; 
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_AGENT_COMMUNICATION_LANGUAGE_ID = "agent_communication_language_id";
	private SQLiteDatabase database;

    public CommunicativeActDAO(Object context) {
    	this.database = (SQLiteDatabase) context;
    }

    public void insert(CommunicativeAct type) throws SQLException {
        //String sql = "INSERT INTO communicative_acts (id, description, agent_communication_language_id) "
        //        + "VALUES (?,?,?);";
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, type.getId());
        values.put(COLUMN_DESCRIPTION, type.getDescription());
        values.put(COLUMN_AGENT_COMMUNICATION_LANGUAGE_ID, type.getAgentCommunicationLanguage().getId());
        
        this.database.insertOrThrow(TABLE_NAME, null, values);
        
        if (DeveloperSettings.SHOW_DAO_SQL) {
        	Log.d("SQL_DEBUG","INSERT INTO communicative_acts (id, description, agent_communication_language_id) "
                    + " VALUES (" + type.getId() + ",'" + type.getDescription() + "'," + type.getAgentCommunicationLanguage().getId() + ");");
        }
    }
    
    public ArrayList<CommunicativeAct> getCommunicativeActs(AgentCommunicationLanguage acl) throws SQLException {
        ArrayList<CommunicativeAct> communicativeActs = new ArrayList();
        CommunicativeAct communicativeAct;
        
        Cursor cursor = this.database.rawQuery(
        		"SELECT id,description FROM agent_communication_languages; ", null);

        while (cursor.moveToNext()) {
            communicativeAct = new CommunicativeAct(
            		cursor.getInt(cursor.getColumnIndex("id")), 
            		cursor.getString(cursor.getColumnIndex("description")), acl);
            communicativeActs.add(communicativeAct);
        }
        return communicativeActs;
    }
}
