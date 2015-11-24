/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.data.dao;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import urbosenti.core.communication.Address;
import urbosenti.core.communication.Message;
import urbosenti.core.communication.MessageWrapper;
import urbosenti.core.device.model.Service;
import urbosenti.user.User;

/**
 *
 * @author Guilherme
 */
public class MessageReportDAO {

	private SQLiteDatabase database;

    public MessageReportDAO(Object context) {
        this.database = (SQLiteDatabase) context;
    }

    public synchronized void insert(MessageWrapper report, Service service) throws SQLException {
        //String sql = "INSERT INTO reports (subject, content_type, priority, content, anonymous_upload, "
        //        + " created_time, uses_urbosenti_xml_envelope, content_size, target_uid, "
        //        + " target_layer, target_address, origin_uid, origin_layer, origin_address, "
        //        + " checked, sent, service_id, timeout) "
        //        + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        ContentValues values = new ContentValues();
        values.put("subject", report.getMessage().getSubject()); // subject integer not null,
        values.put("content_type", report.getMessage().getContentType());// content_type varchar(100) not null,
        values.put("priority", report.getMessage().getPriority());// priority integer not null,
        values.put("content", report.getMessage().getContent());// content text not null ,
        values.put("anonymous_upload", report.getMessage().isAnonymousUpload());// anonymous_upload boolean not null,
        values.put("created_time", report.getMessage().getCreatedTime().getTime());// created_time varchar(100) not null,
        values.put("uses_urbosenti_xml_envelope", report.getMessage().isUsesUrboSentiXMLEnvelope());// uses_urbosenti_xml_envelope boolean not null,
        values.put("content_size", report.getSize());// content_size integer,
        values.put("target_uid", report.getMessage().getTarget().getUid());// target_uid varchar(100) not null,
        values.put("target_layer", report.getMessage().getTarget().getLayer());// target_layer integer not null,
        values.put("target_address", report.getMessage().getTarget().getAddress());// target_address varchar(100),
        values.put("origin_uid", report.getMessage().getOrigin().getUid());// origin_uid varchar(100) not null,
        values.put("origin_layer", report.getMessage().getOrigin().getLayer());// origin_layer integer not null,
        values.put("origin_address", report.getMessage().getOrigin().getAddress());// origin_address varchar(100),
        values.put("checked", report.isChecked());// checked boolean not null,
        values.put("sent", report.isSent());// sent boolean not null,
        values.put("service_id", service.getId());// service_id integer not null,
        values.put("timeout", report.getTimeout());// timeout integer

        report.setId((int)(long)this.database.insertOrThrow("reports", null, values));
    }

    //updateChecked
    public synchronized void updateChecked(MessageWrapper report) throws SQLException {
        //String sql = "UPDATE reports SET checked = ? WHERE id = ? ;";
        report.setChecked();
        ContentValues values = new ContentValues();
        values.put("checked", report.isChecked());
        this.database.update("reports", values, "id = ?", new String[]{String.valueOf(report.getId())});
    }

    //updateSent
    public void updateSent(MessageWrapper report) throws SQLException {
        //String sql = "UPDATE reports SET sent = ? WHERE id = ? ;";
        report.setSent(true);
        ContentValues values = new ContentValues();
        values.put("sent", report.isSent());
        this.database.update("reports", values, "id = ?", new String[]{String.valueOf(report.getId())});
    }

    //delete by id
    public synchronized void delete(MessageWrapper report) throws SQLException {
        //String sql = "DELETE FROM reports WHERE id = ? ;";
        this.database.delete("reports", "id = ?", new String[]{String.valueOf(report.getId())});
    }

    //delete by id
    public synchronized void delete(int reportId) throws SQLException {
        //String sql = "DELETE FROM reports WHERE id = ? ;";
        this.database.delete("reports", "id = ?", new String[]{String.valueOf(reportId)});
    }

    //delete all by params
    private synchronized void deleteAll(boolean unCheckedOnly, boolean isSentOnly, Service service) throws SQLException {
        String sql = "DELETE FROM reports ";
        if (unCheckedOnly && isSentOnly && service != null) {
            sql += " WHERE checked = ? AND sent = ? AND service_id = ? ;";

            this.database.delete("reports", " checked = ? AND sent = ? AND service_id = ? ", new String[]{
            		String.valueOf(0), // false
            		String.valueOf(1), // true
            		String.valueOf(service.getId())
            	});
            //this.stmt.setBoolean(1, false);
            //this.stmt.setBoolean(2, true);
            //this.stmt.setInt(3, service.getId());
        } else if (unCheckedOnly && service != null) {
            sql += " WHERE checked = ? AND service_id = ? ;";
            this.database.delete("reports", " checked = ? AND service_id = ? ", new String[]{
            		String.valueOf(0), // false
            		String.valueOf(service.getId())
            	});
            //this.stmt.setBoolean(1, false);
            //this.stmt.setInt(2, service.getId());
        } else if (isSentOnly && service != null) {
            sql += " WHERE sent = ? AND service_id = ? ;";
            this.database.delete("reports", " sent = ? AND service_id = ? ", new String[]{
            		String.valueOf(1), // true
            		String.valueOf(service.getId())
            	});
            //this.stmt.setBoolean(1, true);
            //this.stmt.setInt(2, service.getId());
        } else if (unCheckedOnly && isSentOnly) {
            sql += " WHERE checked = ? AND sent = ? ;";
            this.database.delete("reports", " checked = ? AND sent = ? ", new String[]{
            		String.valueOf(0), // false
            		String.valueOf(1)  // true
            	});
            //this.stmt.setBoolean(1, false);
            //this.stmt.setBoolean(2, true);
        } else if (unCheckedOnly) {
            sql += " WHERE checked = ? ;";
            this.database.delete("reports", " checked = ? ", new String[]{
            		String.valueOf(0), // false
            	});
            //this.stmt.setBoolean(1, false);
        } else if (isSentOnly) {
            sql += " WHERE sent = ? ;";
            this.database.delete("reports", " sent = ? ", new String[]{
            		String.valueOf(1), // true
            	});
            //this.stmt.setBoolean(1, true);
        } else if (service != null) {
            sql += " WHERE service_id = ? ;";
            this.database.delete("reports", " service_id = ? ", new String[]{
            		String.valueOf(service.getId()), 
            	});
            //this.stmt.setInt(1, service.getId());
        } else {
        	// deleta tudo
        	this.database.execSQL(sql);
        }

    }

    //delete all sent
    public void deleteAllSent() throws SQLException {
        this.deleteAll(false, true, null);
    }

    //delete all sent to service
    public void deleteAllSent(Service service) throws SQLException {
        this.deleteAll(false, true, service);
    }

    //delete all unChecked
    public void deleteAllUnChecked() throws SQLException {
        this.deleteAll(true, false, null);
    }

    //delete all unChecked to service
    public void deleteAllUnChecked(Service service) throws SQLException {
        this.deleteAll(true, false, service);
    }

    //delete all
    public void deleteAll(Service service) throws SQLException {
        this.deleteAll(true, true, service);
    }

    //delete all
    public void deleteAll() throws SQLException {
        this.deleteAll(true, true, null);
    }

    //get by id
    public synchronized MessageWrapper get(int id) throws SQLException {
        MessageWrapper report = null;
        Message message = new Message();
        String sql = " SELECT subject, content_type, priority, content, anonymous_upload, "
                + " created_time, uses_urbosenti_xml_envelope, content_size, target_uid, "
                + " target_layer, target_address, origin_uid, origin_layer, origin_address, "
                + " checked, sent, timeout FROM reports WHERE id = ? ; ";

        Cursor cursor = this.database.rawQuery(sql, new String[]{String.valueOf(id)});
        
        if (cursor.moveToNext()) {
            Address origin = new Address(), target = new Address();
            message.setSubject(cursor.getInt(cursor.getColumnIndex("subject")));
            message.setContent(cursor.getString(cursor.getColumnIndex("content")));
            message.setContentType(cursor.getString(cursor.getColumnIndex("content_type")));
            message.setCreatedTime(new Date(cursor.getLong(cursor.getColumnIndex("created_time"))));
            message.setUsesUrboSentiXMLEnvelope(cursor.getInt(cursor.getColumnIndex("uses_urbosenti_xml_envelope")) > 0);
            message.setAnonymousUpload(cursor.getInt(cursor.getColumnIndex("anonymous_upload")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("priority")) == Message.PREFERENTIAL_PRIORITY) {
                message.setPreferentialPriority();
            } else {
                message.setNormalPriority();
            }
            origin.setLayer(cursor.getInt(cursor.getColumnIndex("origin_layer")));
            origin.setAddress(cursor.getString(cursor.getColumnIndex("origin_address")));
            origin.setUid(cursor.getString(cursor.getColumnIndex("origin_uid")));
            target.setLayer(cursor.getInt(cursor.getColumnIndex("target_layer")));
            target.setAddress(cursor.getString(cursor.getColumnIndex("target_address")));
            target.setUid(cursor.getString(cursor.getColumnIndex("target_uid")));
            report = new MessageWrapper(message);
            report.setSent(cursor.getInt(cursor.getColumnIndex("sent")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("checked")) > 0) {
                report.setChecked();
            } else {
                report.setUnChecked();
            }
            report.setId(id);
            report.setTimeout(cursor.getInt(cursor.getColumnIndex("timeout")));
            message.setOrigin(origin);
            message.setTarget(target);
            report.setMessage(message);
        }
        return report;
    }
    
    //get by id
    public synchronized MessageWrapper get(Date createdTime) throws SQLException {
        MessageWrapper report = null;
        Message message = new Message();
        String sql = " SELECT id, subject, content_type, priority, content, anonymous_upload, "
                + " created_time, uses_urbosenti_xml_envelope, content_size, target_uid, "
                + " target_layer, target_address, origin_uid, origin_layer, origin_address, "
                + " checked, sent, timeout FROM reports WHERE created_time = ? ; ";

        Cursor cursor = this.database.rawQuery(sql, new String[]{String.valueOf(createdTime.getTime())});

        if (cursor.moveToNext()) {
        	Address origin = new Address(), target = new Address();
            message.setSubject(cursor.getInt(cursor.getColumnIndex("subject")));
            message.setContent(cursor.getString(cursor.getColumnIndex("content")));
            message.setContentType(cursor.getString(cursor.getColumnIndex("content_type")));
            message.setCreatedTime(new Date(cursor.getLong(cursor.getColumnIndex("created_time"))));
            message.setUsesUrboSentiXMLEnvelope(cursor.getInt(cursor.getColumnIndex("uses_urbosenti_xml_envelope")) > 0);
            message.setAnonymousUpload(cursor.getInt(cursor.getColumnIndex("anonymous_upload")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("priority")) == Message.PREFERENTIAL_PRIORITY) {
                message.setPreferentialPriority();
            } else {
                message.setNormalPriority();
            }
            origin.setLayer(cursor.getInt(cursor.getColumnIndex("origin_layer")));
            origin.setAddress(cursor.getString(cursor.getColumnIndex("origin_address")));
            origin.setUid(cursor.getString(cursor.getColumnIndex("origin_uid")));
            target.setLayer(cursor.getInt(cursor.getColumnIndex("target_layer")));
            target.setAddress(cursor.getString(cursor.getColumnIndex("target_address")));
            target.setUid(cursor.getString(cursor.getColumnIndex("target_uid")));
            report = new MessageWrapper(message);
            report.setSent(cursor.getInt(cursor.getColumnIndex("sent")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("checked")) > 0) {
                report.setChecked();
            } else {
                report.setUnChecked();
            }
            report.setId(cursor.getInt(cursor.getColumnIndex("id")));
            report.setTimeout(cursor.getInt(cursor.getColumnIndex("timeout")));
            message.setOrigin(origin);
            message.setTarget(target);
            report.setMessage(message);
        }
        return report;
    }

    //get the oldest [not sent, not checked]
    public synchronized MessageWrapper getOldest() throws SQLException {
        MessageWrapper report = null;
        Message message = new Message();
        String sql = " SELECT id, subject, content_type, priority, content, anonymous_upload, "
                + " created_time, uses_urbosenti_xml_envelope, content_size, target_uid, "
                + " target_layer, target_address, origin_uid, origin_layer, origin_address, "
                + " checked, sent, timeout FROM reports ORDER BY id LIMIT 1 ; ";

        Cursor cursor = this.database.rawQuery(sql, null);
        
        if (cursor.moveToNext()) {
        	Address origin = new Address(), target = new Address();
            message.setSubject(cursor.getInt(cursor.getColumnIndex("subject")));
            message.setContent(cursor.getString(cursor.getColumnIndex("content")));
            message.setContentType(cursor.getString(cursor.getColumnIndex("content_type")));
            message.setCreatedTime(new Date(cursor.getLong(cursor.getColumnIndex("created_time"))));
            message.setUsesUrboSentiXMLEnvelope(cursor.getInt(cursor.getColumnIndex("uses_urbosenti_xml_envelope")) > 0);
            message.setAnonymousUpload(cursor.getInt(cursor.getColumnIndex("anonymous_upload")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("priority")) == Message.PREFERENTIAL_PRIORITY) {
                message.setPreferentialPriority();
            } else {
                message.setNormalPriority();
            }
            origin.setLayer(cursor.getInt(cursor.getColumnIndex("origin_layer")));
            origin.setAddress(cursor.getString(cursor.getColumnIndex("origin_address")));
            origin.setUid(cursor.getString(cursor.getColumnIndex("origin_uid")));
            target.setLayer(cursor.getInt(cursor.getColumnIndex("target_layer")));
            target.setAddress(cursor.getString(cursor.getColumnIndex("target_address")));
            target.setUid(cursor.getString(cursor.getColumnIndex("target_uid")));
            report = new MessageWrapper(message);
            report.setSent(cursor.getInt(cursor.getColumnIndex("sent")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("checked")) > 0) {
                report.setChecked();
            } else {
                report.setUnChecked();
            }
            report.setId(cursor.getInt(cursor.getColumnIndex("id")));
            report.setTimeout(cursor.getInt(cursor.getColumnIndex("timeout")));
            message.setOrigin(origin);
            message.setTarget(target);
            report.setMessage(message);
        }
        return report;
    }

    //get the oldest by priority [not sent, not checked]
    public MessageWrapper getOldest(Service service) throws SQLException {
        return getOldest(false, false, -1, service);
    }

    //get the oldest by priority [not sent, not checked]
    public MessageWrapper getOldest(int priority, Service service) throws SQLException {
        return getOldest(false, false, priority, service);
    }

    //get the oldest by priority [not sent, not checked]
    public MessageWrapper getOldestCheckedNotSent(Service service) throws SQLException {
        return getOldest(false, true, -1, service);
    }

    //get the oldest by priority [not sent, not checked]
    public MessageWrapper getOldestCheckedNotSent(int priority, Service service) throws SQLException {
        return getOldest(false, true, priority, service);
    }

    //get the oldest [not sent, not checked]
    public synchronized MessageWrapper getOldest(boolean sent, boolean checked, int priority, Service service) throws SQLException {
        MessageWrapper report = null;
        Message message = new Message();
        String sql;
        String args[] ;
        if ((priority == Message.NORMAL_PRIORITY || priority == Message.PREFERENTIAL_PRIORITY) && service != null) {
            sql = " SELECT id, subject, content_type, priority, content, anonymous_upload, "
                    + " created_time, uses_urbosenti_xml_envelope, content_size, target_uid, "
                    + " target_layer, target_address, origin_uid, origin_layer, origin_address, "
                    + " checked, sent, timeout FROM reports "
                    + " WHERE sent = ? AND checked = ? AND priority = ? AND service_id = ? ORDER BY id LIMIT 1 ; ";
            args = new String[4]; 
            args[2] = String.valueOf(priority);
            args[3] = String.valueOf(service.getId());
            //this.stmt.setInt(3, priority);
            //this.stmt.setInt(4, service.getId());
        } else if (priority == Message.NORMAL_PRIORITY || priority == Message.PREFERENTIAL_PRIORITY) {
            sql = " SELECT id, subject, content_type, priority, content, anonymous_upload, "
                    + " created_time, uses_urbosenti_xml_envelope, content_size, target_uid, "
                    + " target_layer, target_address, origin_uid, origin_layer, origin_address, "
                    + " checked, sent, timeout FROM reports "
                    + " WHERE sent = ? AND checked = ? AND priority = ? ORDER BY id LIMIT 1 ; ";
            args = new String[3]; 
            args[2] = String.valueOf(priority);
            //this.stmt.setInt(3, priority);
        } else if (service != null) {
            sql = " SELECT id, subject, content_type, priority, content, anonymous_upload, "
                    + " created_time, uses_urbosenti_xml_envelope, content_size, target_uid, "
                    + " target_layer, target_address, origin_uid, origin_layer, origin_address, "
                    + " checked, sent, timeout FROM reports "
                    + " WHERE sent = ? AND checked = ? AND service_id = ? ORDER BY id LIMIT 1 ; ";
            args = new String[3];
            args[2] = String.valueOf(service.getId());
            //this.stmt.setInt(3, service.getId());
        } else {
            sql = " SELECT id, subject, content_type, priority, content, anonymous_upload, "
                    + " created_time, uses_urbosenti_xml_envelope, content_size, target_uid, "
                    + " target_layer, target_address, origin_uid, origin_layer, origin_address, "
                    + " checked, sent, timeout FROM reports "
                    + " WHERE sent = ? AND checked = ? ORDER BY id LIMIT 1 ; ";
            args = new String[2];
        }
        args[0] = (sent) ? "1" : "0";
        args[1] = (checked) ? "1" : "0";
        
        Cursor cursor = this.database.rawQuery(sql, args);
        
        if (cursor.moveToNext()) {
        	Address origin = new Address(), target = new Address();
            message.setSubject(cursor.getInt(cursor.getColumnIndex("subject")));
            message.setContent(cursor.getString(cursor.getColumnIndex("content")));
            message.setContentType(cursor.getString(cursor.getColumnIndex("content_type")));
            message.setCreatedTime(new Date(cursor.getLong(cursor.getColumnIndex("created_time"))));
            message.setUsesUrboSentiXMLEnvelope(cursor.getInt(cursor.getColumnIndex("uses_urbosenti_xml_envelope")) > 0);
            message.setAnonymousUpload(cursor.getInt(cursor.getColumnIndex("anonymous_upload")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("priority")) == Message.PREFERENTIAL_PRIORITY) {
                message.setPreferentialPriority();
            } else {
                message.setNormalPriority();
            }
            origin.setLayer(cursor.getInt(cursor.getColumnIndex("origin_layer")));
            origin.setAddress(cursor.getString(cursor.getColumnIndex("origin_address")));
            origin.setUid(cursor.getString(cursor.getColumnIndex("origin_uid")));
            target.setLayer(cursor.getInt(cursor.getColumnIndex("target_layer")));
            target.setAddress(cursor.getString(cursor.getColumnIndex("target_address")));
            target.setUid(cursor.getString(cursor.getColumnIndex("target_uid")));
            report = new MessageWrapper(message);
            report.setSent(cursor.getInt(cursor.getColumnIndex("sent")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("checked")) > 0) {
                report.setChecked();
            } else {
                report.setUnChecked();
            }
            report.setId(cursor.getInt(cursor.getColumnIndex("id")));
            report.setTimeout(cursor.getInt(cursor.getColumnIndex("timeout")));
            message.setOrigin(origin);
            message.setTarget(target);
            report.setMessage(message);
        }
        return report;
    }

    //get all by service
    public synchronized List<MessageWrapper> getList(Service service) throws SQLException {
        List<MessageWrapper> messages = new ArrayList();
        MessageWrapper report = null;
        Message message = new Message();
        String sql = " SELECT id, subject, content_type, priority, content, anonymous_upload, "
                + " created_time, uses_urbosenti_xml_envelope, content_size, target_uid, "
                + " target_layer, target_address, origin_uid, origin_layer, origin_address, "
                + " checked, sent, timeout FROM reports "
                + " WHERE service_id = ? ORDER BY id ; ";

        Cursor cursor = this.database.rawQuery(sql, new String[]{String.valueOf(service.getId())});
        
        while (cursor.moveToNext()) {
        	Address origin = new Address(), target = new Address();
            message.setSubject(cursor.getInt(cursor.getColumnIndex("subject")));
            message.setContent(cursor.getString(cursor.getColumnIndex("content")));
            message.setContentType(cursor.getString(cursor.getColumnIndex("content_type")));
            message.setCreatedTime(new Date(cursor.getLong(cursor.getColumnIndex("created_time"))));
            message.setUsesUrboSentiXMLEnvelope(cursor.getInt(cursor.getColumnIndex("uses_urbosenti_xml_envelope")) > 0);
            message.setAnonymousUpload(cursor.getInt(cursor.getColumnIndex("anonymous_upload")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("priority")) == Message.PREFERENTIAL_PRIORITY) {
                message.setPreferentialPriority();
            } else {
                message.setNormalPriority();
            }
            origin.setLayer(cursor.getInt(cursor.getColumnIndex("origin_layer")));
            origin.setAddress(cursor.getString(cursor.getColumnIndex("origin_address")));
            origin.setUid(cursor.getString(cursor.getColumnIndex("origin_uid")));
            target.setLayer(cursor.getInt(cursor.getColumnIndex("target_layer")));
            target.setAddress(cursor.getString(cursor.getColumnIndex("target_address")));
            target.setUid(cursor.getString(cursor.getColumnIndex("target_uid")));
            report = new MessageWrapper(message);
            report.setSent(cursor.getInt(cursor.getColumnIndex("sent")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("checked")) > 0) {
                report.setChecked();
            } else {
                report.setUnChecked();
            }
            report.setId(cursor.getInt(cursor.getColumnIndex("id")));
            report.setTimeout(cursor.getInt(cursor.getColumnIndex("timeout")));
            message.setOrigin(origin);
            message.setTarget(target);
            report.setMessage(message);
            messages.add(report);
        }
        return messages;
    }

    //get all by service
    public synchronized List<MessageWrapper> getList(boolean sent, boolean checked, int priority, Service service) throws SQLException {
        List<MessageWrapper> messages = new ArrayList();
        MessageWrapper report = null;
        Message message = new Message();
        String sql;
        String args[];
        if ((priority == Message.NORMAL_PRIORITY || priority == Message.PREFERENTIAL_PRIORITY) && service != null) {
            sql = " SELECT id, subject, content_type, priority, content, anonymous_upload, "
                    + " created_time, uses_urbosenti_xml_envelope, content_size, target_uid, "
                    + " target_layer, target_address, origin_uid, origin_layer, origin_address, "
                    + " checked, sent, timeout FROM reports "
                    + " WHERE sent = ? AND checked = ? AND priority = ? AND service_id = ? ORDER BY id ; ";
            args = new String[4]; 
            args[2] = String.valueOf(priority);
            args[3] = String.valueOf(service.getId());
            //this.stmt.setInt(3, priority);
            //this.stmt.setInt(4, service.getId());
        } else if (priority == Message.NORMAL_PRIORITY || priority == Message.PREFERENTIAL_PRIORITY) {
            sql = " SELECT id, subject, content_type, priority, content, anonymous_upload, "
                    + " created_time, uses_urbosenti_xml_envelope, content_size, target_uid, "
                    + " target_layer, target_address, origin_uid, origin_layer, origin_address, "
                    + " checked, sent, timeout FROM reports "
                    + " WHERE sent = ? AND checked = ? AND priority = ? ORDER BY id ; ";
            args = new String[3];
            args[2] = String.valueOf(priority);
            //this.stmt.setInt(3, priority);
        } else if (service != null) {
            sql = " SELECT id, subject, content_type, priority, content, anonymous_upload, "
                    + " created_time, uses_urbosenti_xml_envelope, content_size, target_uid, "
                    + " target_layer, target_address, origin_uid, origin_layer, origin_address, "
                    + " checked, sent, timeout FROM reports "
                    + " WHERE sent = ? AND checked = ? AND service_id = ? ORDER BY id ; ";
            args = new String[3];
            args[2] = String.valueOf(service.getId());
            //this.stmt.setInt(3, service.getId());
        } else {
            sql = " SELECT id, subject, content_type, priority, content, anonymous_upload, "
                    + " created_time, uses_urbosenti_xml_envelope, content_size, target_uid, "
                    + " target_layer, target_address, origin_uid, origin_layer, origin_address, "
                    + " checked, sent, timeout FROM reports "
                    + " WHERE sent = ? AND checked = ? ORDER BY id ; ";
            args = new String[2];
        }
        args[0] = String.valueOf(sent);
        args[1] = String.valueOf(checked);
        //this.stmt.setBoolean(1, sent);
        //this.stmt.setBoolean(2, checked);
        
        Cursor cursor = this.database.rawQuery(sql, args);
        
        while (cursor.moveToNext()) {
        	Address origin = new Address(), target = new Address();
            message.setSubject(cursor.getInt(cursor.getColumnIndex("subject")));
            message.setContent(cursor.getString(cursor.getColumnIndex("content")));
            message.setContentType(cursor.getString(cursor.getColumnIndex("content_type")));
            message.setCreatedTime(new Date(cursor.getLong(cursor.getColumnIndex("created_time"))));
            message.setUsesUrboSentiXMLEnvelope(cursor.getInt(cursor.getColumnIndex("uses_urbosenti_xml_envelope")) > 0);
            message.setAnonymousUpload(cursor.getInt(cursor.getColumnIndex("anonymous_upload")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("priority")) == Message.PREFERENTIAL_PRIORITY) {
                message.setPreferentialPriority();
            } else {
                message.setNormalPriority();
            }
            origin.setLayer(cursor.getInt(cursor.getColumnIndex("origin_layer")));
            origin.setAddress(cursor.getString(cursor.getColumnIndex("origin_address")));
            origin.setUid(cursor.getString(cursor.getColumnIndex("origin_uid")));
            target.setLayer(cursor.getInt(cursor.getColumnIndex("target_layer")));
            target.setAddress(cursor.getString(cursor.getColumnIndex("target_address")));
            target.setUid(cursor.getString(cursor.getColumnIndex("target_uid")));
            report = new MessageWrapper(message);
            report.setSent(cursor.getInt(cursor.getColumnIndex("sent")) > 0);
            if (cursor.getInt(cursor.getColumnIndex("checked")) > 0) {
                report.setChecked();
            } else {
                report.setUnChecked();
            }
            report.setId(cursor.getInt(cursor.getColumnIndex("id")));
            report.setTimeout(cursor.getInt(cursor.getColumnIndex("timeout")));
            message.setOrigin(origin);
            message.setTarget(target);
            report.setMessage(message);
            messages.add(report);
        }
        return messages;
    }

    //counting reports from service
    public synchronized int reportsCount(Service service) throws SQLException {
        int returnedValue = 0;
        String sql = "SELECT count(id) as count FROM reports WHERE service_id = ? ;";
        
        Cursor cursor = this.database.rawQuery(sql, new String[]{String.valueOf(service.getId())});
        if (cursor.moveToNext()) {
            returnedValue = cursor.getInt(0);
        }
        return returnedValue;
    }

    //counting reports
    public int reportsCount() throws SQLException {
        String sql = "SELECT count(id) as count FROM reports ;";
        Cursor cursor = this.database.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            return cursor.getInt(0);
        }        
        return 0;
    }

    public void update(MessageWrapper report) throws SQLException {
        /*
    	String sql = "UPDATE reports SET subject = ? , content_type = ?, priority = ?, content = ?, anonymous_upload = ?, "
                + " created_time = ?, uses_urbosenti_xml_envelope = ?, content_size = ?, target_uid = ?, "
                + " target_layer = ?, target_address = ?, origin_uid = ?, origin_layer = ?, origin_address = ?, "
                + " checked = ?, sent = ?, timeout = ? WHERE id = ? ;";
        */
        ContentValues values = new ContentValues();
        values.put("subject", report.getMessage().getSubject()); // subject integer not null,
        values.put("content_type", report.getMessage().getContentType());// content_type varchar(100) not null,
        values.put("priority", report.getMessage().getPriority());// priority integer not null,
        values.put("content", report.getMessage().getContent());// content text not null ,
        values.put("anonymous_upload", report.getMessage().isAnonymousUpload());// anonymous_upload boolean not null,
        values.put("created_time", report.getMessage().getCreatedTime().getTime());// created_time varchar(100) not null,
        values.put("uses_urbosenti_xml_envelope", report.getMessage().isUsesUrboSentiXMLEnvelope());// uses_urbosenti_xml_envelope boolean not null,
        values.put("content_size", report.getSize());// content_size integer,
        values.put("target_uid", report.getMessage().getTarget().getUid());// target_uid varchar(100) not null,
        values.put("target_layer", report.getMessage().getTarget().getLayer());// target_layer integer not null,
        values.put("target_address", report.getMessage().getTarget().getAddress());// target_address varchar(100),
        values.put("origin_uid", report.getMessage().getOrigin().getUid());// origin_uid varchar(100) not null,
        values.put("origin_layer", report.getMessage().getOrigin().getLayer());// origin_layer integer not null,
        values.put("origin_address", report.getMessage().getOrigin().getAddress());// origin_address varchar(100),
        values.put("checked", report.isChecked());// checked boolean not null,
        values.put("sent", report.isSent());// sent boolean not null,
        values.put("timeout", report.getTimeout());// timeout integer
                
        this.database.update("reports", values, " id = ? ", new String[]{String.valueOf(report.getId())});
    }

    /**
     * Suporte a separação por usuário não está pronta ainda.
     * @param user 
     */
    void deleteAll(User user) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public synchronized void deleteAllExpired(Integer timeLimit) throws SQLException {
        Long limit = System.currentTimeMillis()+timeLimit;
        //String sql = "DELETE FROM reports WHERE created_time >= ? ;";
        this.database.delete("reports", " created_time >= ? ", new String[]{String.valueOf(limit)});
    }

}
