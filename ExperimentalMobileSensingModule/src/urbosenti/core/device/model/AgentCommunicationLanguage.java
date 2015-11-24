package urbosenti.core.device.model;

import java.util.ArrayList;

public class AgentCommunicationLanguage {

    public static int AGENT_COMMUNICATIVE_LANGUAGE_FIPA_ID = 1;
    
    private int id;
    private String description;
    private ArrayList<CommunicativeAct> communicativeActs;

    public AgentCommunicationLanguage(int id, String description) {
        this.id = id;
        this.description = description;
        this.communicativeActs = new ArrayList();
    }

    public AgentCommunicationLanguage(int id, String description, ArrayList<CommunicativeAct> communicativeActs) {
        this.id = id;
        this.description = description;
        this.communicativeActs = communicativeActs;
    }
    
    public AgentCommunicationLanguage() {
        this.communicativeActs = null;
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

    public ArrayList<CommunicativeAct> getCommunicativeActs() {
        return communicativeActs;
    }

    public void setCommunicativeActs(ArrayList<CommunicativeAct> communicativeActs) {
        this.communicativeActs = communicativeActs;
    }
    
}
