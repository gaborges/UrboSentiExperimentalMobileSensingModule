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
import urbosenti.core.device.model.ServiceType;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class ServiceTypeDAO {
    
	public static final String TABLE_NAME = "service_types";  
    public static final String COLUMN_ID = "id"; 
    public static final String COLUMN_DESCRIPTION = "description";
	private SQLiteDatabase database;

    public ServiceTypeDAO(Object context) {
    	this.database = (SQLiteDatabase) context;
    }

    public void insert(ServiceType type) throws SQLException {
        //String sql = "INSERT INTO service_types (id, description) "
        //        + "VALUES (?,?);";
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, type.getId());
        values.put(COLUMN_DESCRIPTION, type.getDescription());
        
        this.database.insertOrThrow(TABLE_NAME, null, values);
        
        if (DeveloperSettings.SHOW_DAO_SQL){
        	Log.d("SQL_DEBUG","INSERT INTO service_types (id, description) "
                + " VALUES (" + type.getId() + ",'" + type.getDescription() + "');");
        }
    }
}
