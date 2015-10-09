/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.rmi;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.ContainerSessionStatistics;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.check.Check;
import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobResult;
import madgik.exareme.worker.art.container.ContainerJobType;
import madgik.exareme.worker.art.container.job.GetStatisticsJobResult;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManager;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;

import java.rmi.RemoteException;

/**
 * University of Athens / Department of Informatics and Telecommunications.
 *
 * @author Herald Kllapi <br>
 * @author John Chronis<br>
 * @author Vaggelis Nikolopoulos<br>
 * @since 1.0
 */
public class RmiStatisticsManager implements StatisticsManager {

    private StatisticsManagerInterface managerInterface = null;
    private EntityName regEntityName = null;

    public RmiStatisticsManager(StatisticsManagerInterface managerInterface,
        EntityName regEntityName) throws RemoteException {
        this.managerInterface = managerInterface;
        this.regEntityName = regEntityName;
    }

    @Override public ContainerSessionStatistics getStatistics(ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        return managerInterface.getStatistics(containerSessionID, sessionID);
    }

    @Override public void destroyContainerSession(ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        managerInterface.destroyContainerSession(containerSessionID, sessionID);
    }

    @Override public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        managerInterface.destroySessions(sessionID);
    }

    @Override public void destroyAllSessions() throws RemoteException {
        managerInterface.destroyAllSessions();
    }

    @Override public void stopManager() throws RemoteException {
    }

    @Override public void execJob(ContainerJob job, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {

    }

    @Override public boolean hasExec(ContainerJob job) {
        return false;
    }

    @Override
    public ContainerJobResult prepareJob(ContainerJob job, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        Check.True(job.getType() == ContainerJobType.getStatistics, null);
        return new GetStatisticsJobResult(
            managerInterface.getStatistics(containerSessionID, sessionID));
    }

}
