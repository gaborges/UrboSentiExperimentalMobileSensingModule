/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.communication;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import urbosenti.core.data.dao.CommunicationDAO;
import urbosenti.core.device.model.Agent;
import urbosenti.core.device.ComponentManager;
import urbosenti.core.device.DeviceManager;
import urbosenti.core.device.model.FeedbackAnswer;
import urbosenti.core.device.model.Service;
import urbosenti.core.events.Action;
import urbosenti.core.events.ApplicationEvent;
import urbosenti.core.events.Event;
import urbosenti.core.events.SystemEvent;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class CommunicationManager extends ComponentManager {

    /**
     * int EVENT_INTERFACE_DISCONNECTION = 1; </ br>
     *
     * <ul><li>id: 1</li>
     * <li>evento: Interface Desconectada</li>
     * <li>parâmetros: Interface desconectada; (opcional) Serviço de
     * reconexão</li></ul>
     *
     */
    public static final int EVENT_INTERFACE_DISCONNECTION = 1;
    /**
     * int EVENT_MESSAGE_DELIVERED = 2;
     *
     * <ul><li>id: 2</li>
     * <li>evento: Mensagem Entregue</li>
     * <li>parâmetros: Mensagem Wrapper; Destinatário; Interface;</li></ul>
     *
     */
    public static final int EVENT_MESSAGE_DELIVERED = 2;
    /**
     * int EVENT_MESSAGE_NOT_DELIVERED = 3;
     *
     * <ul><li>id: 3</li>
     * <li>evento: Mensagem não entregue</li>
     * <li>parâmetros: Mensagem Wrapper; Destinatário;</li></ul>
     *
     */
    public static final int EVENT_MESSAGE_NOT_DELIVERED = 3;
    /**
     * int EVENT_MESSAGE_RECEIVED = 4;
     *
     * <ul><li>id: 4</li>
     * <li>evento: Mensagem recebida</li>
     * <li>parâmetros: Origem; Mensagem</li></ul>
     *
     */
    public static final int EVENT_MESSAGE_RECEIVED = 4;
    /**
     * int EVENT_MESSAGE_RECEIVED_INVALID_FORMAT = 5;
     *
     * <ul><li>id: 5</li>
     * <li>evento: Mensagem recebida em formato inválido</li>
     * <li>parâmetros: Origem; Mensagem Bruta</li></ul>
     *
     */
    public static final int EVENT_MESSAGE_RECEIVED_INVALID_FORMAT = 5;
    /**
     * int EVENT_ADDRESS_NOT_REACHABLE = 6;
     *
     * <ul><li>id: 6</li>
     * <li>evento: Endereço não acessível. Identificado por timeout.</li>
     * <li>parâmetros: Mensagem Wrapper; Destinatário; Interface</li></ul>
     *
     */
    public static final int EVENT_ADDRESS_NOT_REACHABLE = 6;
    /**
     * int EVENT_INTERFACE_DISCONNECTION = 7;
     *
     * <ul><li>id: 7</li>
     * <li>evento: desconexão geral do dispositivo</li>
     * <li>parâmetros: nenhum.</li></ul>
     *
     */
    public static final int EVENT_DISCONNECTION = 7;
    /**
     * int EVENT_RESTORED_CONNECTION = 8;
     *
     * <ul><li>id: 8</li>
     * <li>evento: Conexão Reestabelecida</li>
     * <li>parâmetros: Interface ; (opcional) Serviço de reconexão</li></ul>
     *
     */
    public static final int EVENT_CONNECTION_RESTORED = 8;
    /**
     * int EVENT_REPORT_AWAITING_APPROVAL = 9;
     *
     * <ul><li>id: 9</li>
     * <li>evento: Relato Esperando Aprovação </li>
     * <li>parâmetros: Mensagem</li></ul>
     *
     */
    public static final int EVENT_REPORT_AWAITING_APPROVAL = 9;
    /**
     * int EVENT_MESSAGE_STORED = 10;
     *
     * <ul><li>id: 10</li>
     * <li>evento: Mensagem armazenada </li>
     * <li>parâmetros: Mensagem Wrapper</li></ul>
     *
     */
    public static final int EVENT_MESSAGE_STORED = 10;
    /**
     * int EVENT_MESSAGE_STORED_REMOVED = 11;
     *
     * <ul><li>id: 11</li>
     * <li>evento: Mensagem armazenada foi removida</li>
     * <li>parâmetros: Mensagem Wrapper</li></ul>
     *
     */
    public static final int EVENT_MESSAGE_STORED_REMOVED = 11;
    /**
     * int EVENT_NEW_INPUT_COMMUNICATION_INTERFACE_ADDRESS = 15;
     *
     * <ul><li>id: 15</li>
     * <li>evento: Nova configuração de endereço de entrada</li>
     * <li>parâmetros: Mensagem Wrapper</li></ul>
     *
     */
    public static final int EVENT_NEW_INPUT_COMMUNICATION_INTERFACE_ADDRESS = 15;
    /**
     * int EVENT_NEW_RECONNECTION_ATTEMPT = 16;
     *
     * <ul><li>id: 16</li>
     * <li>evento: Nova tentativa de reconexão</li>
     * <li>parâmetros: Serviço de reconexão</li></ul>
     *
     */
    public static final int EVENT_NEW_RECONNECTION_ATTEMPT = 16;
    /**
     * int EVENT_NEW_START_OF_UPLOAD_SERVICE_FUNCTION_LOOP = 17;
     *
     * <ul><li>id: 17</li>
     * <li>evento: Novo início do loop de serviço de upload</li>
     * <li>parâmetros: Serviço de upload</li></ul>
     *
     */
    public static final int EVENT_NEW_START_OF_UPLOAD_SERVICE_FUNCTION_LOOP = 17;
    /*
     *********************************************************************
     ***************************** Actions ******************************* 
     *********************************************************************
     */
    /**
     * int ACTION_REMOVE_REPORT_FROM_DATABASE = 1;
     *
     * <ul><li>id: 1</li>
     * <li>ação: Remover relato da fila de upload e do banco de dados</li>
     * <li>parâmetros: Id do Relato</li></ul>
     *
     */
    public static final int ACTION_REMOVE_REPORT_FROM_DATABASE = 1;
    /**
     * int ACTION_UPDATE_QUANTITY_LIMIT_OF_REPORTS_STORED = 2;
     *
     * <ul><li>id: 2</li>
     * <li>ação: Alterar limite de relatos armazenados</li>
     * <li>parâmetros: Novo Limite</li></ul>
     *
     */
    public static final int ACTION_UPDATE_QUANTITY_LIMIT_OF_REPORTS_STORED = 2;
    /**
     * int ACTION_UPDATE_TIME_LIMIT_OF_REPORTS_STORED = 3;
     *
     * <ul><li>id: 3</li>
     * <li>ação: Alterar tempo limite de expiração de mensagens armazenadas</li>
     * <li>parâmetros: Nova Política (de 1 a 4)</li></ul>
     *
     */
    public static final int ACTION_UPDATE_TIME_LIMIT_OF_REPORTS_STORED = 3;
    /**
     * int ACTION_UPDATE_STORAGE_POLICY = 4;
     *
     * <ul><li>id: 4</li>
     * <li>ação: Alterar política de relatos armazenados</li>
     * <li>parâmetros: Nova Política (de 1 a 4)</li></ul>
     *
     */
    public static final int ACTION_UPDATE_STORAGE_POLICY = 4;
    /**
     * int ACTION_UPDATE_RECONNECTION_SERVICE_INTERVAL = 5;
     *
     * <ul><li>id: 5</li>
     * <li>ação: Alterar intervalo de reconexão</li>
     * <li>parâmetros: Intervalo em ms; Id da instância</li></ul>
     *
     */
    public static final int ACTION_UPDATE_RECONNECTION_SERVICE_INTERVAL = 5;
    /**
     * int ACTION_UPDATE_RECONNECTION_SERVICE_METHOD = 6;
     *
     * <ul><li>id: 6</li>
     * <li>ação: Alterar metodo de reconexão</li>
     * <li>parâmetros: método (1 ou 2); Id da instância</li></ul>
     *
     */
    public static final int ACTION_UPDATE_RECONNECTION_SERVICE_METHOD = 6;
    /**
     * int ACTION_UPDATE_RECONNECTION_SERVICE_POLICY = 7;
     *
     * <ul><li>id: 7</li>
     * <li>ação: Alterar política de reconexão</li>
     * <li>parâmetros: política (1 ou 2)</li></ul>
     *
     */
    public static final int ACTION_UPDATE_RECONNECTION_SERVICE_POLICY = 7;
    /**
     * int ACTION_UPDATE_UPLOAD_SERVICE_POLICY = 8;
     *
     * <ul><li>id: 8</li>
     * <li>ação: Alterar política dos serviços de reconexão</li>
     * <li>parâmetros: política (1,2,3 ou 4)</li></ul>
     *
     */
    public static final int ACTION_UPDATE_UPLOAD_SERVICE_POLICY = 8;
    /**
     * int ACTION_UPDATE_UPLOAD_SERVICE_UPLOAD_RATE = 9;
     *
     * <ul><li>id: 9</li>
     * <li>ação: Alterar taxa de upload do serviço de reconexão</li>
     * <li>parâmetros: Nova Taxa; Id da instância</li></ul>
     *
     */
    public static final int ACTION_UPDATE_UPLOAD_SERVICE_UPLOAD_RATE = 9;
    /**
     * int ACTION_UPDATE_UPLOAD_SERVICE_INTERVAL = 10;
     *
     * <ul><li>id: 10</li>
     * <li>ação: Alterar tempo do intervalo entre ciclos de upload</li>
     * <li>parâmetros: Novo intervalo; Id da instância</li></ul>
     *
     */
    public static final int ACTION_UPDATE_UPLOAD_SERVICE_INTERVAL = 10;
    /**
     * int ACTION_UPDATE_UPLOAD_SERVICE_REPORTS_BY_INTERVAL = 11;
     *
     * <ul><li>id: 11</li>
     * <li>ação: Alterar quantidade de relatos enviados simultaneamente por
     * ciclo</li>
     * <li>parâmetros: Nova quantidade; Id da instância</li></ul>
     *
     */
    public static final int ACTION_UPDATE_UPLOAD_SERVICE_REPORTS_BY_INTERVAL = 11;
    /**
     * int ACTION_UPDATE_MOBILE_DATA_POLICY = 12;
     *
     * <ul><li>id: 2</li>
     * <li>ação: Alterar política</li>
     * <li>parâmetros: política (1,2,3,4,5 ou 6)</li></ul>
     *
     */
    public static final int ACTION_UPDATE_MOBILE_DATA_POLICY = 12;
    /**
     * int ACTION_UPDATE_MOBILE_DATA_NORMAL_DATA_QUOTA = 13;
     *
     * <ul><li>id: 13</li>
     * <li>ação: Alterar Limite de uso dos Dados Móveis com prioridade
     * normal</li>
     * <li>parâmetros: Novo limite</li></ul>
     *
     */
    public static final int ACTION_UPDATE_MOBILE_DATA_NORMAL_DATA_QUOTA = 13;
    /**
     * int ACTION_UPDATE_MOBILE_DATA_PREFERENTIAL_DATA_QUOTA = 14;
     *
     * <ul><li>id: 14</li>
     * <li>ação: Alterar Limite de uso dos Dados Móveis com prioridade
     * preferêncial</li>
     * <li>parâmetros: Novo limite</li></ul>
     *
     */
    public static final int ACTION_UPDATE_MOBILE_DATA_PREFERENTIAL_DATA_QUOTA = 14;
    /**
     * int ACTION_COMMUNICATION_INTERFACE_ENABLE = 15;
     *
     * <ul><li>id: 15</li>
     * <li>ação: Desabilitar interface</li>
     * <li>parâmetros: Interface;Condição (true)</li></ul>
     *
     */
    public static final int ACTION_COMMUNICATION_INTERFACE_ENABLE = 15;
    /**
     * int ACTION_COMMUNICATION_INTERFACE_DISABLE = 16;
     *
     * <ul><li>id: 16</li>
     * <li>ação: Habilitar interface</li>
     * <li>parâmetros: Interface;Condição (false)</li></ul>
     *
     */
    public static final int ACTION_COMMUNICATION_INTERFACE_DISABLE = 16;
    /**
     * int ACTION_COMMUNICATION_INTERFACE_SET_CURRENT_INTERFACE = 17;
     *
     * <ul><li>id: 17</li>
     * <li>ação: Definir interface como atual</li>
     * <li>parâmetros: Interface</li></ul>
     *
     */
    public static final int ACTION_COMMUNICATION_INTERFACE_SET_CURRENT_INTERFACE = 17;
    /**
     * int ACTION_COMMUNICATION_INTERFACE_POSITION_IN_QUEUE = 18;
     *
     * <ul><li>id: 18</li>
     * <li>ação: Alterar ordem da interface</li>
     * <li>parâmetros: Interface;Nova posição</li></ul>
     *
     */
    public static final int ACTION_COMMUNICATION_INTERFACE_POSITION_IN_QUEUE = 18;
    /**
     * int ACTION_COMMUNICATION_INTERFACE_UPDATE_TIMEOUT = 19;
     *
     * <ul><li>id: 19</li>
     * <li>ação: Alterar timeout</li>
     * <li>parâmetros: Interface;Alterar timeout</li></ul>
     *
     */
    public static final int ACTION_COMMUNICATION_INTERFACE_UPDATE_TIMEOUT = 19;
    /**
     * int ACTION_INPUT_COMMUNICATION_INTERFACE_ENABLE = 20;
     *
     * <ul><li>id: 20</li>
     * <li>ação: Habilitar Interface</li>
     * <li>parâmetros: Instância;Condição(true)</li></ul>
     *
     */
    public static final int ACTION_INPUT_COMMUNICATION_INTERFACE_ENABLE = 20;
    /**
     * int ACTION_INPUT_COMMUNICATION_INTERFACE_DISABLE = 21;
     *
     * <ul><li>id: 21</li>
     * <li>ação: Desabilitar Interface</li>
     * <li>parâmetros: Instância;Condição(false)</li></ul>
     *
     */
    public static final int ACTION_INPUT_COMMUNICATION_INTERFACE_DISABLE = 21;
    /**
     * int ACTION_SEND_SYNCHRONOUS_MESSAGE = 22;
     *
     * <ul><li>id: 22</li>
     * <li>ação: Enviar mensagem síncrona</li>
     * <li>parâmetros: Mensagem;Endereço</li></ul>
     *
     */
    public static final int ACTION_SEND_SYNCHRONOUS_MESSAGE = 22;
    /**
     * int ACTION_SEND_ASSYNCHRONOUS_MESSAGE = 23;
     *
     * <ul><li>id: 23</li>
     * <li>ação: Enviar mensagem assíncrona</li>
     * <li>parâmetros: Mensagem;Endereço</li></ul>
     *
     */
    public static final int ACTION_SEND_ASSYNCHRONOUS_MESSAGE = 23;
    /**
     * int ACTION_DELETE_EXPIRED_MESSAGES = 24;
     *
     * <ul><li>id: 24</li>
     * <li>ação: Apagar mensagens expiradas</li>
     * <li>parâmetros: Nenhum</li></ul>
     *
     */
    public static final int ACTION_DELETE_EXPIRED_MESSAGES = 24;
    /**
     * int ACTION_UPDATE_UPLOAD_SERVICE_ALLOWED_TO_UPLOAD = 25;
     *
     * <ul><li>id: 25</li>
     * <li>ação: Permissão de realizar upload alterada</li>
     * <li>parâmetros: Nenhum</li></ul>
     *
     */
    public static final int ACTION_UPDATE_UPLOAD_SERVICE_ALLOWED_TO_UPLOAD = 25;
    /**
     * int ACTION_UPDATE_UPLOAD_SERVICE_SUBSCRIBED_MAXIMUM_UPLOAD_RATE = 26;
     *
     * <ul><li>id: 26</li>
     * <li>ação: Registrado para receber taxas máximas de upload</li>
     * <li>parâmetros: Nenhum</li></ul>
     *
     */
    public static final int ACTION_UPDATE_UPLOAD_SERVICE_SUBSCRIBED_MAXIMUM_UPLOAD_RATE = 26;
    private int limitPriorityMessage;
    private int limitNormalMessage;
//    private List<MessageWrapper> messagesNotChecked;
//    private final Queue<MessageWrapper> normalMessageQueue;
//    private final Queue<MessageWrapper> priorityMessageQueue;
    private double mobileDataQuota; // em bytes
    private double usedMobileData; // em bytes
    private double mobileDataPriorityQuota; // em bytes
    /**
     * <h3>Política de uso de dados móveis</h3>
     * <ul><li>Políticas:</li>
     * <li>1 = Sem mobilidade. Configuração default.</li>
     * <li>2 = Fazer o uso sempre que possível.</li>
     * <li>3 = Somente fazer uso com relatos de alta prioridade.</li>
     * <li>4 = Utiliza cota por ciclo de uso: Até X todos os tipos de mensagens,
     * após até Y somente de alta prioridade.</li>
     * <li>5 = Utiliza cota por ciclo de uso: Até X todos os tipos de mensagens
     * para mensagens de alta prioridade o uso é liberado.</li>
     * <li>6 = Não utilizar dados móveis.</li></ul>
     */
    private int mobileDataPolicy;
    /**
     * <h3>Política de Upload periódico de Mensagens</h3>
     * <ul><li>Políticas:</li>
     * <li>1 = Sempre que há um relato novo tenta fazer o upload, caso exista
     * conexão, senão espera reconexão. Padrão.</li>
     * <li>2 = Em intervalos fixos. Pode ser definido pela aplicação. Intervalo
     * inicial padrão a cada 15 segundos. Se não há conexão as mensagens são
     * armazenadas.</li>
     * <li>3 = Exige confirmação da aplicação para upload dos relatos. Enquanto
     * não confirmada comportamento na política 1.</li>
     * <li>4 = Adaptativo. O componente de adaptação irá atribuir dinamicamente
     * novos intervalos.</li></ul>
     */
    private int uploadMessagingPolicy;
    /**
     * <h3>Política de armazenamento de mensagem</h3>
     * <ul><li>Políticas:</li>
     * <li>1 = Não armazenar nenhuma.</li>
     * <li>2 = Apagar todas que foram enviadas com sucesso e armazenar as que
     * não foram enviadas. Opção padrão.</li>
     * <li>3 = Armazenar todas e deixar a aplicação decidir quais apagar.</li>
     * <li>4 = Dinâmico (Exige componente de adaptação). Dá poder ao mecanismo
     * decidir quando apagar uma mensagem armazenada. O usuário pode especificar
     * uma quantidade ou um tempo.</li></ul>
     */
    private int messageStoragePolicy;
    /**
     * <h3>Política de armazenamento de mensagem</h3>
     * <ul><li>Políticas:</li>
     * <li>1 = Tentativa em intervalos fixos. Pode ser definido pela aplicação.
     * O padrão é nova tentativa a cada 60 segundos.</li>
     * <li>2 = Adaptativo. Permite o componente de adaptação reconfigure
     * dinamicamente o tempo de reconexão.</li></ul>
     */
    private int reconnectionPolicy; // Política de reconexão
    private CommunicationInterface currentCommunicationInterface;
    private List<CommunicationInterface> communicationInterfaces;
    private ReconnectionService gerenalReconnectionService;
    private final List<PushServiceReceiver> pushServiceReveivers;
    private final List<UploadService> uploadServices;
    private final List<ReconnectionService> reconnectionServices;
    private UploadService backendUploadService;
    private boolean completelyDisconnected;
    private Integer quantityLimit;
    private Integer timeLimit;

    public CommunicationManager(DeviceManager deviceManager) {
        super(deviceManager, CommunicationDAO.COMPONENT_ID);
//        this.normalMessageQueue = new LinkedList();
//        this.priorityMessageQueue = new LinkedList();
        this.pushServiceReveivers = new ArrayList();
        this.uploadServices = new ArrayList();
        this.reconnectionServices = new ArrayList();
        this.completelyDisconnected = false;
        this.backendUploadService = null;
        this.quantityLimit = Integer.MAX_VALUE;
        this.timeLimit = Integer.MAX_VALUE;
    }

    // if do not setted then when the method onCreate was activated it creates automatically
    public void setGeneralReconectionService(ReconnectionService reconnectionService) {
        this.gerenalReconnectionService = reconnectionService;
        boolean isThere = false;
        for (ReconnectionService rs : reconnectionServices) {
            if (rs.getInstance().getId() == reconnectionService.getInstance().getId()) {
                isThere = true;
                break;
            }
        }
        if (!isThere) {
            reconnectionServices.add(reconnectionService);
        }
    }

    @Override
    public void onCreate() {
        try {
            if (DeveloperSettings.SHOW_FUNCTION_DEBUG_ACTIVITY) {
                System.out.println("Activating: " + getClass());
            }
            // Carregar dados e configuraçÃµes que serão utilizados para execução em memória
            // Preparar configuraçÃµes inicias para execução
            // Para tanto utilizar o DataManager para acesso aos dados.
            this.communicationInterfaces = super.getDeviceManager().getDataManager().getCommunicationDAO().getAvailableInterfaces();
            this.currentCommunicationInterface = this.communicationInterfaces.get(0);
            // verifica que o servidor de upload foi atribuído
            if (backendUploadService == null) {
                throw new Error("Backend upload service do was not assigned!");
            }
            // verifica se o serviço geral de reconexão foi atribuído
            if (gerenalReconnectionService == null) {
                throw new Error("Main reconnection service was not assigned!");
            }
//
//        this.mobileDataPolicy = 1; // sem mobilidade - Default
//        this.messagingPolicy = 1;  // Se não der certo avisa a origem da mensagem
//        this.messageStoragePolicy = 2; // Política de armazenamento de mensagem - Padrão: Apagar todas que foram enviadas com sucesso e armazenar as que não foram enviadas. 
//        this.reconnectionPolicy = 1;   // Política de reconexão: Padrão - Tentativa em intervalos fixos. Pode ser definido pela aplicação. O padrão é uma nova tentativa a cada 60 segundos
//        this.uploadMessagingPolicy = 2; //  política de Upload periódico de Mensagens: Sempre que há um relato novo tenta fazer o upload, caso exista conexão, senão espera reconexão. Padrão.
//
            this.mobileDataPolicy = super.getDeviceManager().getDataManager().getCommunicationDAO().getCurrentPreferentialPolicy(CommunicationDAO.MOBILE_DATA_POLICY); // sem mobilidade - Default
            this.messageStoragePolicy = super.getDeviceManager().getDataManager().getCommunicationDAO().getCurrentPreferentialPolicy(CommunicationDAO.MESSAGE_STORAGE_POLICY); // Política de armazenamento de mensagem - Padrão: Apagar todas que foram enviadas com sucesso e armazenar as que não foram enviadas.
            this.reconnectionPolicy = super.getDeviceManager().getDataManager().getCommunicationDAO().getCurrentPreferentialPolicy(CommunicationDAO.RECONNECTION_POLICY);   // Política de reconexão: Padrão - Tentativa em intervalos fixos. Pode ser definido pela aplicação. O padrão é uma nova tentativa a cada 60 segundos
            this.uploadMessagingPolicy = super.getDeviceManager().getDataManager().getCommunicationDAO().getCurrentPreferentialPolicy(CommunicationDAO.UPLOAD_REPORTS_POLICY); //  política de Upload periódico de Mensagens: Sempre que há um relato novo tenta fazer o upload, caso exista conexão, senão espera reconexão. Padrão.

            // Setar dinamica a política de mobile data police aqui.
            // se há politica deve ser setado através das configuraçÃµes:
            this.mobileDataQuota = 1000;
//      implementar em um futuro distante os estados de dados móveis      
//        this.mobileDataQuota = (Integer) super.getDeviceManager().getDataManager().getStateDAO()
//                .getEntityState(CommunicationDAO.COMPONENT_ID, CommunicationDAO.ENTITY_ID_OF_MOBILE_DATA_USAGE, CommunicationDAO.STATE_ID_OF_MOBILE_DATA_USAGE_LIMIT).getCurrentValue();
            this.usedMobileData = 0;
            this.mobileDataPriorityQuota = 2000;
            // Setar dinamicamente a politica de mensagens

            // testar se não foi setado os serviços de entraga e reconexão senão criar e iniciar
            // Testar se os serviços foram iniciados e caso não, iniciá-los
            //Contadores do escalonador da fila de reports
            limitNormalMessage = 1;
            limitPriorityMessage = 4;
            // armazenamento
            this.quantityLimit = Integer.parseInt(getDeviceManager().getDataManager().getEntityStateDAO().getEntityState(
                    CommunicationDAO.COMPONENT_ID,
                    CommunicationDAO.ENTITY_ID_OF_REPORTS_STORAGE,
                    CommunicationDAO.STATE_ID_OF_REPORTS_STORAGE_ABOUT_AMOUNT_LIMIT_OF_STORED_MESSAGES).getCurrentValue().toString());
            this.timeLimit = Integer.parseInt(getDeviceManager().getDataManager().getEntityStateDAO().getEntityState(
                    CommunicationDAO.COMPONENT_ID,
                    CommunicationDAO.ENTITY_ID_OF_REPORTS_STORAGE,
                    CommunicationDAO.STATE_ID_OF_REPORTS_STORAGE_ABOUT_MESSAGE_EXPIRATION_TIME).getCurrentValue().toString());
        } catch (SQLException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw new Error(ex);
        }
    }

    /**
     * AçÃµes disponibilizadas por esse componente por função:
     * <p>
     * <b>Entidade Alvo</b>: Função de Armazenamento de relatos</p>
     * <ul>
     * <li>01 - Remover relato da fila de upload e do banco de dados -
     * reportId</li>
     * <li>02 - Alterar limite de relatos armazenados - limit</li>
     * <li>03 - Alterar tempo limite - limit</li>
     * <li>04 - Alterar política - policy - de 1 a 4</li>
     * <li>24 - Apagar mensagens expiradas</li>
     * </ul>
     * <p>
     * <b>Entidade Alvo</b>: Função de Reconexão</p>
     * <ul>
     * <li>05 - Alterar intervalo de reconexão - interval ; instaceId</li>
     * <li>06 - Alterar método - method - 1 ou 2 ; instaceId</li>
     * <li>07 - Alterar política - policy - 1 ou 2</li>
     * </ul>
     * <p>
     * <b>Entidade Alvo</b>: Função de Otimização de Upload de Relatos</p>
     * <ul>
     * <li>08 - Alterar política - policy - de 1 a 4</li>
     * <li>09 - Alterar taxa de upload - uploadRate - entre 1.0 e 0.0</li>
     * <li>10 - Alterar tempo do intervalo entre ciclos de upload -
     * interval</li>
     * <li>11 - Alterar quantidade de relatos enviados simultaneamente por ciclo
     * - quantity</li>
     * </ul>
     * <p>
     * <b>Entidade Alvo</b>: Função de Uso de Dados Móveis. OBS.:Não operacional
     * ainda</p>
     * <ul>
     * <li>12 - Alterar política - policy - de 1 a 6</li>
     * <li>13 - Alterar Limite de Dados Móveis com prioridade normal -
     * newLimit</li>
     * <li>14 - Alterar Limite de Dados Móveis com prioridade - newLimit</li>
     * </ul>
     * <p>
     * <b>Entidade Alvo</b>: Interface de Comunicação de Saída</p>
     * <ul>
     * <li>15 - Desabilitar interface - interface</li>
     * <li>16 - Habilitar interface - interface</li>
     * <li>17 - Definir interface como atual - interface</li>
     * <li>18 - Alterar ordem da interface - interface e position</li>
     * <li>19 - Alterar timeout - interface - timeout</li>
     * </ul>
     * <p>
     * <b>Entidade Alvo</b>: Interface de Comunicação de Entrada</p>
     * <ul>
     * <li>20 - Habilitar Interface - interface</li>
     * <li>21 - Desabilitar Interface - interface</li>
     * </ul>
     * <p>
     * <b>Entidade Alvo</b>: Função de envio de mensagens</p>
     * <ul>
     * <li>22 - Enviar mensagem síncrona - target,message</li>
     * <li>23 - Enviar mensagem assíncrona - target,message</li>
     * </ul>
     *
     * @param action contém objeto ação.
     * @return
     *
     */
    @Override
    public synchronized FeedbackAnswer applyAction(Action action) {
        Integer policy, genericInteger, instanceId;
        Double genericDouble;
        Agent agent;
        FeedbackAnswer answer = null;
        Message message;
        switch (action.getId()) {
            /**
             * *********** Função de Armazenamento de relatos *****************
             */
            case 1: // Remover relato da fila de upload e da base
                // Parâmetro
                Integer reportId = (Integer) action.getParameters().get("reportId");
                // Remover da fila de upload
                try {
                    MessageWrapper mw = super.getDeviceManager().getDataManager().getReportDAO().get(reportId);
                    // Remover do banco de dados
                    super.getDeviceManager().getDataManager().getReportDAO().delete(reportId);
                    // Caso o mw esteja vazio adicionar o reportId nele
                    if (mw == null) {
                        mw = new MessageWrapper(null);
                        mw.setId(reportId);
                    }
                    // Evento de mensagem removida
                    this.newInternalEvent(EVENT_MESSAGE_STORED_REMOVED, mw);
                    // retorno sucesso
                } catch (SQLException ex) {
                    if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                        Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // retorno erro
                    answer = new FeedbackAnswer(FeedbackAnswer.ACTION_RESULT_FAILED, ex.toString());
                }
                break;
            case 2: // Alterar limite de relatos armazenados
                this.quantityLimit = (Integer) action.getParameters().get("limit");
                break;
            case 3: // Alterar tempo limite - Função de Armazenamento de relatos
                this.timeLimit = (Integer) action.getParameters().get("limit");
                break;
            case 4: // Alterar política - Função de Armazenamento de relatos
                // Parâmetro
                policy = (Integer) action.getParameters().get("policy");
                // em um futuro distante um evento de intenção pode ser gerado aqui para intervenção por permisão de acesso
                // alterar política de armazenamento de relatos
                this.messageStoragePolicy = policy;
                break;
            /**
             * *********** Função de Reconexão *****************
             */
            case 5: // Alterar intervalo de reconexão
                // Parâmetro
                genericInteger = (Integer) action.getParameters().get("interval");
                instanceId = (Integer) action.getParameters().get("instaceId");
                // verifica se a política é a estática e se a origem não é o sistema, pois nesse caso somente a aplicação e usuários podem alterar.
                if (reconnectionPolicy == 1 && action.getOrigin() != Address.LAYER_SYSTEM) {
                    // Atribuir o valor na interface encontrada
                    for (ReconnectionService rs : reconnectionServices) {
                        if (instanceId == rs.getInstance().getModelId()) {
                            rs.setReconnectionTime(genericInteger.longValue());
                            break;
                        }
                    }
                }
                break;
            case 6: // Alterar método
                // Parâmetro
                genericInteger = (Integer) action.getParameters().get("method");
                instanceId = (Integer) action.getParameters().get("instaceId");
                agent = (Agent) action.getParameters().get("origin");
                // verifica se a política é a estática e se a origem não é o sistema, pois nesse caso somente a aplicação e usuários podem alterar.
                if (reconnectionPolicy == 1 && action.getOrigin() != Address.LAYER_SYSTEM) {
                    // Verificar o valor
                    for (ReconnectionService rs : reconnectionServices) {
                        if (instanceId == rs.getInstance().getModelId()) {
                            if (genericInteger == 1) {
                                rs.setReconnectionMethodOneByTime();
                            } else if (genericInteger == 2) {
                                rs.setReconnectionMethodAllByOnce();
                            }
                            break;
                        }
                    }
                }
                break;
            case 7: // Alterar política
                // Parâmetro
                if (reconnectionPolicy == 1 && action.getOrigin() != Address.LAYER_SYSTEM) {
                    // atribuir
                    this.reconnectionPolicy = (Integer) action.getParameters().get("policy");
                }
                break;

            /**
             * *********** Função de Otimização de Upload de
             * Relatos*****************
             */
            case 8: // Alterar política
                // Parâmetro
                policy = (Integer) action.getParameters().get("policy");
                // em um futuro distante um evento de intenção pode ser gerado aqui para intervenção por permisão de acesso
                // alterar política de armazenamento de relatos
                if (policy >= 1 && policy <= 4) {
                    this.messageStoragePolicy = policy;
                }
                break;
            case 9: // Alterar taxa de upload
                instanceId = (Integer) action.getParameters().get("instaceId");
                genericDouble = (Double) action.getParameters().get("uploadRate");
                if (genericDouble <= 1.0 && genericDouble >= 0.0) {
                    for (UploadService us : uploadServices) {
                        if (instanceId == us.getInstance().getModelId()) {
                            us.setUploadRate(genericDouble);
                            break;
                        }
                    }
                }
                // Verificar o valor

                break;
            case 10: // Alterar tempo do intervalo entre ciclos de upload
                instanceId = (Integer) action.getParameters().get("instaceId");
                genericInteger = (Integer) action.getParameters().get("interval");
                if (genericInteger > 0) {
                    for (UploadService us : uploadServices) {
                        if (instanceId == us.getInstance().getModelId()) {
                            us.setUploadInterval(genericInteger.longValue());
                            break;
                        }
                    }
                }
                break;
            case 11: // Alterar quantidade de relatos enviados simultaneamente por ciclo
                instanceId = (Integer) action.getParameters().get("instaceId");
                genericInteger = (Integer) action.getParameters().get("quantity");
                if (genericInteger > 0) {
                    for (UploadService us : uploadServices) {
                        if (instanceId == us.getInstance().getModelId()) {
                            us.setLimitOfReportsSentByUploadInterval(genericInteger);
                            break;
                        }
                    }
                }
                break;
            /**
             * *********** Função de Uso de Dados Móveis *****************
             */
            case 12: // Alterar política
                policy = (Integer) action.getParameters().get("policy");
                // em um futuro distante um evento de intenção pode ser gerado aqui para intervenção por permisão de acesso
                this.mobileDataPolicy = policy;
                break;
            case 13: // Alterar Limite de Dados Móveis com prioridade normal
                this.mobileDataQuota = (Double) action.getParameters().get("newLimit");
                break;
            case 14: // Alterar Limite de Dados Móveis com prioridade 
                this.mobileDataPriorityQuota = (Double) action.getParameters().get("newLimit");
                break;
            /**
             * *********** Interface de Comunicação de Saída *****************
             */
            case 15: // Desabilitar interface
                genericInteger = (Integer) action.getParameters().get("interface");
                for (CommunicationInterface ci : communicationInterfaces) {
                    if (ci.getId() == genericInteger) {
                        ci.setStatus(CommunicationInterface.STATUS_UNAVAILABLE);
                    }
                }
                break;
            case 16: // Habilitar interface
                genericInteger = (Integer) action.getParameters().get("interface");
                for (CommunicationInterface ci : communicationInterfaces) {
                    if (ci.getId() == genericInteger) {
                        ci.setStatus(CommunicationInterface.STATUS_AVAILABLE);
                    }
                }
                break;
            case 17: // Definir interface de comunicação como para uso atual
                for (CommunicationInterface ci : this.communicationInterfaces) {
                    if (ci.getId() == (Integer) action.getParameters().get("interface")) {
                        this.currentCommunicationInterface = ci;
                        break;
                    }
                }
                break;
            case 18: // Alterar ordem da interface
                genericInteger = (Integer) action.getParameters().get("interface");
                // Encontra a interface
                for (int i = 0; i < communicationInterfaces.size(); i++) {
                    CommunicationInterface ci;
                    if (communicationInterfaces.get(i).getId() == genericInteger) {
                        ci = communicationInterfaces.get(i);
                        communicationInterfaces.remove(i); // Apaga a interface da Ãºltima posição
                        communicationInterfaces.add( // Adiciona a interface na possição necessária
                                (Integer) action.getParameters().get("position"),
                                ci);
                        break;
                    }
                }
                break;

            case 19: // Alterar timeout
                for (CommunicationInterface ci : this.communicationInterfaces) {
                    if (ci.getId() == (Integer) action.getParameters().get("interface")) {
                        ci.setTimeout((Integer) action.getParameters().get("timeout"));
                        break;
                    }
                }
                break;
            /**
             * *********** Interface de Comunicação de Entrada
             * *****************
             */
            case 20: // Habilitar Interface
                for (int i = 0; i < pushServiceReveivers.size(); i++) {
                    if (pushServiceReveivers.get(i).getId() == (Integer) action.getParameters().get("interface")) {
                        if ((Boolean) action.getParameters().get("status") == PushServiceReceiver.STATUS_LISTENING) {
                            pushServiceReveivers.get(i).setStatus(PushServiceReceiver.STATUS_LISTENING);
                        }
                    }
                }
                break;
            case 21: // Desabilitar Interface
                for (int i = 0; i < pushServiceReveivers.size(); i++) {
                    if (pushServiceReveivers.get(i).getId() == (Integer) action.getParameters().get("interface")) {
                        if ((Boolean) action.getParameters().get("status") == PushServiceReceiver.STATUS_STOPPED) {
                            pushServiceReveivers.get(i).setStatus(PushServiceReceiver.STATUS_STOPPED);
                        }
                    }
                }
                break;
            case 22: // 22 - Enviar mensagem síncrona - com comfirmação de chegada - target,message
                message = (Message) action.getParameters().get("message");
                message.setTarget((Address) action.getParameters().get("target"));
                try {
                    this.sendMessage(message);
                } catch (SocketTimeoutException ex) {
                    if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                        Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    answer = new FeedbackAnswer(FeedbackAnswer.ACTION_RESULT_FAILED_TIMEOUT, ex.toString());
                } catch (ConnectException ex) {
                    if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                        Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    answer = new FeedbackAnswer(FeedbackAnswer.ACTION_RESULT_FAILED, ex.toString());
                } catch (IOException ex) {
                    if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                        Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    answer = new FeedbackAnswer(FeedbackAnswer.ACTION_RESULT_FAILED, ex.toString());
                }
                break;
            case 23: // 23 - Enviar mensagem assíncrona - target,message
                message = (Message) action.getParameters().get("message");
                message.setTarget((Address) action.getParameters().get("target"));
                try {
                    boolean found = false;
                    // procura o upload service para enviar
                    for (UploadService us : uploadServices) {
                        if (us.getService().getServiceUID().equals(message.getTarget().getUid())) {
                            us.sendAssynchronousMessage(message);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        answer = new FeedbackAnswer(FeedbackAnswer.ACTION_RESULT_FAILED, "Upload Service UID:" + message.getTarget().getUid() + " was not found!");
                    }
                } catch (SQLException ex) {
                    if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                        Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    answer = new FeedbackAnswer(FeedbackAnswer.ACTION_RESULT_FAILED, ex.toString());
                }
                break;
            case 24: // apagar mensagens expiradas
                try {
                    getDeviceManager().getDataManager().getReportDAO().deleteAllExpired(timeLimit);
                } catch (SQLException ex) {
                    if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                        Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    answer = new FeedbackAnswer(FeedbackAnswer.ACTION_RESULT_FAILED, ex.toString());
                }
                break;
            case 25: // Permitir realizar upload
                instanceId = (Integer) action.getParameters().get("instanceId");
                for (UploadService us : uploadServices) {
                    if (us.getInstance().getModelId() == instanceId) {
                        us.setAllowedToPerformUpload((Boolean) action.getParameters().get("value"));
                        break;
                    }
                }
                break;
            case 26: // Registrado para receber taxas máximas de upload
                instanceId = (Integer) action.getParameters().get("instanceId");
                for (UploadService us : uploadServices) {
                    if (us.getInstance().getModelId() == instanceId) {
                        us.setSubscribedMaximumUploadRate((Boolean) action.getParameters().get("value"));
                        break;
                    }
                }
                break;
        }
        // verifica se a ação existe ou se houve algum resultado durante a execução
        if (answer == null && action.getId() >= 1 && action.getId() <= 26) {
            answer = new FeedbackAnswer(FeedbackAnswer.ACTION_RESULT_WAS_SUCCESSFUL);
        } else if (answer == null) {
            answer = new FeedbackAnswer(FeedbackAnswer.ACTION_DOES_NOT_EXIST);
        }
        return answer;
    }

    /**
     * @param message has the message to send. This message can be sent to
     * everyone that can understand the message.
     *
     * @return returns true if the message is sent successfully and false if the
     * device haven't connection.
     * @throws java.net.SocketTimeoutException
     * @throws java.net.ConnectException
     *
     *
     */
    public boolean sendMessage(Message message) throws SocketTimeoutException, java.net.ConnectException, IOException {
        // 1 - Recebe a mensagem - Gets the message
        /*
         * Se quem está enviando não foi explicitado, então, por padrão, são preenxidos os dados do envio da aplicação.
         */
        if (message.getOrigin() == null) {
            message.setOrigin(new Address());
            message.getOrigin().setLayer(Address.LAYER_APPLICATION);
            message.getOrigin().setUid(getDeviceManager().getBackendService().getApplicationUID());
        }
        MessageWrapper messageWrapper = new MessageWrapper(message);
        try {
            // 2 - Cria o envelope XML da UrboSenti correspondente da mensagem
            messageWrapper.build();
        } catch (ParserConfigurationException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (TransformerConfigurationException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (TransformerException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // 3 - Verifica se alguma interface de comunicação está disponível
        CommunicationInterface ci = this.getCommunicationInterfaceWithConnection(); // Método traz a interface de comunicação atual
        // 4 - [Nenhuma disponível]    
        if (ci == null) {
            // Evento desconexão
            this.newInternalEvent(EVENT_DISCONNECTION);
            // Retorna false (ou o erro)
            return false;
        }
        try {
            // 5 - Tenta enviar --- OBS.: Implementar
            ci.sendMessage(this, messageWrapper);
            //[Sucesso]
            // Evento: Mensagem Entregue
            this.newInternalEvent(EVENT_MESSAGE_DELIVERED, messageWrapper, message.getTarget(), currentCommunicationInterface);
            // Returna true para a aplicação
            return true;
        } catch (SocketTimeoutException ex) {
            //[Endereço não acessível]
            // Evento: Endereço não acessível
            // throws host unknown exception
            //[Timeout]
            // Evento: Timeout
            // thows Timeout exception   
            this.newInternalEvent(EVENT_ADDRESS_NOT_REACHABLE, messageWrapper, message.getTarget(), ci);
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw ex;
        } catch (java.net.ConnectException ex) {
            // Evento: Timeout
            // thows Timeout exception   
            this.newInternalEvent(EVENT_ADDRESS_NOT_REACHABLE, messageWrapper, message.getTarget(), ci);
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw ex;
        } catch (IOException ex) {
            //[Erro de IO]
            // Evento: Mensagem não entregue
            // throws Excessção d IO
            this.newInternalEvent(EVENT_MESSAGE_NOT_DELIVERED, messageWrapper, message.getTarget());
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw ex;
        }
    }

    public String sendMessageWithResponse(Message message) throws SocketTimeoutException, java.net.ConnectException, IOException {

        // 1 - Recebe a mensagem - Gets the message
        /*
         * Se quem está enviando não foi explicitado, então, por padrão, são preenxidos os dados do envio da aplicação.
         */
        if (message.getOrigin() == null) {
            message.setOrigin(new Address());
            message.getOrigin().setLayer(Address.LAYER_APPLICATION);
            message.getOrigin().setUid(getDeviceManager().getBackendService().getApplicationUID());
        }
        // Adiciona na mensagem que ele requer resposta
        message.setRequireResponse(true);
        MessageWrapper messageWrapper = new MessageWrapper(message);
        try {
            // 2 - Cria o envelope XML da UrboSenti correspondente da mensagem
            messageWrapper.build();
        } catch (ParserConfigurationException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (TransformerConfigurationException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (TransformerException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // 3 - Verifica se alguma interface de comunicação está disponível
        CommunicationInterface ci = this.getCommunicationInterfaceWithConnection(); // Método traz a interface de comunicação atual
        // 4 - [Nenhuma disponível]    
        if (ci == null) {
            // Evento desconexão
            this.newInternalEvent(EVENT_DISCONNECTION);
            // Retorna false (ou o erro)
            return null;
        }
        try {
            // 5 - Tenta enviar --- OBS.: Implementar
            String response = (String) ci.sendMessageWithResponse(this, messageWrapper);
            // se não conseguir tenta por outro
            HashMap<String, String> contents = processEnvelope(response);
            messageWrapper.setServiceProcessingTime(Long.parseLong(contents.get("processingTime")));
            // evento de mensagem entregue
            this.newInternalEvent(EVENT_MESSAGE_DELIVERED, messageWrapper, message.getTarget(), currentCommunicationInterface);
            // retorna resultado
            return contents.get("content");
        } catch (SocketTimeoutException ex) {
            //[Endereço não acessível]
            // Evento: Endereço não acessível
            // throws host unknown exception
            //[Timeout]
            // Evento: Timeout
            // thows Timeout exception   
            this.newInternalEvent(EVENT_ADDRESS_NOT_REACHABLE, messageWrapper, message.getTarget(), ci);
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw ex;
        } catch (java.net.ConnectException ex) {
            // Evento: Timeout
            // thows Timeout exception   
            this.newInternalEvent(EVENT_ADDRESS_NOT_REACHABLE, messageWrapper, message.getTarget(), ci);
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw ex;
        } catch (IOException ex) {
            //[Erro de IO]
            // Evento: Mensagem não entregue
            // throws Excessção d IO
            this.newInternalEvent(EVENT_MESSAGE_NOT_DELIVERED, messageWrapper, message.getTarget());
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw ex;
        }
    }

    public String sendMessageWithResponse(Message message, int timeout) throws SocketTimeoutException, java.net.ConnectException, IOException {

        // 1 - Recebe a mensagem - Gets the message
        /*
         * Se quem está enviando não foi explicitado, então, por padrão, são preenxidos os dados do envio da aplicação.
         */
        if (message.getOrigin() == null) {
            message.setOrigin(new Address());
            message.getOrigin().setLayer(Address.LAYER_APPLICATION);
            message.getOrigin().setUid(getDeviceManager().getBackendService().getApplicationUID());
        }
        MessageWrapper messageWrapper = new MessageWrapper(message);
        messageWrapper.setTimeout(timeout);
        try {
            // 2 - Cria o envelope XML da UrboSenti correspondente da mensagem
            messageWrapper.build();
        } catch (ParserConfigurationException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (TransformerConfigurationException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (TransformerException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // 3 - Verifica se alguma interface de comunicação está disponível
        CommunicationInterface ci = this.getCommunicationInterfaceWithConnection(); // Método traz a interface de comunicação atual
        // 4 - [Nenhuma disponível]    
        if (ci == null) {
            // Evento desconexão
            this.newInternalEvent(EVENT_DISCONNECTION);
            // Retorna false (ou o erro)
            return null;
        }
        try {
            // 5 - Tenta enviar --- OBS.: Implementar
            String response = (String) ci.sendMessageWithResponse(this, messageWrapper);
            // se não conseguir tenta por outro
            HashMap<String, String> contents = processEnvelope(response);
            messageWrapper.setServiceProcessingTime(Long.parseLong(contents.get("processingTime")));
            // evento de mensagem entregue
            this.newInternalEvent(EVENT_MESSAGE_DELIVERED, messageWrapper, message.getTarget(), currentCommunicationInterface);
            // retorna resultado
            return contents.get("content").toString();
        } catch (SocketTimeoutException ex) {
            //[Endereço não acessível]
            // Evento: Endereço não acessível
            // throws host unknown exception
            //[Timeout]
            // Evento: Timeout
            // thows Timeout exception   
            this.newInternalEvent(EVENT_ADDRESS_NOT_REACHABLE, messageWrapper, message.getTarget(), ci);
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw ex;
        } catch (java.net.ConnectException ex) {
            // Evento: Timeout
            // thows Timeout exception   
            this.newInternalEvent(EVENT_ADDRESS_NOT_REACHABLE, messageWrapper, message.getTarget(), ci);
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw ex;
        } catch (IOException ex) {
            //[Erro de IO]
            // Evento: Mensagem não entregue
            // throws Excessção d IO
            this.newInternalEvent(EVENT_MESSAGE_NOT_DELIVERED, messageWrapper, message.getTarget());
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw ex;
        }
    }

    /**
     *
     * @param message Adiciona um report para envio ao servidor.
     */
    public void addReportToSend(Message message) throws SQLException, Exception {
        // se não possui algo então o alvo é o servidor base
        if (message.getTarget() == null) {
            backendUploadService.sendAssynchronousReport(message);
        } else {
            boolean found = false;
            // procura o upload service para enviar
            for (UploadService us : uploadServices) {
                if (us.getService().getServiceUID().equals(message.getTarget().getUid())) {
                    us.sendAssynchronousReport(message);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new Exception("Upload service not found.");
            }
        }
    }

    /**
     * Recebe a mensagem de alguma origem; O GCM deve enviar o endereço de
     * origem como parâmetro separado para colocar no parâmetro. Nesta versão
     * somente suporta dados em formato texto. Futuramente outros formatos podem
     * ser considerados.
     *
     * @param originAddress -- Contem o endereço de quem enviou no formato
     * ip:porta ou um host
     * @param bruteMessage -- Contem a mensagem em formato de texto.
     */
    public void newPushMessage(String originAddress, String bruteMessage) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            // Tratar dados substituindo lixos
            bruteMessage = bruteMessage.replace("&gt;", ">");
            bruteMessage = bruteMessage.replace("&lt;", "<");
            // Criar o documento e com verte a String em DOC 
            Document doc = builder.parse(new InputSource(new StringReader(bruteMessage)));
            // Cria a mensagem para passar para o sistema
            Message msg = new Message();
            msg.setOrigin(new Address());
            msg.setTarget(new Address());
            // Verifica se o endereço de origem foi atribuído
            if (originAddress.length() > 0) {
                msg.getOrigin().setAddress(originAddress);
            } else {
                throw new Error("Origin address does not specified!");
            }
            // Acessa o elemento raiz para processar o XML
            // <message requireResponse="false">
            Element response = doc.getDocumentElement();
            // requireResponse
            if (response.hasAttribute("requireResponse")) {
                msg.setRequireResponse(response.getAttribute("requireResponse").equals("true"));
            }
            //<header>
            Element header = (Element) response.getElementsByTagName("header").item(0);

            // <origin> -> <uid>
            msg.getOrigin().setUid(((Element) header.getElementsByTagName("origin").item(0)).getElementsByTagName("uid").item(0).getTextContent());
            // <origin> -> <layer>
            msg.getOrigin().setLayer(Integer.parseInt(((Element) header.getElementsByTagName("origin").item(0)).getElementsByTagName("layer").item(0).getTextContent()));
            // <target> -> <uid>
            msg.getTarget().setUid(((Element) header.getElementsByTagName("target").item(0)).getElementsByTagName("uid").item(0).getTextContent());
            // <target> -> <layer>
            msg.getTarget().setLayer(Integer.parseInt(((Element) header.getElementsByTagName("target").item(0)).getElementsByTagName("layer").item(0).getTextContent()));

            // <priority>
            if (header.getElementsByTagName("priority").getLength() > 0) {
                if (header.getElementsByTagName("priority").item(0).getTextContent().equals("preferential")) {
                    msg.setPreferentialPriority();
                } else {
                    msg.setNormalPriority();
                }
            }
            //<subject>
            msg.setSubject(Integer.parseInt(header.getElementsByTagName("subject").item(0).getTextContent()));
            //<contentType>
            msg.setContentType(header.getElementsByTagName("contentType").item(0).getTextContent());
            //<contentSize> -- utilizado somente para comparar;

            //<anonymousUpload>
            if (header.getElementsByTagName("anonymousUpload").getLength() > 0) {
                msg.setAnonymousUpload(Boolean.parseBoolean(header.getElementsByTagName("anonymousUpload").item(0).getTextContent()));
            } else {
                msg.setAnonymousUpload(false);
            }
            // converter elemento <content> para String
            StringWriter stw = new StringWriter();
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.transform(new DOMSource(response.getElementsByTagName("content").item(0)), new StreamResult(stw));
            // <content> - conteÃºdo da mensagem
            msg.setContent(stw.getBuffer().toString());

            if (DeveloperSettings.SHOW_FUNCTION_DEBUG_ACTIVITY) {
                System.out.println("Reveived message layer: " + msg.getTarget().getLayer());
            }
            // Evento - Mensagem Recebida
            this.newInternalEvent(EVENT_MESSAGE_RECEIVED, msg.getOrigin(), msg);

        } catch (ParserConfigurationException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.newInternalEvent(EVENT_MESSAGE_RECEIVED_INVALID_FORMAT, originAddress, bruteMessage);
        } catch (SAXException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.newInternalEvent(EVENT_MESSAGE_RECEIVED_INVALID_FORMAT, originAddress, bruteMessage);
        } catch (IOException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.newInternalEvent(EVENT_MESSAGE_RECEIVED_INVALID_FORMAT, originAddress, bruteMessage);
        } catch (TransformerException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.newInternalEvent(EVENT_MESSAGE_RECEIVED_INVALID_FORMAT, originAddress, bruteMessage);
        }

    }

    /**
     * @param messageWrapper contém a mensagem e mais informaçÃµes que podem ser
     * enviadas
     *
     * Políticas: <\br>
     * 1) Não armazenar nenhuma. <\br>
     * 2) Apagar todas que foram enviadas com sucesso e armazenar as que não
     * foram enviadas. Opção padrão. <\br>
     * 3) Armazenar todas e deixar a aplicação decidir quais apagar. <\br>
     * 4) Dinâmico (Exige componente de adaptação). Dá poder ao mecanismo
     * decidir quando apagar uma mensagem armazenada. O usuário pode especificar
     * uma quantidade ou um tempo.
     * @throws java.sql.SQLException
     */
    protected synchronized void storagePolice(MessageWrapper messageWrapper, Service service) throws SQLException {
        switch (messageStoragePolicy) {
            case 1: // Não armazenar nenhuma
                break;
            case 2: // Apagar todas que foram enviadas com sucesso e armazenar as que não foram enviadas. Opção padrão.
                if (messageWrapper.isSent()) { // Não foi enviada. Armazenar
                    getDeviceManager().getDataManager().getReportDAO().delete(messageWrapper);
                } else { // senão foi enviada. Armazenar
                    if (messageWrapper.getId() > 0) {
                        getDeviceManager().getDataManager().getReportDAO().update(messageWrapper);
                    } else {
                        getDeviceManager().getDataManager().getReportDAO().insert(messageWrapper, service);
                    }
                }
                break;
            case 3: // Armazenar todas e deixar a aplicação decidir quais apagar.
                if (messageWrapper.getId() > 0) {
                    getDeviceManager().getDataManager().getReportDAO().update(messageWrapper);
                } else {
                    getDeviceManager().getDataManager().getReportDAO().insert(messageWrapper, service);
                }
                break;
            case 4: // Dinâmico (Exige componente de adaptação). Dá poder ao mecanismo decidir quando apagar uma mensagem armazenada. O usuário pode especificar uma quantidade ou um tempo.
                if (messageWrapper.getId() > 0) {
                    getDeviceManager().getDataManager().getReportDAO().update(messageWrapper);
                } else {
                    getDeviceManager().getDataManager().getReportDAO().insert(messageWrapper, service);
                    // Gerar evento -- Mensagem armazenada.
                    this.newInternalEvent(EVENT_MESSAGE_STORED, messageWrapper);
                }
                break;
        }
    }

    /**
     *
     * @param message - Mensagem aprovada
     * @return return true se a mensagem foi atualizada com sucesso. Caso ela
     * não esteja mais na lista o der algum erro será retornado false
     * @throws java.sql.SQLException
     */
    public synchronized boolean approvalReport(Message message) throws SQLException {
        // buscar relato
        MessageWrapper report = super.getDeviceManager().getDataManager().getReportDAO().get(message.getCreatedTime());
        // atualizar para checado
        super.getDeviceManager().getDataManager().getReportDAO().updateChecked(report);
        return true;
    }

    /**
     * Função que gera os eventos. Cada evento possui um descritor que define os
     * parâmetros necessários
     * <br><br>Eventos possíveis ordenador pelo ID do evento - descrição do
     * evento
     * <ul>
     * <li>CommunicationManager.EVENT_INTERFACE_DISCONNECTION - Interface
     * Desconectada</li>
     * <li>CommunicationManager.EVENT_MESSAGE_DELIVERED - Mensagem Entregue;
     * (Opcional) UploadService</li>
     * <li>CommunicationManager.EVENT_MESSAGE_NOT_DELIVERED - Mensagem não
     * Entregue; (Opcional) UploadService</li>
     * <li>CommunicationManager.EVENT_MESSAGE_RECEIVED - Mensagem recebida</li>
     * <li>CommunicationManager.EVENT_MESSAGE_RECEIVED_INVALID_FORMAT - Mensagem
     * recebida em formato inválido</li>
     * <li>CommunicationManager.EVENT_ADDRESS_NOT_REACHABLE - Endereço não
     * acessível; (Opcional) UploadService</li>
     * <li>CommunicationManager.EVENT_DISCONNECTION - Desconexão geral</li>
     * <li>CommunicationManager.EVENT_RESTORED_CONNECTION - Conexão
     * reestabelecida</li>
     * <li>CommunicationManager.EVENT_REPORT_AWAITING_APPROVAL - Relato
     * esperando Aprovação</li>
     * <li>CommunicationManager.EVENT_MESSAGE_STORED - Mensagem Armazenada</li>
     * <li>@see #CommunicationManager.EVENT_MESSAGE_STORED_REMOVED - Mensagem
     * Removida</li>
     * <li>CommunicationManager.EVENT_NEW_INPUT_COMMUNICATION_INTERFACE_ADDRESS
     * - novo endereço da interface de comunicação de entrada</li>
     * <li>CommunicationManager.EVENT_NEW_RECONNECTION_ATTEMPT - Serviço de
     * reconexão</li>
     * <li>CommunicationManager.EVENT_NEW_START_OF_UPLOAD_SERVICE_FUNCTION_LOOP
     * - Serviço de upload</li>
     * </ul>
     *
     *
     * @param eventId
     * @param parameters
     * @see #EVENT_INTERFACE_DISCONNECTION
     * @see #EVENT_MESSAGE_DELIVERED
     * @see #EVENT_MESSAGE_NOT_DELIVERED
     * @see #EVENT_MESSAGE_RECEIVED
     * @see #EVENT_MESSAGE_RECEIVED_INVALID_FORMAT
     * @see #EVENT_ADDRESS_NOT_REACHABLE
     * @see #EVENT_DISCONNECTION
     * @see #EVENT_CONNECTION_RESTORED
     * @see #EVENT_REPORT_AWAITING_APPROVAL
     * @see #EVENT_MESSAGE_STORED
     * @see #EVENT_MESSAGE_STORED_REMOVED
     * @see #EVENT_NEW_INPUT_COMMUNICATION_INTERFACE_ADDRESS
     * @see #EVENT_NEW_RECONNECTION_ATTEMPT
     * @see #EVENT_NEW_START_OF_UPLOAD_SERVICE_FUNCTION_LOOP
     */
    protected synchronized void newInternalEvent(int eventId, Object... parameters) {
        Address address;
        CommunicationInterface ci;
        MessageWrapper mw;
        Event event = null;
        Message msg;
        HashMap<String, Object> values;
        String bruteMessage;
        switch (eventId) {
            case EVENT_INTERFACE_DISCONNECTION: // 1 - Desconexão - parâmetros: Interface desconectada + (optional) instanceId(reconectionService)
                ci = (CommunicationInterface) parameters[0];
                // cria o evento
                event = new SystemEvent(this);// Event: new Message
                event.setId(1);
                event.setName("Desconexão");
                event.setTime(new Date());

                // Adiciona os valores que serão passados para serem tratados
                values = new HashMap<String, Object>();
                values.put("interface", ci);
                if (parameters.length > 1) {
                    values.put("reconnectionService", parameters[1]);
                    event.setEntityId(CommunicationDAO.ENTITY_ID_OF_RECONNECTION);
                } else {
                    event.setEntityId(CommunicationDAO.ENTITY_ID_OF_SENDING_MESSAGES);
                }
                event.setParameters(values);

                // envia o evento
                getEventManager().newEvent(event);

                break;
            case EVENT_MESSAGE_DELIVERED: // 2 - Mensagem Entregue - parâmetros: Mensagem; Destinatário; Interface; (Opcional) UploadService
                mw = (MessageWrapper) parameters[0];
                address = (Address) parameters[1];
                ci = (CommunicationInterface) parameters[2];

                // Adiciona os valores que serão passados para serem tratados
                values = new HashMap<String, Object>();
                values.put("messageWrapper", mw);
                values.put("target", address);
                values.put("interface", ci);
                if (parameters.length > 3) {
                    values.put("uploadServiceId", parameters[3]);
                }

                // cria o evento
                event = new SystemEvent(this);// Event: new Message
                event.setId(2);
                event.setName("Mensagem entregue");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(CommunicationDAO.ENTITY_ID_OF_SENDING_MESSAGES);
                if (parameters.length > 3) {
                    event.setEntityId(CommunicationDAO.ENTITY_ID_OF_SERVICE_OF_UPLOAD_REPORTS);
                }

                // envia o evento
                getEventManager().newEvent(event);

                break;
            case EVENT_MESSAGE_NOT_DELIVERED: // 3 - Mensagem Não Entregue - parâmetros: Mensagem; Destinatário; (Opcional) UploadService
                mw = (MessageWrapper) parameters[0];
                address = (Address) parameters[1];

                // Adiciona os valores que serão passados para serem tratados
                values = new HashMap<String, Object>();
                values.put("messageWrapper", mw);
                values.put("target", address);
                if (parameters.length > 2) {
                    values.put("uploadServiceId", parameters[2]);
                }

                // cria o evento de Sistema
                event = new SystemEvent(this);// Event: new Message
                event.setId(3);
                event.setName("Mensagem não entregue");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(CommunicationDAO.ENTITY_ID_OF_SENDING_MESSAGES);
                if (parameters.length > 2) {
                    event.setEntityId(CommunicationDAO.ENTITY_ID_OF_SERVICE_OF_UPLOAD_REPORTS);
                }

                // Se foi enviado para aplicação avisa também a aplicação
                if (mw.getMessage().getOrigin().getLayer() == Address.LAYER_APPLICATION) {
                    // cria o evento de Aplicação
                    event = new ApplicationEvent(this);// Event: new Message
                    event.setId(3);
                    event.setName("Mensagem não entregue");
                    event.setTime(new Date());
                    event.setParameters(values);
                }

                // envia o evento
                getEventManager().newEvent(event);
                break;

            case EVENT_MESSAGE_RECEIVED: // 4 - Mensagem Recebida - parâmetros: Origem; Mensagem
                address = (Address) parameters[0];
                msg = (Message) parameters[1];

                if (msg != null) {
                    if (msg.getTarget().getLayer() == Address.LAYER_SYSTEM) { // if the target is the system
                        event = new SystemEvent(this);
                        // adiciona no evento que ele é um evento de interação
                        event.setEventType(Event.INTERATION_EVENT);
                    } else if (msg.getTarget().getLayer() == Address.LAYER_APPLICATION) { // if the target is the application
                        event = new ApplicationEvent(this);
                    }

                    // Adiciona os valores que serão passados para serem tratados
                    values = new HashMap<String, Object>();
                    values.put("message", msg);
                    values.put("sender", address);

                    // Event: new Message
                    if (event != null) {
                        event.setId(4);
                        event.setName("new message");
                        event.setTime(new Date());
                        event.setParameters(values);
                        event.setEntityId(CommunicationDAO.ENTITY_ID_OF_INPUT_COMMUNICATION_INTERFACES);

                        // envia o evento
                        getEventManager().newEvent(event);
                    }
                }
                break;

            case EVENT_MESSAGE_RECEIVED_INVALID_FORMAT: // 5 - Mensagem Recebida em Formato Inválido - parâmetros: Origem; Mensagem
                address = (Address) parameters[0];
                bruteMessage = (String) parameters[1];

                // Adiciona os valores que serão passados para serem tratados
                values = new HashMap<String, Object>();
                values.put("message", bruteMessage);
                values.put("sender", address);

                // Event: new Message
                event = new SystemEvent(this);

                event.setId(5);
                event.setName("Message received with invalid format");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(CommunicationDAO.ENTITY_ID_OF_INPUT_COMMUNICATION_INTERFACES);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_ADDRESS_NOT_REACHABLE: // 6 - Endereço não acessível - Mensagem; Destinatário; Interface; (Opcional) UploadService
                address = (Address) parameters[1];
                mw = (MessageWrapper) parameters[0];
                ci = (CommunicationInterface) parameters[2];

                // Adiciona os valores que serão passados para serem tratados
                values = new HashMap<String, Object>();
                values.put("messageWrapper", mw);
                values.put("interface", ci);
                values.put("target", address);
                if (parameters.length > 3) {
                    values.put("uploadServiceId", parameters[3]);
                }

                // Event: new Message
                if (mw.getMessage().getOrigin().getLayer() == Address.LAYER_SYSTEM) {
                    event = new SystemEvent(this);
                } else {
                    event = new ApplicationEvent(this);
                }

                event.setId(6);
                event.setName("Address not reachable");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(CommunicationDAO.ENTITY_ID_OF_SENDING_MESSAGES);
                if (parameters.length > 3) {
                    event.setEntityId(CommunicationDAO.ENTITY_ID_OF_SERVICE_OF_UPLOAD_REPORTS);
                }

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_DISCONNECTION: // 7 - Desconexão  geral - parâmetros: nenhum

                // Event
                event = new SystemEvent(this);

                event.setId(7);
                event.setName("The device was completely disconnected");
                event.setTime(new Date());
                event.setEntityId(CommunicationDAO.ENTITY_ID_OF_SENDING_MESSAGES);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_CONNECTION_RESTORED: // 8 - Conexão Reestabelecida - parâmetros: interface + (optional) instanceId(reconectionService)
                ci = (CommunicationInterface) parameters[0];

                // Adiciona os valores que serão passados para serem tratados
                values = new HashMap<String, Object>();
                values.put("interface", ci);
                if (parameters.length > 1) {
                    values.put("reconnectionService", parameters[1]);
                }

                // Event: new Message
                event = new SystemEvent(this);

                event.setId(8);
                event.setName("The interface connection was restored");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(CommunicationDAO.ENTITY_ID_OF_RECONNECTION);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_REPORT_AWAITING_APPROVAL: // 9 - Relato Esperando Aprovação  - parâmetros: mensagem
                msg = (Message) parameters[0];

                event = new ApplicationEvent(this);

                // Adiciona os valores que serão passados para serem tratados
                values = new HashMap<String, Object>();
                values.put("message", msg);

                event.setId(9);
                event.setName("Report awaiting approval");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(CommunicationDAO.ENTITY_ID_OF_SERVICE_OF_UPLOAD_REPORTS);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_MESSAGE_STORED:
                mw = (MessageWrapper) parameters[0];

                event = new SystemEvent(this);

                // Adiciona os valores que serão passados para serem tratados
                values = new HashMap<String, Object>();
                values.put("messageWrapper", mw);

                event.setId(10);
                event.setName("Message Stored");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(CommunicationDAO.ENTITY_ID_OF_REPORTS_STORAGE);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_MESSAGE_STORED_REMOVED:
                mw = (MessageWrapper) parameters[0];

                event = new SystemEvent(this);

                // Adiciona os valores que serão passados para serem tratados
                values = new HashMap<String, Object>();
                values.put("messageWrapper", mw);

                event.setId(11);
                event.setName("Stored message was removed");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(CommunicationDAO.ENTITY_ID_OF_REPORTS_STORAGE);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_NEW_INPUT_COMMUNICATION_INTERFACE_ADDRESS:
                PushServiceReceiver receiver = (PushServiceReceiver) parameters[0];
                HashMap<String, String> configurations = (HashMap<String, String>) parameters[1];

                event = new SystemEvent(this);

                // Adiciona os valores que serão passados para serem tratados
                values = new HashMap<String, Object>();
                values.put("interface", receiver);
                values.put("configurations", configurations);
                event.setEntityId(CommunicationDAO.ENTITY_ID_OF_INPUT_COMMUNICATION_INTERFACES);

                event.setId(15);
                event.setName("The receiver's communication interfacer has changed the address configuration");
                event.setTime(new Date());
                event.setParameters(values);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_NEW_RECONNECTION_ATTEMPT:
                event = new SystemEvent(this);

                // Adiciona os valores que serão passados para serem tratados
                values = new HashMap<String, Object>();
                values.put("reconnectionService", parameters[0]);

                event.setId(16);
                event.setName("New attempt to reconnect on communicationinterface by reconnection service");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(CommunicationDAO.ENTITY_ID_OF_RECONNECTION);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_NEW_START_OF_UPLOAD_SERVICE_FUNCTION_LOOP:
                event = new SystemEvent(this);

                // Adiciona os valores que serão passados para serem tratados
                values = new HashMap<String, Object>();
                values.put("uploadServiceId", parameters[0]);

                event.setId(17);
                event.setName("New start of upload service function loop");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(CommunicationDAO.ENTITY_ID_OF_SERVICE_OF_UPLOAD_REPORTS);

                // envia o evento
                getEventManager().newEvent(event);
                break;
        }
        // inserir métricas e medidas no evento
    }

    public CommunicationInterface getCurrentCommunicationInterface() {
        return currentCommunicationInterface;
    }

    /**
     * ReconectionService informa que interface conseguiu reconectar-se com
     * sucesso.
     *
     * @param current
     */
    void notifyReconnection(CommunicationInterface current, ReconnectionService reconnectionService) {
        this.newInternalEvent(EVENT_CONNECTION_RESTORED, current, reconnectionService);
        current.setStatus(CommunicationInterface.STATUS_CONNECTED);
        // se continua desconectado coloca a interface como primeira.
        synchronized (this) {
            if (completelyDisconnected) {
                this.completelyDisconnected = false;
                this.currentCommunicationInterface = current;
                this.notifyAllUploadServices();
            }
        }
    }

    /**
     * ReconectionService informa que interface não conseguiu reconectar-se com
     * sucesso.
     *
     * @param current
     */
    void notifyReconnectionNotSucceed(CommunicationInterface current, ReconnectionService reconnectionService) {
        this.newInternalEvent(EVENT_INTERFACE_DISCONNECTION, current, reconnectionService);
    }

    void notifyNewAttemptToReconnect(ReconnectionService reconnectionService) {
        this.newInternalEvent(EVENT_NEW_RECONNECTION_ATTEMPT, reconnectionService);
    }

    /**
     * NOtifica todos os serviços de upload
     */
    public synchronized void notifyAllUploadServices() {
        for (UploadService up : uploadServices) {
            up.wakeUp();
        }
        notifyAll();
    }

    /**
     *
     * @return retorna se o serviço de upload automático está em funcionamento
     */
    public synchronized boolean isBackendUploadServerRunning() {
        return backendUploadService.isRunning();
    }

    /**
     * Método retorna uma Interface de comunicação disponível
     * (CommunicationInterface). Se nenhuma estiver disponível então retorna
     * null.
     *
     * @return CommunicationInterface or null if no one interface is available
     */
    private synchronized CommunicationInterface getCommunicationInterfaceWithConnection() {
        // Se há uma interface atual testa se ela possuí conexão
        if (currentCommunicationInterface != null) {
            if (currentCommunicationInterface.getStatus() != CommunicationInterface.STATUS_UNAVAILABLE
                    && currentCommunicationInterface.getStatus() != CommunicationInterface.STATUS_DISCONNECTED) {
                try {
                    if (currentCommunicationInterface.testConnection()) {
                        return currentCommunicationInterface;
                    }
                } catch (IOException ex) {
                    if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                        Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (UnsupportedOperationException ex) {
                    if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                        Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        // Testa se outra interface possuí conexão, se sim ela passa a ser a atual e é retornada
        for (CommunicationInterface ci : communicationInterfaces) {
            if (currentCommunicationInterface.getStatus() != CommunicationInterface.STATUS_UNAVAILABLE
                    && currentCommunicationInterface.getStatus() != CommunicationInterface.STATUS_DISCONNECTED) {
                try {
                    if (ci.testConnection()) {
                        currentCommunicationInterface = ci;
                        return ci;
                    }
                } catch (IOException ex) {
                    if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                        Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (UnsupportedOperationException ex) {
                    if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                        Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        return null;
    }

    /**
     *
     * @param service Adiciona o servidor que a função de upload dinâmico vai
     * reportar.
     */
    public void addUploadServer(UploadService service) {
        // se já está na lista não adiciona
        for (UploadService us : this.uploadServices) {
            if (service.getService().getId() == us.getService().getId()) {
                return;
            }
        }
        this.uploadServices.add(service);
        if (service.getService().getId() == getDeviceManager().getBackendService().getId()) {
            this.backendUploadService = service;
        }
    }

    /**
     * Este método é responsável por executar o processo de upload automático de
     * relatos. Idealmente, no futuro, poderia ser colocado em uma classe
     * separada que faça esse papel , o que permitiria fazer upload para
     * multiplos servidores, onde o run dessa classe inicia os servidores.
     *
     * @param server contém o agente que será usado de servidor.
     *
     */
    protected void uploadServiceFunction(Service server, UploadService uploadService, Long uploadInterval, Double uploadRate, int limitOfReportsSentByUploadInterval) throws InterruptedException {
        final Integer waitUpload = 0;
        int reportsCountSentByUploadInterval = 0; // Contagem de relatos enviados por intervalo, inicial 0
        // [Início Loop]
        // Aguarda condição da política de upload ser atendida
        switch (this.uploadMessagingPolicy) {
            case 1: // Sempre que há um relato novo tenta fazer o upload, caso exista conexão, senão espera reconexão. Padrão.
                break;
            case 2: // Em intervalos fixos. Pode ser definido pela aplicação. Intervalo inicial padrão a cada 15 segundos. Se não há conexão as mensagens são armazenadas.
                // enquanto a quantidade de relatos por intervalo não for atendida, somente soma, senão zera a contagem e espera o tempo do intervalo
                if (reportsCountSentByUploadInterval == 0 && reportsCountSentByUploadInterval < limitOfReportsSentByUploadInterval) {
                    reportsCountSentByUploadInterval = 0;
                    synchronized (waitUpload) {
                        waitUpload.wait(uploadInterval);
                    }
                } else {
                    reportsCountSentByUploadInterval++;
                }
                break;
            case 3: // Está implementada no método ADDMessage. Exige confirmação da aplicação para upload dos relatos. Enquanto não confirmada comportamento na política 1.  
                break;
            case 4: // Adaptativo. O componente de adaptação irá atribuir dinamicamente novos intervalos.
                // Se a taxa de upload for menor que 1 então verificar se nessse intervalo será necessário fazer o upload
                if (uploadRate < 1.0 && reportsCountSentByUploadInterval == 0) {
                    while (true) {
                        if (uploadRate <= Math.random()) { // Testa se é necessário fazer o upload, caso não, espera mais um intervalo
                            synchronized (waitUpload) {
                                waitUpload.wait(uploadInterval);
                            }
                        } else {
                            synchronized (waitUpload) {
                                waitUpload.wait(uploadInterval);
                            }
                            reportsCountSentByUploadInterval++;
                            break;
                        }
                    }
                } else {
                    // enquanto a quantidade de relatos por intervalo não for atendida, somente soma, senão zera a contagem e espera o tempo do intervalo
                    if (reportsCountSentByUploadInterval == 0 && reportsCountSentByUploadInterval < limitOfReportsSentByUploadInterval) {
                        reportsCountSentByUploadInterval = 0;
                        synchronized (waitUpload) {
                            waitUpload.wait(uploadInterval);
                        }
                    } else {
                        // Soma até atingir limite de relatos
                        reportsCountSentByUploadInterval++;
                    }
                }
                break;
        }
        // Busca uma mensagem da fila para a upload
        // Aguarda condiçÃµes de dados móveis [To do - implement the mobile data policy] - Serviço compartilhado entre os servers
        MessageWrapper mw;
        try {
            mw = uploadService.getReportByMobileDataPolicyCriteria();
            // construir a mensagem
            mw.build();
            // 3 - Verifica se alguma interface de comunicação está disponível
            CommunicationInterface ci = this.getCommunicationInterfaceWithConnection(); // Método traz a interface de comunicação atual
            // 4 - [disponível]
            if (ci != null) {
                try {
                    // Tempo inicial de envio
                    Date initialTime = new Date();
                    // Executa função SendMessage
                    ci.sendMessage(this, mw);
                    // Marca como mensagem enviada e algumas métricas
                    ci.updateEvaluationMetrics(mw, initialTime);
                    // Evento: Mensagem Entregue
                    this.newInternalEvent(EVENT_MESSAGE_DELIVERED, mw, mw.getMessage().getTarget(), currentCommunicationInterface, uploadService.getInstance().getModelId());
                    // Política de Armazenamento
                    this.storagePolice(mw, server);
                    // confirmação do funcionamento sem erros
                    ci.setStatus(CommunicationInterface.STATUS_CONNECTED);
                    // this.newInternalEvent(EVENT_SERVICE_FUNCTIONS_END, CommunicationDAO.UPLOAD_MESSAGING_POLICY, uploadServer);
                } catch (java.net.ConnectException ex) {
                    // Evento: Timeout
                    // thows Timeout exception   
                    this.newInternalEvent(EVENT_ADDRESS_NOT_REACHABLE, mw, mw.getMessage().getTarget(), currentCommunicationInterface, uploadService.getInstance().getModelId());
                    if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                        Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    ci.setStatus(CommunicationInterface.STATUS_DISCONNECTED);
                } catch (SocketTimeoutException ex) {
                    //[Timeout]
                    // Evento: Timeout
                    this.newInternalEvent(EVENT_ADDRESS_NOT_REACHABLE, mw, mw.getMessage().getTarget(), currentCommunicationInterface, uploadService.getInstance().getModelId());
                    if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                        Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    ci.setStatus(CommunicationInterface.STATUS_DISCONNECTED);
                } catch (IOException ex) {
                    //[Erro de IO]
                    // Evento: Mensagem não entregue
                    this.newInternalEvent(EVENT_MESSAGE_NOT_DELIVERED, mw, mw.getMessage().getTarget(), uploadService.getInstance().getModelId());
                    if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                        Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    ci.setStatus(CommunicationInterface.STATUS_DISCONNECTED);
                }
            } else {
                // Evento desconexão
                this.newInternalEvent(EVENT_DISCONNECTION);
                this.completelyDisconnected = true;
                // [Sem sucesso] [IO Exception] [Timeout]
                // [Aguarda Política de Reconexão]        
                System.out.println("All functions communication interfaces are disconnected. Starting Reconection Service");
                //this.gerenalReconnectionService.setReconnectionMethodAllByOnce();
                this.gerenalReconnectionService.requireConnectionTest();
                //this.gerenalReconnectionService.reconectionProcess();
                // espera conectar
                synchronized (this) {
                    while (isCompletelyDisconnected()) {
                        wait();
                    }
                }
            }
        } catch (SQLException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            // criar
            //this.newInternalEvent(EVENT_SERVICE_FUNCTION_ERROR, CommunicationDAO.UPLOAD_MESSAGING_POLICY, ex ,uploadServer);// fazer depois
        } catch (ParserConfigurationException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            // criar
            // this.newInternalEvent(EVENT_SERVICE_FUNCTION_ERROR, CommunicationDAO.UPLOAD_MESSAGING_POLICY, ex ,uploadServer);// fazer depois
        } catch (TransformerException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            // criar
            // this.newInternalEvent(EVENT_SERVICE_FUNCTION_ERROR, CommunicationDAO.UPLOAD_MESSAGING_POLICY, ex ,uploadServer);// fazer depois
        }

        // [Fim Loop]
    }

    public void addPushServiceReceiver(PushServiceReceiver receiver) {
        this.pushServiceReveivers.add(receiver);
    }

    private HashMap<String, String> processEnvelope(String response) {
        try {
            HashMap<String, String> map = new HashMap();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            // Criar o documento e com verte a String em DOC 
            Document doc = builder.parse(new InputSource(new StringReader(response)));
            // Acessa o elemento raiz para processar o XML
            // <message>
            Element message = doc.getDocumentElement();
            //<header>
            Element header = (Element) message.getElementsByTagName("header").item(0);

            // <header> -> <contentType>
            map.put("contentType", header.getElementsByTagName("contentType").item(0).getTextContent());
            // <header> -> <contentSize>
            map.put("contentSize", header.getElementsByTagName("contentSize").item(0).getTextContent());
            // <header> -> <performanceMeasure>s
            NodeList elementsByTagName = header.getElementsByTagName("performanceMeasure");
            for (int i = 0; i < elementsByTagName.getLength(); i++) {
                Element performanceMeasure = (Element) elementsByTagName.item(i);
                // adiciona todas as métricas utilizando o nome citado
                map.put(performanceMeasure.getAttribute("metric"), elementsByTagName.item(i).getTextContent());
            }
            StringWriter stw = new StringWriter();
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.transform(new DOMSource(message.getElementsByTagName("content").item(0)), new StreamResult(stw));
            map.put("content", stw.getBuffer().toString());

            return map;
        } catch (SAXException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ParserConfigurationException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (TransformerException ex) {
            if (DeveloperSettings.SHOW_EXCEPTION_ERRORS) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public void updateInputCommunicationInterfaceConfiguration(PushServiceReceiver inputInterface, HashMap<String, String> interfaceConfigurations) {
        this.newInternalEvent(EVENT_NEW_INPUT_COMMUNICATION_INTERFACE_ADDRESS, inputInterface, interfaceConfigurations);
    }

    /**
     * Inicia todos os serviços de upload
     */
    public void startAllCommunicationServices() {
        for (UploadService us : uploadServices) {
            us.start();
        }
        for (ReconnectionService rs : this.reconnectionServices) {
            rs.start();
        }
    }

    /**
     * Para todos o serviços de upload
     */
    public void stopAllCommunicationServices() {
        for (UploadService us : uploadServices) {
            us.stop();
        }
        for (ReconnectionService rs : this.reconnectionServices) {
            rs.stop();
        }
    }

    public List<PushServiceReceiver> getPushServiceReveivers() {
        return pushServiceReveivers;
    }

    public PushServiceReceiver getMainPushServiceReceiver() {
        for (PushServiceReceiver receiver : pushServiceReveivers) {
            if (receiver.getStatus() == PushServiceReceiver.STATUS_LISTENING) {
                return receiver;
            }
        }
        return null;
    }

    protected int getUploadMessagingPolicy() {
        return uploadMessagingPolicy;
    }

    protected int getLimitNormalMessage() {
        return limitNormalMessage;
    }

    protected int getLimitPriorityMessage() {
        return limitPriorityMessage;
    }

    public int getMobileDataPolicy() {
        return mobileDataPolicy;
    }

    public synchronized boolean isCompletelyDisconnected() {
        return completelyDisconnected;
    }

    public List<UploadService> getUploadServices() {
        return uploadServices;
    }

    public List<ReconnectionService> getReconnectionServices() {
        return reconnectionServices;
    }

    public List<CommunicationInterface> getCommunicationInterfaces() {
        return communicationInterfaces;
    }

}
