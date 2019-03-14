/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.sessionMgr;

import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;
import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.remote.RemoteObject;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface PlanSessionManager extends RemoteObject<PlanSessionManagerProxy> {

    PlanSessionID createNewSession() throws RemoteException;

    ContainerSessionID createContainerSession(PlanSessionID planSessionID) throws RemoteException;

    void executeElasticTree(ExecutionPlan plan, SLA sla, PlanSessionID sessionID)
            throws RemoteException;


    void execute(ExecutionPlan plan, PlanSessionID sessionID) throws RemoteException;

    void destroySession(PlanSessionID sessionID) throws RemoteException;

    void destroyAllSessions() throws RemoteException;

    void stopManager() throws RemoteException;

    PlanSessionID createNewSessionElasticTree() throws RemoteException;

    void stopManager(boolean force) throws RemoteException;
}
