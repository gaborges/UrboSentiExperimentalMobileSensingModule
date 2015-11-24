/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.communication.interfaces;

import java.io.IOException;
import java.net.URL;
import urbosenti.core.communication.CommunicationInterface;
import urbosenti.core.communication.CommunicationManager;
import urbosenti.core.communication.MessageWrapper;

/**
 *
 * @author Guilherme
 */
public class MobileDataCommunicationInterface extends CommunicationInterface{
    
    public MobileDataCommunicationInterface() {
        super();
        setId(3);
        setName("Wired Interface");
        setUsesMobileData(false);
        setStatus(CommunicationInterface.STATUS_UNAVAILABLE);
    }
    
    @Override
    public boolean testConnection() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean connect(String address) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean disconnect() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean isAvailable() throws IOException, UnsupportedOperationException {
        setStatus(CommunicationInterface.STATUS_UNAVAILABLE);
        return false;
    }

    @Override
    public Object sendMessageWithResponse(CommunicationManager communicationManager, MessageWrapper messageWrapper) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean sendMessage(CommunicationManager communicationManager, MessageWrapper messageWrapper) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object receiveMessage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Object receiveMessage(int timeout) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean testConnection(URL url) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}