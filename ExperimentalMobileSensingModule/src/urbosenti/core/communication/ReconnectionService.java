/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.communication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.util.Log;
import urbosenti.core.data.dao.CommunicationDAO;
import urbosenti.core.device.UrboSentiService;
import urbosenti.core.device.model.Content;
import urbosenti.core.device.model.Instance;
import urbosenti.core.device.model.InstanceRepresentative;
import urbosenti.core.device.model.State;

/**
 *
 * @author Guilherme
 */
public class ReconnectionService extends UrboSentiService implements Runnable, InstanceRepresentative {

    private final CommunicationManager communicationManager;
    private final List<CommunicationInterface> communicationInterfaces;
    private boolean reconnected;
    public static int METHOD_ONE_BY_TIME = 1;
    public static int METHOD_ALL_BY_ONCE = 2;
    private final List<Thread> connectionTesters;
    private final Object monitor;
    private final Thread service;
    private final Instance instance;
    /**
     * default 60 segundos Representado em milisegundos então (60 000)
     */
    private Long reconnectionTime;
    /**
     * Se 1 = Tenta somente em um método. Default. Se 2 = Tenta em todos os
     * métodos.
     */
    private int methodOfReconnection;

//    public ReconnectionService(CommunicationManager cm, List<CommunicationInterface> communicationInterfaces) {
//        this.communicationManager = cm;
//        this.communicationInterfaces = communicationInterfaces;
//        this.reconnectionTime = 60000;
//        this.methodOfReconnection = 1;
//        this.reconnected = false;
//        this.monitor = new Object();
//        this.connectionTesters = new ArrayList(communicationInterfaces.size());
//        for (CommunicationInterface ci : communicationInterfaces) {
//            this.connectionTesters.add(new Thread(new ConnectionTester(ci)));
//        }
//        this.service = new Thread(this);
//        this.instance = null;
//    }
    public ReconnectionService(CommunicationManager cm, List<CommunicationInterface> communicationInterfaces, Instance instance) {
        this.communicationManager = cm;
        this.communicationInterfaces = communicationInterfaces;
        this.reconnectionTime = new Long(60000);
        this.methodOfReconnection = 1;
        this.reconnected = false;
        this.monitor = new Object();
        this.connectionTesters = new ArrayList(communicationInterfaces.size());
        for (CommunicationInterface ci : communicationInterfaces) {
            this.connectionTesters.add(new Thread(new ConnectionTester(ci,this)));
        }
        this.service = new Thread(this);
        this.instance = instance;
        for (State s : instance.getStates()) {
            if (s.getModelId() == CommunicationDAO.STATE_ID_OF_RECONNECTION_INTERVAL) {
                this.reconnectionTime = (Long) Content.parseContent(s.getDataType(), s.getCurrentValue());
            } else {
                if (s.getModelId() == CommunicationDAO.STATE_ID_OF_RECONNECTION_METHOD) {
                    this.methodOfReconnection = (Integer) Content.parseContent(s.getDataType(), s.getCurrentValue());
                    if(this.methodOfReconnection != 1 && this.methodOfReconnection != 2){
                        throw new Error("Reconnection Method value '"+this.methodOfReconnection+"' is invalid!");
                    }
                }
            }
        }
    }

    public synchronized void setReconnectionTime(Long reconnectionTime) {
        this.reconnectionTime = reconnectionTime;
    }

    public synchronized Long getReconnectionTime() {
        return reconnectionTime;
    }

    public void setReconnectionMethodOneByTime() {
        this.methodOfReconnection = 1;
    }

    public void setReconnectionMethodAllByOnce() {
        this.methodOfReconnection = 2;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (reconnected) {
                    synchronized (this) {
                        wait();
                    }
                } else {
                    reconectionProcess();
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ReconnectionService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void reconectionProcess() throws InterruptedException {
        CommunicationInterface current;
        while (!reconnected) {
            communicationManager.notifyNewAttemptToReconnect(this);
            synchronized (this) {
                if (reconnectionTime > 0) {
                    wait(reconnectionTime);
                }             
            }
            if (this.methodOfReconnection == METHOD_ONE_BY_TIME) {
                Iterator<CommunicationInterface> iterator = communicationInterfaces.iterator();
                while (iterator.hasNext()) {
                    current = iterator.next();
                    try {
                        if (current.testConnection()) {
                            communicationManager.notifyReconnection(current,this);
                            reconnected = true;
                        } else {
                            communicationManager.notifyReconnectionNotSucceed(current,this);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ReconnectionService.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnsupportedOperationException ex) {
                        Logger.getLogger(ReconnectionService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if (this.methodOfReconnection == METHOD_ALL_BY_ONCE) {
                for (Thread tester : connectionTesters) {
                    tester.start();
                }
                for (Thread tester : connectionTesters) {
                    tester.join();
                }
            }
        }
    }

    /**
     *
     * @return Retorna a instância se esta foi atribuída pelo construtor, senão
     * retorna <b>null</b>;
     */
    @Override
    public Instance getInstance() {
        return instance;
    }

    @Override
    public void start() {
        if (!service.isAlive()) {
            this.service.start();
        }
        // testa conexães, se uma estiver conectada adiciona true em reconnected para não necessitar executar o processo
        for(CommunicationInterface ci : communicationInterfaces){
            try {
                if(ci.testConnection()){
                    this.reconnected = true;
                    break;
                }
            } catch (IOException ex) {
                Logger.getLogger(ReconnectionService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedOperationException ex) {
                Logger.getLogger(ReconnectionService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void stop() {
        for (Thread tester : connectionTesters) {
            tester.interrupt();
        }
        this.service.interrupt();
    }

    public synchronized void requireConnectionTest() {
        this.reconnected = false;
        this.wakeUp();
    }

    @Override
    public synchronized void wakeUp() {
        notifyAll();
    }

    class ConnectionTester implements Runnable {

        private final CommunicationInterface communicationInterface;
        private final ReconnectionService reconnectionService;

        public ConnectionTester(CommunicationInterface communicationInterface, ReconnectionService reconnectionService) {
            this.communicationInterface = communicationInterface;
            this.reconnectionService = reconnectionService;
        }

        @Override
        public void run() {
            try {
                if (communicationInterface.testConnection()) {
                    communicationManager.notifyReconnection(communicationInterface,reconnectionService);
                    synchronized (monitor) {
                        reconnected = true;
                    }
                } else {
                    communicationManager.notifyReconnectionNotSucceed(communicationInterface,reconnectionService);
                }
            } catch (IOException ex) {
                communicationManager.notifyReconnectionNotSucceed(communicationInterface,reconnectionService);
            } catch (UnsupportedOperationException ex) {
                communicationManager.notifyReconnectionNotSucceed(communicationInterface,reconnectionService);
            }
        }
    }

    @Override
    public String toString() {
        return String.valueOf(instance.getId());
    }

    public int getMethodOfReconnection() {
        return methodOfReconnection;
    }
    
    /**
     * 
     * @return retorna se alguma interface desse serviço tem conexão
     */
    public boolean hasSomeInterfaceConnection(){
        for(CommunicationInterface ci : communicationInterfaces){
            if(ci.getStatus() == CommunicationInterface.STATUS_CONNECTED){
                return true;
            }
        }
        return false;
    }
}
