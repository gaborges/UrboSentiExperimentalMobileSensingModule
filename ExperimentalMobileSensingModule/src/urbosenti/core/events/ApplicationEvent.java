/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.events;

import urbosenti.core.device.ComponentManager;

/**
 *
 * @author Guilherme
 */
public class ApplicationEvent extends Event {

    public ApplicationEvent(ComponentManager origin) {
        super(origin, Event.APPLICATION_EVENT);
    }

}
