/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.adaptation;

import urbosenti.core.data.dao.AdaptationDAO;
import urbosenti.core.device.DeviceManager;

/**
 *
 * @author Guilherme
 */
public abstract class AbstractPlanningModel {

    private final AdaptationDAO adaptationDAO;
    private final DeviceManager deviceManager;
    private Plan plan;
    private ExecutionPlan executionPlan;

    public AbstractPlanningModel(AdaptationDAO adaptationDAO, DeviceManager deviceManager) {
        this.adaptationDAO = adaptationDAO;
        this.deviceManager = deviceManager;
        this.plan = new Plan();
    }

    public Plan planning(Diagnosis diagnosis, AbstractDiagnosisModel diagnosisModel) {
        // cria um novo plano se o antigo continha um plano elaborado
        if (this.plan.getExecutionPlans().size() > 0) {
        	this.plan.getExecutionPlans().clear();
            //this.plan = new Plan();
        }
        // se for vazio o plano � retornado vazio
        if (diagnosis.getChanges().isEmpty()) {
            return plan;
        }
        // para cada mudan�a verificar a mudan�a
        for (Change change : diagnosis.getChanges()) {
            // retorna o plano de a��o para cada mundan�a
            this.executionPlan = this.getExecutionPlan(change, plan, adaptationDAO, diagnosisModel);
            // verifica se � nulo, se n�o for adiciona
            if (executionPlan != null) {
                plan.addExecutionPlan(executionPlan);
            }
        }
        return plan;
    }

    public abstract ExecutionPlan getExecutionPlan(Change change, Plan plan, AdaptationDAO adaptationDAO, AbstractDiagnosisModel diagnosisModel);

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public AdaptationDAO getAdaptationDAO() {
        return adaptationDAO;
    }

    public Plan getPlan() {
        return plan;
    }

}
