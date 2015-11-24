/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.adaptation.models;

import java.sql.SQLException;
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
public class ECADiagnosisModel extends AbstractDiagnosisModel {

    private HashMap<String, Object> values;
    private Event generatedEvent;
    private InteractionModel interactionModel;
    private Address target;
    private Content content;
    private UploadService up;

    public ECADiagnosisModel(AdaptationDAO adaptationDAO, DeviceManager deviceManager) {
        super(adaptationDAO, deviceManager);
    }

    @Override
    public Diagnosis interactionAnalysis(Event event, Diagnosis diagnosis, AdaptationDAO adaptationDAO) throws SQLException {
        if (event.getId() == AdaptationDAO.INTERACTION_TO_INFORM_NEW_MAXIMUM_UPLOAD_RATE) {
            // verifica se tem permitido alterar
            if (getDeviceManager().getDataManager().getCommunicationDAO().getCurrentPreferentialPolicy(CommunicationDAO.UPLOAD_REPORTS_POLICY) == 4) {
                for (UploadService uploadService : getDeviceManager().getCommunicationManager().getUploadServices()) {
                    // procura o serviço
                    if (uploadService.getService().getServiceUID().equals(event.getParameters().get("uid").toString())) {
                        // verifica se o valor é diferente do anterior
                        if (uploadService.getUploadRate() != Double.parseDouble(event.getParameters().get("uploadRate").toString())) {
                            // se sim
                            values = new HashMap<String, Object>();
                            values.put("uploadRate", event.getParameters().get("uploadRate"));  // Alterar taxa de upload; Nova Taxa	double	entre 1 e 0	uploadRate
                            values.put("instanceId", uploadService.getInstance().getModelId()); // Id da instância; inteito 	int	instanceId
                            diagnosis.addChange(new Change(5, values));
                        }
                        break;
                    }
                }
            }
        } else if (event.getId() == AdaptationDAO.INTERACTION_OF_FAIL_ON_SUBSCRIBE) { // Falha ao assinar
            // não necessário agora
        } else if (event.getId() == AdaptationDAO.INTERACTION_OF_MESSAGE_WONT_UNDERSTOOD) { // Mensagem não entendida
            // não necessário agora
        } else if (event.getId() == AdaptationDAO.INTERACTION_TO_CONFIRM_REGISTRATION) { // Assinatura aceita
            for (UploadService uploadService : getDeviceManager().getCommunicationManager().getUploadServices()) {
                // procura o serviço
                if (uploadService.getService().getServiceUID().equals(event.getParameters().get("uid").toString())) {
                    // se encontrar plano estático para alteração
                    values = new HashMap<String, Object>();
                    values.put("value", true); //	Alterar taxa de upload	Nova Taxa	double	entre 1 e 0	uploadRate
                    values.put("instanceId", uploadService.getInstance().getModelId()); //  Id da instância	inteito 	int	instanceId
                    diagnosis.addChange(new Change(6, values));
                    break;
                }
            }
        } else if (event.getId() == AdaptationDAO.INTERACTION_TO_REFUSE_REGISTRATION) { // Assinatura recusada
            // não necessário agora
        } else if (event.getId() == AdaptationDAO.INTERACTION_TO_CANCEL_REGISTRATION) { // Assinatura cancelada
            // não necessáro agora
        }
        return diagnosis;
    }

    @Override
    public Diagnosis eventAnalysis(Event event, Diagnosis diagnosis, AdaptationDAO adaptationDAO) throws SQLException, Exception {
        /* Analysis -- Diagnosis */
        if (event.getComponentManager().getComponentId() == DeviceManager.DEVICE_COMPONENT_ID) {
            if (event.getId() == DeviceManager.EVENT_DEVICE_SERVICES_INITIATED) {
                                // para cada serviço de upload, verificar se já estão registrados para receber atualizações do tempo de expiração dos relatos? Se não Adaptação
                // change=1;
                // testa se a política do serviço de upload é 4 = adaptativa, se não for não inicia isso
                if (getDeviceManager().getDataManager().getCommunicationDAO().getCurrentPreferentialPolicy(CommunicationDAO.UPLOAD_REPORTS_POLICY) == 4) {
                    // busca os serviços de upload, verifica se eles estão registrados para receber uploads
                    for (UploadService service : getDeviceManager().getCommunicationManager().getUploadServices()) {
                        if (!service.isSubscribedMaximumUploadRate()) {
                            // interação de subscribe -- fazer para cada uploadService
                            interactionModel = adaptationDAO.getInteractionModel(AdaptationDAO.INTERACTION_TO_SUBSCRIBE_THE_MAXIMUM_UPLOAD_RATE);
                                            // estático
                            // target = new Address(getDeviceManager().getBackendService().getAddress());
                            // target.setLayer(Address.LAYER_SYSTEM);
                            // target.setUid(getDeviceManager().getBackendService().getServiceUID());
                            // dinâmico
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

                // verificar se é permitido inicializar contador de envio de relatos  de funcionamento ao servidor
                if (getDeviceManager().getAdaptationManager().isAllowedReportingFunctionsToUploadService()) {
                    // inicializar contador de envio de relatos  de funcionamento ao servidor
                    generatedEvent = new SystemEvent(getDeviceManager().getAdaptationManager());
                    generatedEvent.setEntityId(AdaptationDAO.ENTITY_ID_OF_ADAPTATION_MANAGEMENT);
                    generatedEvent.setName("Gatilho de relatórios do sistema ativado");
                    generatedEvent.setId(AdaptationManager.EVENT_GENERATED_EVENT_TO_REPORTING_TRIGGED);
                    values = new HashMap<String, Object>();
                    values.put("event", generatedEvent);
                    values.put("time", getDeviceManager().getAdaptationManager().getIntervalAmongModuleStateReports()); // 
                    values.put("date", new Date());
                    values.put("method", EventManager.METHOD_DATE_PLUS_REPEATED_INTERVALS);
                    values.put("handler", this);
                    diagnosis.addChange(new Change(2, values));
                }
                                // iniciar varredura para exclusão de mensagens expiradas
                // se a política de armazenamento for 4 faz isso, senão não
                if (getDeviceManager().getDataManager().getCommunicationDAO().getCurrentPreferentialPolicy(CommunicationDAO.MESSAGE_STORAGE_POLICY) == 4) {
                    // iniciar varredura para exclusão de mensagens expiradas
                    generatedEvent = new SystemEvent(getDeviceManager().getAdaptationManager());
                    generatedEvent.setEntityId(AdaptationDAO.ENTITY_ID_OF_ADAPTATION_MANAGEMENT);
                    generatedEvent.setName("Gatilho para exclusão de mensagens expiradas ativado");
                    generatedEvent.setId(AdaptationManager.EVENT_START_TASK_OF_CLEANING_REPORS);
                    values = new HashMap<String, Object>();
                    values.put("event", generatedEvent);
                    values.put("time", getDeviceManager().getAdaptationManager().getIntervalCleanStoredMessages());
                    values.put("date", new Date());
                    values.put("method", EventManager.METHOD_DATE_PLUS_REPEATED_INTERVALS);
                    values.put("handler", this);
                    diagnosis.addChange(new Change(3, values));
                }
                // Inicia tarefa de varredura de erros em serviços usando intervalo fixo (usado pelo caso 3);
                generatedEvent = new SystemEvent(getDeviceManager().getAdaptationManager());
                generatedEvent.setEntityId(AdaptationDAO.ENTITY_ID_OF_ADAPTATION_MANAGEMENT);
                generatedEvent.setName("Gatilho para varredura de erros em serviços ativado");
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
                // busca estado anterior. Se for o mesmo não envia.
                content = getDeviceManager().getDataManager().getInstanceDAO().getBeforeCurrentContentValue(
                        CommunicationDAO.STATE_ID_OF_INPUT_COMMUNICATION_INTERFACE_CONFIGURATIONS,
                        ((PushServiceReceiver) event.getParameters().get("interface")).getInstance().getModelId(),
                        CommunicationDAO.ENTITY_ID_OF_INPUT_COMMUNICATION_INTERFACES,
                        CommunicationDAO.COMPONENT_ID);
                // se forem iguais não há necessidade de atualizar
                if (!content.getValue().toString().equals(event.getParameters().get("configurations").toString())) {
                                    // enviar novo endereço ao backend
                    // fazer interação
                    interactionModel = adaptationDAO.getInteractionModel(AdaptationDAO.INTERACTION_TO_INFORM_NEW_INPUT_ADDRESS);
                    target = new Address(getDeviceManager().getBackendService().getAddress());
                    target.setLayer(Address.LAYER_SYSTEM);
                    target.setUid(getDeviceManager().getBackendService().getServiceUID());
                    values = new HashMap<String, Object>();
                    values.put("target", target);
                    values.put("interactionModel", interactionModel);
                    // adicionar a mudança
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
            // Iniciar varredura de erros nos serviços
            if (event.getId() == AdaptationManager.EVENT_START_TASK_OF_CHECKING_SERVICE_ERRORS) {
                // serviço de upload
                up = getDeviceManager().getCommunicationManager().getUploadServices().get(0);
                // última vez que esse serviço de upload enviou uma mensagem
                Content c = getDeviceManager().getDataManager().getEventModelDAO()
                        .getLastEventContentByLabelAndValue(up.getInstance().getId(), "uploadServiceId",
                                CommunicationManager.EVENT_MESSAGE_DELIVERED, CommunicationDAO.ENTITY_ID_OF_SERVICE_OF_UPLOAD_REPORTS,
                                CommunicationDAO.COMPONENT_ID);
                Date lastSentMessageByServiceUpload = (c == null) ? new Date(0L) : c.getTime();
                /* Se serviço de upload possui mensagens para enviar, possui conexão e 
                 não enviar a mensagem nem um intervalo definido pelo timeout da interface de 
                 conexão utilizada mais um valor de tolerância em milissegundos e politica de uso de 
                 dados móveis não permite o uso de dados móveis e esta interface conectada. */
                /*
                 System.out.println("Checando erros do sersiço de upload: ");
                 System.out.println("Não Desconectado? " + !getDeviceManager().getCommunicationManager().isCompletelyDisconnected());
                 System.out.println("Relatos no banco de dados: " + getDeviceManager().getDataManager().getReportDAO().reportsCount(up.getService()));
                 System.out.println("Não Utiliza dados móveis: " + !getDeviceManager().getCommunicationManager().getCurrentCommunicationInterface().isUsesMobileData());
                 System.out.println("Timeout + intervalo limite: " + getDeviceManager().getCommunicationManager().getCurrentCommunicationInterface().getTimeout() + limitIntervalToUploadService);
                 System.out.println("Último relato enviado: " + (System.currentTimeMillis() - lastSentMessageByServiceUpload.getTime()));
                 System.out.println("Último erro da instância: " + adaptationDAO.getLastRecordedErrorFromInstance(up.getInstance().getId(), scanIntervalOfServiceErrors));
                 */
                if (up.isAllowedToPerformUpload()
                        && !getDeviceManager().getCommunicationManager().isCompletelyDisconnected() // não está desconectado
                        && (getDeviceManager().getDataManager().getReportDAO().reportsCount(up.getService()) > 0) //  possui mensagens para enviar
                        && (getDeviceManager().getDataManager().getCommunicationDAO().getCurrentPreferentialPolicy(CommunicationDAO.MOBILE_DATA_POLICY) != 6 // politica de dados móveis para não enviar
                        && !getDeviceManager().getCommunicationManager().getCurrentCommunicationInterface().isUsesMobileData()) // se não é interface de dados móveis
                        && (getDeviceManager().getCommunicationManager().getCurrentCommunicationInterface().getTimeout() + getDeviceManager().getAdaptationManager().getLimitIntervalToUploadService())
                        < (System.currentTimeMillis() - lastSentMessageByServiceUpload.getTime())) {
                    if (adaptationDAO.getLastRecordedErrorFromInstance(up.getInstance().getId(), getDeviceManager().getAdaptationManager().getScanIntervalOfServiceErrors()) == null) { // para checar o último intervalo de erro, checar se no último intervalo foi identificado um warning
                        //Acorda o serviço de upload;
                        values = new HashMap<String, Object>();
                        values.put("componentId", CommunicationDAO.COMPONENT_ID);
                        values.put("entityId", up.getInstance().getEntity().getId());
                        values.put("instanceId", up.getInstance().getModelId());
                        diagnosis.addChange(new Change(8, values));

                        // Salva no relatório de funcionamento um aviso;
                        values = new HashMap<String, Object>();
                        values.put("type", AdaptationDAO.FUNCTIONALITY_STATUS_TYPE_WARNING);
                        values.put("description", "UploadService to " + up.getService().getServiceUID() + " didn't executed on the expected interval and he was waked up.");
                        values.put("time", event.getTime());
                        values.put("instanceId", up.getInstance().getId()); // usado para verificar se já deu um warning
                        diagnosis.addChange(new Change(10, values));
                    } else {
                        // Reinicia o serviço
                        values = new HashMap<String, Object>();
                        values.put("componentId", CommunicationDAO.COMPONENT_ID);
                        values.put("entityId", up.getInstance().getEntity().getId());
                        values.put("instanceId", up.getInstance().getModelId());
                        diagnosis.addChange(new Change(9, values));

                        // Salva no relatório de funcionamento o erro
                        values = new HashMap<String, Object>();
                        values.put("type", AdaptationDAO.FUNCTIONALITY_STATUS_TYPE_ERROR);
                        values.put("description", "UploadService to " + up.getService().getServiceUID() + " didn't executed on the expected interval and he was restarted.");
                        values.put("time", event.getTime());
                        values.put("instanceId", up.getInstance().getId()); // usado para verificar se já deu um warning
                        diagnosis.addChange(new Change(10, values));
                    }
                }
                                // auxiliar reconection service
                // general reconection service
                                /*
                 Se o modulo de sensoriamento estiver completamente desconectado e 
                 serviço de reconexão geral não responder dentro do intervalo de maior 
                 timeout da interface tentando reconectar mais o limite do intervalo de reconexão.
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
//                                    System.out.println("Teste de erro do serviço de reconexão geral: ");
//                                    System.out.println("Alguma interface possuí conexão? "+rs.hasSomeInterfaceConnection());
//                                    System.out.println("Timeout + limit + reconnectionTime = "+(timeout + limitIntervalToUploadReconnectionService + rs.getReconnectionTime()));
//                                    System.out.println("Última tentativa: "+(System.currentTimeMillis() - lastReconectionAttempt.getTime()));
//                                    System.out.println("Último erro: "+adaptationDAO.getLastRecordedErrorFromInstance(rs.getInstance().getId(), scanIntervalOfServiceErrors));
                    // deixar genérico em breve
                    if ((!rs.hasSomeInterfaceConnection()) // está desconectado
                            && (timeout + getDeviceManager().getAdaptationManager().getLimitIntervalToUploadReconnectionService() + rs.getReconnectionTime())
                            < (System.currentTimeMillis() - lastReconectionAttempt.getTime())) {
                        if (adaptationDAO.getLastRecordedErrorFromInstance(rs.getInstance().getId(),
                                getDeviceManager().getAdaptationManager().getScanIntervalOfServiceErrors()) == null) {
                            //Acorda o serviço de upload;
                            values = new HashMap<String, Object>();
                            values.put("componentId", CommunicationDAO.COMPONENT_ID);
                            values.put("entityId", rs.getInstance().getEntity().getId());
                            values.put("instanceId", rs.getInstance().getModelId());
                            diagnosis.addChange(new Change(8, values));

                            // Salva no relatório de funcionamento um aviso;
                            values = new HashMap<String, Object>();
                            values.put("type", AdaptationDAO.FUNCTIONALITY_STATUS_TYPE_WARNING);
                            values.put("description", "ReconectionService instanceId: " + rs.getInstance().getId() + " didn't executed on the expected interval and he was waked up.");
                            values.put("time", event.getTime());
                            values.put("instanceId", rs.getInstance().getId()); // usado para verificar se já deu um warning
                            diagnosis.addChange(new Change(10, values));

                        } else {
                            // Reinicia o serviço
                            values = new HashMap<String, Object>();
                            values.put("componentId", CommunicationDAO.COMPONENT_ID);
                            values.put("entityId", rs.getInstance().getEntity().getId());
                            values.put("instanceId", rs.getInstance().getModelId());
                            diagnosis.addChange(new Change(9, values));

                            // Salva no relatório de funcionamento o erro
                            values = new HashMap<String, Object>();
                            values.put("type", AdaptationDAO.FUNCTIONALITY_STATUS_TYPE_ERROR);
                            values.put("description", "ReconectionService instanceId: " + rs.getInstance().getId() + " didn't executed on the expected interval and he was restarted.");
                            values.put("time", event.getTime());
                            values.put("instanceId", rs.getInstance().getId()); // usado para verificar se já deu um warning
                            diagnosis.addChange(new Change(10, values));
                        }
                    }
                }
            } else // Iniciar exclusão de mensagens expiradas
            if (event.getId() == AdaptationManager.EVENT_START_TASK_OF_CLEANING_REPORS) {
                diagnosis.addChange(new Change(11, null));
            } else // Gerar de relatórios de funcionamento
            if (event.getId() == AdaptationManager.EVENT_GENERATED_EVENT_TO_REPORTING_TRIGGED) {
                values = new HashMap<String, Object>();
                values.put("time", new Date());
                diagnosis.addChange(new Change(12, values));
            } else // Relatório de funcionamento gerado
            if (event.getId() == AdaptationManager.EVENT_SYSTEM_REPORT_GENERATED) {
                                // action: enviar relatório de funcionamento - fazer
                //values.put("report", event.getParameters().get("report"));
                //values.put("serviceId", getDeviceManager().getBackendService().getId());
                // fazer interação
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
            } else // Erro no loop de adaptação
            if (event.getId() == AdaptationManager.EVENT_ADAPTATION_LOOP_ERROR) {
                // Salva no relatório de funcionamento um aviso;
                values = new HashMap<String, Object>();
                values.put("type", AdaptationDAO.FUNCTIONALITY_STATUS_TYPE_ERROR);
                values.put("description", "Adaptation loop error was found:{" + event.getParameters().get("error") + "}");
                values.put("time", event.getTime());
                diagnosis.addChange(new Change(10, values));
            } else // Erro no loop de adaptação
            if (event.getId() == AdaptationManager.EVENT_UNKNOWN_EVENT_WARNING) {
                // Salva no relatório de funcionamento um aviso;
                values = new HashMap<String, Object>();
                values.put("type", AdaptationDAO.FUNCTIONALITY_STATUS_TYPE_WARNING);
                values.put("description", "Unknown was found:{" + event.getParameters().get("error") + "}");
                values.put("time", event.getTime());
                diagnosis.addChange(new Change(10, values));
            }
        }
        return diagnosis;
    }

}
