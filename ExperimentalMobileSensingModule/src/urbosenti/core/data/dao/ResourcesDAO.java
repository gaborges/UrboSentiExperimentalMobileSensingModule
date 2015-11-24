/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.data.dao;

import java.sql.SQLException;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import urbosenti.core.data.DataManager;
import urbosenti.core.device.model.Component;
import urbosenti.core.device.model.Entity;
import urbosenti.core.device.model.EntityType;

/**
 *
 * @author Guilherme
 */
public final class ResourcesDAO {
    public final static int  COMPONENT_ID = 8;
    private final DataManager dataManager;
	private SQLiteDatabase database;

    public ResourcesDAO(Object context, DataManager dataManager) {
        this.dataManager = dataManager;
        this.database = (SQLiteDatabase) context;
    }    
    
    public Component getComponentDeviceModel() throws SQLException {
        Component deviceComponent = null;
        String sql = "SELECT components.description as component_desc, code_class, entities.id as entity_id, "
                + " entities.description as entity_desc, entity_type_id, entity_types.description as type_desc\n"
                + " FROM components, entities, entity_types\n"
                + " WHERE components.id = ? and component_id = components.id and entity_type_id = entity_types.id;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(COMPONENT_ID)});

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
            EntityType type = new EntityType(cursor.getInt(cursor.getColumnIndex("entity_type_id")), cursor.getString(cursor.getColumnIndex("type_desc")));
            entity.setEntityType(type);
            entity.setStateModels(this.dataManager.getEntityStateDAO().getEntityStateModels(entity));
            deviceComponent.getEntities().add(entity);
        }
        return deviceComponent;
    }
    
}
