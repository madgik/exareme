/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.statsMgr.simple;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.ContainerSessionStatistics;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;
import madgik.exareme.worker.art.container.statsMgr.StatsMContainerSession;
import madgik.exareme.worker.art.container.statsMgr.StatsMSession;

import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * @author herald
 */
public class SimpleStatisticsManager implements StatisticsManagerInterface {

    private HashMap<PlanSessionID, StatsMSession> sessionMap =
            new HashMap<PlanSessionID, StatsMSession>();
    private String containerName = null;

    public SimpleStatisticsManager(String containerName) {
        this.containerName = NetUtil.getIPv4() + ":" + containerName;
    }

    private StatsMSession getSession(PlanSessionID sessionID) {
        StatsMSession session = sessionMap.get(sessionID);
        if (session == null) {
            session = new StatsMSession(sessionID, containerName);
            sessionMap.put(sessionID, session);
        }

        return session;
    }

    public ContainerSessionStatistics getStatistics(ContainerSessionID containerSessionID,
                                                    PlanSessionID sessionID) throws RemoteException {
        StatsMSession session = getSession(sessionID);
        StatsMContainerSession cSession = session.getSession(containerSessionID);
        return cSession.getStatistics();
    }

    public void destroyContainerSession(ContainerSessionID containerSessionID,
                                        PlanSessionID sessionID) throws RemoteException {
        StatsMSession session = getSession(sessionID);
        session.destroySession(containerSessionID);
    }

    public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        StatsMSession session = getSession(sessionID);
        session.destroyAllSessions();
    }

    public void destroyAllSessions() throws RemoteException {
        for (StatsMSession session : sessionMap.values()) {
            session.destroyAllSessions();
        }
        sessionMap.clear();
    }
}
