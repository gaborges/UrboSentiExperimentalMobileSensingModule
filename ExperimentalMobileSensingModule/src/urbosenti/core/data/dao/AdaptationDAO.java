/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.data.dao;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import urbosenti.adaptation.AdaptationManager;
import urbosenti.adaptation.ExecutionPlan;
import urbosenti.core.communication.Address;
import urbosenti.core.communication.CommunicationInterface;
import urbosenti.core.communication.CommunicationManager;
import urbosenti.core.communication.Message;
import urbosenti.core.communication.PushServiceReceiver;
import urbosenti.core.communication.ReconnectionService;
import urbosenti.core.communication.UploadService;
import urbosenti.core.data.DataManager;
import urbosenti.core.device.BaseComponentManager;
import urbosenti.core.device.model.ActionModel;
import urbosenti.core.device.model.AgentCommunicationLanguage;
import urbosenti.core.device.model.AgentType;
import urbosenti.core.device.model.Component;
import urbosenti.core.device.model.Content;
import urbosenti.core.device.model.Device;
import urbosenti.core.device.model.Entity;
import urbosenti.core.device.model.EntityType;
import urbosenti.core.device.model.EventModel;
import urbosenti.core.device.model.FeedbackAnswer;
import urbosenti.core.device.model.Instance;
import urbosenti.core.device.model.InteractionModel;
import urbosenti.core.device.model.Parameter;
import urbosenti.core.device.model.Service;
import urbosenti.core.device.model.State;
import urbosenti.core.events.Action;
import urbosenti.core.events.Event;
import urbosenti.user.User;

/**
 *
 * @author Guilherme
 */
public final class AdaptationDAO {

    public final static int COMPONENT_ID = 5;
    public static final int ENTITY_ID_OF_ADAPTATION_MANAGEMENT = 1;
    public static final int AGENT_TYPE_BACKEND_SERVICE = 1;
    public static final int STATE_ID_OF_ADAPTATION_MANAGEMENT_INTERVAL_TO_REPORT_SYSTEM_FUNCIONS = 1;
    public static final int STATE_ID_OF_ADAPTATION_MANAGEMENT_INTERNAL_TO_DELETE_EXPIRED_MESSAGES = 2;
    public static final int STATE_ID_OF_ADAPTATION_MANAGEMENT_ALLOWED_SEND_REPORT_SYSTEM_FUNCIONS = 3;
    public static final int STATE_ID_OF_ADAPTATION_MANAGEMENT_LAST_REPORTED_DATE = 4;
    public static final int INTERACTION_TO_INFORM_NEW_MAXIMUM_UPLOAD_RATE = 1;
    public static final int INTERACTION_TO_SUBSCRIBE_THE_MAXIMUM_UPLOAD_RATE = 2;
    public static final int INTERACTION_TO_UNSUBSCRIBE_THE_MAXIMUM_UPLOAD_RATE = 3;
    public static final int INTERACTION_OF_FAIL_ON_SUBSCRIBE = 4;
    public static final int INTERACTION_OF_MESSAGE_WONT_UNDERSTOOD = 5;
    public static final int INTERACTION_TO_CONFIRM_REGISTRATION = 6;
    public static final int INTERACTION_TO_REFUSE_REGISTRATION = 7;
    public static final int INTERACTION_TO_CANCEL_REGISTRATION = 8;
    public static final int INTERACTION_TO_INFORM_NEW_INPUT_ADDRESS = 9;
    public static final int INTERACTION_TO_REPORT_SENSING_MODULE_FUNCTIONALITY = 10;
    public static final int FUNCTIONALITY_STATUS_TYPE_INFO = 1;
    public static final int FUNCTIONALITY_STATUS_TYPE_WARNING = 1;
    public static final int FUNCTIONALITY_STATUS_TYPE_ERROR = 1;

    private final DataManager dataManager;
    private List<AgentCommunicationLanguage> acls;
    private Content content;
    // somente os eventos
    private List<AgentType> agentTypes;
    // todo o esquema
    private Device deviceModel;
    private EventModel eventModel;
    private InteractionModel interactionModel;
	private SQLiteDatabase database;

    public AdaptationDAO(Object context, DataManager dataManager) {
        this.dataManager = dataManager;
        this.database = (SQLiteDatabase) context;
        this.acls = null;
        this.deviceModel = null;
        this.agentTypes = null;
    }

    public void loadStructureModels() throws SQLException {
        // Modelo do dispositivo, usado para busca, principalmente
        this.deviceModel = this.dataManager.getDeviceDAO().getDeviceModel(dataManager);
        // Todos os tipos de agente
        this.agentTypes = this.dataManager.getAgentTypeDAO().getAgentTypes();
        for (AgentType agentType : this.agentTypes) {
            agentType.setInteractionModels(dataManager.getAgentTypeDAO().getAgentInteractions(agentType));
            agentType.setStateModels(dataManager.getAgentTypeDAO().getAgentStates(agentType));
        }
        // Busca todos os Acls para serem usados em mem√≥ria
        this.populateAcls();
    }

    /**
     * Recebe um evento por par√¢metro, persiste no banco de dados o valor e
     * returna o correspendente do evento entre InteractionModel ou EventModel.
     * Se n√£o encontrar nenhum retorna null
     *
     * @param event
     * @return
     * @throws java.sql.SQLException
     */
    public Object updateWorldModel(Event event) throws SQLException, NullPointerException, NumberFormatException, Exception {
        this.interactionModel = null;
        this.eventModel = null;
        /**
         * ************** Update world model **************
         */
        // verifica se È uma interaÁ„o ou um evento
        // se uma interaÁ„o extrai a interaÁ„o dos par√¢metros do evento
        // de forma semelhante ao evento salva as informa√ß√µes
        // gera uma interaÁ„o que È passada para an√°lise -- o mesmo ocorre com o evento
        // buscar o modelo de evento
        eventModel = this.getEventModel(event);
        // se n√£o encontrar o evento
        if (eventModel == null) {
            // gera um evento de evento desconhecido ou n√£o existente e adiciona os dados do evento n√£o reconecido nos par√¢metros
            throw new NullPointerException("Evento " + event.getId() + " " + event.getName() + " " + event.toString() + "n√£o existe no banco de dados!");
        } else {  // caso encontre
            // adiciona o id o BD, se for necess√°rio armazenar ent√£o usa o gerado pelo banco de dados
            event.setDatabaseId(eventModel.getId());
            // verificar se o modelo de evento precisa ser salvo
            //if (eventModel.isNecessaryStore()) {
            // salvar se sim
            this.dataManager.getEventModelDAO().insert(event, eventModel);
            //}
            // verificar cada par√¢metro, se relacionado a um estado 
            for (Parameter p : eventModel.getParameters()) {
                // se relacionado verifica se È um estado de inst√¢ncia
                if (p.getRelatedState() != null) {
                    // preenche o conte√∫do
                    if (content == null) {
                        content = new Content(Content.parseContent(
                                p.getRelatedState().getDataType(),
                                event.getParameters().get(p.getLabel())),
                                event.getTime());
                    } else {
                        content.setValue(Content.parseContent(
                                p.getRelatedState().getDataType(), event.getParameters().get(p.getLabel())));
                        content.setTime(event.getTime());
                    }
                    // se for estado de inst√¢ncia acessa o par√¢metro instanceId e salva na referida inst√¢ncia, sen√£o salva no estado
                    if (p.getRelatedState().isStateInstance()) {
                        //--- Testar se e uma instancia ou uma InstanceRepresentative, se n√£o for, indica erro, para ficar genÈrico
                        //--- colocar todos os r√≥tulos de inst√¢ncia como instance
                        // Existem 3 inst√¢ncias - User, CommunicationInterface e PushServiceReceiver
                        // TambÈm aceitar que o par√¢metro instance tenha uma inst√¢ncia
                        if (event.getComponentManager().getComponentId() == CommunicationDAO.COMPONENT_ID) {
                            if (event.getParameters().get("interface") instanceof CommunicationInterface) {
                                for (State instanceState : ((CommunicationInterface) event.getParameters().get("interface")).getInstance().getStates()) {
                                    if (p.getRelatedState().getModelId() == instanceState.getModelId()) {
                                        instanceState.setContent(content);
                                        this.dataManager.getInstanceDAO().insertContent(instanceState);
                                        break;
                                    }
                                }
                            } else if (event.getParameters().get("interface") instanceof PushServiceReceiver) {
                                for (State instanceState : ((PushServiceReceiver) event.getParameters().get("interface")).getInstance().getStates()) {
                                    if (p.getRelatedState().getModelId() == instanceState.getModelId()) {
                                        instanceState.setContent(content);
                                        this.dataManager.getInstanceDAO().insertContent(instanceState);
                                        break;
                                    }
                                }
                            } else if (event.getParameters().get("reconnectionService") instanceof ReconnectionService) {
                                for (State instanceState : ((ReconnectionService) event.getParameters().get("reconnectionService")).getInstance().getStates()) {
                                    if (p.getRelatedState().getModelId() == instanceState.getModelId()) {
                                        instanceState.setContent(content);
                                        this.dataManager.getInstanceDAO().insertContent(instanceState);
                                        break;
                                    }
                                }
                            } else if (event.getParameters().get("uploadService") instanceof UploadService) {
                                for (State instanceState : ((UploadService) event.getParameters().get("uploadService")).getInstance().getStates()) {
                                    if (p.getRelatedState().getModelId() == instanceState.getModelId()) {
                                        instanceState.setContent(content);
                                        this.dataManager.getInstanceDAO().insertContent(instanceState);
                                        break;
                                    }
                                }
                            }
                        } else if (event.getComponentManager().getComponentId() == UserDAO.COMPONENT_ID) {
                            for (State instanceState : ((User) event.getParameters().get("user")).getInstance().getStates()) {
                                if (p.getRelatedState().getModelId() == instanceState.getModelId()) {
                                    instanceState.setContent(content);
                                    this.dataManager.getInstanceDAO().insertContent(instanceState);
                                    break;
                                }
                            }
                        }
                    } else {
                        // se for salva como estado de entidade salva
                        p.getRelatedState().setContent(content);
                        this.dataManager.getStateDAO().insertContent(p.getRelatedState());
                    }
                }
            }
            if (Event.INTERATION_EVENT == event.getEventType()) {
                // extract interaction
                event = this.extractInteractionFromMessageEvent(event);
                // get Interaction model
                interactionModel = this.getInteractionModel(event);
                // verificar cada par√¢metro, se relacionado a um estado 
                for (Parameter p : interactionModel.getParameters()) {
                    // se relacionado verifica se È um estado de inst√¢ncia
                    if (p.getRelatedState() != null) {
                        // se for salva como estado de inst√¢ncia
                        p.getRelatedState().setContent(
                                new Content(Content.parseContent(
                                                p.getRelatedState().getDataType(),
                                                event.getParameters().get(p.getLabel())),
                                        event.getTime()));

                        this.dataManager.getAgentTypeDAO().insertContent(p.getRelatedState());
                    }
                }
            }
        }
        if (this.interactionModel != null) {
            return interactionModel;
        }
        if (this.eventModel != null) {
            return eventModel;
        }
        throw new NullPointerException("Evento " + event.getId() + " " + event.getName() + " " + event.toString() + "n√£o existe no banco de dados!");
    }

    public void populateAcls() throws SQLException {
        if (acls == null) {
            this.acls = this.dataManager.getAgentCommunicationLanguageDAO().getAgentCommunicationLanguages();
            {
                for (AgentCommunicationLanguage acl : acls) {
                    acl.setCommunicativeActs(this.dataManager.getCommunicativeActDAO().getCommunicativeActs(acl));
                }
            }
        }
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

    /**
     * Entrai a mensagem do evento. O evento passado por par√¢metro representa o
     * evento id=4 do componente de comunicaÁ„o referente ao recebimento de
     * messagens. Esse evento È um evento de interaÁ„o e os par√¢metros de
     * entrada s√£o:
     * <ul><li>message: urbosenti.core.communication.Message, contÈm a mensagem,
     * bem como quem enviou a mensagem</li>
     * <li>sender: urbosenti.core.communication.Address, contÈm o endere√ßo de
     * quem enviou a mensagem</li></ul>
     *
     * @param event
     * @return
     * @throws ClassCastException
     * @throws NumberFormatException
     * @throws SQLException
     * @throws Exception
     */
    public Event extractInteractionFromMessageEvent(Event event) throws ClassCastException, NumberFormatException, SQLException, Exception {
        Address sender = (Address) event.getParameters().get("sender");
        Message message = (Message) event.getParameters().get("message");
        AgentCommunicationLanguage usedAgentCommunicationLanguage = null;
        boolean interactionExists = false;
        /**
         * **** Valida√ß√µes ******
         */
        // verifica se o UID da mensagem existe no sistema, se sim retorna o agente referente e o servi√ßo
        Service service = this.getServiceByUid(message.getOrigin().getUid());
        if (service == null) {
            service = dataManager.getServiceDAO().getServiceByUid(message.getOrigin().getUid());
            // se volta null o servi√ßo n√£o est√° cadastrado
            if (service == null) {
                throw new Exception("Service UID '" + message.getOrigin().getUid() + "' not found.");
            } else {
                // adiciona em mem√≥ria se o novo servi√ßo estiver no banco
                deviceModel.getServices().add(service);
            }
        }
        // se retorna null, o servi√ßo n√£o possui agente para interaÁ„o
        if (service.getAgent() == null) {
            // caso n√£o
            // excess√£o agente n√£o registrado
            throw new Exception("Agent of service UID '" + message.getOrigin().getUid() + "' not was found.");
        }
        /**
         * *** Extrair a mensagem *****
         */
        /* Formato:
         <content>
         <acl>fipa</acl>
         <interactionId>1</interactionId>
         <message>
         <fipa-message act="inform" >
         <ontology>UrboSenti 1.0</ontology>
         <protocol>UrboSenti-interaction-1.0</protocol>
         <language>json</language> 
         <content>{chave:"valor"}</content>
         </fipa-message>
         </message>
         </content> */
        // extrai a acl e verifica qual a linguagem, verificando se o tipo de agente suporta, caso n√£o, exceÁ„o
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(message.getContent())));
        // <content>
        Element root = doc.getDocumentElement(), messageContent;
        // <acl>fipa</acl>
        // testa se a ACL È conhecida, se n√£o for gera exceÁ„o, se sim retorna o id
        for (AgentCommunicationLanguage acl : acls) {
            if (dataManager.getAgentCommunicationLanguageDAO()
                    .isAgentCommunicationLanguageKnown(acl, root.getElementsByTagName("acl").item(0).getTextContent())) {
                usedAgentCommunicationLanguage = acl;
                break;
            }
        }
        if (usedAgentCommunicationLanguage == null) {
            throw new Exception("Agent Communication Language '" + root.getElementsByTagName("acl").item(0).getTextContent()
                    + "' from UID:'" + message.getOrigin().getUid()
                    + "', address: '" + message.getOrigin().getAddress() + "' is not supported.");
        }
        // <interactionId>1</interactionId>
        int interactionId = Integer.parseInt(root.getElementsByTagName("interactionId").item(0).getTextContent());
        // flag = false
        // checa em mem√≥ria primeiro pelo servi√ßo
        for (InteractionModel interaction : service.getAgent().getAgentType().getInteractionModels()) {
            if (interaction.getId() == interactionId) {
                interactionExists = true;
                break;
            }
        }
        if (!interactionExists) {
            // checa em mem√≥ria pelos tipos de agente
            for (AgentType agentType : this.agentTypes) {
                for (InteractionModel interaction : agentType.getInteractionModels()) {
                    if (interaction.getId() == interactionId) {
                        interactionExists = true;
                        break;
                    }
                }
            }
        }
        // checa no banco se n√£o encontrou em mem√≥ria
        if (!interactionExists) {
            if (dataManager.getAgentTypeDAO().getInteractionModel(interactionId) == null) {
                throw new Exception("Interaction model referred by the value interactionId:'" + interactionId + "' was not found.");
            }
        }

        /**
         * *** Processar conte√∫do segundo Agent Communication Language *****
         */
        // se for FIPA executa esse processo, se houvesse outras a implementaÁ„o seria dada por esta
        // extrai a mensagem e processa na linguagem conhecida (FIPA)
        if (usedAgentCommunicationLanguage.getId() == AgentCommunicationLanguage.AGENT_COMMUNICATIVE_LANGUAGE_FIPA_ID) {
            // <fipa-message>
            messageContent = (Element) root.getElementsByTagName("fipa-message").item(0);
            /**
             * A informaÁ„o de que ato comunicativo est√° sendo passado È
             * despres√≠vel por causa do interactionId
             * <fipa-message act="inform" >
             * String communicativeAct = messageContent.getAttribute("act");
             */
            /**
             * Elementos est√°ticos por enquanto, n√£o h√° necessidade de
             * implementar dinamicamente. Trabalho futuro, deixar din√¢mico.
             * <ontology>UrboSenti 1.0</ontology>
             * <protocol>UrboSenti-interaction-1.0</protocol>
             * <language>xml</language>
             */
            // Processar conte√∫do da mensagem adicionando os elementos como par√¢metros no HashMap do objetivo event
            // <content><chave>valor</chave></content>
            Element content = (Element) messageContent.getElementsByTagName("content").item(0);
            for (Parameter p : interactionModel.getParameters()) {
                if (content.getElementsByTagName(p.getLabel()).item(0) != null) {
                    event.getParameters().put(p.getLabel(),
                            Content.parseContent(
                                    p.getDataType(),
                                    content.getElementsByTagName(p.getLabel()).item(0).getTextContent()));
                }
            }
        }
        // adiciona o interaction ID no evento
        event.setId(interactionId);
        // retorna o evento de interaÁ„o com os par√¢metros e o interaction id referentes do Interaction Model
        return event;
    }

//    private Event proccessFIPAInteractionMessage (Element root){
//        
//    }
    public Action makeInteractionMessage(Action action) throws ClassCastException, NumberFormatException, SQLException, Exception {
        String finalString;
        interactionModel = (InteractionModel) action.getParameters().get("interactionModel");
        /**
         * *** Extrair a mensagem *****
         */
        //<content> -- ser√° adicionado na mensagem
        // <acl>fipa</acl>
        finalString = "<acl>fipa</acl>";
        // <interactionId>1</interactionId>
        finalString += "<interactionId>" + interactionModel.getId() + "</interactionId>";
        // <message>
        finalString += "<message>";
        //<fipa-message act="inform" >
        finalString += "<fipa-message act=\"" + interactionModel.getCommunicativeAct().getDescription() + "\" >";
        // <ontology>UrboSenti 1.0</ontology>
        finalString += "<ontology>UrboSenti 1.0</ontology>";
        // <protocol>UrboSenti-interaction-1.0</protocol>
        finalString += "<protocol>UrboSenti-interaction-1.0</protocol>";
        // <language>xml</language> 
        finalString += "<language>xml</language>";
        // <content>{chave:"valor"}</content>
        finalString += "<content>";
        for (Parameter p : interactionModel.getParameters()) {
            finalString += "<" + p.getLabel() + ">";
            finalString += action.getParameters().get(p.getLabel());
            finalString += "</" + p.getLabel() + ">";
        }
        finalString += "</content>";
        // </fipa-message>
        finalString += "</fipa-message>";
        // </message>
        finalString += "</message>";
        /**
         * * alterar o action **
         */
        Message message = new Message();
        message.setContent(finalString);
        message.setTarget((Address) action.getParameters().get("target"));
        if (action.getParameters().get("origin") != null) {
            message.setOrigin((Address) action.getParameters().get("origin"));
        } else {
            message.setOrigin(new Address());
            message.getOrigin().setLayer(Address.LAYER_SYSTEM);
        }
        message.setSubject(Message.SUBJECT_SYSTEM_MESSAGE);
        message.setContentType("text/xml");
        message.setUsesUrboSentiXMLEnvelope(true);
        // Preparar aÁ„o para envio: envio de mensagens ass√≠ncrona
        action.getParameters().put("message", message);
        if (action.isSynchronous()) {
            action.setId(CommunicationManager.ACTION_SEND_SYNCHRONOUS_MESSAGE);
        } else {
            action.setId(CommunicationManager.ACTION_SEND_ASSYNCHRONOUS_MESSAGE);
        }
        action.setOrigin(Address.LAYER_SYSTEM);
        action.setTargetComponentId(CommunicationDAO.COMPONENT_ID);
        action.setTargetEntityId(CommunicationDAO.ENTITY_ID_OF_SENDING_MESSAGES);
        action.setActionType(Event.INTERATION_EVENT);
        return action;
    }

    public void updateDecision(FeedbackAnswer response, Event event, Action actionToExecute, ExecutionPlan ep) throws SQLException, java.lang.NullPointerException {
        // regsitra a aÁ„o
        this.dataManager.getActionModelDAO().insertAction(response, event, actionToExecute, ep);
        // se a resposta da aÁ„o foi sucesso, verifica  se algum dos par√¢metros do actionModel È relacionado com algum estado
        if (response.getId() == FeedbackAnswer.ACTION_RESULT_WAS_SUCCESSFUL) {
            ActionModel actionModel = this.getActionModel(
                    actionToExecute.getId(),
                    actionToExecute.getTargetEntityId(),
                    actionToExecute.getTargetComponentId());
            if (actionModel == null) {
                actionModel = this.dataManager.getActionModelDAO().getActionModel(
                        actionToExecute.getId(),
                        actionToExecute.getTargetEntityId(),
                        actionToExecute.getTargetComponentId());
            }
            // atualiza par√¢metros
            for (Parameter p : actionModel.getParameters()) {
                if (content == null) {
                    content = new Content(Content.parseContent(
                            p.getDataType(),
                            actionToExecute.getParameters().get(p.getLabel())),
                            response.getTime());
                } else {
                    content.setValue(Content.parseContent(
                            p.getDataType(), actionToExecute.getParameters().get(p.getLabel())));
                    content.setTime(response.getTime());
                }
                //verifica  se algum dos par√¢metros do actionModel È relacionado com algum estado
                if (p.getRelatedState() != null) {
                    // verifica se È estado de inst√¢ncia ou de entidade
                    if (p.getRelatedState().isStateInstance()) {
                        int instanceId;
                        if (actionToExecute.getParameters().get("interface") != null) {
                            instanceId = (Integer) actionToExecute.getParameters().get("interface");
                        } else {
                            instanceId = (Integer) actionToExecute.getParameters().get("instanceId");
                        }
                        // recuperar a inst√¢ncia
                        Instance instance = this.getInstance(
                                actionToExecute.getTargetComponentId(),
                                actionToExecute.getTargetEntityId(),
                                instanceId);
                        if (instance == null) {
                            instance = dataManager.getInstanceDAO().getInstance(
                                    actionToExecute.getTargetComponentId(),
                                    actionToExecute.getTargetEntityId(),
                                    instanceId);
                            if (instance != null) {
                                this.addInstanceInMemory(instance);
                            }
                        }
                        // procurar o estado para salvar o conte√∫do
                        for (State s : instance.getStates()) {
                            if (s.getModelId() == p.getRelatedState().getModelId()) {
                                s.setContent(content);
                                this.dataManager.getInstanceDAO().insertContent(s);
                                break;
                            }
                        }
                    } else {
                        // relacionado com estado de entidade. Atualiza diretamente
                        p.getRelatedState().setContent(content);
                        this.dataManager.getEntityStateDAO().insertContent(p.getRelatedState());
                    }
                }
                // Testa se o par√¢metro existe e È objegrat√≥rio, depois salva o conte√∫do do par√¢metro
                if (actionToExecute.getParameters().get(p.getLabel()) == null && !p.isOptional()) {
                    throw new Error("Parameter " + p.getLabel() + " from the event " + actionModel.getDescription() + " id " + actionModel.getId()
                            + " was not found. Such parameter is not optional!");
                } else {
                    if (actionToExecute.getParameters().get(p.getLabel()) != null) {
                        p.setContent(content);
                        dataManager.getActionModelDAO().insertContent(p, actionToExecute);
                    }
                }
            }
        }
    }

    /**
     *
     * @param instanceId inst√¢ncia
     * @param lastIntervalOfServiceErrors intervalo para busca
     * @return null if do not find any record or the generated action ID if was
     * find.
     * @throws SQLException
     */
    public Integer getLastRecordedErrorFromInstance(int instanceId, long lastIntervalOfServiceErrors) throws SQLException {
        String sql = "SELECT  feedback_id, generated_actions.id as id FROM generated_actions, action_contents, action_parameters "
                + " WHERE  generated_action_id = generated_actions.id AND action_parameter_id = action_parameters.id AND reading_time > ? "
                + " AND reading_value = ? AND label = ? AND action_model_id = ? AND  entity_id = ? AND component_id = ? "
                + " ORDER BY reading_time DESC;";
        
        Cursor cursor = this.database.rawQuery(sql,	new String[]{
        		String.valueOf((System.currentTimeMillis() - (lastIntervalOfServiceErrors * 1.3))),
        		String.valueOf(instanceId),
        		String.valueOf("instanceId"),
        		String.valueOf(AdaptationManager.ACTION_STORE_INTERNAL_ERROR),
        		String.valueOf(AdaptationDAO.ENTITY_ID_OF_ADAPTATION_MANAGEMENT),
        		String.valueOf(AdaptationDAO.COMPONENT_ID)
        });
        
        /*this.stmt.setObject(1, (System.currentTimeMillis() - (lastIntervalOfServiceErrors * 1.3)));
        this.stmt.setObject(2, instanceId);
        this.stmt.setString(3, "instanceId");
        this.stmt.setInt(4, AdaptationManager.ACTION_STORE_INTERNAL_ERROR);
        this.stmt.setInt(5, AdaptationDAO.ENTITY_ID_OF_ADAPTATION_MANAGEMENT);
        this.stmt.setInt(6, AdaptationDAO.COMPONENT_ID);*/

        while (cursor.moveToNext()) {
            return cursor.getInt(cursor.getColumnIndex("id"));
        }
        return null;
    }

    public String getErrorReporting(Date startDate, Date endDate) throws SQLException {
        String json = "{ ";
        // buscar todas as a√ß√µes de erro a partir da √∫ltima data atÈ a data atual
        List<Action> actions = dataManager.getActionModelDAO()
                .getActions(AdaptationManager.ACTION_STORE_INTERNAL_ERROR,
                        ENTITY_ID_OF_ADAPTATION_MANAGEMENT, COMPONENT_ID, startDate, endDate);
        List<Action> feedbackErros = dataManager.getActionModelDAO()
                .getActionsFeedbackErrors(startDate, endDate);
        List<Parameter> parameters;
        // se n√£o houver nenhuma aÁ„o com erro, cria uma de sucesso. 
        if (actions.isEmpty() && feedbackErros.isEmpty()) {
            json += "\"nodeStatusReported\" : \"stable\"";
        } else {
            // cria erros
            json += "\"nodeStatusReported\" : \"error\", ";
            // para cada aÁ„o buscar os par√¢metros
            json += "\"errors\" : [ ";
            /*
             Para o erro:
             - Tipo de erro
             - DescriÁ„o
             - Tempo
             - Instance id (se houver)
             "message" : ["type":"2", "description":"Inst√¢ncia demorou para iniciar", "time":"1439426402937", "instanceId": 1 ] ,
             */
            for (Action action : actions) {
                json += " { \"message\" : [ ";
                parameters = this.dataManager.getActionModelDAO().getActionParameterContents(action);
                for (Parameter parameter : parameters) {
                    json += " {\"" + parameter.getLabel() + "\": \"" + parameter.getContent().getValue().toString() + "\" }"
                            + ((parameters.get(parameters.size() - 1).getId() == parameter.getId()) ? "" : " ,");

                }
                json += " ] } " + ((actions.get(actions.size() - 1).getDataBaseId() == action.getDataBaseId()) ? "" : ", ");
            }
            if (!feedbackErros.isEmpty()) {
                json += ", ";
            }
            /*
             Para cada feedback error
             - tempo de resposta
             - modelo de aÁ„o
             - entidade_acao
             - componente_acao
             - par√¢metros_acao
             - modelo de evento inicial, 
             - entidade_evento,
             - componente_evento,
             - par√¢metros_evento,
             "feedback" : ["responseTime":560 , "actionType":0, "actionModel":1 , "actionEntity":1 , "actionComponent": 1, 
             "actionParameters": ["param1":"value1","param2":"value2"], "eventType":0, "eventModel": 1, "eventEntity":1, 
             "eventComponent":1, "eventTime":1439426402937 , "eventParameters" : ["param1":"value1","param2":"value2"] ] ,
             "feedback" : ["responseTime":931 , "actionType":1, "interactionModel":1 , "targetServiceId":1 , 
             "interactionParameters":["param1":"value1", "param2":"value2"], "eventType":0, "eventModel": 1, "eventEntity":1,
             "eventComponent":1, "eventParameters" : ["param1":"value1", "param2":"value2"] , "eventTime": 1439426402937   ] 
             */
            for (Action action : feedbackErros) {
                Event event = this.dataManager.getEventModelDAO().getEvent(action, (BaseComponentManager) this.dataManager.getDeviceManager());
                json += " {\"feedback\" : [ ";
                // se a aÁ„o È interaÁ„o
                if (event.getEventType() == Event.INTERATION_EVENT) {
                    parameters = this.dataManager.getAgentTypeDAO().getInteractionParameterContents(action);
                    json += "\"{ responseTime\":" + (action.getFeedbackAnswer().getTime().getTime() - event.getTime().getTime()) + ", ";
                    for (Parameter parameter : parameters) {
                        json += " \"" + parameter.getLabel() + "\": \"" + parameter.getContent().getValue().toString() + "\"}"
                                + (parameters.get(parameters.size() - 1).getId() == parameter.getId() ? "" : " ,");
                    }
                } else {
                    json += "\"{ responseTime\":" + (action.getFeedbackAnswer().getTime().getTime() - event.getTime().getTime()) + ", ";
                    json += "\"actionType\":" + (action.getActionType()) + ", ";
                    json += "\"actionModel\":" + (action.getId()) + ", ";
                    json += "\"actionEntity\":" + (action.getTargetEntityId()) + ", ";
                    json += "\"actionComponent\":" + (action.getTargetComponentId()) + ", ";
                    json += "\"actionParameters\":[";
                    parameters = this.dataManager.getActionModelDAO().getActionParameterContents(action);
                    for (Parameter parameter : parameters) {
                        json += " {\"" + parameter.getLabel() + "\": \"" + parameter.getContent().getValue().toString() + "\"}"
                                + (parameters.get(parameters.size() - 1).getId() == parameter.getId() ? "" : " ,");

                    }
                    json += " ] ";
                    json += "\"eventTime\":" + (event.getTime().getTime()) + ", ";
                    json += "\"eventType\":" + (event.getEventType()) + ", ";
                    json += "\"eventModel\":" + (event.getId()) + ", ";
                    json += "\"eventEntity\":" + (event.getEntityId()) + ", ";
                    json += "\"eventComponent\":" + (event.getComponentManager().getComponentId()) + ", ";
                    json += "\"eventParameters\":[";
                    parameters = this.dataManager.getEventModelDAO().getEventParameterContents(action);
                    for (Parameter parameter : parameters) {
                        json += " {\"" + parameter.getLabel() + "\": \"" + parameter.getContent().getValue().toString() + "\"}"
                                + (parameters.get(parameters.size() - 1).getId() == parameter.getId() ? "" : " ,");
                    }
                }
                json += " ] } " + ((feedbackErros.get(feedbackErros.size() - 1).getDataBaseId() == action.getDataBaseId() || feedbackErros.isEmpty()) ? "" : ", ");
            }
            json += " ] } ";
        }
        json += "}";
        return json;
    }

    /**
     * Remove todas os relatos de funcionamento armazenados atÈ a data
     * especificada (Ainda falta terminar)
     *
     * @param date
     */
    public void removeSentReportedErrors(Date date) throws Exception {
        // fazer mais tarde.
        throw new Exception("ExceÁ„o para teste.");
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public EventModel getEventModel(Event event) {
        return this.getEventModel(event.getId(), event.getEntityId(), event.getComponentManager().getComponentId());
    }

    public EventModel getEventModel(int eventModelId, int entityModelId, int componentId) {
        for (Component component : this.deviceModel.getComponents()) {
            if (component.getId() == componentId) {
                for (Entity entity : component.getEntities()) {
                    if (entity.getModelId() == entityModelId) {
                        for (EventModel eventModelTemp : entity.getEventModels()) {
                            if (eventModelTemp.getModelId() == eventModelId) {
                                return eventModelTemp;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public Service getServiceByUid(String serviceUid) {
        for (Service service : this.deviceModel.getServices()) {
            if (service.getServiceUID().equals(serviceUid)) {
                return service;
            }
        }
        return null;
    }

    public InteractionModel getInteractionModel(Event event) {
        // se o evento j√° existe n√£o precisa buscar
        if(interactionModel.getId()==event.getId()&&event.getEventType()==Event.INTERATION_EVENT){
            return interactionModel;
        }
        for (AgentType agentType : this.agentTypes) {
            for (InteractionModel interaction : agentType.getInteractionModels()) {
                if (interaction.getId() == event.getId()) {
                    interactionModel = interaction;
                    return interaction;
                }
            }
        }
        return null;
    }

    /**
     * Retorna da mem√≥ria ou do banco de dados
     *
     * @param interactionId
     * @return
     * @throws SQLException
     */
    public InteractionModel getInteractionModel(int interactionId) throws SQLException {
        for (AgentType agentType : this.agentTypes) {
// por enquanto n√£o h√° filtro por tipo de agente. Existe somente um tipo de agente            
//            if (agentType.getId() == event.getId()) {
            for (InteractionModel im : agentType.getInteractionModels()) {
                if (im.getId() == interactionId) {
                    return im;
                }
            }
            //          }
        }
        return (dataManager.getAgentTypeDAO().getInteractionModel(interactionId));
    }

    public ActionModel getActionModel(int actionModelId, int targetEntityId, int targetComponentId) {
        for (Component component : this.deviceModel.getComponents()) {
            if (component.getId() == targetComponentId) {
                for (Entity entity : component.getEntities()) {
                    if (entity.getModelId() == targetEntityId) {
                        for (ActionModel model : entity.getActionModels()) {
                            if (model.getModelId() == actionModelId) {
                                return model;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public Instance getInstance(int targetComponentId, int targetEntityId, int instanceId) {
        for (Component component : this.deviceModel.getComponents()) {
            if (component.getId() == targetComponentId) {
                for (Entity entity : component.getEntities()) {
                    if (entity.getModelId() == targetEntityId) {
                        for (Instance model : entity.getInstanceModels()) {
                            if (model.getModelId() == instanceId) {
                                return model;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void addInstanceInMemory(Instance instance) {
        for (Component component : this.deviceModel.getComponents()) {
            if (component.getId() == instance.getEntity().getComponent().getId()) {
                for (Entity entity : component.getEntities()) {
                    if (entity.getModelId() == instance.getEntity().getModelId()) {
                        entity.getInstanceModels().add(instance);
                    }
                }
            }
        }
    }
}
