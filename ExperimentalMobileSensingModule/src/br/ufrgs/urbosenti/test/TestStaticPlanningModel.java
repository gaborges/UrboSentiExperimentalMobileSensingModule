/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufrgs.urbosenti.test;

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
public class TestStaticPlanningModel extends AbstractPlanningModel {

    private HashMap<String, Object> values;
    private ArrayList<Action> actions;
    private Action action;

    public TestStaticPlanningModel(DeviceManager deviceManager) {
        super(deviceManager.getDataManager().getAdaptationDAO(),deviceManager);
        this.actions = new ArrayList();
    }

    @Override
    public ExecutionPlan getExecutionPlan(Change change, Plan plan, AdaptationDAO adaptationDAO, AbstractDiagnosisModel diagnosisModel) {
        // pega os par�metros atribu�dos pelo Analysis
        this.values = change.getParameters();
        // instancia a lista de a��es a ser adicionada no plano
        if(actions.size() > 0){
            this.actions = new ArrayList<Action>();
        }
        // vou bucar a mudan�a na verdade no banco de dados -- todas as a��es estáticas por enquanto
        switch (change.getId()) {
            case 1: // Subscribe no servidor
                action = new Action();
                action.setId(AdaptationDAO.INTERACTION_TO_SUBSCRIBE_THE_MAXIMUM_UPLOAD_RATE); // registro no servidor
                action.setActionType(Event.INTERATION_EVENT);
                action.setParameters(values);
                //action.setSynchronous(true);
                actions.add(action);
                break;
            case 2: // in�cializar o contador de envio de relat�rios de funcionamento ao servidor
                action = new Action();
                action.setId(EventManager.ACTION_ADD_TEMPORAL_TRIGGER_EVENT); // registro no servidor
                action.setTargetComponentId(EventDAO.COMPONENT_ID);
                action.setTargetEntityId(EventDAO.ENTITY_ID_OF_TEMPORAL_TRIGGER_OF_DYNAMIC_EVENTS);
                action.setParameters(values);
                actions.add(action);
                break;
            case 3: // iniciar varredura para exclus�o de mensagens expiradas
                action = new Action();
                action.setId(EventManager.ACTION_ADD_TEMPORAL_TRIGGER_EVENT); // registro no servidor
                action.setTargetComponentId(EventDAO.COMPONENT_ID);
                action.setTargetEntityId(EventDAO.ENTITY_ID_OF_TEMPORAL_TRIGGER_OF_DYNAMIC_EVENTS);
                action.setParameters(values);
                actions.add(action);
                break;
            case 4: // atualizar no servidor o novo endere�o
                action = new Action();
                action.setId(AdaptationDAO.INTERACTION_TO_SUBSCRIBE_THE_MAXIMUM_UPLOAD_RATE); // registro no servidor
                action.setActionType(Event.INTERATION_EVENT);
                action.setParameters(values);
                //action.setSynchronous(false);
                actions.add(action);
                break;
            case 5: // atualiza taxa de upload do servi�o
                action = new Action();
                action.setId(CommunicationManager.ACTION_UPDATE_UPLOAD_SERVICE_UPLOAD_RATE); // atualizar taxa de upload
                action.setParameters(values);
                action.setTargetEntityId(CommunicationDAO.ENTITY_ID_OF_SERVICE_OF_UPLOAD_REPORTS);
                action.setTargetComponentId(CommunicationDAO.COMPONENT_ID);
                actions.add(action);
                break;
            case 6: // atualiza taxa de upload do servi�o
                action = new Action();
                action.setId(CommunicationManager.ACTION_UPDATE_UPLOAD_SERVICE_SUBSCRIBED_MAXIMUM_UPLOAD_RATE); // atualizar taxa de upload
                action.setParameters(values);
                action.setTargetEntityId(CommunicationDAO.ENTITY_ID_OF_SERVICE_OF_UPLOAD_REPORTS);
                action.setTargetComponentId(CommunicationDAO.COMPONENT_ID);
                actions.add(action);
                break;
            case 7: // Inicia tarefa de varredura de erros em servi�os usando intervalo fixo (usado pelo caso 3);
                action = new Action();
                action.setId(EventManager.ACTION_ADD_TEMPORAL_TRIGGER_EVENT); // registro no servidor
                action.setTargetComponentId(EventDAO.COMPONENT_ID);
                action.setTargetEntityId(EventDAO.ENTITY_ID_OF_TEMPORAL_TRIGGER_OF_DYNAMIC_EVENTS);
                action.setParameters(values);
                actions.add(action);
                break;
            case 8: // acordar servi�o
                action = new Action();
                action.setId(DeviceManager.ACTION_WAKE_UP_SERVICE); // atualizar taxa de upload
                action.setParameters(values);
                action.setTargetEntityId(DeviceDAO.ENTITY_ID_OF_URBOSENTI_SERVICES);
                action.setTargetComponentId(DeviceDAO.COMPONENT_ID);
                actions.add(action);
                break;
            case 9: // reiniciar servi�o
                action = new Action();
                action.setId(DeviceManager.ACTION_RESTART_SERVICE);
                action.setParameters(values);
                action.setTargetEntityId(DeviceDAO.ENTITY_ID_OF_URBOSENTI_SERVICES);
                action.setTargetComponentId(DeviceDAO.COMPONENT_ID);
                actions.add(action);
                break;
            case 10: // armazenar erro/aviso para relato do funcionamento do m�dulo
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
            case 12: // Gerar de relat�rios de funcionamento
                action = new Action();
                action.setId(AdaptationManager.ACTION_GENERATE_EVENT_ERROR_REPORTING);
                action.setParameters(values);
                action.setTargetEntityId(AdaptationDAO.ENTITY_ID_OF_ADAPTATION_MANAGEMENT);
                action.setTargetComponentId(AdaptationDAO.COMPONENT_ID);
                actions.add(action);
                break;
            case 13: // action: enviar relat�rio de funcionamento
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
            case 15: // action: atualizar �ltima data de relato
                action = new Action();
                action.setId(AdaptationManager.ACTION_UPDATE_LAST_SENT_REPORT_DATE);
                action.setParameters(values);
                action.setTargetEntityId(AdaptationDAO.ENTITY_ID_OF_ADAPTATION_MANAGEMENT);
                action.setTargetComponentId(AdaptationDAO.COMPONENT_ID);
                // comentei para teste
                actions.add(action);
                break;
            case 16: // action: a��o gen�rica para teste
                action = new Action();
                action.setId(TestManager.ACTION_GENERIC_ACTION);
                action.setParameters(values);
                action.setTargetEntityId(TestManager.ENTITY_TEST_ENTITY);
                action.setTargetComponentId(TestManager.COMPONENT_ID);
                actions.add(action);
                break;
            case 17: // a��o: intera��o para requirir resposta (S�ncrona)
                action = new Action();
                action.setId(TestManager.INTERACTION_REQUEST_RESPONSE);
                action.setActionType(Event.INTERATION_EVENT);
                action.setParameters(values);
                action.setSynchronous(true);
                actions.add(action);
                break;
            case 18: // a��o: intera��o para requirir o desligamento (S�ncrona)
                action = new Action();
                action.setId(TestManager.INTERACTION_REQUEST_SHUTDOWN);
                action.setActionType(Event.INTERATION_EVENT);
                action.setParameters(values);
                action.setSynchronous(true);
                actions.add(action);
                break;
            case 19: // a��o: a��o para informar resposta de intera��o de teste
                action = new Action();
                action.setId(TestManager.ACTION_INTERACTION_RESULT);
                action.setParameters(values);
                action.setTargetEntityId(TestManager.ENTITY_TEST_ENTITY);
                action.setTargetComponentId(TestManager.COMPONENT_ID);
                actions.add(action);
                break;
            case 20: // a��o: intera��o resposta para requirir resposta (S�ncrona)
                action = new Action();
                action.setId(TestManager.INTERACTION_ANSWER_THE_REQUEST_RESPONSE);
                action.setActionType(Event.INTERATION_EVENT);
                action.setParameters(values);
                action.setSynchronous(true);
                actions.add(action);
                break;
            case 21: // a��o: desligar
                action = new Action();
                action.setId(TestManager.ACTION_SHUTDOWN);
                action.setTargetEntityId(TestManager.ENTITY_TEST_ENTITY);
                action.setTargetComponentId(TestManager.COMPONENT_ID);
                action.setParameters(values);
                actions.add(action);
                break;
            case 22: // action: a��o de intera��o para confirmar resposta
                action = new Action();
                action.setId(TestManager.ACTION_INTERACTION_RESULT);
                action.setParameters(values);
                action.setTargetEntityId(TestManager.ENTITY_TEST_ENTITY);
                action.setTargetComponentId(TestManager.COMPONENT_ID);
                actions.add(action);
                break;
            default:
                // evento de erro. Diagn�stico n�o conhecido
                break;
        }
        if (actions.size() > 0) {
            return new ExecutionPlan(actions);
        } else {
            return null;
        }
    }
}

