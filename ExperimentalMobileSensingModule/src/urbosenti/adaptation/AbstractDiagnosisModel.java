/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.adaptation;

import java.sql.SQLException;
import urbosenti.core.data.dao.AdaptationDAO;
import urbosenti.core.device.DeviceManager;
import urbosenti.core.events.Event;

/**
 *
 * @author Guilherme
 */
public abstract class AbstractDiagnosisModel {

    private final AdaptationDAO adaptationDAO;
    private final DeviceManager deviceManager;
    private Diagnosis diagnosis;

    public AbstractDiagnosisModel(AdaptationDAO adaptationDAO, DeviceManager deviceManager) {
        this.adaptationDAO = adaptationDAO;
        this.deviceManager = deviceManager;
        this.diagnosis = new Diagnosis();
    }

    /**
     * Executa o processo de análise retornando o diagnóstico. Caso o evento for
     * uma interação elexecuta a função interactionAnalysis(Event event); Se for
     * um evento interno utiliza a função eventAnalysis(Event event). Em último
     * caso retorna o diagnóstico sem mudanças.
     *
     * @param event
     * @return
     * @throws java.sql.SQLException
     */
    public Diagnosis analysis(Event event)throws SQLException, Exception {
        /* se o tamanho das mudanças (changes) for maior de 0, então quer dizer 
         que a análise anterior não teve diagnóstico e não precisa que uma nova 
         instância de diagnóstico seja feita */
        if (diagnosis.getChanges().size() > 0) {
            this.diagnosis = new Diagnosis();
        }
        // Tipo de evento interação, análise de interação
        if (event.getEventType() == Event.INTERATION_EVENT) {
            this.interactionAnalysis(event, this.diagnosis, this.adaptationDAO);
        } // tipo de evento de componente. Análise de eventos internos
        else if (event.getEventType() == Event.COMPONENT_EVENT) {
            this.eventAnalysis(event, this.diagnosis, this.adaptationDAO);
        }
        return diagnosis;
    }

    public abstract Diagnosis interactionAnalysis(Event event, Diagnosis diagnosis, AdaptationDAO adaptationDAO) throws SQLException, Exception;

    public abstract Diagnosis eventAnalysis(Event event, Diagnosis diagnosis, AdaptationDAO adaptationDAO) throws SQLException, Exception;

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public AdaptationDAO getAdaptationDAO() {
        return adaptationDAO;
    }

    public Diagnosis getDiagnosis() {
        return diagnosis;
    }

}
