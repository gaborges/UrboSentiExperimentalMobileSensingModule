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
import urbosenti.core.device.model.Agent;
import urbosenti.core.device.model.AgentType;
import urbosenti.core.device.model.Device;
import urbosenti.core.device.model.Service;
import urbosenti.core.device.model.ServiceType;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class ServiceDAO {

	public static final String TABLE_NAME = "services";  
    public static final String COLUMN_ID = "id"; 
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_SERVICE_UID = "service_uid";
    public static final String COLUMN_APPLICATION_UID = "application_uid";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_SERVICE_TYPE_ID = "service_type_id";
    public static final String COLUMN_DEVICE_ID = "device_id";
	private SQLiteDatabase database;

    public ServiceDAO(Object context) {
    	this.database = (SQLiteDatabase) context;
    }

    public void insert(Service service) throws SQLException {
        //String sql = "INSERT INTO services (description,service_uid,application_uid,address,service_type_id,device_id) "
        //        + "VALUES (?,?,?,?,?,?);";
        ContentValues values = new ContentValues();
        values.put(COLUMN_DESCRIPTION, service.getDescription());
        values.put(COLUMN_SERVICE_UID, service.getServiceUID());
        values.put(COLUMN_APPLICATION_UID, (service.getApplicationUID() == null) ? "" : service.getApplicationUID());
        values.put(COLUMN_ADDRESS, service.getAddress());
        values.put(COLUMN_SERVICE_TYPE_ID, service.getServiceType().getId());
        values.put(COLUMN_DEVICE_ID, service.getDevice().getId());
        // executar
    	service.setId((int)(long)this.database.insertOrThrow(TABLE_NAME, null, values));

    	if (DeveloperSettings.SHOW_DAO_SQL) {
    		Log.d("SQL_DEBUG","INSERT INTO services (id,description,service_uid,application_uid,address,service_type_id,device_id) "
                    + " VALUES (" + service.getId() + ",'" + service.getDescription() + "','"
                    + service.getServiceUID() + "','" + service.getApplicationUID() + "','" + service.getAddress() + "',"
                    + service.getServiceType().getId() + "," + service.getDevice().getId() + ");");
        }
    }

    public List<Service> getDeviceServices(Device device) throws SQLException {
        List<Service> services = new ArrayList();
        Service service = null;
        String sql = "SELECT services.id as service_id, services.description as service_description, service_uid, application_uid, address, "
                + " service_type_id, service_types.description as type_description\n"
                + " FROM services, service_types\n"
                + " WHERE device_id = ? AND service_types.id = service_type_id ;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(device.getId())});
        
        if (cursor.moveToNext()) {
            service = new Service();
            service.setId(cursor.getInt(cursor.getColumnIndex("service_id")));
            service.setDescription(cursor.getString(cursor.getColumnIndex("service_description")));
            service.setAddress(cursor.getString(cursor.getColumnIndex("address")));
            service.setServiceUID(cursor.getString(cursor.getColumnIndex("service_uid")));
            service.setApplicationUID((cursor.getString(cursor.getColumnIndex("application_uid")).length() <= 6) ? "" : cursor.getString(cursor.getColumnIndex("application_uid")));
            service.setServiceType(new ServiceType(cursor.getInt(cursor.getColumnIndex("service_type_id")), cursor.getString(cursor.getColumnIndex("type_description"))));
            service.setAgent(this.getServiceAgent(service));
            services.add(service);
        }
        return services;
    }

    public List<Service> getDeviceServices() throws SQLException {
        List<Service> services = new ArrayList();
        Service service = null;
        String sql = "SELECT services.id as service_id, services.description as service_description, service_uid, application_uid, address, "
                + " service_type_id, service_types.description as type_description\n"
                + " FROM services, service_types\n"
                + " WHERE service_types.id = service_type_id ;";
        
        Cursor cursor = this.database.rawQuery(sql,	null);
        
        while (cursor.moveToNext()) {
            service = new Service();
            service.setId(cursor.getInt(cursor.getColumnIndex("service_id")));
            service.setDescription(cursor.getString(cursor.getColumnIndex("service_description")));
            service.setAddress(cursor.getString(cursor.getColumnIndex("address")));
            service.setServiceUID(cursor.getString(cursor.getColumnIndex("service_uid")));
            service.setApplicationUID((cursor.getString(cursor.getColumnIndex("application_uid")).length() <= 6) ? "" : cursor.getString(cursor.getColumnIndex("application_uid")));
            service.setServiceType(new ServiceType(cursor.getInt(cursor.getColumnIndex("service_type_id")), cursor.getString(cursor.getColumnIndex("type_description"))));
            service.setAgent(this.getServiceAgent(service));
            services.add(service);
        }
        return services;
    }

    public Service getService(int id) throws SQLException {
        Service service = null;
        String sql = "SELECT services.id as service_id, services.description as service_description, service_uid, application_uid, address, "
                + " service_type_id, service_types.description as type_description\n"
                + " FROM services, service_types\n"
                + " WHERE services.id = ? AND service_types.id = service_type_id ;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(id)});
        
        if (cursor.moveToNext()) {
            service = new Service();
            service.setId(cursor.getInt(cursor.getColumnIndex("service_id")));
            service.setDescription(cursor.getString(cursor.getColumnIndex("service_description")));
            service.setAddress(cursor.getString(cursor.getColumnIndex("address")));
            service.setServiceUID(cursor.getString(cursor.getColumnIndex("service_uid")));
            service.setApplicationUID((cursor.getString(cursor.getColumnIndex("application_uid")).length() <= 6) ? "" : cursor.getString(cursor.getColumnIndex("application_uid")));
            service.setServiceType(new ServiceType(cursor.getInt(cursor.getColumnIndex("service_type_id")), cursor.getString(cursor.getColumnIndex("type_description"))));
            service.setAgent(this.getServiceAgent(service));
        }
        return service;
    }

    private Agent getServiceAgent(Service service) throws SQLException {
        Agent agent = null;
        String sql = "SELECT agents.id as agent_id,address, agent_type_id, description, layer \n"
                + " FROM agents, agent_types\n"
                + " WHERE service_id = ? AND agent_types.id = agent_type_id ;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(service.getId())});
        
        if (cursor.moveToNext()) {
            agent = new Agent();
            agent.setId(cursor.getInt(cursor.getColumnIndex("agent_id")));
            agent.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            agent.setRelativeAddress(cursor.getString(cursor.getColumnIndex("address")));
            agent.setAgentType(new AgentType(cursor.getInt(cursor.getColumnIndex("agent_type_id")), cursor.getString(cursor.getColumnIndex("description"))));
            agent.setLayer(cursor.getInt(cursor.getColumnIndex("layer")));
            agent.setService(service);
        }
        return agent;
    }

    public void updateServiceUIDs(Service service) throws SQLException {
        //String sql = "UPDATE services SET service_uid = ?,application_uid = ? "
        //        + " WHERE id = ?;";
        // valores
        ContentValues values = new ContentValues();
        values.put(COLUMN_SERVICE_UID, service.getServiceUID());
        values.put(COLUMN_APPLICATION_UID, (service.getApplicationUID() == null) ? "" : service.getApplicationUID());
        // executar
        this.database.update(TABLE_NAME, values, " id = ? ", new String[]{String.valueOf(service.getId())});
    }

    public Service getServiceByUid(String uid) throws SQLException {
        Service service = null;
        String sql = "SELECT services.id as service_id, services.description as service_description, service_uid, application_uid, address, "
                + " service_type_id, service_types.description as type_description\n"
                + " FROM services, service_types\n"
                + " WHERE services.service_uid = ? AND service_types.id = service_type_id ;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(uid)});
        
        if (cursor.moveToNext()) {
            service = new Service();
            service.setId(cursor.getInt(cursor.getColumnIndex("service_id")));
            service.setDescription(cursor.getString(cursor.getColumnIndex("service_description")));
            service.setAddress(cursor.getString(cursor.getColumnIndex("address")));
            service.setServiceUID(cursor.getString(cursor.getColumnIndex("service_uid")));
            service.setApplicationUID((cursor.getString(cursor.getColumnIndex("application_uid")).length() <= 6) ? "" : cursor.getString(cursor.getColumnIndex("application_uid")));
            service.setServiceType(new ServiceType(cursor.getInt(cursor.getColumnIndex("service_type_id")), cursor.getString(cursor.getColumnIndex("type_description"))));
            service.setAgent(this.getServiceAgent(service));
        }
        return service;
    }
}
