/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.data;

import java.io.File;
import urbosenti.core.data.dao.CommunicationDAO;
import urbosenti.core.data.dao.UserDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import urbosenti.core.communication.CommunicationInterface;
import urbosenti.core.data.dao.ActionModelDAO;
import urbosenti.core.data.dao.AdaptationDAO;
import urbosenti.core.data.dao.AgentAddressTypeDAO;
import urbosenti.core.data.dao.AgentCommunicationLanguageDAO;
import urbosenti.core.data.dao.AgentDAO;
import urbosenti.core.data.dao.AgentTypeDAO;
import urbosenti.core.data.dao.CommunicativeActDAO;
import urbosenti.core.data.dao.ComponentDAO;
import urbosenti.core.data.dao.ConcernsDAO;
import urbosenti.core.data.dao.ContextDAO;
import urbosenti.core.data.dao.DataDAO;
import urbosenti.core.data.dao.DataTypeDAO;
import urbosenti.core.data.dao.DeviceDAO;
import urbosenti.core.data.dao.EntityDAO;
import urbosenti.core.data.dao.EntityTypeDAO;
import urbosenti.core.data.dao.EventModelDAO;
import urbosenti.core.data.dao.ImplementationTypeDAO;
import urbosenti.core.data.dao.InstanceDAO;
import urbosenti.core.data.dao.InteractionDirectionDAO;
import urbosenti.core.data.dao.InteractionTypeDAO;
import urbosenti.core.data.dao.ServiceDAO;
import urbosenti.core.data.dao.EntityStateDAO;
import urbosenti.core.data.dao.EventDAO;
import urbosenti.core.data.dao.LocationDAO;
import urbosenti.core.data.dao.MessageReportDAO;
import urbosenti.core.data.dao.ResourcesDAO;
import urbosenti.core.data.dao.ServiceTypeDAO;
import urbosenti.core.data.dao.TargetOriginDAO;
import urbosenti.core.device.ComponentManager;
import urbosenti.core.device.DeviceManager;
import urbosenti.core.device.model.FeedbackAnswer;
import urbosenti.core.events.Action;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class DataManager extends ComponentManager {

    private final List<CommunicationInterface> supportedCommunicationInterfaces;
    private CommunicationDAO communicationDAO;
    private EventDAO eventDAO;
    private UserDAO userDAO;
    private Object knowledgeRepresentation;
    private String knowledgeDataType;
    private EntityTypeDAO entityTypeDAO;
    private DataTypeDAO dataTypeDAO;
    private ImplementationTypeDAO implementationTypeDAO;
    private AgentCommunicationLanguageDAO agentCommunicationLanguageDAO;
    private CommunicativeActDAO communicativeActDAO;
    private InteractionTypeDAO interactionTypeDAO;
    private InteractionDirectionDAO interactionDirectionDAO;
    private TargetOriginDAO targetOriginDAO;
    private AgentAddressTypeDAO agentAddressTypeDAO;
    private DeviceDAO deviceDAO;
    private ServiceDAO serviceDAO;
    private AgentDAO agentDAO;
    private AgentTypeDAO agentTypeDAO;
    private ComponentDAO componentDAO;
    private EntityDAO entityDAO;
    private EntityStateDAO stateDAO;
    private EventModelDAO eventModelDAO;
    private ActionModelDAO actionModelDAO;
    private InstanceDAO instanceDAO;
    private AdaptationDAO adaptationDAO;
    private LocationDAO locationDAO;
    private ContextDAO contextDAO;
    private ResourcesDAO resourcesDAO;
    private ConcernsDAO concernsDAO;
    private ServiceTypeDAO serviceTypeDAO;
    private DataDAO dataDAO;
    private MessageReportDAO reportDAO;
    private UrboSentiDatabaseHelper databaseHelper;

    public DataManager(DeviceManager deviceManager) {
        super(deviceManager, DataDAO.COMPONENT_ID);
        this.supportedCommunicationInterfaces = new ArrayList<CommunicationInterface>();
        this.knowledgeRepresentation = null;
        this.databaseHelper = null;
    }

    @Override
    public void onCreate() {
        if (DeveloperSettings.SHOW_FUNCTION_DEBUG_ACTIVITY) {
            System.out.println("Activating: " + getClass());
        }
        if(this.databaseHelper==null){
            // Gerente do conhecimento
            this.databaseHelper = new SQLiteJDBCDatabaseHelper(this);
        }
        // Conecta ao banco
        Object connection = null;
        try {
            connection = this.databaseHelper.openDatabaseConnection();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        if (DeveloperSettings.SHOW_FUNCTION_DEBUG_ACTIVITY) {
            System.out.println("Opened database successfully");
        }
        try {
            // Cria o banco de dados
            this.databaseHelper.createDatabase();
        } catch (SQLException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw new Error("Error during the database creation!");
        }
        // Cria uma instância para cada DAO;
        // DAO dos componentes
        deviceDAO = new DeviceDAO(connection, this);
        communicationDAO = new CommunicationDAO(connection, this);
        userDAO = new UserDAO(connection, this);
        adaptationDAO = new AdaptationDAO(connection, this);
        contextDAO = new ContextDAO(connection, this);
        locationDAO = new LocationDAO(connection, this);
        concernsDAO = new ConcernsDAO(connection, this);
        dataDAO = new DataDAO(connection, this);
        eventDAO = new EventDAO(connection, this);
        resourcesDAO = new ResourcesDAO(connection, this);
        // General Definition DAOs
        this.agentTypeDAO = new AgentTypeDAO(connection);
        this.serviceTypeDAO = new ServiceTypeDAO(connection);
        this.entityTypeDAO = new EntityTypeDAO(connection);
        this.dataTypeDAO = new DataTypeDAO(connection);
        this.implementationTypeDAO = new ImplementationTypeDAO(connection);
        this.agentCommunicationLanguageDAO = new AgentCommunicationLanguageDAO(connection);
        this.communicativeActDAO = new CommunicativeActDAO(connection);
        this.interactionTypeDAO = new InteractionTypeDAO(connection);
        this.interactionDirectionDAO = new InteractionDirectionDAO(connection);
        this.targetOriginDAO = new TargetOriginDAO(connection);
        this.agentAddressTypeDAO = new AgentAddressTypeDAO(connection);
        this.serviceDAO = new ServiceDAO(connection);
        this.agentDAO = new AgentDAO(connection);
        // Device DAO
        this.componentDAO = new ComponentDAO(connection);
        this.entityDAO = new EntityDAO(connection);
        this.stateDAO = new EntityStateDAO(connection);
        this.eventModelDAO = new EventModelDAO(connection,this);
        this.actionModelDAO = new ActionModelDAO(connection,this);
        this.instanceDAO = new InstanceDAO(connection);
        // Communication DAO
        this.reportDAO = new MessageReportDAO(connection);
        // Carrega interfaces de comunicação disponíveis (testa disponibilidade antes de executar - lookback)
        for (CommunicationInterface ci : supportedCommunicationInterfaces) {
            try {
                if (ci.isAvailable()) {
                    this.communicationDAO.addAvailableCommunicationInterface(ci);
                    System.out.println("Interface successfully added: " + ci.getName());
                }
            } catch (UnsupportedOperationException ex) {
                System.out.println("Not implemented yet: " + ci.getName());
                //Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, "Not implemented yet.", ex);
            } catch (IOException ex) {
                if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                    Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        // Verifica se o conhecimento foi adicionado, se não foi busca o padrão
        if (this.knowledgeRepresentation == null) {
            this.knowledgeRepresentation = new File("deviceKnowledgeModel.xml");
            this.knowledgeDataType = "xmlFile";
            // caso o arquivo não exista então
            if (!((File) this.knowledgeRepresentation).exists()) {
                throw new Error("Knowledge representation was not specified or not exists!");
            }
        }
        // Carregar dados e configurações que serão utilizados para execução em memória
        if (this.knowledgeDataType.equals("xmlFile")||this.knowledgeDataType.equals("xmlInputStream")) {
            try {
                /*
                 Fazer metodos de validação depois. Eles devem testar se tem todos os atributos obrigatórios,
                 e se existem os valores especificados com as configurações gerais e pependências
                 */
//            if(kp.validateGeneralConfigurations(file)) throw new Error ("");
//            if(kp.validateDeviceModel(file)) System.exit(-1);
//            if(kp.validateAgentModel(file)) System.exit(-1);
                    /*
                 processa o arquivo de entrada com o modelo de conhecimento e coloca em memória. Atualmente pronto.
                 */
                this.databaseHelper.loadingGeneralDefinitions(this.knowledgeRepresentation);
                this.databaseHelper.loadingDevice(this.knowledgeRepresentation);
                this.databaseHelper.loadingAgentModels(this.knowledgeRepresentation);
                /*
                 grava no banco de dados os dados processados -- falta fazer. Primeiro fazer gerar os SQLs, depois fazer os DAO
                 OBS.: Sempre que esses métodos são executados ele verifica a versão salva dos modeloas anteriores e substitui somente
                 caso o conhecimento de entrada possuir uma versão mais recente, ou maior.
                 */
                this.databaseHelper.saveGeneralDefinitions();
                this.databaseHelper.saveDevice();
                this.databaseHelper.saveAgentModels();
            } catch (ParserConfigurationException ex) {
                if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                    Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                    Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (SAXException ex) {
                if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                    Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            throw new Error("Specified knowledge data type is not supported!");
        }
        // Preparar configurações inicias para execução
        // Para tanto utilizar o DataManager para acesso aos dados.
        // Descobrir todo o conhecimento e criar o banco
    }

    public CommunicationDAO getCommunicationDAO() {

        return communicationDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    @Override
    public FeedbackAnswer applyAction(Action action) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void addSupportedCommunicationInterface(CommunicationInterface ci) {
        supportedCommunicationInterfaces.add(ci);
    }

    public void setKnowledgeRepresentation(Object o, String dataType) {
        this.knowledgeRepresentation = o;
        this.knowledgeDataType = dataType;
    }

    public EntityTypeDAO getEntityTypeDAO() {
        return entityTypeDAO;
    }

    public DataTypeDAO getDataTypeDAO() {
        return dataTypeDAO;
    }

    public ImplementationTypeDAO getImplementationTypeDAO() {
        return implementationTypeDAO;
    }

    public AgentCommunicationLanguageDAO getAgentCommunicationLanguageDAO() {
        return agentCommunicationLanguageDAO;
    }

    public CommunicativeActDAO getCommunicativeActDAO() {
        return communicativeActDAO;
    }

    public InteractionTypeDAO getInteractionTypeDAO() {
        return interactionTypeDAO;
    }

    public InteractionDirectionDAO getInteractionDirectionDAO() {
        return interactionDirectionDAO;
    }

    public TargetOriginDAO getTargetOriginDAO() {
        return targetOriginDAO;
    }

    public AgentAddressTypeDAO getAgentAddressTypeDAO() {
        return agentAddressTypeDAO;
    }

    public DeviceDAO getDeviceDAO() {
        return deviceDAO;
    }

    public ServiceDAO getServiceDAO() {
        return serviceDAO;
    }

    public AgentDAO getAgentDAO() {
        return agentDAO;
    }

    public AgentTypeDAO getAgentTypeDAO() {
        return agentTypeDAO;
    }

    public ComponentDAO getComponentDAO() {
        return componentDAO;
    }

    public EntityDAO getEntityDAO() {
        return entityDAO;
    }

    public EntityStateDAO getEntityStateDAO() {
        return stateDAO;
    }

    public EventModelDAO getEventModelDAO() {
        return eventModelDAO;
    }

    public ActionModelDAO getActionModelDAO() {
        return actionModelDAO;
    }

    public InstanceDAO getInstanceDAO() {
        return instanceDAO;
    }

    public AdaptationDAO getAdaptationDAO() {
        return adaptationDAO;
    }

    public LocationDAO getLocationDAO() {
        return locationDAO;
    }

    public ContextDAO getContextDAO() {
        return contextDAO;
    }

    public ResourcesDAO getResourcesDAO() {
        return resourcesDAO;
    }

    public ConcernsDAO getConcernsDAO() {
        return concernsDAO;
    }

    public ServiceTypeDAO getServiceTypeDAO() {
        return serviceTypeDAO;
    }

    public EventDAO getEventDAO() {
        return eventDAO;
    }

    public EntityStateDAO getStateDAO() {
        return stateDAO;
    }

    public MessageReportDAO getReportDAO() {
        return reportDAO;
    }

    public void setUrboSentiDatabaseHelper(UrboSentiDatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
}
