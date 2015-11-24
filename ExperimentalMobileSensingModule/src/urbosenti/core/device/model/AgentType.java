package urbosenti.core.device.model;

import java.util.ArrayList;
import java.util.List;

public class AgentType {

    public static final int NO_RELATED_AGENT = 0;
    public static final int BACKEND_SYSTEM_SERVICE = 1;
    private int id;
    private String description;
    private List<InteractionModel> interaction;
    private List<State> states;

    public AgentType(int id, String description) {
        this.id = id;
        this.description = description;
        this.interaction = new ArrayList();
        this.states = new ArrayList();
    }

    public AgentType() {
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

    public List<InteractionModel> getInteractionModels() {
        return interaction;
    }

    public void setInteractionModels(List<InteractionModel> interaction) {
        this.interaction = interaction;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStateModels(List<State> states) {
        this.states = states;
    }

}
