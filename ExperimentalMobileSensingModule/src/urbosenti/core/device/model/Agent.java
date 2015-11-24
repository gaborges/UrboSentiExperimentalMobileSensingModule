/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.device.model;

import java.util.List;

/**
 *
 * @author Guilherme
 */
public class Agent {

    private int layer;
    private String description;
    private int id;
    private String address;
    private int systemPort;
    private AgentType agentType;
    private List<Conversation> conversations;
    private AddressAgentType addressType;
    private String serviceAddress;
    private Service service;

    public Agent() {
        layer = TargetOrigin.APPLICATION_LAYER;
        service = null;
    }
    
    /**
     *
     * @return Retorna a camada do agente: Agent.LAYER_SYSTEM = "system";
     * Agent.LAYER_APPLICATION = "application";
     */
    public int getLayer() {
        return layer;
    }

    /**
     *
     * @param layer pode conter os valores: Agent.LAYER_SYSTEM = "system";
     * Agent.LAYER_APPLICATION = "application";
     */
    public void setLayer(int layer) {
        this.layer = layer;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Agent{" + "id=" + id + ", address=" + address + ", layer=" + layer + ", description=" + description + '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        if (this.service == null) {
            return this.serviceAddress + this.address;
        }
        return this.service.getAddress() + this.address;
    }

    public void setServiceAddress(String address) {
        if (this.service == null) {
            this.serviceAddress = address;
        } else {
            this.service.setAddress(address);
        }
    }

    public int getSystemPort() {
        return systemPort;
    }

    public void setSystemPort(int systemPort) {
        this.systemPort = systemPort;
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public void setAgentType(AgentType agentType) {
        this.agentType = agentType;
    }

    public List<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public AddressAgentType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressAgentType addressType) {
        this.addressType = addressType;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getRelativeAddress() {
        return this.address;
    }

    public void setRelativeAddress(String address) {
        this.address = address;
    }
}
