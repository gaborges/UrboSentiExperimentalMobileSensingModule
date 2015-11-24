package urbosenti.core.device.model;

public class Service {

    private int id;
    /**
     * ID do servi�o
     */
    private String serviceUID;
    /**
     * ID dado pelo servi�o para o dispositivo
     */
    private String applicationUID;
    private String description;
    private String address;
    private int port;
    private ServiceType serviceType;
    private Agent agent;
    private Device device;

    public Service(String serviceUID, String applicationUID, String description, String address, int port, ServiceType serviceType, Agent agent) {
        this.serviceUID = serviceUID;
        this.applicationUID = applicationUID;
        this.description = description;
        this.address = address;
        this.port = port;
        this.serviceType = serviceType;
        this.agent = agent;
    }

    public Service() {
    }

    public String getServiceUID() {
        return serviceUID;
    }

    public void setServiceUID(String serviceUID) {
        this.serviceUID = serviceUID;
    }

    public String getApplicationUID() {
        return applicationUID;
    }

    public void setApplicationUID(String applicationUID) {
        this.applicationUID = applicationUID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

}
