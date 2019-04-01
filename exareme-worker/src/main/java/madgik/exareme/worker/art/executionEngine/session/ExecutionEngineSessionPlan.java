/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.session;

import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.executionEngine.ExecutionEngine;
import madgik.exareme.worker.art.executionEngine.sessionMgr.PlanSessionManagerProxy;
import madgik.exareme.worker.art.executionEngine.statisticsMgr.PlanSessionStatisticsManagerProxy;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManagerProxy;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}@di.uoa.gr<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ExecutionEngineSessionPlan implements Serializable {
    private static final long serialVersionUID = 1L;
    private final PlanSessionID sessionID;
    private ExecutionEngine engine = null;
    private boolean isClosed = false;
    private PlanSessionStatusManagerProxy managerProxy = null;
    private ReentrantLock lock = new ReentrantLock();

    public ExecutionEngineSessionPlan(PlanSessionID sessionID, ExecutionEngine engine)
            throws RemoteException {
        this.sessionID = sessionID;
        this.engine = engine;
        this.managerProxy = getPlanSessionStatusManagerProxy();
    }

    public PlanSessionID getSessionID() {
        return sessionID;
    }

    public void submitPlan(ExecutionPlan plan) throws RemoteException {
        lock.lock();
        try {
            engine.getPlanSessionManagerProxy(sessionID).execute(plan);
        } finally {
            lock.unlock();
        }
    }

    public final PlanSessionStatusManagerProxy getPlanSessionStatusManagerProxy()
            throws RemoteException {
        lock.lock();
        try {
            return engine.getPlanSessionStatusManagerProxy(sessionID);
        } finally {
            lock.unlock();
        }
    }


    public void submitPlanElasticTree(ExecutionPlan plan, SLA sla) throws RemoteException {
        lock.lock();
        try {
            engine.getPlanSessionManagerProxy(sessionID).executeElasticTree(plan, sla);
        } finally {
            lock.unlock();
        }
    }

    public PlanSessionManagerProxy getPlanSessionManagerProxy() throws RemoteException {
        lock.lock();
        try {
            return engine.getPlanSessionManagerProxy(sessionID);
        } finally {
            lock.unlock();
        }
    }

    public PlanSessionStatisticsManagerProxy getPlanSessionStatisticsManagerProxy()
            throws RemoteException {
        lock.lock();
        try {
            return engine.getPlanSessionStatisticsManagerProxy(sessionID);
        } finally {
            lock.unlock();
        }
    }

    public void close() throws RemoteException {
        lock.lock();
        try {
            if (isClosed == false) {
                engine.destroySession(sessionID);
                isClosed = true;
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean hasError() throws RemoteException {
        lock.lock();
        try {
            return managerProxy.hasError();
        } finally {
            lock.unlock();
        }
    }

    public boolean isClosed() throws RemoteException {
        lock.lock();
        try {
            return isClosed;
        } finally {
            lock.unlock();
        }
    }
}
