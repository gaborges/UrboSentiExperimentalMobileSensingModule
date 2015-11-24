/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.events.timer;

import urbosenti.core.events.EventManager;

/**
 *
 * @author Guilherme
 */
public abstract class EventTimerFactory {

    private String defaultObjectName;

    public EventTimerFactory() {
        this.defaultObjectName = "";
    }

    public EventTimerFactory(String defaultObjectName) {
        this.defaultObjectName = defaultObjectName;
    }
    
    /**
     * @param objectName nome do objeto a ser criado. A implementação nativa utiliza o nome "UrboSentiNativeEventTimer"
     * @param tr utilizada para a instancia de EventTimer
     * @param em utilizada para a instancia de EventTimer
     * @return Retorna o Objeto encontrado, caso não encontre retorna null.
     */    
    public abstract EventTimer getEventTimer(String objectName, TriggerRequest tr,EventManager em);
        
    /**
     *
     * @param tr utilizada para a instancia de EventTimer
     * @param em utilizada para a instancia de EventTimer
     * @return Retorna o Objeto com o nome atribuído como defaultObjectName, caso não encontre retorna null.
     */
    public EventTimer getEventTimer(TriggerRequest tr,EventManager em){
        return this.getEventTimer(defaultObjectName, tr, em);
    }

    public String getDefaultObjectName() {
        return defaultObjectName;
    }

    public void setDefaultObjectName(String defaultObjectName) {
        this.defaultObjectName = defaultObjectName;
    }
  
}
