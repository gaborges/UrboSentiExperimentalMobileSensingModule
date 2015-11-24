/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.data.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import urbosenti.core.device.model.Component;
import urbosenti.core.device.model.Device;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class ComponentDAO {

	public static final String TABLE_NAME = "components";  
    public static final String COLUMN_ID = "id"; 
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CODE_CLASS = "code_class";
    public static final String COLUMN_DEVICE_ID = "device_id";
	private SQLiteDatabase database;

    public ComponentDAO(Object context) {
    	this.database = (SQLiteDatabase) context;
    }

    public void insert(Component component) throws SQLException {
        //String sql = "INSERT INTO components (description,code_class,device_id) "
        //        + " VALUES (?,?,?);";
        ContentValues values = new ContentValues();
        values.put(COLUMN_DESCRIPTION, component.getDescription());
        values.put(COLUMN_CODE_CLASS, component.getReferedClass());
        values.put(COLUMN_DEVICE_ID, component.getDevice().getId());
        
        component.setId((int)(long)this.database.insertOrThrow(TABLE_NAME, null, values));

        if (DeveloperSettings.SHOW_DAO_SQL){
        	Log.d("SQL_DEBUG","INSERT INTO components (id,description,code_class,device_id) "
                + " VALUES (" + component.getId() + ",'" + component.getDescription() + "','" + component.getReferedClass() + "'," + component.getDevice().getId() + ");");
        }
    }

    public List<Component> getDeviceComponents(Device device) throws SQLException {
        List<Component> components = new ArrayList();
        String sql = " SELECT id, description, code_class "
                + " FROM components "
                + " WHERE device_id = ?;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(device.getId())});
        
        while (cursor.moveToNext()) {
            components.add(
                    new Component(
                            cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("description")),
                            cursor.getString(cursor.getColumnIndex("code_class"))));
        }
        return components;

    }

    public Component getComponent(int id) throws SQLException {
        Component component = null;
        String sql = " SELECT id, description, code_class "
                + " FROM components "
                + " WHERE id = ?;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(id)});
        
        if (cursor.moveToNext()) {
            component
                    = new Component(
                            cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("possible_value")),
                            cursor.getString(cursor.getColumnIndex("default_value")));
        }
        return component;

    }
}
