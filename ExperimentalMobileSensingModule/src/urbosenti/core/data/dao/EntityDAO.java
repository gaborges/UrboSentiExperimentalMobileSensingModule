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
import urbosenti.core.device.model.Entity;
import urbosenti.core.device.model.EntityType;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class EntityDAO {

	public static final String TABLE_NAME = "entities";  
    public static final String COLUMN_ID = "id"; 
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_ENTITY_TYPE_ID = "entity_type_id";
    public static final String COLUMN_COMPONENT_ID = "component_id";
    public static final String COLUMN_MODEL_ID = "model_id";
	private SQLiteDatabase database;

    public EntityDAO(Object context) {
    	this.database = (SQLiteDatabase) context;
    }

    public void insert(Entity entity) throws SQLException {
        //String sql = "INSERT INTO entities (description,entity_type_id, component_id, model_id) "
        //        + " VALUES (?,?,?,?);";
        ContentValues values = new ContentValues();
        values.put(COLUMN_DESCRIPTION, entity.getDescription());
        values.put(COLUMN_ENTITY_TYPE_ID, entity.getEntityType().getId());
        values.put(COLUMN_COMPONENT_ID, entity.getComponent().getId());
        values.put(COLUMN_MODEL_ID, entity.getModelId());
        
        entity.setId((int)(long)this.database.insertOrThrow(TABLE_NAME, null, values));
     
        if (DeveloperSettings.SHOW_DAO_SQL){
        	Log.d("SQL_DEBUG","INSERT INTO entities (id,description,entity_type_id, component_id,model_id) "
                + " VALUES (" + entity.getId() + ",'" + entity.getDescription() + "'," + entity.getEntityType().getId() + "," + entity.getComponent().getId() + "," + entity.getModelId() + ");");
        }
    }

    public List<Entity> getComponentEntities(Component component) throws SQLException {
        List<Entity> entities = new ArrayList();
        Entity entity = null;
        String sql = "SELECT entities.id as entity_id, entity_type_id, model_id, "
                + " entities.description as entity_desc, entity_types.description as type_desc\n"
                + " FROM entities, entity_types\n"
                + " WHERE component_id = ? and entity_type_id = entity_types.id;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{String.valueOf(component.getId())});
        
        while (cursor.moveToNext()) {
            entity = new Entity();
            entity.setId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            entity.setDescription(cursor.getString(cursor.getColumnIndex("entity_desc")));
            entity.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            EntityType type = new EntityType(cursor.getInt(cursor.getColumnIndex("entity_type_id")), cursor.getString(cursor.getColumnIndex("type_desc")));
            entity.setEntityType(type);
            entities.add(entity);
        }
        return entities;
    }

    public Entity getEntity(int componentId, int entityModelId) throws SQLException {
        Entity entity = null;
        String sql = "SELECT entities.id as entity_id, entity_type_id, model_id, "
                + " entities.description as entity_desc, entity_types.description as type_desc\n"
                + " FROM entities, entity_types\n"
                + " WHERE model_id = ? AND component_id = ? AND entity_type_id = entity_types.id;";

        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf(entityModelId),
        		String.valueOf(componentId)
        });
        
        if (cursor.moveToNext()) {
            entity = new Entity();
            entity.setId(cursor.getInt(cursor.getColumnIndex("entity_id")));
            entity.setModelId(cursor.getInt(cursor.getColumnIndex("model_id")));
            entity.setDescription(cursor.getString(cursor.getColumnIndex("entity_desc")));
            EntityType type = new EntityType(cursor.getInt(cursor.getColumnIndex("entity_type_id")), cursor.getString(cursor.getColumnIndex("type_desc")));
            entity.setEntityType(type);
        }
        return entity;
    }
}
