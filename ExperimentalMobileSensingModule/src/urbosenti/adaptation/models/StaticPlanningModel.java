/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.adaptation.models;

import java.util.ArrayList;
import java.util.HashMap;
import urbosenti.adaptation.AbstractDiagnosisModel;
import urbosenti.adaptation.AbstractPlanningModel;
import urbosenti.adaptation.AdaptationManager;
import urbosenti.adaptation.Change;
import urbosenti.adaptation.ExecutionPlan;
import urbosenti.adaptation.Plan;
import urbosenti.core.communication.CommunicationManager;
import urbosenti.core.data.dao.AdaptationDAO;
import urbosenti.core.data.dao.CommunicationDAO;
import urbosenti.core.data.dao.DeviceDAO;
import urbosenti.core.data.dao.EventDAO;
import urbosenti.core.device.DeviceManager;
import urbosenti.core.events.Action;
import urbosenti.core.events.Event;
import urbosenti.core.events.EventManager;

/**
 *
 * @author Guilherme
 */
public class StaticPlanningModel extends AbstractPlanningModel {

    private HashMap<String, Object> values;
    private ArrayList<Action> actions;
    private Action action;

    public StaticPlanningModel(AdaptationDAO adaptationDAO, DeviceManager deviceManager) {
        super(adaptationDAO, deviceManager);
        this.actions = new ArrayList<Action>();
    }

    @Override
    public ExecutionPlan getExecutionPlan(Change change, Plan plan, AdaptationDAO adaptationDAO, AbstractDiagnosisModel diagnosisModel) {
        // pega os parâmetros atribuídos pelo Analysis
        this.values = change.getParameters();
        // instancia a lista de ações a ser adicionada no plano
        if(actions.size() > 0){
            this.actions = new ArrayList<Action>();
        }
        // vou bucar a mudança na verdade no banco de dados -- todas as ações estáticas por enquanto
        switch (change.getId()) {
            case 1: // Subscribe no servidor
                action = new Action();
                action.setId(AdaptationDAO.INTERACTION_TO_SUBSCRIBE_THE_MAXIMUM_UPLOAD_RATE); // registro no servidor
                action.setActionType(Event.INTERATION_EVENT);
                action.setParameters(values);
                //action.setSynchronous(true);
                actions.add(action);
                break;
            case 2: // inícializar o contador de envio de relatórios de funcionamento ao servidor
                action = new Action();
                action.setId(EventManager.ACTION_ADD_TEMPORAL_TRIGGER_EVENT); // registro no servidor
                action.setTargetComponentId(EventDAO.COMPONENT_ID);
                action.setTargetEntityId(EventDAO.ENTITY_ID_OF_TEMPORAL_TRIGGER_OF_DYNAMIC_EVENTS);
                action.setParameters(values);
                actions.add(action);
                break;
            case 3: // iniciar varredura para exclusão de mensagens expiradas
                action = new Action();
                action.setId(EventManager.ACTION_ADD_TEMPORAL_TRIGGER_EVENT); // registro no servidor
                action.setTargetComponentId(EventDAO.COMPONENT_ID);
                action.setTargetEntityId(EventDAO.ENTITY_ID_OF_TEMPORAL_TRIGGER_OF_DYNAMIC_EVENTS);
                action.setParameters(values);
                actions.add(action);
                break;
            case 4: // atualizar no servidor o novo endereço
                action = new Action();
                action.setId(AdaptationDAO.INTERACTION_TO_SUBSCRIBE_THE_MAXIMUM_UPLOAD_RATE); // registro no servidor
                action.setActionType(Event.INTERATION_EVENT);
                action.setParameters(values);
                //action.setSynchronous(false);
                actions.add(action);
                break;
            case 5: // atualiza taxa de upload do serviço
                action = new Action();
                action.setId(CommunicationManager.ACTION_UPDATE_UPLOAD_SERVICE_UPLOAD_RATE); // atualizar taxa de upload
                action.setParameters(values);
                action.setTargetEntityId(CommunicationDAO.ENTITY_ID_OF_SERVICE_OF_UPLOAD_REPORTS);
                action.setTargetComponentId(CommunicationDAO.COMPONENT_ID);
                actions.add(action);
                break;
            case 6: // atualiza taxa de upload do serviço
                action = new Action();
                action.setId(CommunicationManager.ACTION_UPDATE_UPLOAD_SERVICE_SUBSCRIBED_MAXIMUM_UPLOAD_RATE); // atualizar taxa de upload
                action.setParameters(values);
                action.setTargetEntityId(CommunicationDAO.ENTITY_ID_OF_SERVICE_OF_UPLOAD_REPORTS);
                action.setTargetComponentId(CommunicationDAO.COMPONENT_ID);
                actions.add(action);
                break;
            case 7: // Inicia tarefa de varredura de erros em serviços usando intervalo fixo (usado pelo caso 3);
                action = new Action();
                action.setId(EventManager.ACTION_ADD_TEMPORAL_TRIGGER_EVENT); // registro no servidor
                action.setTargetComponentId(EventDAO.COMPONENT_ID);
                action.setTargetEntityId(EventDAO.ENTITY_ID_OF_TEMPORAL_TRIGGER_OF_DYNAMIC_EVENTS);
                action.setParameters(values);
                actions.add(action);
                break;
            case 8: // acordar serviço
                action = new Action();
                action.setId(DeviceManager.ACTION_WAKE_UP_SERVICE); // atualizar taxa de upload
                action.setParameters(values);
                action.setTargetEntityId(DeviceDAO.ENTITY_ID_OF_URBOSENTI_SERVICES);
                action.setTargetComponentId(DeviceDAO.COMPONENT_ID);
                actions.add(action);
                break;
            case 9: // reiniciar serviço
                action = new Action();
                action.setId(DeviceManager.ACTION_RESTART_SERVICE);
                action.setParameters(values);
                action.setTargetEntityId(DeviceDAO.ENTITY_ID_OF_URBOSENTI_SERVICES);
                action.setTargetComponentId(DeviceDAO.COMPONENT_ID);
                actions.add(action);
                break;
            case 10: // armazenar erro/aviso para relato do funcionamento do módulo
                action = new Action();
                action.setId(AdaptationManager.ACTION_STORE_INTERNAL_ERROR);
                action.setParameters(values);
                action.setTargetEntityId(AdaptationDAO.ENTITY_ID_OF_ADAPTATION_MANAGEMENT);
                action.setTargetComponentId(AdaptationDAO.COMPONENT_ID);
                actions.add(action);
                break;
            case 11: //Apagar mensagens expiradas
                action = new Action();
                action.setId(CommunicationManager.ACTION_DELETE_EXPIRED_MESSAGES);
                action.setParameters(values);
                action.setTargetEntityId(CommunicationDAO.ENTITY_ID_OF_REPORTS_STORAGE);
                action.setTargetComponentId(CommunicationDAO.COMPONENT_ID);
                actions.add(action);
                break;
            case 12: // Gerar de relatórios de funcionamento
                action = new Action();
                action.setId(AdaptationManager.ACTION_GENERATE_EVENT_ERROR_REPORTING);
                action.setParameters(values);
                action.setTargetEntityId(AdaptationDAO.ENTITY_ID_OF_ADAPTATION_MANAGEMENT);
                action.setTargetComponentId(AdaptationDAO.COMPONENT_ID);
                actions.add(action);
                break;
            case 13: // action: enviar relatório de funcionamento
                action = new Action();
                action.setId(AdaptationDAO.INTERACTION_TO_REPORT_SENSING_MODULE_FUNCTIONALITY); // registro no servidor
                action.setActionType(Event.INTERATION_EVENT);
                action.setParameters(values);
                action.setSynchronous(false);
                actions.add(action);
                break;
            case 14: // apagar relatos de funcionamento antigos
                action = new Action();
                action.setId(AdaptationManager.ACTION_REMOVE_SENT_FUNCTIONALITY_REPORTS);
                action.setParameters(values);
                action.setTargetEntityId(AdaptationDAO.ENTITY_ID_OF_ADAPTATION_MANAGEMENT);
                action.setTargetComponentId(AdaptationDAO.COMPONENT_ID);
                //actions.add(action);
                break;
            case 15: // action: atualizar última data de relato
                action = new Action();
                action.setId(AdaptationManager.ACTION_UPDATE_LAST_SENT_REPORT_DATE);
                action.setParameters(values);
                action.setTargetEntityId(AdaptationDAO.ENTITY_ID_OF_ADAPTATION_MANAGEMENT);
                action.setTargetComponentId(AdaptationDAO.COMPONENT_ID);
                // comentei para teste
                actions.add(action);
                break;
            default:
                // evento de erro. Diagnóstico não conhecido
                break;
        }
        if (actions.size() > 0) {
            return new ExecutionPlan(actions);
        } else {
            return null;
        }
    }
}
