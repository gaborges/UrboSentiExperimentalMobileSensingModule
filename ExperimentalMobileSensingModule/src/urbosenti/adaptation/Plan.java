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
public class Plan {
    private final ArrayList<ExecutionPlan> executionPlans;

    public Plan() {
        this.executionPlans = new ArrayList<ExecutionPlan>();
    }

    public void addExecutionPlan(ExecutionPlan executionPlan){
        this.executionPlans.add(executionPlan);
    }
    
    public ArrayList<ExecutionPlan> getExecutionPlans() {
        return executionPlans;
    }
    
}
