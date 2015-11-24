package urbosenti.core.device.model;

/**
 * POde ser backend Service
 */
public class ServiceType {

    private int id;
    private String description;

    public ServiceType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public ServiceType() {
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

}
