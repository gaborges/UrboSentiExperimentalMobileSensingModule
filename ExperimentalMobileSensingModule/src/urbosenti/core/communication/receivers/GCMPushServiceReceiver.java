/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.communication.receivers;

import urbosenti.core.communication.CommunicationManager;
import urbosenti.core.communication.PushServiceReceiver;

/**
 *
 * @author Guilherme
 */
public class GCMPushServiceReceiver extends PushServiceReceiver{

    public GCMPushServiceReceiver(CommunicationManager communicationManager) {
        super(communicationManager);
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addressDiscovery() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
