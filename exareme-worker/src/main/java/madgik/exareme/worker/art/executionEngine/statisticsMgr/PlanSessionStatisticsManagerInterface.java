/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.statisticsMgr;

import madgik.exareme.common.art.BufferStatistics;
import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.PlanSessionStatistics;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.ElasticTreeStatistics;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionProgressStats;
import madgik.exareme.worker.art.executionPlan.entity.BufferEntity;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface PlanSessionStatisticsManagerInterface {
    ConcreteOperatorStatistics getOperatorStatistics(OperatorEntity operatorEntity,
        PlanSessionID sessionID) throws RemoteException;

    BufferStatistics getBufferStatistics(BufferEntity bufferEntity, PlanSessionID sessionID)
        throws RemoteException;

    PlanSessionStatistics getStatistics(PlanSessionID sessionID) throws RemoteException;

    PlanSessionProgressStats getProgress(PlanSessionID sessionID) throws RemoteException;

    ElasticTreeStatistics getElasticTreeStatistics() throws RemoteException;

}
