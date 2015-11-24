/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.events;

/**
 *
 * @author Guilherme
 */
public class SynchronousEventSession {
    private final Event event;
    private final long timeout;
    private Action returnedAction;

    public SynchronousEventSession(Event event, long timeout) {
        this.event = event;
        this.timeout = timeout;
        this.returnedAction = null;
    }

    public void setReturnedAction(Action returnedAction) {
        this.returnedAction = returnedAction;
    }

    public Event getEvent() {
        return event;
    }

    public Action getReturnedAction() {
        return returnedAction;
    }

    public long getTimeout() {
        return timeout;
    }    
        
}
