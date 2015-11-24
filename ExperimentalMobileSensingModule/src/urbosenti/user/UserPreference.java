/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.user;

import java.util.List;
import urbosenti.core.device.model.Content;
import urbosenti.core.device.model.DataType;
import urbosenti.core.device.model.Entity;
import urbosenti.core.device.model.Instance;
import urbosenti.core.device.model.PossibleContent;
import urbosenti.core.device.model.State;

/**
 *
 * @author Guilherme
 */
public class UserPreference {
    
    private final State state;
    private final Instance instance;
    private final Entity entity;

    public UserPreference(State state, Instance instance) {
        this.state = state;
        this.instance = instance;
        this.entity = instance.getEntity();
    }

    public State getState() {
        return state;
    }
    /**
     * Retorna a instância deste estado. Se ela não for um estado de instância retorna null.
     * @return 
     */
    public Instance getInstance() {
        return instance;
    }

    public Entity getEntity() {
        return entity;
    }

    public UserPreference(State state) {
        this.state = state;
        this.instance = null;
        this.entity = state.getEntity();
    }
    
    public DataType getDataType(){
        return state.getDataType();
    }
    
    public Object getValue(){
        return state.getCurrentValue();
    }
    
    public void setNewValue(Object value) throws NumberFormatException, NullPointerException{
        Content content = new Content(Content.parseContent(state.getDataType(), value));
        state.setContent(content);
    }
    /**
     * Retorna os valores possíveis, se estes não estiverem delimitados retorna null.
     * @return 
     */
    public List<PossibleContent> getPossibleContents(){
        return state.getPossibleContents();
    }
    /**
     * Retorna o limite inferior, se esse não existe retorna null;
     * @return 
     */
    public Object inferiorLimit(){
        return state.getInferiorLimit();
    }
    /**
     * Retorna o limite superior, se esse não existe retorna null;
     * @return 
     */
    public Object superiorLimit(){
        return state.getSuperiorLimit();
    }
    
    public String getDescription(){
        return state.getDescription();
    }
    
    public boolean isInstanceState(){
        return state.isStateInstance();
    }
    /**
     * Retorna o nome da instância. Caso este não exista retorna um valor vaziu.
     * @return 
     */
    public String getInstanceDescription(){
        return (instance != null)?instance.getDescription():"";
    }
    
    public String getEntityDescription(){
        return entity.getDescription();
    }
}
