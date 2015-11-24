package urbosenti.core.device.model;

public class TargetOrigin {

    public static final int APPLICATION_LAYER = 1;
    public static final int SYSTEM_LAYER = 2;
    
    private int id;
    private String description;

    public TargetOrigin() {
    }

    public TargetOrigin(int id, String description) {
        this.id = id;
        this.description = description;
    }
    
    public TargetOrigin(String description) {
        this.description = description;
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
