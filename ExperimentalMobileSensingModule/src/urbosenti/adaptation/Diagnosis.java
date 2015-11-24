/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.adaptation;

import java.util.ArrayList;

/**
 *
 * @author Guilherme
 */
public class Diagnosis {
    public static final int DIAGNOSIS_NO_ADAPTATION_NEEDED = 0;
    private final ArrayList<Change> objectChanges;

    public Diagnosis() {
        this.objectChanges = new ArrayList();
    }
    
    public void addChange(Change change){
        this.objectChanges.add(change);
    }

    public ArrayList<Change> getChanges() {
        return objectChanges;
    }
    
}
