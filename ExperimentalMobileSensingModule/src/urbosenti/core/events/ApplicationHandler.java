/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.events;

import urbosenti.core.device.DeviceManager;

/**
 *
 * @author Guilherme
 */
public abstract class ApplicationHandler {
    /**
     *
     * @param This method receives a event from the Event Manager to process the event applying changes directly in the component.
     */
    private DeviceManager deviceManager;
    
    
    public ApplicationHandler(DeviceManager deviceManager){
        this.deviceManager = deviceManager;
    }

    public abstract void newEvent(Event event);
        
}
