/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.rmi;

import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionEngine.sessionMgr.PlanSessionManager;
import madgik.exareme.worker.art.executionEngine.sessionMgr.PlanSessionManagerProxy;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br> University of Athens / Department of Informatics
 *         and Telecommunications.
 * @since 1.0
 */
public class RmiPlanSessionManagerProxy extends RmiObjectProxy<PlanSessionManager>
    implements PlanSessionManagerProxy {
    private static final long serialVersionUID = 1L;
    public PlanSessionID sessionID = null;

    public RmiPlanSessionManagerProxy(String regEntryName, EntityName regEntityName) {
        super(regEntryName, regEntityName);
    }

    @Override public void execute(ExecutionPlan plan) throws RemoteException {
        super.getRemoteObject().execute(plan, sessionID);
    }


    @Override public void executeElasticTree(ExecutionPlan plan, SLA sla) throws RemoteException {
        super.getRemoteObject().executeElasticTree(plan, sla, sessionID);
    }
}
