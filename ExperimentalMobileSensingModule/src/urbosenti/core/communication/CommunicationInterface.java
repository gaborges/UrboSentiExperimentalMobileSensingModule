/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.communication;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import urbosenti.core.device.model.Instance;
import urbosenti.core.device.model.InstanceRepresentative;
import java.net.URL;

/**
 *
 * @author Guilherme
 */
public abstract class CommunicationInterface<Object> implements InstanceRepresentative, Serializable  {
    
    public final static int STATUS_DISCONNECTED = 0;
    public final static int STATUS_CONNECTED = 1;
    public final static int STATUS_UNAVAILABLE = 2;
    public final static int STATUS_AVAILABLE = 3;
    // Tipos de interface
    public final static int WIRED_INTERFACE = 1;
    public final static int WIRELESS_INTERFACE = 2;
    public final static int MOBILE_DATA_INTERFACE = 3;
    public final static int DNT_INTERFACE = 4;
    // Suported Tecnologies
    // current tecnology
    private int id;
    private Instance instance;
    private String name;
    private double mobileDataUse; // only for Mobile data Interface
    private double averageResponseTime;
    private int status; // connected, disconected, disabled (not able to use)  able (able to use, but not tested)
    private int score;
    private int timeout; // ms
    private boolean usesMobileData;

    public CommunicationInterface() {
        averageResponseTime = 0;
        mobileDataUse = 0;
        score = 0;
        timeout = 0;
        usesMobileData = false;
    }
    
    public boolean isUsesMobileData() {
        return usesMobileData;
    }

    public void setUsesMobileData(boolean usesMobileData) {
        this.usesMobileData = usesMobileData;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMobileDataUse() {
        return mobileDataUse;
    }

    public void setMobileDataUse(double mobileDataUse) {
        this.mobileDataUse = mobileDataUse;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageLatency) {
        this.averageResponseTime = averageLatency;
    }
    
    public void updateEvaluationMetrics(MessageWrapper messageWrapper, Date initialTime, Date finalTime){
        messageWrapper.setSentTime(new Date());
        messageWrapper.setResponseTime(finalTime.getTime() - initialTime.getTime());
        messageWrapper.setSent(true);
        messageWrapper.setUsedCommunicationInterface(this);
        // Adiciona a latência média se naõ for a primeira vez
        if(this.getAverageResponseTime() > 0){
            this.setAverageResponseTime((this.getAverageResponseTime() + messageWrapper.getResponseTime())/2);
        }else{
            this.setAverageResponseTime(messageWrapper.getResponseTime());
        }  
    }
    
    public void updateEvaluationMetrics(MessageWrapper messageWrapper, long responseTime){
        messageWrapper.setSentTime(new Date());
        messageWrapper.setResponseTime(responseTime);
        messageWrapper.setSent(true);
        messageWrapper.setUsedCommunicationInterface(this);
        // Adiciona a latência média se naõ for a primeira vez
        if(this.getAverageResponseTime() > 0){
            this.setAverageResponseTime((this.getAverageResponseTime() + messageWrapper.getResponseTime())/2);
        }else{
            this.setAverageResponseTime(messageWrapper.getResponseTime());
        }  
    }
    
     public void updateEvaluationMetrics(MessageWrapper messageWrapper, Date initialTime){
        messageWrapper.setSentTime(new Date());
        messageWrapper.setResponseTime((new Date()).getTime() - initialTime.getTime());
        messageWrapper.setSent(true);
        messageWrapper.setUsedCommunicationInterface(this);
        // Adiciona a lat�ncia m�dia se n�o for a primeira vez
        if(this.getAverageResponseTime() > 0){
            this.setAverageResponseTime((this.getAverageResponseTime() + messageWrapper.getResponseTime())/2);
        }else{
            this.setAverageResponseTime(messageWrapper.getResponseTime());
        }  
    }

     /**
      * 
      * @return  retorna o estado da interface
      * @see #STATUS_CONNECTED
      * @see #STATUS_DISCONNECTED
      * @see #STATUS_AVAILABLE
      * @see #STATUS_UNAVAILABLE
      */
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
    
    /**
     * 
     * @return retorna true se foi suportada pelo sistema senão retorna false; OBS.: o Desenvolvedor deve definir é é possível ou não utilizá-la por implementação
     * @throws IOException
     * @throws UnsupportedOperationException 
     */    
    public abstract boolean isAvailable() throws UnsupportedOperationException,IOException;
    public abstract boolean testConnection() throws IOException, UnsupportedOperationException;
    public abstract boolean testConnection(URL url) throws IOException, UnsupportedOperationException;
    public abstract boolean connect(String address) throws IOException, UnsupportedOperationException;
    public abstract boolean disconnect() throws IOException, UnsupportedOperationException;
    public abstract boolean sendMessage(CommunicationManager communicationManager, MessageWrapper messageWrapper) throws java.net.SocketTimeoutException,IOException;
    public abstract Object sendMessageWithResponse(CommunicationManager communicationManager, MessageWrapper messageWrapper) throws java.net.SocketTimeoutException,IOException;
    public abstract Object receiveMessage() throws java.net.SocketTimeoutException,IOException;
    public abstract Object receiveMessage(int timeout) throws java.net.SocketTimeoutException,IOException;

    @Override
    public String toString() {
        return String.valueOf(instance.getId());
    }

}
