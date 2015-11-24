package urbosenti.core.device.model;

import java.util.ArrayList;
import java.util.List;

public class EventModel {

    private int id;
    private int modelId;
    private String description;
    private boolean synchronous;
    private List<EventTarget> targets;
    private Implementation implementation;
    private List<Parameter> parameters;
    private Entity entity;
    private boolean isNecessaryStore;

    public EventModel(int id, String description, Implementation implementation) {
        this();
        this.id = id;
        this.description = description;
        this.synchronous = false;
        this.implementation = implementation;
    }

    public EventModel() {
        this.parameters = new ArrayList();
        this.targets = new ArrayList();
        this.isNecessaryStore = true;
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

    public boolean isSynchronous() {
        return synchronous;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

      public Implementation getImplementation() {
        return implementation;
    }

    public void setImplementation(Implementation implementation) {
        this.implementation = implementation;
    }

    public List<EventTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<EventTarget> targets) {
        this.targets = targets;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public int getModelId() {
        return modelId;
    }

    public void setModelId(int modelId) {
        this.modelId = modelId;
    }

    public boolean isNecessaryStore() {
        return isNecessaryStore;
    }

    public void setIsNecessaryStore(boolean isNecessaryStore) {
        this.isNecessaryStore = isNecessaryStore;
    }

}
