/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.adaptation;

import java.util.HashMap;

/**
 *
 * @author Guilherme
 */
public class Change {
    private final Integer id;
    private final HashMap<String,Object> parameters;

    public Change(Integer id, HashMap<String, Object> parameters) {
        this.id = id;
        this.parameters = parameters;
    }

    public Integer getId() {
        return id;
    }

    public HashMap<String, Object> getParameters() {
        return parameters;
    }
    
}
