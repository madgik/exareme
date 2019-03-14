/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.netMgr.simple;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.netMgr.NetManagerInterface;
import madgik.exareme.worker.art.container.netMgr.NetManagerStatus;
import madgik.exareme.worker.art.container.netMgr.NetSession;
import madgik.exareme.worker.art.container.netMgr.session.NetSessionSimple;
import madgik.exareme.worker.art.container.netMgr.session.NetSessionSynchronized;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class SimpleNetManager implements NetManagerInterface {

    private NetManagerStatus status = null;
    private StatisticsManagerInterface statistics = null;
    private NetManagerInterface wrapper = null;

    public SimpleNetManager(NetManagerStatus status, StatisticsManagerInterface statistics) {
        this.status = status;
        this.statistics = statistics;
    }

    @Override
    public NetSession getGlobalSession(PlanSessionID planSessionID)
            throws RemoteException {
        return new NetSessionSynchronized(new NetSessionSimple());
    }

    @Override
    public NetSession getContainerSession(ContainerSessionID containerSessionID,
                                          PlanSessionID planSessionID) throws RemoteException {
        return new NetSessionSynchronized(new NetSessionSimple());
    }

    @Override
    public NetSession getOperatorSession(ConcreteOperatorID opID,
                                         ContainerSessionID containerSessionID, PlanSessionID planSessionID) throws RemoteException {
        return new NetSessionSynchronized(new NetSessionSimple());
    }

    @Override
    public void destroyContainerSession(ContainerSessionID containerSessionID,
                                        PlanSessionID sessionID) throws RemoteException {
    }

    @Override
    public void destroySessions(PlanSessionID sessionID) throws RemoteException {
    }

    @Override
    public void destroyAllSessions() throws RemoteException {
    }

    public void setWrapper(NetManagerInterface wrapper) {
        this.wrapper = wrapper;
    }
}
