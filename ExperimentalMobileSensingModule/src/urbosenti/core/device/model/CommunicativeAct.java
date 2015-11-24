package urbosenti.core.device.model;

public class CommunicativeAct {

    private int id;
    private String description;
    private AgentCommunicationLanguage agentCommunicationLanguage;

    public CommunicativeAct(int id, String description, AgentCommunicationLanguage agentCommunicationLanguage) {
        this.id = id;
        this.description = description;
        this.agentCommunicationLanguage = agentCommunicationLanguage;        
    }

    public CommunicativeAct() {
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

    public AgentCommunicationLanguage getAgentCommunicationLanguage() {
        return agentCommunicationLanguage;
    }

    public void setAgentCommunicationLanguage(AgentCommunicationLanguage agentCommunicationLanguage) {
        this.agentCommunicationLanguage = agentCommunicationLanguage;
    }

    
}
