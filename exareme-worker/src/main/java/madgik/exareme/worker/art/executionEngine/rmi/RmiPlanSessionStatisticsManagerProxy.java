/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.rmi;

import madgik.exareme.common.art.BufferStatistics;
import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.PlanSessionStatistics;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.ElasticTreeStatistics;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionProgressStats;
import madgik.exareme.worker.art.executionEngine.statisticsMgr.PlanSessionStatisticsManager;
import madgik.exareme.worker.art.executionEngine.statisticsMgr.PlanSessionStatisticsManagerProxy;
import madgik.exareme.worker.art.executionPlan.entity.BufferEntity;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.remote.RmiObjectProxy;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class RmiPlanSessionStatisticsManagerProxy
        extends RmiObjectProxy<PlanSessionStatisticsManager>
        implements PlanSessionStatisticsManagerProxy {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(PlanSessionStatisticsManagerProxy.class);
    public PlanSessionID sessionID = null;

    public RmiPlanSessionStatisticsManagerProxy(String regEntryName, EntityName regEntityName) {
        super(regEntryName, regEntityName);
    }

    @Override
    public ConcreteOperatorStatistics getOperatorStatistics(OperatorEntity operatorEntity)
            throws RemoteException {
        return super.getRemoteObject().getOperatorStatistics(operatorEntity, sessionID);
    }

    @Override
    public BufferStatistics getBufferStatistics(BufferEntity bufferEntity, PlanSessionID sessionID)
            throws RemoteException {
        return super.getRemoteObject().getBufferStatistics(bufferEntity, sessionID);
    }

    @Override
    public PlanSessionStatistics getStatistics() throws RemoteException {
        log.debug("Try to get stats for " + sessionID);
        return super.getRemoteObject().getStatistics(sessionID);
    }

    @Override
    public PlanSessionProgressStats getProgress() throws RemoteException {
        return super.getRemoteObject().getProgress(sessionID);
    }

    @Override
    public ElasticTreeStatistics getElasticTreeStatistics() throws RemoteException {
        return super.getRemoteObject().getElasticTreeStatistics();
    }
}
