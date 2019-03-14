/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.rmi;

import madgik.exareme.common.art.BufferStatistics;
import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.PlanSessionStatistics;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.ElasticTreeStatistics;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionProgressStats;
import madgik.exareme.worker.art.executionEngine.statisticsMgr.PlanSessionStatisticsManager;
import madgik.exareme.worker.art.executionEngine.statisticsMgr.PlanSessionStatisticsManagerInterface;
import madgik.exareme.worker.art.executionEngine.statisticsMgr.PlanSessionStatisticsManagerProxy;
import madgik.exareme.worker.art.executionPlan.entity.BufferEntity;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.remote.RmiRemoteObject;

import java.rmi.RemoteException;
import java.util.UUID;

/**
 * @author herald
 */
public class RmiPlanSessionStatisticsManager
        extends RmiRemoteObject<PlanSessionStatisticsManagerProxy>
        implements PlanSessionStatisticsManager {

    private PlanSessionStatisticsManagerInterface statisticsManagerInterface = null;
    private EntityName regEntityName = null;

    public RmiPlanSessionStatisticsManager(
            PlanSessionStatisticsManagerInterface statisticsManagerInterface, EntityName regEntityName)
            throws RemoteException {
        super(NetUtil.getIPv4() + "_planSessionStatisticsManager_" + UUID.randomUUID().toString());

        this.statisticsManagerInterface = statisticsManagerInterface;
        this.regEntityName = regEntityName;

        super.register();
    }

    @Override
    public PlanSessionStatisticsManagerProxy createProxy() throws RemoteException {
        return new RmiPlanSessionStatisticsManagerProxy(super.getRegEntryName(), regEntityName);
    }

    @Override
    public ConcreteOperatorStatistics getOperatorStatistics(OperatorEntity operatorEntity,
                                                            PlanSessionID sessionID) throws RemoteException {
        return statisticsManagerInterface.getOperatorStatistics(operatorEntity, sessionID);
    }

    @Override
    public ElasticTreeStatistics getElasticTreeStatistics() throws RemoteException {
        return statisticsManagerInterface.getElasticTreeStatistics();
    }

    @Override
    public BufferStatistics getBufferStatistics(BufferEntity bufferEntity, PlanSessionID sessionID)
            throws RemoteException {
        return statisticsManagerInterface.getBufferStatistics(bufferEntity, sessionID);
    }

    @Override
    public PlanSessionStatistics getStatistics(PlanSessionID sessionID)
            throws RemoteException {
        return statisticsManagerInterface.getStatistics(sessionID);
    }

    @Override
    public void stopManager() throws RemoteException {
        super.unregister();
    }

    @Override
    public PlanSessionProgressStats getProgress(PlanSessionID sessionID)
            throws RemoteException {
        return statisticsManagerInterface.getProgress(sessionID);
    }
}
