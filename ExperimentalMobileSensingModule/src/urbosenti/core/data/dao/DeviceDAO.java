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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import urbosenti.core.data.DataManager;
import urbosenti.core.device.model.Component;
import urbosenti.core.device.model.Device;
import urbosenti.core.device.model.Entity;
import urbosenti.core.device.model.EntityType;
import urbosenti.core.device.model.Service;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public final class DeviceDAO {

    public static final int COMPONENT_ID = 1;
    public static final int ENTITY_ID_OF_SERVICE_REGISTRATION = 1;
    public static final int ENTITY_ID_OF_URBOSENTI_SERVICES = 2;
    public static final int ENTITY_ID_OF_BASIC_DEVICE_INFORMATIONS = 3;
    public static final int STATE_ID_OF_URBOSENTI_SERVICES_ACTIVITY_STATUS = 1;
    public static final int STATE_ID_OF_URBOSENTI_SERVICES_ADAPTATION_COMPONENT_STATUS = 2;
    public static final int STATE_ID_OF_URBOSENTI_SERVICES_LOCATION_COMPONENT_STATUS = 3;
    public static final int STATE_ID_OF_URBOSENTI_SERVICES_CONTEXT_COMPONENT_STATUS = 4;
    public static final int STATE_ID_OF_URBOSENTI_SERVICES_USER_COMPONENT_STATUS = 5;
    public static final int STATE_ID_OF_URBOSENTI_SERVICES_CONCERNS_COMPONENT_STATUS = 6;
    public static final int STATE_ID_OF_URBOSENTI_SERVICES_RESOURCES_COMPONENT_STATUS = 7;
    public static final int STATE_ID_OF_URBOSENTI_SERVICES_WIRED_COMMUNICATION_INTERFACE_STATUS = 8;
    public static final int STATE_ID_OF_URBOSENTI_SERVICES_WIFI_COMMUNICATION_INTERFACE_STATUS = 9;
    public static final int STATE_ID_OF_URBOSENTI_SERVICES_MOBILE_DATA_COMMUNICATION_INTERFACE_STATUS = 10;
    public static final int STATE_ID_OF_URBOSENTI_SERVICES_DTN_COMMUNICATION_INTERFACE_STATUS = 11;
    public static final int STATE_ID_OF_URBOSENTI_SERVICES_GCM_INPUT_INTERFACE_STATUS = 12;
    public static final int STATE_ID_OF_URBOSENTI_SERVICES_SOCKET_INPUT_INTERFACE_STATUS = 13;
    public static final int STATE_ID_OF_SERVICE_REGISTRATION_FOR_SERVICE_UID = 1;
    public static final int STATE_ID_OF_SERVICE_REGISTRATION_FOR_REMOTE_PASSWORD = 2;
    public static final int STATE_ID_OF_SERVICE_REGISTRATION_FOR_APPLICATION_UID = 3;
    public static final int STATE_ID_OF_SERVICE_REGISTRATION_FOR_SERVICE_EXPIRATION_TIME = 4;
    public static final int STATE_ID_OF_BASIC_DEVICE_INFORMATIONS_ABOUT_STORAGE_SYSTEM = 1;
    public static final int STATE_ID_OF_BASIC_DEVICE_INFORMATIONS_ABOUT_CPU_CORES = 2;
    public static final int STATE_ID_OF_BASIC_DEVICE_INFORMATIONS_ABOUT_CPU_CORE_FREQUENCY_CLOCK = 3;
    public static final int STATE_ID_OF_BASIC_DEVICE_INFORMATIONS_ABOUT_CPU_MODEL = 4;
    public static final int STATE_ID_OF_BASIC_DEVICE_INFORMATIONS_ABOUT_NATIVE_OPERATIONAL_SYSTEM = 5;
    public static final int STATE_ID_OF_BASIC_DEVICE_INFORMATIONS_ABOUT_MEMORY_RAM = 6;
    public static final int STATE_ID_OF_BASIC_DEVICE_INFORMATIONS_ABOUT_DEVICE_MODEL = 7;
    public static final int STATE_ID_OF_BASIC_DEVICE_INFORMATIONS_ABOUT_BATTERY_CAPACITY = 8;

    public static final String TABLE_NAME = "devices"; 
    public static final String COLUMN_DESCRIPTION = "description"; 
    public static final String COLUMN_GENERAL_DEFINITIONS_VERSION = "generalDefinitionsVersion"; 
    public static final String COLUMN_DEVICE_VERSION = "deviceVersion"; 
    public static final String COLUMN_AGENTE_MODEL_VERSION = "agentModelVersion"; 
    public static final String COLUMN_ID = "id"; 
    
    public static final int DEVICE_DB_ID = 1;
    private final DataManager dataManager;
	private SQLiteDatabase database;
	private ContentValues values;

    public DeviceDAO(Object context, DataManager dataManager) {
        this.dataManager = dataManager;
        this.database = (SQLiteDatabase) context;
    }

    public void insert(Device device) throws SQLException {
        /*String sql = "INSERT INTO devices (description, generalDefinitionsVersion, deviceVersion, agentModelVersion) "
                + "VALUES (?,?,?,?);";*/
        this.values = new ContentValues();
        values.put(COLUMN_DESCRIPTION, device.getDescription());
        values.put(COLUMN_DEVICE_VERSION, device.getDeviceVersion());
        values.put(COLUMN_GENERAL_DEFINITIONS_VERSION, device.getGeneralDefinitionsVersion());
        values.put(COLUMN_AGENTE_MODEL_VERSION, device.getAgentModelVersion());
        
        device.setId((int)(long)this.database.insertOrThrow(TABLE_NAME, null, values));
         
        if (DeveloperSettings.SHOW_DAO_SQL) {
            Log.d("SQL_DEBUG","INSERT INTO devices (id,description, generalDefinitionsVersion, deviceVersion, agentModelVersion) "
                    + " VALUES (" + device.getId() + ",'" + device.getDescription() + "'," + device.getDeviceVersion() + "," + device.getGeneralDefinitionsVersion() + "," + device.getAgentModelVersion() + ");");
        }
    }

    public int getCount() throws SQLException {
        int count = 0;
        Cursor cursor = this.database.rawQuery("SELECT count(*) FROM devices;", null);
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        } else {
            throw new SQLException("Query failed to return the row counting.");
        }
        return count;
    }

    public double getStoredDeviceVersion(Device device) throws SQLException {
        double count = 0.0;
        Cursor cursor = this.database.rawQuery("SELECT deviceVersion FROM devices WHERE id = ? ;",
        		new String[]{(device.getId() > 0) ? String.valueOf(device.getId()) : "1"});
        if (cursor.moveToFirst()) {
            count = cursor.getDouble(0);
        } else {
            throw new SQLException("Query failed to return the row value.");
        }
        return count;
    }

    public double getStoredAgentModelVersion(Device device) throws SQLException {
    	double count = 0.0;
    	Cursor cursor = this.database.rawQuery("SELECT agentModelVersion FROM devices WHERE id = ? ;",
        		new String[]{(device.getId() > 0) ? String.valueOf(device.getId()) : "1"});
        if (cursor.moveToFirst()) {
            count = cursor.getDouble(0);
        } else {
            throw new SQLException("Query failed to return the row value.");
        }
        return count;
    }

    public double getStoredGeneralDefinitionsVersion(Device device) throws SQLException {
        double count = 0.0;		
		Cursor cursor = this.database.rawQuery("SELECT generalDefinitionsVersion FROM devices WHERE id = ? ;",
        		new String[]{(device.getId() > 0) ? String.valueOf(device.getId()) : "1"});
        if (cursor.moveToFirst()) {
            count = cursor.getDouble(0);
        } else {
            throw new SQLException("Query failed to return the row value.");
        }
        return count;
        		
    }

    public Device getDevice() throws SQLException {
        Device device = null;
        Cursor cursor = this.database.rawQuery("SELECT agentModelVersion,description,deviceVersion,generalDefinitionsVersion FROM devices WHERE id = ?;",
        		new String[]{String.valueOf(DEVICE_DB_ID)});
		if (cursor.moveToFirst()) {
            device = new Device();
            device.setId(DEVICE_DB_ID);
            device.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            device.setAgentModelVersion(cursor.getDouble(cursor.getColumnIndex("agentModelVersion")));
            device.setDeviceVersion(cursor.getDouble(cursor.getColumnIndex("deviceVersion")));
            device.setGeneralDefinitionsVersion(cursor.getDouble(cursor.getColumnIndex("generalDefinitionsVersion")));
        }
        return device;
    }

    /**
     * Retorna todo o modelo do dispositivo ou null
     * @param dataManager
     * @return
     * @throws SQLException 
     */
    public Device getDeviceModel(DataManager dataManager) throws SQLException {
        Device device = getDevice();
        device.setComponents(dataManager.getComponentDAO().getDeviceComponents(device));
        for (Component component : device.getComponents()) {
            component.setEntities(dataManager.getEntityDAO().getComponentEntities(component));
            for (Entity entity : component.getEntities()) {
                entity.setActionModels(dataManager.getActionModelDAO().getEntityActionModels(entity));
                entity.setEventModels(dataManager.getEventModelDAO().getEntityEventModels(entity));
                entity.setInstaceModels(dataManager.getInstanceDAO().getEntityInstanceModels(entity));
                entity.setStateModels(dataManager.getEntityStateDAO().getEntityStateModels(entity));
            }
        }
        device.setServices(dataManager.getServiceDAO().getDeviceServices(device));
        for (Service service : device.getServices()) {
            service.getAgent().getAgentType().setInteractionModels(dataManager.getAgentTypeDAO().getAgentInteractions(service.getAgent().getAgentType()));
            service.getAgent().getAgentType().setStateModels(dataManager.getAgentTypeDAO().getAgentStates(service.getAgent().getAgentType()));
            service.getAgent().setConversations(dataManager.getAgentTypeDAO().getAgentConversations(service.getAgent()));
        }
        return device;
    }

    public Component getComponentDeviceModel() throws SQLException {
        Component deviceComponent = null;
        String sql = "SELECT components.description as component_desc, code_class, entities.id as entity_id, "
                + " entities.description as entity_desc, entity_type_id, entity_types.description as type_desc\n"
                + " FROM components, entities, entity_types\n"
                + " WHERE components.id = ? and component_id = components.id and entity_type_id = entity_types.id;";
        Cursor cursor = this.database.rawQuery(sql,
        		new String[]{String.valueOf(COMPONENT_ID)});
        while (cursor.moveToNext()) {
            if (deviceComponent == null) {
                deviceComponent = new Component();
                deviceComponent.setId(COMPONENT_ID);
                deviceComponent.setDescription(cursor.getString(cursor.getColumnIndex("component_desc")));
                deviceComponent.setReferedClass(cursor.getString(cursor.getColumnIndex("code_class")));
            }
            Entity entity = new Entity();
            entity.setId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            entity.setDescription(cursor.getString(cursor.getColumnIndex("entity_desc")));
            EntityType type = new EntityType(
            		cursor.getInt(cursor.getColumnIndex("entity_type_id")), 
            		cursor.getString(cursor.getColumnIndex("type_desc")));
            entity.setEntityType(type);
            entity.setStateModels(this.dataManager.getStateDAO().getEntityStateModels(entity));
            deviceComponent.getEntities().add(entity);
        }
        return deviceComponent;
    }

    public void updateStoredAgentModelVersion(Device device) throws SQLException {
        //String sql = "UPDATE devices SET agentModelVersion = ? WHERE id = ?;";
        values = new ContentValues();
        values.put(COLUMN_AGENTE_MODEL_VERSION, device.getAgentModelVersion());
        this.database.update(TABLE_NAME, values, " id = ? ", 
        		new String[]{String.valueOf(device.getId())});
    }

    public void updateStoredGeneralDefinitionsVersion(Device device) throws SQLException {
        //String sql = "UPDATE devices SET generalDefinitionsVersion = ? WHERE id = ?;";
        values = new ContentValues();
        values.put(COLUMN_GENERAL_DEFINITIONS_VERSION, device.getGeneralDefinitionsVersion());
        this.database.update(TABLE_NAME, values, " id = ? ", 
        		new String[]{String.valueOf(device.getId())});
    }

    public void updateStoredDeviceVersion(Device device) throws SQLException {
        //String sql = "UPDATE devices SET deviceVersion = ? WHERE id = ?;";
        values = new ContentValues();
        values.put(COLUMN_DEVICE_VERSION, device.getDeviceVersion());
        this.database.update(TABLE_NAME, values, " id = ? ", 
        		new String[]{String.valueOf(device.getId())});
    }
}
