package urbosenti.core.device.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InteractionModel {

    private int id;
    private String description;
    private List<Parameter> parameters;
    private InteractionType interactionType;
    private Direction direction;
    private InteractionModel primaryInteraction;
    private CommunicativeAct communicativeAct;
    private AgentType agentType;

    public InteractionModel(int id, String description) {
        this();
        this.id = id;
        this.description = description;
    }

    public InteractionModel() {
        this.parameters = new ArrayList();
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

    public InteractionType getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(InteractionType interactionType) {
        this.interactionType = interactionType;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public CommunicativeAct getCommunicativeAct() {
        return communicativeAct;
    }

    public void setCommunicativeAct(CommunicativeAct communicativeAct) {
        this.communicativeAct = communicativeAct;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public InteractionModel getPrimaryInteraction() {
        return primaryInteraction;
    }

    public void setPrimaryInteraction(InteractionModel primaryInteraction) {
        this.primaryInteraction = primaryInteraction;
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public void setAgentType(AgentType agentType) {
        this.agentType = agentType;
    }

    public void setContentToParameter(String label, Object value){
        Content content = new Content();
        content.setTime(new Date());
        for(Parameter p : parameters){
            if(p.getLabel().equals(label)){
                content.setValue(Content.parseContent(p.getDataType(), value));
                p.setContent(content);
                break;
            }
        }
    }
}
