/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.communication;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import android.util.Log;
import urbosenti.core.data.dao.CommunicationDAO;
import urbosenti.core.device.UrboSentiService;
import urbosenti.core.device.model.Content;
import urbosenti.core.device.model.Instance;
import urbosenti.core.device.model.InstanceRepresentative;
import urbosenti.core.device.model.Service;
import urbosenti.core.device.model.State;
import urbosenti.user.User;

/**
 *
 * @author Guilherme
 */
public class UploadService extends UrboSentiService implements Runnable, InstanceRepresentative {

    public Service service;
    private boolean running; // indica se o servidor está rodando ou não
    private final Thread uploadServiceThread;
    private final CommunicationManager communicationManager;
    private int countPriorityMessage;
    private int countNormalMessage;
    private final Instance instance;
    private Long uploadInterval; // Intervalo de upload
    private Double uploadRate; // Taxa de upload atribuída dinãmicamente. Inicialmente 1;
    private int limitOfReportsSentByUploadInterval; // Limite do envio de relatos por intervalo, padrão 20 (Posso fazer experimentos)
    private boolean allowedToPerformUpload;
    private boolean subscribedMaximumUploadRate;

    public UploadService(Service service, CommunicationManager communicationManager, Instance instance) {
        this.service = service;
        this.communicationManager = communicationManager;
        this.uploadServiceThread = new Thread(this);
        this.instance = instance;
        for (State s : instance.getStates()) {
            if (s.getModelId() == CommunicationDAO.STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_UPLOAD_INTERVAL) {
                uploadInterval = (Long) Content.parseContent(s.getDataType(), s.getCurrentValue());
            } else if (s.getModelId() == CommunicationDAO.STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_FOR_UPLOAD_RATE) {
                uploadRate = (Double) Content.parseContent(s.getDataType(), s.getCurrentValue());
            } else if (s.getModelId() == CommunicationDAO.STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_ABOUT_IS_EXECUTING) {
                // não precisa usar - estado não é interessante
            } else if (s.getModelId() == CommunicationDAO.STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_ALLOWED_TO_PERFORM_UPLOAD) {
                allowedToPerformUpload = (Boolean) Content.parseContent(s.getDataType(), s.getCurrentValue());
                allowedToPerformUpload = true;
            } else if (s.getModelId() == CommunicationDAO.STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_ABOUT_AMOUNT_OF_MESSAGES_UPLOADED_BY_INTERVAL) {
                limitOfReportsSentByUploadInterval = (Integer) Content.parseContent(s.getDataType(), s.getCurrentValue());
            } else if (s.getModelId() == CommunicationDAO.STATE_ID_OF_UPLOAD_PERIODIC_REPORTS_SUBSCRIBED_MAXIMUM_UPLOAD_RATE) {
                this.subscribedMaximumUploadRate = (Boolean) Content.parseContent(s.getDataType(), s.getCurrentValue());
            } 
        }
    }

    /**
     * Inicia o serviço de upload
     */
    @Override
    public void start() {
        if (service == null) {
            throw new Error("Upload server not specified! - Remember to use: deviceManager.getCommunicationManager().addUploadServer(uploadServer)");
        }
        this.running = true;
        if (!uploadServiceThread.isAlive()) {
            uploadServiceThread.start();
        }
    }

    /**
     * Para o serviço de upload
     */
    @Override
    public synchronized void stop() {
        this.running = false;
        this.uploadServiceThread.interrupt();
    }

    /**
     * Método utilizado para upload de relatos para um Ãºnico servidor. Depois
     * fazer um para uploads mÃºltiplos adicionando 2 métodos.
     * <ul><li>public static UploadService createUploadService(Agent Server)
     * throws InterruptException; </li>
     * <li>public synchronized addUploadService(UploadService up);</li>
     * <li> public boolean startUploadService(UploadService up);</li>
     * <li> UploadService up = createUploadService(Agent Server);</li>
     * <li> up.addReport();</li></ul>
     *
     */
    @Override
    public void run() {
        try {
            while (isRunning()) {
                boolean upload = true;
                synchronized (this) {
                    if (!allowedToPerformUpload) {
                        upload = false;
                        wait();
                    }
                }
                if (upload) {
                    communicationManager.newInternalEvent(CommunicationManager.EVENT_NEW_START_OF_UPLOAD_SERVICE_FUNCTION_LOOP, this.getInstance().getModelId());
                    communicationManager.uploadServiceFunction(service, this,this.uploadInterval,this.uploadRate,this.limitOfReportsSentByUploadInterval);
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param message Adiciona um report para envio ao servidor.
     */
    public void sendAssynchronousReport(Message message) throws SQLException {
        // recebe o report para envio
        // cria envelope
        if (message.getOrigin() == null) {
            message.setOrigin(new Address());
            message.getOrigin().setLayer(Address.LAYER_APPLICATION);
        }
        // adiciona o application UID para o serviço
        message.getOrigin().setUid(service.getApplicationUID());
        // cria o MessageWrapper que será utilizado para criar o envelope
        MessageWrapper messageWrapper = new MessageWrapper(message);
        // Adiciona o servidor visado
        messageWrapper.getMessage().setTarget(new Address());
        messageWrapper.getMessage().getTarget().setAddress(service.getAddress());
        messageWrapper.getMessage().getTarget().setLayer(Address.LAYER_APPLICATION);
        messageWrapper.getMessage().getTarget().setUid(service.getServiceUID());
        // adiciona o assunto da mensagem
        messageWrapper.getMessage().setSubject(Message.SUBJECT_UPLOAD_REPORT);
        // adiciona o encapsulamente específico para relatos
        // verifica se o módulo de usuário está habilitado, se tiver ID do usuário monitorado é enviado
        if (communicationManager.getDeviceManager().getUserManager() != null) {
            User user;
            String contentReport = "<report>";
            try {
                user = communicationManager.getDeviceManager().getUserManager().getMonitoredUser();
                if (user != null) {
                    contentReport = "<report userId=\"" + user.getId() + "\">";
                }
            } catch (SQLException ex) {
                Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            messageWrapper.getMessage().setContent(contentReport + messageWrapper.getMessage().getContent() + "</report>");
        } else {
            messageWrapper.getMessage().setContent("<report>" + messageWrapper.getMessage().getContent() + "</report>");
        }
        try {
            // Cria o envelope XML da UrboSenti correspondente da mensagem
            messageWrapper.build();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        // adiciona na fila de upload
        this.addReport(messageWrapper);
    }

    /**
     * @param messageWrapper Adiciona um novo relato para ser feito o upload. Se
     * política 3 e o relato não foi aprovado ainda ele é adicionado na fila de
     * espera e um evento avisando a aplicação que o relato está esperando sua
     * aprovação.
     */
    private synchronized void addReport(MessageWrapper messageWrapper) throws SQLException {
        // Verifica política de upload de relatos
        // se 3 gera adiciona na fila para aprovação da aplicação e utiliza política de armazenamento
        if (communicationManager.getUploadMessagingPolicy() == 3 && !messageWrapper.isChecked()) {
            //this.messagesNotChecked.add(messageWrapper);
            messageWrapper.setUnChecked();
            communicationManager.storagePolice(messageWrapper, this.service); // depois vejo se uso ou não
            communicationManager.newInternalEvent(CommunicationManager.EVENT_REPORT_AWAITING_APPROVAL, messageWrapper.getMessage());
        } else {
            // mensagem pode ser enviada
            messageWrapper.setChecked();
            communicationManager.storagePolice(messageWrapper, this.service);
            notifyAll();
        }
    }

    /**
     * @return retorna a primeira MessageWrapper da fila <b>normal</b> contendo
     * um relato com prioridade <b>normal</b>. Esse relato continua da fila.
     * OBS.: SOmente utilizado pelo método de upload dinãmico, pois tem um loop
     * infinito dentro.
     * @throws InterruptedException
     * @throws java.sql.SQLException
     */
    protected synchronized MessageWrapper getNormalReport() throws InterruptedException, SQLException {
        MessageWrapper mw;
        while (true) {
            mw = communicationManager.getDeviceManager().getDataManager().getReportDAO()
                    .getOldestCheckedNotSent(Message.NORMAL_PRIORITY, this.service);
            if (mw == null) {
                wait();
            } else {
                break;
            }
        }
        return mw;
    }

    /**
     * @return retorna a primeira MessageWrapper da fila <b>prioritária</b>
     * contendo um relato com prioridade <b>prioritária</b>. Esse relato
     * continua da fila. OBS.: SOmente utilizado pelo método de upload dinãmico,
     * pois tem um loop infinito dentro.
     * @throws InterruptedException
     * @throws java.sql.SQLException
     */
    protected synchronized MessageWrapper getPriorityReport() throws InterruptedException, SQLException {
        MessageWrapper mw;
        while (true) {
            mw = communicationManager.getDeviceManager().getDataManager().getReportDAO()
                    .getOldest(false, true, Message.PREFERENTIAL_PRIORITY, this.service);
            if (mw == null) {
                wait();
            } else {
                break;
            }
        }
        return mw;
    }

    /**
     * @return retorna a primeira MessageWrapper de uma das duas filas
     * <b>prioritária</b> ou <b>normal</b> conforme o escalonamento dinãmico
     * pré-configurado no método OnCreate. Esse relato escolhido não é remvido
     * da fila. OBS.: SOmente utilizado pelo método de upload dinãmico, pois tem
     * um loop infinito dentro.
     * @throws InterruptedException
     * @throws java.sql.SQLException
     */
    protected synchronized MessageWrapper getReport() throws InterruptedException, SQLException {
        MessageWrapper mwp, mwn;
        while (true) {
            /*
             * Fazer um escalonamento dinãmico entre prioridades;
             * // Se a outra fila está vazia faz a atual e zera os contadores
             // obedece os dois limites priorizando os prioritários
             */

            // Pega primeiras mensagens
            mwp = communicationManager.getDeviceManager().getDataManager().getReportDAO()
                    .getOldestCheckedNotSent(Message.PREFERENTIAL_PRIORITY, this.service);
            mwn = communicationManager.getDeviceManager().getDataManager().getReportDAO()
                    .getOldestCheckedNotSent(Message.NORMAL_PRIORITY, this.service);
            // Se ambas estão vazias, zera as contagens e espera
            if (mwn == null && mwp == null) {
                countNormalMessage = 0;
                countPriorityMessage = 0;
                wait();
            }
            // Se houver mensagens prioritárias pega elas até o limite
            if (mwp != null && countPriorityMessage < communicationManager.getLimitPriorityMessage()) {
                if (countNormalMessage == 0) {
                    countPriorityMessage = 0;
                } else {
                    countPriorityMessage++;
                }
                return mwp;
            }
            // se houver mensagens normais depois de atingido o limite das prioritárias então pega as mensagens normais até atingir seu limite.
            // Atingido o limite normal a contagem é zerada de ambas as filas é zerada
            if (mwn != null && countNormalMessage < communicationManager.getLimitNormalMessage()) {
                if (countPriorityMessage == 0) {
                    countNormalMessage = 0;
                } else {
                    countNormalMessage++;
                }
                return mwn;
            }
        }
    }

    /**
     * Função referente a política de dados móveis. Somente estratégias 1 e 2
     * funcionam, as demais necessitam de implementação
     *
     * @return
     * @throws InterruptedException
     */
    protected MessageWrapper getReportByMobileDataPolicyCriteria() throws InterruptedException, SQLException {
        // Política de dados móveis, se a interface usa dados móveis
        MessageWrapper mw = null;
        switch (communicationManager.getMobileDataPolicy()) {
            case 1: // Sem mobilidade. Configuração default. Nenhuma ação.
                mw = this.getReport();
                break;
            case 2: // Fazer o uso sempre que possível. Nenhuma ação adicional.
                mw = this.getReport();
                break;
            case 3: // Somente fazer uso com mensagens de alta prioridade.
                if (communicationManager.getCurrentCommunicationInterface().isUsesMobileData()) {
                    mw = this.getPriorityReport();
                } else {
                    mw = this.getReport();
                }
                break;
        }
        return mw;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    public Service getService() {
        return service;
    }

    public synchronized void sendAssynchronousMessage(Message message) throws SQLException {
        // recebe o report para envio
        // cria envelope
        if (message.getOrigin() == null) {
            message.setOrigin(new Address());
            message.getOrigin().setLayer(Address.LAYER_APPLICATION);
        }
        // adiciona o application UID para o serviço
        message.getOrigin().setUid(service.getApplicationUID());
        // cria o MessageWrapper que será utilizado para criar o envelope
        MessageWrapper messageWrapper = new MessageWrapper(message);
        // Adiciona o servidor visado, se ele não foi setado
        if (messageWrapper.getMessage().getTarget() == null) {
            messageWrapper.getMessage().setTarget(new Address());
            messageWrapper.getMessage().getTarget().setAddress(service.getAddress());
            messageWrapper.getMessage().getTarget().setLayer(Address.LAYER_APPLICATION);
            messageWrapper.getMessage().getTarget().setUid(service.getServiceUID());
            // adiciona o assunto da mensagem
            messageWrapper.getMessage().setSubject(Message.SUBJECT_APPLICATION_MESSAGE);
        }
        try {
            // Cria o envelope XML da UrboSenti correspondente da mensagem
            messageWrapper.build();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(CommunicationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        // adiciona na fila de upload
        messageWrapper.setChecked();
        communicationManager.storagePolice(messageWrapper, this.service);
        notifyAll();
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public synchronized void wakeUp() {
        notifyAll();
    }

    public synchronized int getLimitOfReportsSentByUploadInterval() {
        return limitOfReportsSentByUploadInterval;
    }

    public synchronized Long getUploadInterval() {
        return uploadInterval;
    }

    public synchronized Double getUploadRate() {
        return uploadRate;
    }

    public synchronized boolean isAllowedToPerformUpload() {
        return allowedToPerformUpload;
    }
    
    public synchronized void setUploadInterval(Long uploadInterval) {
        this.uploadInterval = uploadInterval;
    }

    public synchronized void setUploadRate(Double uploadRate) {
        this.uploadRate = uploadRate;
    }

    public synchronized void setAllowedToPerformUpload(boolean allowedToPerformUpload) {
        this.allowedToPerformUpload = allowedToPerformUpload;
    }

    public synchronized void setLimitOfReportsSentByUploadInterval(int limitOfReportsSentByUploadInterval) {
        this.limitOfReportsSentByUploadInterval = limitOfReportsSentByUploadInterval;
    }

    public synchronized boolean isSubscribedMaximumUploadRate() {
        return subscribedMaximumUploadRate;
    }

    public synchronized void setSubscribedMaximumUploadRate(boolean subscribedMaximumUploadRate) {
        this.subscribedMaximumUploadRate = subscribedMaximumUploadRate;
        notifyAll();
    }
    
    @Override
    public String toString() {
        return String.valueOf(instance.getId());
    }

}
