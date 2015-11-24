/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.device.model;

/**
 *
 * @author Guilherme
 */
public class EventTarget {
    private TargetOrigin target;
    private EventModel event;
    private boolean mandatory;

    public TargetOrigin getTarget() {
        return target;
    }

    public void setTarget(TargetOrigin target) {
        this.target = target;
    }

    public EventModel getEvent() {
        return event;
    }

    public void setEvent(EventModel event) {
        this.event = event;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }
    
    
}
