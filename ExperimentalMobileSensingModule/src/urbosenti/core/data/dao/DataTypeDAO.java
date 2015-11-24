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
import urbosenti.core.device.model.Content;
import urbosenti.core.device.model.DataType;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class DataTypeDAO {

	public static final String TABLE_NAME = "data_types";  
    public static final String COLUMN_ID = "id"; 
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_INITIAL_VALUE = "initial_value";
	private SQLiteDatabase database;

    public DataTypeDAO(Object context) {
    	this.database = (SQLiteDatabase) context;
    }

    public void insert(DataType type) throws SQLException {
        //String sql = "INSERT INTO data_types (id, description, initial_value) "
        //        + " VALUES (?,?,?);";
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, type.getId());
        values.put(COLUMN_DESCRIPTION, type.getDescription());
        values.put(COLUMN_INITIAL_VALUE, String.valueOf(Content.parseContent(type,type.getInitialValue())));
        
        this.database.insertOrThrow(TABLE_NAME, null, values);
        
        if (DeveloperSettings.SHOW_DAO_SQL) {
        	Log.d("SQL_DEBUG","INSERT INTO data_types (id, description,initial_value) "
                    + " VALUES (" + type.getId() + ",'" + type.getDescription() + "'," + Content.parseContent(type,type.getInitialValue()) + ");");
        }
    }

}
