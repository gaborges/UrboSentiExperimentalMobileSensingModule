/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.events;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import urbosenti.core.device.ComponentManager;

/**
 *
 * @author Guilherme
 */
public abstract class Event implements Serializable  {
    /*
     * Types of event:
     * * COMPONENT_EVENT is used in events from components
     * * INTERATION_EVENT is used in social interactions with other agents
     */

    public static final int COMPONENT_EVENT = 0;
    public static final int INTERATION_EVENT = 1;

    /*
     * Types of component:
     * * SYSTEM_EVENT  is a event to be handled by the system automaticaly, if the adaptation component was activated
     * * APPLICATION_EVENT is a event to be handled by the sensing application
     */
    public static final int SYSTEM_EVENT = 0;
    public static final int APPLICATION_EVENT = 1;

    private int id;
    private int databaseId;
    private String name;
    private boolean synchronous;
    private int eventType;
    private int originType;
    private int entityId;
    private ComponentManager component; // for internal events is a ComponentManager otherwise is a Agent
    private HashMap<String, Object> parameters;  // it is a value used to handle the event, if necessary.
    private boolean hasTimeout;
    private int timeout; // in ms
    private Date time;

    public Event(ComponentManager component, int originType) {
        this.id = 0;
        this.synchronous = false;
        this.eventType = Event.COMPONENT_EVENT;
        this.hasTimeout = false;
        this.time = new Date();
        this.timeout = 1000;
        this.component = component;
        this.originType = originType;
    }

    public void setSynchronousSettings(int timeout, boolean hasTimeout) {
        this.timeout = timeout;
        this.hasTimeout = hasTimeout;
        this.synchronous = true;
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

    public boolean isSynchronous() {
        return synchronous;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    /**
     * @return Types of event:
     * <ul>
     * <li> Event.COMPONENT_EVENT is used in events from components</li>
     * <li> Event.INTERATION_EVENT is used in social interactions with other
     * agents </li>
     * </ul>
     */
    public int getEventType() {
        return eventType;
    }

    /**
     * @param eventType of component:
     * <ul>
     * <li> Event.COMPONENT_EVENT is used in events from components</li>
     * <li> Event.INTERATION_EVENT is used in social interactions with other
     * agents </li>
     * </ul>
     */
    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public ComponentManager getComponentManager() {
        return component;
    }

    public void setComponentManager(ComponentManager origin) {
        this.component = origin;
    }

    public HashMap<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    public boolean isHasTimeout() {
        return hasTimeout;
    }

    public void setHasTimeout(boolean hasTimeout) {
        this.hasTimeout = hasTimeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * @param destiny of component:
     * <ul>
     * <li>Event.SYSTEM_EVENT is a event to be handled by the system
     * automaticaly, if the adaptation component was activated</li>
     * <li>Event.APPLICATION_EVENT is a event to be handled by the sensing
     * application</li>
     * </ul>
     */
    public void setDestiny(int destiny) {
        this.originType = destiny;
    }

    /**
     * @return Types of component:
     * <ul>
     * <li>Event.SYSTEM_EVENT is a event to be handled by the system
     * automaticaly, if the adaptation component was activated</li>
     * <li>Event.APPLICATION_EVENT is a event to be handled by the sensing
     * application</li>
     * </ul>
     */
    public int getOriginType() {
        return originType;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public String toString() {
        return "Event{" + "id=" + id + ", name=" + name + ", synchronous=" + synchronous + ", eventType=" + eventType + ", originType=" + originType + ", origin=" + component + ", value=" + parameters + ", hasTimeout=" + hasTimeout + ", timeout=" + timeout + ", time=" + time + '}';
    }

    /**
     * Verifica se o evento foi expirado, caso seja uma ação síncrona que
     * demorou mais que o tempo de espera para retornar a ação.
     *
     * @param event
     * @return
     */
    public static boolean isEventExpired(Event event) {
        if (event.isHasTimeout()) {
            if ((new Date().getTime() - event.getTime().getTime()) > event.getTimeout()) {
                return true;
            }
        }
        return false;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public void newTime() {
        this.time = new Date();
    }
    
}
