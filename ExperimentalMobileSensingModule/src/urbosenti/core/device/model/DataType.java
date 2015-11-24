package urbosenti.core.device.model;

public class DataType {

    private int id;
    private String description;
    private java.lang.Object initialValue;

    public DataType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public DataType() {
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

    public java.lang.Object  getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(java.lang.Object  initialValue) {
        this.initialValue = initialValue;
    }

}
