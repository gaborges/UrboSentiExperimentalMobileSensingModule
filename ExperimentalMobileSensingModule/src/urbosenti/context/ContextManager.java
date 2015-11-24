/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.context;

import urbosenti.core.data.dao.ContextDAO;
import urbosenti.core.device.ComponentManager;
import urbosenti.core.device.DeviceManager;
import urbosenti.core.device.model.FeedbackAnswer;
import urbosenti.core.events.Action;
import urbosenti.core.events.EventManager;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class ContextManager extends ComponentManager{

    public ContextManager(DeviceManager deviceManager) {
        super(deviceManager, ContextDAO.COMPONENT_ID);
    }
    
    @Override
    public void onCreate() {
        if(DeveloperSettings.SHOW_FUNCTION_DEBUG_ACTIVITY){
            System.out.println("Activating: " + getClass());
        }
        // Carregar dados e configurações que serão utilizados para executar em memória
        // Preparar configurações inicias para execução
        // Para tanto utilizar o DataManager para acesso aos dados.
    }

    @Override
    public FeedbackAnswer applyAction(Action action) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
