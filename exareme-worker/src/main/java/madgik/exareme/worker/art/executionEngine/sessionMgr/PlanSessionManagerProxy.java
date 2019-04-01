/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.sessionMgr;

import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.remote.ObjectProxy;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface PlanSessionManagerProxy extends ObjectProxy<PlanSessionManager> {
    // This is the most generic method. It schedules a dag graph for execution.
    void execute(ExecutionPlan plan) throws RemoteException;

    // This create a Google Dremel like tree on top of the table partitions.
    void executeElasticTree(ExecutionPlan plan, SLA sla) throws RemoteException;
}
