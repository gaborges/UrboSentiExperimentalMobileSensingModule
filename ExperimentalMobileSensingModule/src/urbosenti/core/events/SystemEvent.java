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
public class SystemEvent extends Event{

    public SystemEvent(ComponentManager origin) {
        super(origin, Event.SYSTEM_EVENT);
    }
    
}
