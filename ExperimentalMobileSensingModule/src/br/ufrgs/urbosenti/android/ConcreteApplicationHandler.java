package br.ufrgs.urbosenti.android;

import android.util.Log;
import urbosenti.core.device.DeviceManager;
import urbosenti.core.events.ApplicationHandler;
import urbosenti.core.events.Event;

/**
 *
 * @author Guilherme
 */
public class ConcreteApplicationHandler extends ApplicationHandler{

    public ConcreteApplicationHandler(DeviceManager deviceManager) {
        super(deviceManager);
    }

    @Override
    public void newEvent(Event event) {
        //System.out.println("New application event: "+event.toString());
    	try{
    	Event.cleanEvent(event);
    	} catch (Exception ex){
    		Log.d("Erro", ex.getLocalizedMessage());
    	}
    }
    
}
