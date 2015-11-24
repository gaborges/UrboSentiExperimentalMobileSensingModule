package urbosenti.core.device.model;

public class PossibleContent {

    private int id;
    private Object value;
    private boolean isDefault;

    public PossibleContent(int id, Object value, boolean isDefault) {
        this.id = id;
        this.value = value;
        this.isDefault = isDefault;
    }
    public PossibleContent(Object value) {
        this.value = value;
    }

    public PossibleContent() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

}
