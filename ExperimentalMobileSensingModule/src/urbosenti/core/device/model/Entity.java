package urbosenti.core.device.model;

import java.util.ArrayList;
import java.util.List;

public class Entity {

    private int id;
    private int model_id;
    private String description;
    private EntityType EntityType;
    private Component component;
    private List<Instance> instances;
    private List<State> states;
    private List<EventModel> events;
    private List<ActionModel> actions;

    public Entity(int id, String description, EntityType objectType, List<Instance> instace, List<State> state, List<EventModel> event, List<ActionModel> action) {
        this.id = id;
        this.description = description;
        this.EntityType = objectType;
        this.instances = instace;
        this.states = state;
        this.events = event;
        this.actions = action;
    }
    
    public Entity(String description, EntityType objectType, List<Instance> instace, List<State> state, List<EventModel> event, List<ActionModel> action) {
        this.description = description;
        this.EntityType = objectType;
        this.instances = instace;
        this.states = state;
        this.events = event;
        this.actions = action;
    }
    
    public Entity(String description) {
        this.description = description;
        this.actions = new ArrayList();
        this.events = new ArrayList();
        this.instances = new ArrayList();
        this.states = new ArrayList();
    }
    
    public Entity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EntityType getEntityType() {
        return EntityType;
    }

    public void setEntityType(EntityType objectType) {
        this.EntityType = objectType;
    }

    public List<Instance> getInstanceModels() {
        return instances;
    }

    public void setInstaceModels(List<Instance> instance) {
        this.instances = instance;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStateModels(List<State> states) {
        this.states = states;
    }

    public List<EventModel> getEventModels() {
        return events;
    }

    public void setEventModels(List<EventModel> events) {
        this.events = events;
    }

    public List<ActionModel> getActionModels() {
        return actions;
    }

    public void setActionModels(List<ActionModel> actions) {
        this.actions = actions;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public int getModelId() {
        return model_id;
    }

    public void setModelId(int model_id) {
        this.model_id = model_id;
    }

}
