/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.adaptation;

import java.util.ArrayList;
import urbosenti.core.events.Action;

/**
 *
 * @author Guilherme
 */
public class ExecutionPlan {
    public static int DEFAULT_STOPPTING_CONDITION = 1;
    public static int STOPPING_CONDITION_UNTIL_SUCCESS = 1;
    public static int STOPPING_CONDITION_UNTIL_FAIL_OR_END = 2;
    public static int STOPPING_CONDITION_ALL = 3;
    private final ArrayList<Action> queueOfActions; 
    private int stoppingCondition;
    private int id;

    public ExecutionPlan() {
        this.queueOfActions = null;
        this.id = 0;
    }
    
    public ExecutionPlan(ArrayList<Action> queueOfActions) {
        this.stoppingCondition = DEFAULT_STOPPTING_CONDITION;
        this.queueOfActions = queueOfActions;
    }

    public int getStoppingCondition() {
        return stoppingCondition;
    }

    public void setStoppingCondition(int stoppingCondition) {
        this.stoppingCondition = stoppingCondition;
    }

    public ArrayList<Action> getQueueOfActions() {
        return queueOfActions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
}
