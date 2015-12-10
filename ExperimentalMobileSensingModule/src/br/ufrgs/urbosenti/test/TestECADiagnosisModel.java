/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufrgs.urbosenti.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import urbosenti.adaptation.AbstractDiagnosisModel;
import urbosenti.adaptation.AdaptationManager;
import urbosenti.adaptation.Change;
import urbosenti.adaptation.Diagnosis;
import urbosenti.core.communication.Address;
import urbosenti.core.communication.CommunicationInterface;
import urbosenti.core.communication.CommunicationManager;
import urbosenti.core.communication.PushServiceReceiver;
import urbosenti.core.communication.ReconnectionService;
import urbosenti.core.communication.UploadService;
import urbosenti.core.data.dao.AdaptationDAO;
import urbosenti.core.data.dao.CommunicationDAO;
import urbosenti.core.device.DeviceManager;
import urbosenti.core.device.model.Content;
import urbosenti.core.device.model.InteractionModel;
import urbosenti.core.events.Event;
import urbosenti.core.events.EventManager;
import urbosenti.core.events.SystemEvent;

/**
 *
 * @author Guilherme
 */
public class TestECADiagnosisModel extends AbstractDiagnosisModel {

    private HashMap<String, Object> values;
    private Event generatedEvent;
    private InteractionModel interactionModel;
    private Address target;
    private Content content;
    private UploadService up;
    private Integer genericInteger2;
    private int genericInteger1;

    public TestECADiagnosisModel(DeviceManager deviceManager) {
        super(deviceManager.getDataManager().getAdaptationDAO(),deviceManager);
    }

    @Override
    public Diagnosis interactionAnalysis(Event event, Diagnosis diagnosis, AdaptationDAO adaptationDAO) throws SQLException {
        if (event.getId() == AdaptationDAO.INTERACTION_TO_INFORM_NEW_MAXIMUM_UPLOAD_RATE) {
            // verifica se tem permitido alterar
            if (getDeviceManager().getDataManager().getCommunicationDAO().getCurrentPreferentialPolicy(CommunicationDAO.UPLOAD_REPORTS_POLICY) == 4) {
                for (UploadService uploadService : getDeviceManager().getCommunicationManager().getUploadServices()) {
                    // procura o servi�o
                    if (uploadService.getService().getServiceUID().equals(event.getParameters().get("uid").toString())) {
                        // verifica se o valor � diferente do anterior
                        if (uploadService.getUploadRate() != Double.parseDouble(event.getParameters().get("uploadRate").toString())) {
                            // se sim
                            values = new HashMap<String, Object>();
                            values.put("uploadRate", event.getParameters().get("uploadRate"));  // Alterar taxa de upload; Nova Taxa	double	entre 1 e 0	uploadRate
                            values.put("instanceId", uploadService.getInstance().getModelId()); // Id da inst�ncia; inteito 	int	instanceId
                            diagnosis.addChange(new Change(5, values));
                        }
                        break;
                    }
                }
            }
        } else if (event.getId() == AdaptationDAO.INTERACTION_OF_FAIL_ON_SUBSCRIBE) { // Falha ao assinar
            // n�o necess�rio agora
        } else if (event.getId() == AdaptationDAO.INTERACTION_OF_MESSAGE_WONT_UNDERSTOOD) { // Mensagem n�o entendida
            // n�o necess�rio agora
        } else if (event.getId() == AdaptationDAO.INTERACTION_TO_CONFIRM_REGISTRATION) { // Assinatura aceita
            for (UploadService uploadService : getDeviceManager().getCommunicationManager().getUploadServices()) {
                // procura o servi�o
                if (uploadService.getService().getServiceUID().equals(event.getParameters().get("uid").toString())) {
                    // se encontrar plano est�tico para altera��o
                    values = new HashMap<String, Object>();
                    values.put("value", true); //	Alterar taxa de upload	Nova Taxa	double	entre 1 e 0	uploadRate
                    values.put("instanceId", uploadService.getInstance().getModelId()); //  Id da inst�ncia	inteito 	int	instanceId
                    diagnosis.addChange(new Change(6, values));
                    break;
                }
            }
        } else if (event.getId() == AdaptationDAO.INTERACTION_TO_REFUSE_REGISTRATION) { // Assinatura recusada
            // n�o necess�rio agora
        } else if (event.getId() == AdaptationDAO.INTERACTION_TO_CANCEL_REGISTRATION) { // Assinatura cancelada
            // n�o necess�ro agora
        } else if (event.getId() == TestManager.INTERACTION_REQUEST_RESPONSE) { // resposta da mensagem de teste
            interactionModel = (super.getDeviceManager().getDataManager().getAgentTypeDAO().getInteractionModel(TestManager.INTERACTION_ANSWER_THE_REQUEST_RESPONSE));
            // par�metros: id o evento (eventId); tempo do evento (timestampEvent); ip (ip); porta(port)
            //target = new Address("http://" + event.getParameters().get("ip") + ":" + event.getParameters().get("port"));
            target = new Address(event.getParameters().get("ip") + ":" + event.getParameters().get("port"));
            target.setLayer(Address.LAYER_SYSTEM);
            target.setUid(((Address) event.getParameters().get("sender")).getUid());
            event.getParameters().put("target", target);
            event.getParameters().put("interactionModel", interactionModel);
            event.getParameters().put("ip", getDeviceManager().getCommunicationManager().getMainPushServiceReceiver().getInterfaceConfigurations().get("ipv4Address"));
            event.getParameters().put("port", getDeviceManager().getCommunicationManager().getMainPushServiceReceiver().getInterfaceConfigurations().get("port"));
            //values.put("eventId", event.getDatabaseId()); j� est�o no conteúdo do HashMap
            //values.put("timestampEvent", event.getTime().getTime());
            diagnosis.addChange(new Change(20, event.getParameters())); // par�metros s�o os mesmos
        } else if (event.getId() == TestManager.INTERACTION_ANSWER_THE_REQUEST_RESPONSE) { // resposta da mensagem de teste
            //id do evento (eventId);tempo de evento (timestampEvent);
            diagnosis.addChange(new Change(22, event.getParameters())); // par�metros s�o os mesmos
        } else if (event.getId() == TestManager.INTERACTION_REQUEST_SHUTDOWN) {
            //desligar
            diagnosis.addChange(new Change(21, null)); // sem par�metros
        }
        return diagnosis;
    }

    @Override
    public Diagnosis eventAnalysis(Event event, Diagnosis diagnosis, AdaptationDAO adaptationDAO) throws SQLException, Exception {
        /* Analysis -- Diagnosis */
        if (event.getComponentManager().getComponentId() == DeviceManager.DEVICE_COMPONENT_ID) {
            if (event.getId() == DeviceManager.EVENT_DEVICE_SERVICES_INITIATED) {
                // para cada servi�o de upload, verificar se j� est�o registrados para receber atualiza��es do tempo de expira��o dos relatos? Se n�o Adapta��o
                // change=1;
                // testa se a pol�tica do servi�o de upload � 4 = adaptativa, se n�o for n�o inicia isso
                if (getDeviceManager().getDataManager().getCommunicationDAO().getCurrentPreferentialPolicy(CommunicationDAO.UPLOAD_REPORTS_POLICY) == 4) {
                    // busca os servi�os de upload, verifica se eles est�o registrados para receber uploads
                    for (UploadService service : getDeviceManager().getCommunicationManager().getUploadServices()) {
                        if (!service.isSubscribedMaximumUploadRate()) {
                            // intera��o de subscribe -- fazer para cada uploadService
                            interactionModel = adaptationDAO.getInteractionModel(AdaptationDAO.INTERACTION_TO_SUBSCRIBE_THE_MAXIMUM_UPLOAD_RATE);
                            // est�tico
                            // target = new Address(getDeviceManager().getBackendService().getAddress());
                            // target.setLayer(Address.LAYER_SYSTEM);
                            // target.setUid(getDeviceManager().getBackendService().getServiceUID());
                            // din�mico
                            target = new Address(service.getService().getAddress());
                            target.setLayer(Address.LAYER_SYSTEM);
                            target.setUid(service.getService().getServiceUID());
                            values = new HashMap<String, Object>();
                            values.put("target", target);
                            values.put("interactionModel", interactionModel);
                            values.put("address", getDeviceManager().getCommunicationManager().getMainPushServiceReceiver().getInterfaceConfigurations());
                            values.put("interface", getDeviceManager().getCommunicationManager().getMainPushServiceReceiver().getInstance().getDescription());
                            values.put("uid", service.getService().getApplicationUID());
                            values.put("layer", "System");
                            diagnosis.addChange(new Change(1, values));
                            break;
                        }
                    }
                }

                // verificar se � permitido inicializar contador de envio de relatos  de funcionamento ao servidor
                if (getDeviceManager().getAdaptationManager().isAllowedReportingFunctionsToUploadService()) {
                    // inicializar contador de envio de relatos  de funcionamento ao servidor
                    generatedEvent = new SystemEvent(getDeviceManager().getAdaptationManager());
                    generatedEvent.setEntityId(AdaptationDAO.ENTITY_ID_OF_ADAPTATION_MANAGEMENT);
                    generatedEvent.setName("Gatilho de relat�rios do sistema ativado");
                    generatedEvent.setId(AdaptationManager.EVENT_GENERATED_EVENT_TO_REPORTING_TRIGGED);
                    values = new HashMap<String, Object>();
                    values.put("event", generatedEvent);
                    values.put("time", getDeviceManager().getAdaptationManager().getIntervalAmongModuleStateReports()); // 
                    values.put("date", new Date());
                    values.put("method", EventManager.METHOD_DATE_PLUS_REPEATED_INTERVALS);
                    values.put("handler", this);
                    diagnosis.addChange(new Change(2, values));
                }
                // iniciar varredura para exclus�o de mensagens expiradas
                // se a pol�tica de armazenamento for 4 faz isso, sen�o n�o
                if (getDeviceManager().getDataManager().getCommunicationDAO().getCurrentPreferentialPolicy(CommunicationDAO.MESSAGE_STORAGE_POLICY) == 4) {
                    // iniciar varredura para exclus�o de mensagens expiradas
                    generatedEvent = new SystemEvent(getDeviceManager().getAdaptationManager());
                    generatedEvent.setEntityId(AdaptationDAO.ENTITY_ID_OF_ADAPTATION_MANAGEMENT);
                    generatedEvent.setName("Gatilho para exclus�o de mensagens expiradas ativado");
                    generatedEvent.setId(AdaptationManager.EVENT_START_TASK_OF_CLEANING_REPORS);
                    values = new HashMap<String, Object>();
                    values.put("event", generatedEvent);
                    values.put("time", getDeviceManager().getAdaptationManager().getIntervalCleanStoredMessages());
                    values.put("date", new Date());
                    values.put("method", EventManager.METHOD_DATE_PLUS_REPEATED_INTERVALS);
                    values.put("handler", this);
                    diagnosis.addChange(new Change(3, values));
                }
                // Inicia tarefa de varredura de erros em servi�os usando intervalo fixo (usado pelo caso 3);
                generatedEvent = new SystemEvent(getDeviceManager().getAdaptationManager());
                generatedEvent.setEntityId(AdaptationDAO.ENTITY_ID_OF_ADAPTATION_MANAGEMENT);
                generatedEvent.setName("Gatilho para varredura de erros em servi�os ativado");
                generatedEvent.setId(AdaptationManager.EVENT_START_TASK_OF_CHECKING_SERVICE_ERRORS);
                values = new HashMap<String, Object>();
                values.put("event", generatedEvent);
                values.put("time", getDeviceManager().getAdaptationManager().getScanIntervalOfServiceErrors()); // 
                values.put("date", new Date());
                values.put("method", EventManager.METHOD_DATE_PLUS_REPEATED_INTERVALS);
                values.put("handler", this);
                diagnosis.addChange(new Change(7, values));
            }
        } else if (event.getComponentManager().getComponentId() == DeviceManager.COMMUNICATION_COMPONENT_ID) {
            if (event.getId() == CommunicationManager.EVENT_NEW_INPUT_COMMUNICATION_INTERFACE_ADDRESS) {
                // busca estado anterior. Se for o mesmo n�o envia.
                content = getDeviceManager().getDataManager().getInstanceDAO().getBeforeCurrentContentValue(
                        CommunicationDAO.STATE_ID_OF_INPUT_COMMUNICATION_INTERFACE_CONFIGURATIONS,
                        ((PushServiceReceiver) event.getParameters().get("interface")).getInstance().getModelId(),
                        CommunicationDAO.ENTITY_ID_OF_INPUT_COMMUNICATION_INTERFACES,
                        CommunicationDAO.COMPONENT_ID);
                // se forem iguais n�o h� necessidade de atualizar
                if (!content.getValue().toString().equals(event.getParameters().get("configurations").toString())) {
                    // enviar novo endere�o ao backend
                    // fazer intera��o
                    interactionModel = adaptationDAO.getInteractionModel(AdaptationDAO.INTERACTION_TO_INFORM_NEW_INPUT_ADDRESS);
                    target = new Address(getDeviceManager().getBackendService().getAddress());
                    target.setLayer(Address.LAYER_SYSTEM);
                    target.setUid(getDeviceManager().getBackendService().getServiceUID());
                    values = new HashMap<String, Object>();
                    values.put("target", target);
                    values.put("interactionModel", interactionModel);
                    // adicionar a mudan�a
                    diagnosis.addChange(new Change(4, values));
                }
            }
        } else if (event.getComponentManager().getComponentId() == DeviceManager.EVENTS_COMPONENT_ID) {
            if (event.getId() == EventManager.EVENT_TIME_TRIGGER_ACHIEVED) {
                // apenas adiciona na fila: "event"
                generatedEvent = (Event) event.getParameters().get("event");
                generatedEvent.newTime();
                getDeviceManager().getAdaptationManager().newEvent(generatedEvent);
                // Futuramente fazer um teste se precisa cancelar: values.put("trigger")
            }
        } else if (event.getComponentManager().getComponentId() == DeviceManager.USER_COMPONENT_ID) {

        } else if (event.getComponentManager().getComponentId() == DeviceManager.ADAPTATION_COMPONENT_ID) {
            // Iniciar varredura de erros nos servi�os
            if (event.getId() == AdaptationManager.EVENT_START_TASK_OF_CHECKING_SERVICE_ERRORS) {
                // servi�o de upload
                up = getDeviceManager().getCommunicationManager().getUploadServices().get(0);
                // última vez que esse servi�o de upload enviou uma mensagem
                Content c = getDeviceManager().getDataManager().getEventModelDAO()
                        .getLastEventContentByLabelAndValue(up.getInstance().getId(), "uploadServiceId",
                                CommunicationManager.EVENT_MESSAGE_DELIVERED, CommunicationDAO.ENTITY_ID_OF_SERVICE_OF_UPLOAD_REPORTS,
                                CommunicationDAO.COMPONENT_ID);
                Date lastSentMessageByServiceUpload = (c == null) ? new Date(0L) : c.getTime();
                /* Se servi�o de upload possui mensagens para enviar, possui conex�o e 
                 n�o enviar a mensagem nem um intervalo definido pelo timeout da interface de 
                 conex�o utilizada mais um valor de toler�ncia em milissegundos e politica de uso de 
                 dados m�veis n�o permite o uso de dados m�veis e esta interface conectada. */
                /*
                 System.out.println("Checando erros do sersi�o de upload: ");
                 System.out.println("N�o Desconectado? " + !getDeviceManager().getCommunicationManager().isCompletelyDisconnected());
                 System.out.println("Relatos no banco de dados: " + getDeviceManager().getDataManager().getReportDAO().reportsCount(up.getService()));
                 System.out.println("N�o Utiliza dados m�veis: " + !getDeviceManager().getCommunicationManager().getCurrentCommunicationInterface().isUsesMobileData());
                 System.out.println("Timeout + intervalo limite: " + getDeviceManager().getCommunicationManager().getCurrentCommunicationInterface().getTimeout() + limitIntervalToUploadService);
                 System.out.println("�ltimo relato enviado: " + (System.currentTimeMillis() - lastSentMessageByServiceUpload.getTime()));
                 System.out.println("�ltimo erro da inst�ncia: " + adaptationDAO.getLastRecordedErrorFromInstance(up.getInstance().getId(), scanIntervalOfServiceErrors));
                 */
                if (up.isAllowedToPerformUpload()
                        && !getDeviceManager().getCommunicationManager().isCompletelyDisconnected() // n�o est� desconectado
                        && (getDeviceManager().getDataManager().getReportDAO().reportsCount(up.getService()) > 0) //  possui mensagens para enviar
                        && (getDeviceManager().getDataManager().getCommunicationDAO().getCurrentPreferentialPolicy(CommunicationDAO.MOBILE_DATA_POLICY) != 6 // politica de dados m�veis para n�o enviar
                        && !getDeviceManager().getCommunicationManager().getCurrentCommunicationInterface().isUsesMobileData()) // se n�o � interface de dados m�veis
                        && (getDeviceManager().getCommunicationManager().getCurrentCommunicationInterface().getTimeout() + getDeviceManager().getAdaptationManager().getLimitIntervalToUploadService())
                        < (System.currentTimeMillis() - lastSentMessageByServiceUpload.getTime())) {
                    if (adaptationDAO.getLastRecordedErrorFromInstance(up.getInstance().getId(), getDeviceManager().getAdaptationManager().getScanIntervalOfServiceErrors()) == null) { // para checar o último intervalo de erro, checar se no último intervalo foi identificado um warning
                        //Acorda o servi�o de upload;
                        values = new HashMap<String, Object>();
                        values.put("componentId", CommunicationDAO.COMPONENT_ID);
                        values.put("entityId", up.getInstance().getEntity().getId());
                        values.put("instanceId", up.getInstance().getModelId());
                        diagnosis.addChange(new Change(8, values));

                        // Salva no relat�rio de funcionamento um aviso;
                        values = new HashMap<String, Object>();
                        values.put("type", AdaptationDAO.FUNCTIONALITY_STATUS_TYPE_WARNING);
                        values.put("description", "UploadService to " + up.getService().getServiceUID() + " didn't executed on the expected interval and he was waked up.");
                        values.put("time", event.getTime());
                        values.put("instanceId", up.getInstance().getId()); // usado para verificar se j� deu um warning
                        diagnosis.addChange(new Change(10, values));
                    } else {
                        // Reinicia o servi�o
                        values = new HashMap<String, Object>();
                        values.put("componentId", CommunicationDAO.COMPONENT_ID);
                        values.put("entityId", up.getInstance().getEntity().getId());
                        values.put("instanceId", up.getInstance().getModelId());
                        diagnosis.addChange(new Change(9, values));

                        // Salva no relat�rio de funcionamento o erro
                        values = new HashMap<String, Object>();
                        values.put("type", AdaptationDAO.FUNCTIONALITY_STATUS_TYPE_ERROR);
                        values.put("description", "UploadService to " + up.getService().getServiceUID() + " didn't executed on the expected interval and he was restarted.");
                        values.put("time", event.getTime());
                        values.put("instanceId", up.getInstance().getId()); // usado para verificar se j� deu um warning
                        diagnosis.addChange(new Change(10, values));
                    }
                }
                // auxiliar reconection service
                // general reconection service
                                /*
                 Se o modulo de sensoriamento estiver completamente desconectado e 
                 servi�o de reconex�o geral n�o responder dentro do intervalo de maior 
                 timeout da interface tentando reconectar mais o limite do intervalo de reconex�o.
                 */
                for (ReconnectionService rs : getDeviceManager().getCommunicationManager().getReconnectionServices()) {
                    c = getDeviceManager().getDataManager().getEventModelDAO()
                            .getLastEventContentByLabelAndValue(rs.getInstance().getId(), "reconnectionService",
                                    CommunicationManager.EVENT_NEW_RECONNECTION_ATTEMPT,
                                    CommunicationDAO.ENTITY_ID_OF_RECONNECTION,
                                    CommunicationDAO.COMPONENT_ID);
                    Date lastReconectionAttempt = (c == null ? new Date() : c.getTime());
                    long timeout = 0;
                    for (CommunicationInterface ci : getDeviceManager().getCommunicationManager().getCommunicationInterfaces()) {
                        if (rs.getMethodOfReconnection() == ReconnectionService.METHOD_ONE_BY_TIME) {
                            // soma todos
                            timeout += ci.getTimeout();
                        } else {
                            // pega o maior
                            if (ci.getTimeout() > timeout) {
                                timeout = ci.getTimeout();
                            }
                        }
                    }
//                                    System.out.println("Teste de erro do servi�o de reconex�o geral: ");
//                                    System.out.println("Alguma interface possu� conex�o? "+rs.hasSomeInterfaceConnection());
//                                    System.out.println("Timeout + limit + reconnectionTime = "+(timeout + limitIntervalToUploadReconnectionService + rs.getReconnectionTime()));
//                                    System.out.println("�ltima tentativa: "+(System.currentTimeMillis() - lastReconectionAttempt.getTime()));
//                                    System.out.println("�ltimo erro: "+adaptationDAO.getLastRecordedErrorFromInstance(rs.getInstance().getId(), scanIntervalOfServiceErrors));
                    // deixar gen�rico em breve
                    if ((!rs.hasSomeInterfaceConnection()) // est� desconectado
                            && (timeout + getDeviceManager().getAdaptationManager().getLimitIntervalToUploadReconnectionService() + rs.getReconnectionTime())
                            < (System.currentTimeMillis() - lastReconectionAttempt.getTime())) {
                        if (adaptationDAO.getLastRecordedErrorFromInstance(rs.getInstance().getId(),
                                getDeviceManager().getAdaptationManager().getScanIntervalOfServiceErrors()) == null) {
                            //Acorda o servi�o de upload;
                            values = new HashMap<String, Object>();
                            values.put("componentId", CommunicationDAO.COMPONENT_ID);
                            values.put("entityId", rs.getInstance().getEntity().getId());
                            values.put("instanceId", rs.getInstance().getModelId());
                            diagnosis.addChange(new Change(8, values));

                            // Salva no relat�rio de funcionamento um aviso;
                            values = new HashMap<String, Object>();
                            values.put("type", AdaptationDAO.FUNCTIONALITY_STATUS_TYPE_WARNING);
                            values.put("description", "ReconectionService instanceId: " + rs.getInstance().getId() + " didn't executed on the expected interval and he was waked up.");
                            values.put("time", event.getTime());
                            values.put("instanceId", rs.getInstance().getId()); // usado para verificar se j� deu um warning
                            diagnosis.addChange(new Change(10, values));

                        } else {
                            // Reinicia o servi�o
                            values = new HashMap<String, Object>();
                            values.put("componentId", CommunicationDAO.COMPONENT_ID);
                            values.put("entityId", rs.getInstance().getEntity().getId());
                            values.put("instanceId", rs.getInstance().getModelId());
                            diagnosis.addChange(new Change(9, values));

                            // Salva no relat�rio de funcionamento o erro
                            values = new HashMap<String, Object>();
                            values.put("type", AdaptationDAO.FUNCTIONALITY_STATUS_TYPE_ERROR);
                            values.put("description", "ReconectionService instanceId: " + rs.getInstance().getId() + " didn't executed on the expected interval and he was restarted.");
                            values.put("time", event.getTime());
                            values.put("instanceId", rs.getInstance().getId()); // usado para verificar se j� deu um warning
                            diagnosis.addChange(new Change(10, values));
                        }
                    }
                }
            } else // Iniciar exclus�o de mensagens expiradas
            if (event.getId() == AdaptationManager.EVENT_START_TASK_OF_CLEANING_REPORS) {
                diagnosis.addChange(new Change(11, null));
            } else // Gerar de relat�rios de funcionamento
            if (event.getId() == AdaptationManager.EVENT_GENERATED_EVENT_TO_REPORTING_TRIGGED) {
                values = new HashMap<String, Object>();
                values.put("time", new Date());
                diagnosis.addChange(new Change(12, values));
            } else // Relat�rio de funcionamento gerado
            if (event.getId() == AdaptationManager.EVENT_SYSTEM_REPORT_GENERATED) {
                // action: enviar relat�rio de funcionamento - fazer
                //values.put("report", event.getParameters().get("report"));
                //values.put("serviceId", getDeviceManager().getBackendService().getId());
                // fazer intera��o
                interactionModel = adaptationDAO.getInteractionModel(AdaptationDAO.INTERACTION_TO_REPORT_SENSING_MODULE_FUNCTIONALITY);
                target = new Address(getDeviceManager().getBackendService().getAddress());
                target.setLayer(Address.LAYER_SYSTEM);
                target.setUid(getDeviceManager().getBackendService().getServiceUID());
                values = new HashMap<String, Object>();
                values.put("target", target);
                values.put("interactionModel", interactionModel);
                values.put("report", event.getParameters().get("report"));
                values.put("uid", getDeviceManager().getBackendService().getApplicationUID());
                diagnosis.addChange(new Change(13, values));
                // apagar relatos de funcionamento antigos - fazer
                values = new HashMap<String, Object>();
                values.put("time", event.getParameters().get("time"));
                diagnosis.addChange(new Change(14, values));
                // action: atualizar última data de relato
                diagnosis.addChange(new Change(15, values));
            } else // Erro no loop de adapta��o
            if (event.getId() == AdaptationManager.EVENT_ADAPTATION_LOOP_ERROR) {
                // Salva no relat�rio de funcionamento um aviso;
                values = new HashMap<String, Object>();
                values.put("type", AdaptationDAO.FUNCTIONALITY_STATUS_TYPE_ERROR);
                values.put("description", "Adaptation loop error was found:{" + event.getParameters().get("error") + "}");
                values.put("time", event.getTime());
                diagnosis.addChange(new Change(10, values));
            } else // Erro no loop de adapta��o
            if (event.getId() == AdaptationManager.EVENT_UNKNOWN_EVENT_WARNING) {
                // Salva no relat�rio de funcionamento um aviso;
                values = new HashMap<String, Object>();
                values.put("type", AdaptationDAO.FUNCTIONALITY_STATUS_TYPE_WARNING);
                values.put("description", "Unknown was found:{" + event.getParameters().get("error") + "}");
                values.put("time", event.getTime());
                diagnosis.addChange(new Change(10, values));
            }
        } else if (event.getComponentManager().getComponentId() == TestManager.COMPONENT_ID) {
            if (event.getId() == TestManager.EVENT_GENERIC_EVENT) {
                // Quantidade de regras (rules); quantidade de condi��es (conditions);
                genericInteger1 = (Integer) event.getParameters().get("rules");
                genericInteger2 = (Integer) event.getParameters().get("conditions");
                for (int i = 0; i < genericInteger1; i++) {
                    for (int j = 0; j < genericInteger2; j++) {
                        adaptationDAO.getLastRecordedErrorFromInstance(1, getDeviceManager().getAdaptationManager().getIntervalAmongModuleStateReports());
                    }
                }
                values = new HashMap<String, Object>();
                values.put("event", event);
                diagnosis.addChange(new Change(16, values));
            } else if (event.getId() == TestManager.EVENT_START_INTERACTION) {
                /// fazer depois
                // ip (ip); porta (port);Quantidade de regras (rules); quantidade de condi��es (conditions);
                genericInteger1 = (Integer) event.getParameters().get("rules");
                genericInteger2 = (Integer) event.getParameters().get("conditions");
                for (int i = 0; i < genericInteger1; i++) {
                    for (int j = 0; j < genericInteger2; j++) {
                        adaptationDAO.getLastRecordedErrorFromInstance(1, getDeviceManager().getAdaptationManager().getIntervalAmongModuleStateReports());
                    }
                }
                interactionModel = (super.getDeviceManager().getDataManager().getAgentTypeDAO().getInteractionModel(TestManager.INTERACTION_REQUEST_RESPONSE));
                // par�metros: id o evento (eventId); tempo do evento (timestampEvent); ip (ip); porta(port)
                values = new HashMap<String, Object>();
                //target = new Address("http://" + event.getParameters().get("ip") + ":" + event.getParameters().get("port"));
                target = new Address(event.getParameters().get("ip") + ":" + event.getParameters().get("port"));
                target.setLayer(Address.LAYER_SYSTEM);
                target.setUid(event.getParameters().get("uid").toString());
                values.put("target", target);
                values.put("interactionModel", interactionModel);
                values.put("eventId", event.getDatabaseId());
                values.put("timestampEvent", event.getTime().getTime());
                // vai funcionar somente para desktop
                values.put("ip", getDeviceManager().getCommunicationManager().getMainPushServiceReceiver().getInterfaceConfigurations().get("ipv4Address"));
                values.put("port", getDeviceManager().getCommunicationManager().getMainPushServiceReceiver().getInterfaceConfigurations().get("port"));
                interactionModel.setContentToParameter("eventId", event.getDatabaseId());
                interactionModel.setContentToParameter("timestampEvent", event.getTime().getTime());
                diagnosis.addChange(new Change(17, values));
            } else if (event.getId() == TestManager.EVENT_SHUTDOWN_ANOTHER_AGENT) {
                interactionModel = (super.getDeviceManager().getDataManager().getAgentTypeDAO().getInteractionModel(TestManager.INTERACTION_REQUEST_SHUTDOWN));

                // par�metros: id o evento (eventId); tempo do evento (timestampEvent); ip (ip); porta(port)
                values = new HashMap<String, Object>();
                //target = new Address("http://" + event.getParameters().get("ip") + ":" + event.getParameters().get("port"));
                target = new Address(event.getParameters().get("ip") + ":" + event.getParameters().get("port"));
                target.setLayer(Address.LAYER_SYSTEM);
                target.setUid(event.getParameters().get("uid").toString());
                values.put("target", target);
                values.put("interactionModel", interactionModel);
                target = new Address();
                target.setLayer(Address.LAYER_SYSTEM);
                target.setUid(getDeviceManager().getBackendService().getApplicationUID());
                event.getParameters().put("origin", target);
                diagnosis.addChange(new Change(18, values));
            }
        }
        return diagnosis;
    }

}
