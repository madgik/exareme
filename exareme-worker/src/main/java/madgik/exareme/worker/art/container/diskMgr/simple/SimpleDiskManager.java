/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.diskMgr.simple;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.diskMgr.DMSession;
import madgik.exareme.worker.art.container.diskMgr.DiskManagerInterface;
import madgik.exareme.worker.art.container.diskMgr.DiskManagerStatus;
import madgik.exareme.worker.art.container.diskMgr.DiskSession;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author herald
 */
public class SimpleDiskManager implements DiskManagerInterface {

    private static final Logger log = Logger.getLogger(SimpleDiskManager.class);
    private HashMap<PlanSessionID, DMSession> sessionMap = new HashMap<PlanSessionID, DMSession>();
    private DiskManagerInterface wrapper = null;
    private DiskManagerStatus status = null;
    private StatisticsManagerInterface statistics = null;

    public SimpleDiskManager(DiskManagerStatus status, StatisticsManagerInterface statistics) {
        this.status = status;
        this.statistics = statistics;
    }

    public void setWrapper(DiskManagerInterface wrapper) {
        this.wrapper = wrapper;
    }

    private DMSession getOrCreateDBSession(PlanSessionID planSessionID) throws RemoteException {
        DMSession newSession = sessionMap.get(planSessionID);
        if (newSession == null) {
            newSession = new DMSession(planSessionID, wrapper);
            sessionMap.put(planSessionID, newSession);
        }
        return newSession;
    }

    public DiskSession getGlobalSession(PlanSessionID planSessionID) throws RemoteException {
        DMSession session = getOrCreateDBSession(planSessionID);
        return session.getGlobalSession();
    }

    public DiskSession getContainerSession(ContainerSessionID containerSessionID,
        PlanSessionID planSessionID) throws RemoteException {
        DMSession session = getOrCreateDBSession(planSessionID);
        return session.getContainerSession(containerSessionID);
    }

    public DiskSession getOperatorSession(ConcreteOperatorID opID,
        ContainerSessionID containerSessionID, PlanSessionID planSessionID) throws RemoteException {
        DMSession session = getOrCreateDBSession(planSessionID);
        return session.getOperatorSession(opID, containerSessionID);
    }

    public void destroyContainerSession(ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        DMSession session = getOrCreateDBSession(sessionID);
        session.destroyContainerSession(containerSessionID);
    }

    public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        DMSession session = getOrCreateDBSession(sessionID);
        session.destroyAllSessions();
    }

    public void destroyAllSessions() throws RemoteException {
        LinkedList<PlanSessionID> sessionIDs = new LinkedList<PlanSessionID>(sessionMap.keySet());
        for (PlanSessionID sID : sessionIDs) {
            destroySessions(sID);
        }
    }
}
