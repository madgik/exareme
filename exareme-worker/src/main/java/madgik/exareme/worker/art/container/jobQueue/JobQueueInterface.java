/*
 * Copyright MaDgIK Group 2010-2014.
 */
package madgik.exareme.worker.art.container.jobQueue;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobResult;
import madgik.exareme.worker.art.container.Executor;
import madgik.exareme.worker.art.container.SessionBased;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorManager;
import madgik.exareme.worker.art.container.bufferMgr.BufferManager;
import madgik.exareme.worker.art.container.dataTrasferMgr.DataTransferMgrInterface;
import madgik.exareme.worker.art.container.job.AbstractContainerJob;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManager;
import madgik.exareme.worker.art.container.resources.ContainerResources;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManager;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author John Chronis<br>
 * @author Vaggelis Nikolopoulos<br>
 */
public interface JobQueueInterface extends SessionBased {

    void setExecutor(Executor executor);

    ArrayList<AbstractContainerJob> getNextJob(ContainerResources resources);

    ContainerJobResult addJob(ContainerJob job, ContainerSessionID contSessionID,
        PlanSessionID sessionID) throws RemoteException;

    void setManagers(StatisticsManager statisticsManager,
        ConcreteOperatorManager concreteOperatorManager, BufferManager bufferManager,
        AdaptorManager adaptorManager, DataTransferMgrInterface dataTransferManagerDTP);

    void freeResources(ConcreteOperatorID opID);

}
