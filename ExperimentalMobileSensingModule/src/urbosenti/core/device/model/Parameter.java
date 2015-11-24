package urbosenti.core.device.model;

import java.util.ArrayList;
import java.util.List;

public class Parameter {

    private int id;
    private String label;
    private String description;
    private boolean optional;
    private Object superiorLimit;
    private Object inferiorLimit;
    private Object initialValue;
    private DataType dataType;
    private Content content;
    private List<PossibleContent> possibleContents;
    private State relatedState;
    
    public Parameter(String label) {
        this();
        this.label = label;
        
    }
    
    public Parameter(String label, DataType dataType) {
        this();
        this.label = label; 
        this.dataType = dataType;
        this.initialValue = dataType.getInitialValue();
    }

    public Parameter() {
        this.description = "unknown";
        this.optional = false;
        this.superiorLimit = null;
        this.inferiorLimit = null;
        this.initialValue = null;
        this.dataType = null;
        this.content = null;
        this.possibleContents = new ArrayList();
        this.relatedState = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public Object getSuperiorLimit() {
        return superiorLimit;
    }

    public void setSuperiorLimit(Object superiorLimit) {
        this.superiorLimit = superiorLimit;
    }

    public Object getInferiorLimit() {
        return inferiorLimit;
    }

    public void setInferiorLimit(Object inferiorLimit) {
        this.inferiorLimit = inferiorLimit;
    }

    public Object getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(Object initialValue) {
        this.initialValue = initialValue;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        if(this.initialValue == null) this.initialValue = dataType.getInitialValue();
        this.dataType = dataType;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public List<PossibleContent> getPossibleContents() {
        return possibleContents;
    }

    public void setPossibleContents(List<PossibleContent> possibleContents) {
        this.possibleContents = possibleContents;
    }

    public State getRelatedState() {
        return relatedState;
    }

    public void setRelatedState(State relatedState) {
        this.relatedState = relatedState;
    }
    
}
