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
     * Executa o processo de an�lise retornando o diagn�stico. Caso o evento for
     * uma intera��o elexecuta a fun��o interactionAnalysis(Event event); Se for
     * um evento interno utiliza a fun��o eventAnalysis(Event event). Em �ltimo
     * caso retorna o diagn�stico sem mudan�as.
     *
     * @param event
     * @return
     * @throws java.sql.SQLException
     */
    public Diagnosis analysis(Event event)throws SQLException, Exception {
        /* se o tamanho das mudan�as (changes) for maior de 0, ent�o quer dizer 
         que a an�lise anterior n�o teve diagn�stico e n�o precisa que uma nova 
         inst�ncia de diagn�stico seja feita */
        if (diagnosis.getChanges().size() > 0) {
        	this.getDiagnosis().getChanges().clear();
        }
        // Tipo de evento intera��o, an�lise de intera��o
        if (event.getEventType() == Event.INTERATION_EVENT) {
            this.interactionAnalysis(event, this.diagnosis, this.adaptationDAO);
        } // tipo de evento de componente. An�lise de eventos internos
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
