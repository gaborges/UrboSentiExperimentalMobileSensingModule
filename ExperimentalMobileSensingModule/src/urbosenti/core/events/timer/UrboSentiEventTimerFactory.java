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
public class UrboSentiEventTimerFactory extends EventTimerFactory{

    public UrboSentiEventTimerFactory() {
        // Atribuiu como a única instancia possível como padrão
        super("UrboSentiNativeEventTimer");
    }

    /**
     * @param objectName nome do objeto. A implementação nativa utiliza o nome "UrboSentiNativeEventTimer" e já foi atribuída no construtor
     * @param tr utilizada para a instancia de EventTimer
     * @param em utilizada para a instancia de EventTimer
     * @return Retorna o Objeto encontrado, caso não encontre retorna null.
     */    
    @Override
    public EventTimer getEventTimer(String objectName, TriggerRequest tr, EventManager em){
        if(objectName.equals("UrboSentiNativeEventTimer")){
            return new UrboSentiNativeEventTimer(tr, em);
        }
        return null;
    }
}
