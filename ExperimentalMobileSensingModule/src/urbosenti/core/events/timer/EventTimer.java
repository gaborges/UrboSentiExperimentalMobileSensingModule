/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.events.timer;

import urbosenti.core.events.ApplicationHandler;
import urbosenti.core.events.EventManager;
import urbosenti.core.events.SystemHandler;

/**
 *
 * @author Guilherme
 */
public abstract class EventTimer {

    protected final TriggerRequest request;
    private final EventManager eventManager;

    public EventTimer(TriggerRequest request, EventManager em) {
        this.request = request;
        this.eventManager = em;
    }

    public TriggerRequest getTriggerRequest() {
        return this.request;
    }

    public abstract boolean isFinished();

    public abstract void start();

    public abstract void cancel();

    public boolean equalsTriggerRequest(TriggerRequest triggerRequest) {
        if (triggerRequest.getEvent().getId() == request.getEvent().getId()
                && triggerRequest.getTime().getTime() == request.getTime().getTime()
                && triggerRequest.getInterval().equals(request.getInterval())
                && triggerRequest.getMethod().equals(request.getMethod())) {
            if (triggerRequest.getHandler() instanceof ApplicationHandler 
                    && request.getHandler() instanceof ApplicationHandler
                    || triggerRequest.getHandler() instanceof SystemHandler 
                    && request.getHandler() instanceof SystemHandler) {
                return true;
            }
        }
        return false;
    }
    
    public void notifyTriggeredEvent(TriggerRequest tr){
        eventManager.newInternalEvent(EventManager.EVENT_TIME_TRIGGER_ACHIEVED,tr);        
    }
}
