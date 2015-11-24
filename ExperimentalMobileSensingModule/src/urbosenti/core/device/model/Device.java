package urbosenti.core.device.model;

import java.util.List;

public class Device {

    private String description;
    private int id;
    private double deviceVersion;
    private double agentModelVersion;
    private double generalDefinitionsVersion;
    private List<Service> services;
    private List<Component> components;

    public Device() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public double getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(double deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public double getAgentModelVersion() {
        return agentModelVersion;
    }

    public void setAgentModelVersion(double agentModelVersion) {
        this.agentModelVersion = agentModelVersion;
    }

    public double getGeneralDefinitionsVersion() {
        return generalDefinitionsVersion;
    }

    public void setGeneralDefinitionsVersion(double generalDefinitionsVersion) {
        this.generalDefinitionsVersion = generalDefinitionsVersion;
    }

}
