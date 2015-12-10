/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import urbosenti.adaptation.AdaptationManager;
import urbosenti.core.data.dao.EventDAO;
import urbosenti.core.device.ComponentManager;
import urbosenti.core.device.DeviceManager;
import urbosenti.core.device.model.FeedbackAnswer;
import urbosenti.core.events.timer.EventTimer;
import urbosenti.core.events.timer.EventTimerFactory;
import urbosenti.core.events.timer.TriggerRequest;
import urbosenti.core.events.timer.UrboSentiEventTimerFactory;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class EventManager extends ComponentManager {

    /**
     * int EVENT_TIME_TRIGGER_ACHIEVED = 1;
     *
     * <ul><li>id: 1</li>
     * <li>evento: Evento agendado</li>
     * <li>par�metros: Evento definido por quem agendou; Gatilho de
     * tempo</li></ul>
     */
    public static final int EVENT_TIME_TRIGGER_ACHIEVED = 1;
    /**
     * int ACTION_ADD_TEMPORAL_TRIGGER_EVENT = 1;
     *
     * <ul><li>id: 1</li>
     * <li>A��o: Adicionar um gatilho temporal de evento </li>
     * <li>par�metros: Evento; Intervalo de tempo (ms); Data inicial do
     * gatilho;Modo de opera��o;Tratador do evento</li></ul>
     */
    public static final int ACTION_ADD_TEMPORAL_TRIGGER_EVENT = 1;
    /**
     * int ACTION_CANCEL_TEMPORAL_TRIGGER_EVENT = 2;
     *
     * <ul><li>id: 2</li>
     * <li>A��o: Cancelar gatilho</li>
     * <li>par�metros: Evento; Intervalo de tempo (ms); Data inicial do
     * gatilho;Modo de opera��o;Tratador do evento</li></ul>
     */
    public static final int ACTION_CANCEL_TEMPORAL_TRIGGER_EVENT = 2;
    /**
     * int ACTION_TO_SYNCHROUNOUS_RETURN = 3;
     *
     * <ul><li>id: 3</li>
     * <li>A��o: A��o s�ncrona para retorno</li>
     * <li>par�metros: Evento da sess�o (sessionEvent) ; timeout para retorno do
     * resultado da aplica��o da a��o em ms (opcional- actionTimeout)</li></ul>
     */
    public static final int ACTION_TO_SYNCHROUNOUS_RETURN = 3;
    public static final int METHOD_ONLY_INTERVAL = 1;
    public static final int METHOD_ONLY_DATE = 2;
    public static final int METHOD_DATE_PLUS_REPEATED_INTERVALS = 3;
    public static final long DEFAULT_SYNCHRONOUS_EVENT_TIMEOUT = 20000;
    public static final long DEFAULT_FEEDBACK_DEFAULT_LIMITED_TIME = 500; // 500 milisegundos

    //private Timer timer;
    private final List<EventTimer> eventTimerWorkers;
    private boolean enableSystemHandler;
    private AdaptationManager systemHandler;
    private List<ApplicationHandler> applicationHandlers;
    // F�brica respons�vel por criar os objetos
    private EventTimerFactory eventTimerFactory;
    // Eventos s�ncronos
    private List<SynchronousEventSession> synchronousEvents;

    public EventManager(DeviceManager deviceManager, AdaptationManager systemHandler) {
        this(deviceManager);
        this.systemHandler = systemHandler;
        this.enableSystemHandler = true;
    }

    public EventManager(DeviceManager deviceManager) {
        super(deviceManager, urbosenti.core.data.dao.EventDAO.COMPONENT_ID);
        this.systemHandler = null;
        this.enableSystemHandler = false;
        this.applicationHandlers = new ArrayList();
        this.eventTimerWorkers = new ArrayList();
        this.eventTimerFactory = new UrboSentiEventTimerFactory();
        this.synchronousEvents = new ArrayList();
    }

    @Override
    public void onCreate() {
        if (DeveloperSettings.SHOW_FUNCTION_DEBUG_ACTIVITY) {
            System.out.println("Activating: " + getClass());
        }
    }

    public void newEvent(Event event) {
        /* if is a application event foward to the Application Handler*/
        switch (event.getOriginType()) {
            case Event.APPLICATION_EVENT:
                for (ApplicationHandler ah : applicationHandlers) {
                    ah.newEvent(event);
                }
                break;
            /* if is a system event foward to the SystemHandler*/
            case Event.SYSTEM_EVENT:
                if (systemHandler != null && enableSystemHandler) { /* if the SystemHandler is inactive the message is sent to the Application Handler*/

                    systemHandler.newEvent(event);
                } else {
                    for (ApplicationHandler ah : applicationHandlers) {
                        ah.newEvent(event);
                    }
                }
                break;
        }
    }

    /**
     * Retorna uma a��o para o evento s�ncrono.
     *
     * @param event
     * @return Retorna uma a��o, se retornal null ent�o o tempo foi expirado.
     * Utiliza o tempo padr�o de 20s para limite da resposta;
     */
    public Action newSynchronousEvent(Event event) throws InterruptedException {
        return this.newSynchronousEvent(event, DEFAULT_SYNCHRONOUS_EVENT_TIMEOUT);
    }

    /**
     * Retorna uma a��o para o evento s�ncrono ou null se o tempo for expirado
     *
     * @param event
     * @param timeout
     * @return Retorna uma a��o, se retornal null ent�o o tempo foi expirado.
     */
    public Action newSynchronousEvent(Event event, long timeout) throws InterruptedException {
        event.setSynchronous(true);
        SynchronousEventSession synchronousEventSession = new SynchronousEventSession(event, timeout);
        this.synchronousEvents.add(synchronousEventSession);
        this.newEvent(event);
        synchronized (event) {
            event.wait(timeout);
        }
        return synchronousEventSession.getReturnedAction();
    }

    public void setSystemHandler(AdaptationManager systemHandler) {
        this.systemHandler = systemHandler;
    }

    public void enableSystemHandler() {
        this.enableSystemHandler = true;
    }

    public void disableSystemHandler() {
        this.enableSystemHandler = true;
    }

    public void subscribe(ApplicationHandler applicationHandler) {
        applicationHandlers.add(applicationHandler);
    }

    public void unsubscribe(ApplicationHandler applicationHandler) {
        applicationHandlers.remove(applicationHandler);
    }

    /**
     * A��es disponibilizadas por esse componente:
     * <ul>
     * <li>1 - Adicionar um gatilho temporal de evento</li>
     * <li>2 - Cancelar gatilho</li>
     * </ul>
     *
     * @param action cont�m objeto a��o.
     * @return
     *
     */
    @Override
    public synchronized FeedbackAnswer applyAction(Action action) {
        TriggerRequest tr;
        Event event;
        switch (action.getId()) {
            case 1: // 1 - Adicionar um gatilho de tempo
                // Par�metros:
                // Evento
                tr = new TriggerRequest();
                tr.setEvent((Event) action.getParameters().get("event"));
                // Intervalo de tempo
                tr.setInterval((Long) action.getParameters().get("time"));
                // Data do gatilho
                tr.setTime((Date) action.getParameters().get("date"));
                // Modo de opera��o
                tr.setMethod((Integer) action.getParameters().get("method"));
                // Quem Registrou
                tr.setHandler((Object) action.getParameters().get("handler"));
                // Criar o worker e adicionar na fila --- Colocar um factory aqui, e adicionar as inst�ncias pela aplica��o
                EventTimer timerWorker = this.eventTimerFactory.getEventTimer(tr, this);
                this.eventTimerWorkers.add(timerWorker);
                // Iniciar o contador
                timerWorker.start();
                break;
            case 2: // 2 - Cancelar gatilho
                tr = new TriggerRequest();
                tr.setEvent((Event) action.getParameters().get("event"));
                // Intervalo de tempo
                tr.setInterval((Long) action.getParameters().get("time"));
                // Data do gatilho
                tr.setTime((Date) action.getParameters().get("date"));
                // Modo de opera��o
                tr.setMethod((Integer) action.getParameters().get("method"));
                // Quem Registrou
                tr.setHandler((Object) action.getParameters().get("handler"));
                // Verificar os workers ativos qual depes possui a requisi��o a ser cancelada
                boolean found = false;
                for (EventTimer t : eventTimerWorkers) {
                    if (!t.isFinished()
                            && t.equalsTriggerRequest(tr)) {
                        t.cancel();
                        found = true;
                        break;
                    }
                }
//                if(found)System.out.println("Achou :D"); 
//                else System.out.println("N�o Achou :'(");
                break;
            case 3:
                event = (Event) action.getParameters().get("sessionEvent");
                boolean flag = false;
                for (int i = 0; i < this.synchronousEvents.size(); i++) {
                    // procura o event
                    if (this.synchronousEvents.get(i).getEvent().getTime().getTime() == event.getTime().getTime()) {
                        // se o timeout n�o passou: adiciona a a��o e acorda o evento
                        if ((this.synchronousEvents.get(i).getEvent().getTime().getTime() + this.synchronousEvents.get(i).getTimeout()) > System.currentTimeMillis()) {
                            this.synchronousEvents.get(i).setReturnedAction(action);
                            this.synchronousEvents.get(i).getEvent().notifyAll();
                            // espera um tempo para verificar se ser� retornado um erro:
                            try {
                                if (action.getParameters().get("actionTimeout") != null) {
                                    wait((Long) action.getParameters().get("actionTimeout"));
                                } else {
                                    wait(DEFAULT_FEEDBACK_DEFAULT_LIMITED_TIME);
                                }
                            } catch (InterruptedException ex) {
                                Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            flag = true;
                            break;
                        } else {
                            flag = false;
                            break;
                        }
                    }
                }
                if (flag == false) {
                    action.setFeedbackAnswer(FeedbackAnswer.makeFeedbackAnswer(FeedbackAnswer.ACTION_RESULT_FAILED_TIMEOUT));
                }
                break;
        }
        // verifica se a a��o existe ou se houve algum resultado durante a execu��o
        if (action.getFeedbackAnswer() == null && action.getId() >= 1 && action.getId() <= 3) {
            action.setFeedbackAnswer(FeedbackAnswer.makeFeedbackAnswer(FeedbackAnswer.ACTION_RESULT_WAS_SUCCESSFUL));
        } else if (action.getFeedbackAnswer() == null) {
            action.setFeedbackAnswer(FeedbackAnswer.makeFeedbackAnswer(FeedbackAnswer.ACTION_DOES_NOT_EXIST));
        }
        return action.getFeedbackAnswer();
    }

    /**
     * Nesta fun��o os eventos gerados pelo gatilho de tempo, diferentemente dos
     * demais componentes, s�o enviados diretamente para quem realizou a
     * requisi��o do gatilho
     * <br><br>Eventos poss�veis
     * <ul>
     * <li>EventManager.EVENT_TIME_TRIGGER_ACHIEVED - Evento agendado</li>
     * </ul>
     *
     * @param eventId identificador do evento citado acima
     * @param params Par�metros oriundo dos objetos do componente <br><br>
     * @see #EVENT_TIME_TRIGGER_ACHIEVED
     */
    public void newInternalEvent(int eventId, Object... params) {
        TriggerRequest tr;
        HashMap<String, Object> values;
        Event event;
        switch (eventId) {
            case EVENT_TIME_TRIGGER_ACHIEVED: // 1 - Evento agendado

                tr = (TriggerRequest) params[0];

                // Par�metros do Evento
                values = new HashMap();
                values.put("event", tr.getEvent());
                values.put("trigger", tr);

                // O agendador pertence ao sistema
                if (tr.getHandler() instanceof SystemHandler) {

                    // cria��o do evento
                    event = new SystemEvent(this);
                    event.setId(EventManager.EVENT_TIME_TRIGGER_ACHIEVED);
                    event.setName("Evento agendado");
                    event.setTime(new Date());
                    event.setParameters(values);
                    event.setEntityId(EventDAO.ENTITY_ID_OF_TEMPORAL_TRIGGER_OF_DYNAMIC_EVENTS);

                    // Envia o evento para o tratador de sistema
                    ((SystemHandler) tr.getHandler()).newEvent(event);

                    // sen�o o agendador pertence a aplica��o
                } else if (tr.getHandler() instanceof ApplicationHandler) {

                    // cria��o do evento
                    event = new ApplicationEvent(this);
                    event.setId(EventManager.EVENT_TIME_TRIGGER_ACHIEVED);
                    event.setName("Evento agendado");
                    event.setTime(new Date());
                    event.setParameters(values);
                    event.setEntityId(EventDAO.ENTITY_ID_OF_TEMPORAL_TRIGGER_OF_DYNAMIC_EVENTS);

                    // Envia o evento para o tratador da aplica��o
                    ((ApplicationHandler) tr.getHandler()).newEvent(event);
                }
                break;
        }
    }

    /**
     *
     * @param externalEventTimerFactory contem a F�brica que foi desenvolvida
     * para o sistema operacional nativo
     */
    public void setExternalEventTimer(EventTimerFactory externalEventTimerFactory) {
        this.eventTimerFactory = externalEventTimerFactory;
    }

    /**
     * Para todos os gatilhos de tempo criados.
     */
    public void stopAllTriggers() {
        for (EventTimer eventTimerWorker : this.eventTimerWorkers) {
            eventTimerWorker.cancel();
        }
    }
}
