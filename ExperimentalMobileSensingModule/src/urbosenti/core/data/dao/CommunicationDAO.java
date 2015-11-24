/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.data.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import urbosenti.core.communication.CommunicationInterface;
import urbosenti.core.communication.CommunicationManager;
import urbosenti.core.communication.PushServiceReceiver;
import urbosenti.core.communication.UploadService;
import urbosenti.core.data.DataManager;
import urbosenti.core.device.model.Component;
import urbosenti.core.device.model.Content;
import urbosenti.core.device.model.Entity;
import urbosenti.core.device.model.EntityType;
import urbosenti.core.device.model.State;
import urbosenti.user.User;

/**
 *
 * @author Guilherme
 */
public final class CommunicationDAO {
    
    public final static int COMPONENT_ID = 4;
    public static final int ENTITY_ID_OF_REPORTS_STORAGE = 1;
    public static final int ENTITY_ID_OF_RECONNECTION = 2;
    public static final int ENTITY_ID_OF_SERVICE_OF_UPLOAD_REPORTS = 3;
    public static final int ENTITY_ID_OF_SENDING_MESSAGES = 4;
    public static final int ENTITY_ID_OF_MOBILE_DATA_USAGE = 5;
    public static final int ENTITY_ID_OF_OUTPUT_COMMUNICATION_INTERFACES = 6;
    public static final int ENTITY_ID_OF_INPUT_COMMUNICATION_INTERFACES = 7;
    public static final int STATE_ID_OF_REPORTS_STORAGE_ABOUT_AMOUNT_OF_STORED_MESSAGES = 1;
    public static final int STATE_ID_OF_REPORTS_STORAGE_ABOUT_AMOUNT_LIMIT_OF_STORED_MESSAGES = 2;
    public static final int STATE_ID_OF_REPORTS_STORAGE_ABOUT_MESSAGE_EXPIRATION_TIME = 3;
    public static final int STATE_ID_OF_REPORTS_STORAGE_POLICY = 4;
    public static final int STATE_ID_OF_RECONNECTION_INTERVAL = 1;
    public static final int STATE_ID_OF_RECONNECTION_METHOD = 2;
    public static final int STATE_ID_OF_RECONNECTION_POLICY = 3;
    public static final int STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_ABOUT_AMOUNT_OF_MESSAGES_UPLOADED_BY_INTERVAL = 1;
    public static final int STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_UPLOAD_INTERVAL = 2;
    public static final int STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_FOR_UPLOAD_RATE = 3;
    public static final int STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_ABOUT_IS_EXECUTING = 4;
    public static final int STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_ABOUT_IS_DESCONNECTED = 5;
    public static final int STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_POLICY = 6;
    public static final int STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_ALLOWED_TO_PERFORM_UPLOAD = 7;
    public static final int STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_SERVICE_ID = 8;
    public static final int STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_SUBSCRIBED_MAXIMUM_UPLOAD_RATE = 9;
    public static final int STATE_ID_OF_OUTPUT_COMMUNICATION_INTERFACE_POSITION = 1;
    public static final int STATE_ID_OF_OUTPUT_COMMUNICATION_INTERFACE_IS_ENABLED = 2;
    public static final int STATE_ID_OF_OUTPUT_COMMUNICATION_INTERFACE_TIMEOUT = 3;
    public static final int STATE_ID_OF_INPUT_COMMUNICATION_INTERFACE_EXECUTION_STATUS = 1;
    public static final int STATE_ID_OF_INPUT_COMMUNICATION_INTERFACE_CONFIGURATIONS = 2;
    
    public final static int  MOBILE_DATA_POLICY = 1;
    public final static int  MESSAGE_STORAGE_POLICY = 2;
    public final static int  RECONNECTION_POLICY = 3;
    public final static int  UPLOAD_REPORTS_POLICY = 4;
    
    // Variáveis temporárias        
    private int mobileDataPolicy;  // Política de uso de dados móveis
    private int messageStoragePolicy; // Política de armazenamento de mensagem
    private int reconnectionPolicy; // Política de reconexão
    private int uploadMessagingPolicy; // política de Upload periódico de Mensagens
    private List<PushServiceReceiver> inputCommunicationInterfaces;
    private final List<CommunicationInterface> availableCommunicationInterfaces;
    private DataManager dataManager;
	private SQLiteDatabase database;

    public CommunicationDAO() {
        this.mobileDataPolicy = 1; // sem mobilidade - Default
        this.messageStoragePolicy = 2; // Política de armazenamento de mensagem - Padrão: Apagar todas que foram enviadas com sucesso e armazenar as que não foram enviadas. 
        this.reconnectionPolicy = 1;   // Política de reconexão: Padrão - Tentativa em intervalos fixos. Pode ser definido pela aplicação. O padrão é uma nova tentativa a cada 60 segundos
        this.uploadMessagingPolicy = 2; //  política de Upload periódico de Mensagens: Sempre que há um relato novo tenta fazer o upload, caso exista conexão, senão espera reconexão. Padrão.
        this.availableCommunicationInterfaces = new ArrayList();
    }
    
    public CommunicationDAO(Object context, DataManager dataManager) {
        this();
        this.database = (SQLiteDatabase) context;
        this.dataManager = dataManager;
    }
    
   
    
    public int getCurrentPreferentialPolicy (int policyId) throws SQLException{
        switch(policyId){
            case MOBILE_DATA_POLICY:
                //return Integer.parseInt(this.dataManager.getEntityStateDAO().getEntityState(COMPONENT_ID, ENTITY_ID_OF_MOBILE_DATA_USAGE, )); - não implementado ainda
                return mobileDataPolicy;
            case MESSAGE_STORAGE_POLICY:
                return Integer.parseInt(this.dataManager.getEntityStateDAO().getEntityState(COMPONENT_ID, ENTITY_ID_OF_REPORTS_STORAGE,STATE_ID_OF_REPORTS_STORAGE_POLICY).getCurrentValue().toString());
                //return messageStoragePolicy;
            case RECONNECTION_POLICY:
                return Integer.parseInt(this.dataManager.getEntityStateDAO().getEntityState(COMPONENT_ID, ENTITY_ID_OF_RECONNECTION,STATE_ID_OF_RECONNECTION_POLICY).getCurrentValue().toString());
                //return reconnectionPolicy;
            case UPLOAD_REPORTS_POLICY:
                return Integer.parseInt(this.dataManager.getEntityStateDAO().getEntityState(COMPONENT_ID, ENTITY_ID_OF_SERVICE_OF_UPLOAD_REPORTS,STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_POLICY).getCurrentValue().toString());
                //return uploadMessagingPolicy;                //return uploadMessagingPolicy;
        }
        return 0;
    }
    
    // POde ter um evento associado para reconfiguração
    public void updatePreferentialPolicy(int policyId, int newValue) throws SQLException{
        State state;
        Content content;
        switch(policyId){
            case MOBILE_DATA_POLICY:
                mobileDataPolicy = newValue;
                //state = this.dataManager.getEntityStateDAO().getEntityState(policyId, policyId, newValue) // Não implementado
                //this.dataManager.getEntityStateDAO().insertContent(null);
                break;
            case MESSAGE_STORAGE_POLICY:
                messageStoragePolicy = newValue;
                state = this.dataManager.getEntityStateDAO().getEntityState(COMPONENT_ID, ENTITY_ID_OF_REPORTS_STORAGE,STATE_ID_OF_REPORTS_STORAGE_POLICY);
                content = new Content();
                content.setValue(newValue);
                content.setTime(new Date());
                state.setContent(content);
                this.dataManager.getEntityStateDAO().insertContent(state);
                break;
            case RECONNECTION_POLICY:
                reconnectionPolicy = newValue;
                state = this.dataManager.getEntityStateDAO().getEntityState(COMPONENT_ID, ENTITY_ID_OF_RECONNECTION,STATE_ID_OF_RECONNECTION_POLICY);
                content = new Content();
                content.setValue(newValue);
                content.setTime(new Date());
                state.setContent(content);
                this.dataManager.getEntityStateDAO().insertContent(state);
                break;
            case UPLOAD_REPORTS_POLICY:
                uploadMessagingPolicy = newValue;
                state = this.dataManager.getEntityStateDAO().getEntityState(COMPONENT_ID, ENTITY_ID_OF_SERVICE_OF_UPLOAD_REPORTS,STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_POLICY);
                content = new Content();
                content.setValue(newValue);
                content.setTime(new Date());
                state.setContent(content);
                this.dataManager.getEntityStateDAO().insertContent(state);
                break;
        }
    }
    
    public List<CommunicationInterface> getAvailableInterfaces(){
        /*
         * POde consultar no banco se há alguma existente, se existe instancia o objeto.
         */
        return availableCommunicationInterfaces;
    }   

    
    /**
     *  adiciona uma nova interface de comunicação. Se ela existe ela é setada como suportada, se não existe ela é adicionada.
     *  No caso em questão todas já existem. Adicionar a nova possibilidade em futuras implementações.;
     * @param ci
     */
    public void addAvailableCommunicationInterface(CommunicationInterface ci) {
        ci.setStatus(CommunicationInterface.STATUS_AVAILABLE);
        /*
         * Persiste no banco de dados. Se já existe somente atualiza.
         */
        availableCommunicationInterfaces.add(ci);
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

    public void deleteUserReports(User user) {
        dataManager.getReportDAO().deleteAll(user);
    }

    public List<UploadService> getUploadServices(CommunicationManager communicationManager) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
