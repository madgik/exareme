/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;

import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerID;
import madgik.exareme.worker.art.executionEngine.reportMgr.PlanSessionReportManagerInterface;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author herald
 */
public class DynamicReportManager extends EventSchedulerManipulator
        implements PlanSessionReportManagerInterface {
    private final Logger log = Logger.getLogger(DynamicReportManager.class);
    private ReentrantLock lock = null;

    public DynamicReportManager() {
        this.lock = new ReentrantLock();
    }

    @Override
    public void planStart(Date time, ContainerID containerID, PlanSessionReportID sessionID)
            throws RemoteException {
        lock.lock();
        try {
            log.trace("Plan Start:" + sessionID);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void planInstantiationException(RemoteException exception, Date time,
                                           ContainerID containerID, PlanSessionReportID sessionID) throws RemoteException {
        lock.lock();
        try {
            PlanEventScheduler eventScheduler = getSchedulerWiReportId(sessionID);
            eventScheduler.destroyPlanWithError();
            log.error("Plan Instantiation:" + sessionID, exception);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void operatorSuccess(ConcreteOperatorID operatorID, int exidCode,
                                Serializable exitMessage, Date time, ContainerID containerID, PlanSessionReportID sessionID,
                                boolean terminateGroup) throws RemoteException {
        lock.lock();
        try {
            PlanEventScheduler eventScheduler = getSchedulerWiReportId(sessionID);
            eventScheduler.terminated(operatorID, exidCode, exitMessage, time, terminateGroup);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void operatorError(ConcreteOperatorID operatorID, RemoteException exception, Date time,
                              ContainerID containerID, PlanSessionReportID sessionID) throws RemoteException {
        lock.lock();
        try {
            PlanEventScheduler eventScheduler = getSchedulerWiReportId(sessionID);
            eventScheduler.exception(operatorID, exception, time);
            log.error("Operator error: " + sessionID, exception);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void quantumFinished(int quantumNum, ContainerID containerID,
                                PlanSessionReportID sessionID) throws RemoteException {
        lock.lock();
        try {
            // ...
        } finally {
            lock.unlock();
        }
    }
}
