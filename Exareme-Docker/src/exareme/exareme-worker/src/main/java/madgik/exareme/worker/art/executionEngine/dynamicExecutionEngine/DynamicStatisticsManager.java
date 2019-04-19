/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;

import madgik.exareme.common.art.BufferStatistics;
import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.PlanSessionStatistics;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionProgressStats;
import madgik.exareme.worker.art.executionEngine.statisticsMgr.PlanSessionStatisticsManagerInterface;
import madgik.exareme.worker.art.executionPlan.entity.BufferEntity;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;

import java.rmi.RemoteException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author herald
 */
public class DynamicStatisticsManager extends EventSchedulerManipulator
        implements PlanSessionStatisticsManagerInterface {
    private ReentrantLock lock = null;

    public DynamicStatisticsManager() {
        this.lock = new ReentrantLock();
    }

    @Override
    public ConcreteOperatorStatistics getOperatorStatistics(OperatorEntity operatorEntity,
                                                            PlanSessionID sessionID) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ElasticTreeStatistics getElasticTreeStatistics() throws RemoteException {
        lock.lock();
        try {
            return getElasticTreeScheduler().getStatistics();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public BufferStatistics getBufferStatistics(BufferEntity bufferEntity, PlanSessionID sessionID)
            throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PlanSessionStatistics getStatistics(PlanSessionID sessionID)
            throws RemoteException {
        lock.lock();
        try {
            PlanEventScheduler eventScheduler = getSchedulerWithId(sessionID);
            return eventScheduler.getState().getStatistics();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public PlanSessionProgressStats getProgress(PlanSessionID sessionID)
            throws RemoteException {
        lock.lock();
        try {
            PlanEventScheduler eventScheduler = getSchedulerWithId(sessionID);
            return eventScheduler.getState().getProgress();
        } finally {
            lock.unlock();
        }
    }
}
