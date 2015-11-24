/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.device;

import java.io.Serializable;
import urbosenti.core.device.model.FeedbackAnswer;
import urbosenti.core.events.Action;
import urbosenti.core.events.EventManager;

/**
 *
 * @author Guilherme
 */
public abstract class ComponentManager  implements Serializable  {

    /**
     *
     */
    private int componentId;
    private EventManager eventManager;
    private DeviceManager deviceManager;

    /**
     * Construtor só pode ser utilizado pelo componente base, pois os demais
     * devem conhecer o componente base para resgatar os endereços dos
     * componentes dependentes.
     */
    protected ComponentManager() {
    }

    /**
     * Método utilizado pelos componentes que não são base.
     *
     * @param deviceManager
     * @param componentId
     */
    public ComponentManager(DeviceManager deviceManager, int componentId) {
        this.deviceManager = deviceManager;
        this.eventManager = this.deviceManager.getEventManager();
        this.componentId = componentId;
    }

    /**
     * Método de inicialização do componente.
     */
    public abstract void onCreate();

    /**
     * Método recebe uma ação e aplica ela caso ela exista.
     *
     * @param action
     * @return returna um FeedbackAnswer caso exista. Retorna null caso a ação
     * não existe ou não foi implementada.
     */
    public abstract FeedbackAnswer applyAction(Action action);

    /**
     * Adiciona o EventManager utilizado para enviar os eventos. Somente deve
     * ser utilizado pelo componente Base
     *
     * @param eventManager
     */
    protected void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }
    /**
     * Retorna o componente de eventos utilizado para receber os eventos.
     * @return 
     */
    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * Adiciona o DeviceManager utilizado para acessar os demais recursos do
     * middleware. Somente deve ser utilizado pelo componente Base
     *
     * @param deviceManager
     */
    protected void setDeviceManager(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    /**
     * Adiciona o identificador do componente. Somente deve ser utilizado pelo
     * componente Base.
     *
     * @param componentId
     */
    protected void setComponentId(int componentId) {
        this.componentId = componentId;
    }
    /** 
     * Returns the component id
     * @return 
     */
    public int getComponentId() {
        return componentId;
    }

    @Override
    public String toString() {
        return "ComponentManager{" + "componentId=" + componentId + ", deviceManager=" + deviceManager + '}';
    }
    
}
