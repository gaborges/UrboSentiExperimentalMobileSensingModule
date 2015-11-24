package urbosenti.core.device.model;

import java.util.ArrayList;
import java.util.List;

public class Instance {

    private int id;
    private int modelId;
    private String description;
    private String representativeClass;
    private List<State> states;
    private Entity entity;

    public Instance(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public Instance(int id, String description, String representativeClass) {
        this.id = id;
        this.description = description;
        this.representativeClass = representativeClass;
        this.states = new ArrayList();
    }
    
    public Instance() {
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

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }

    public String getRepresentativeClass() {
        return representativeClass;
    }

    public void setRepresentativeClass(String representativeClass) {
        this.representativeClass = representativeClass;
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
    
}
