/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.netMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * @author heraldkllapi
 */
public class NMSession {
    private static final Logger log = Logger.getLogger(NMSession.class);

    private PlanSessionID sessionID = null;
    private StatisticsManagerInterface statistics = null;
    private NetManagerInterface manager = null;
    private HashMap<ContainerSessionID, NMContainerSession> containerSessionMap =
        new HashMap<ContainerSessionID, NMContainerSession>();

    public NMSession(PlanSessionID sessionID, StatisticsManagerInterface statistics,
        NetManagerInterface manager) {
        this.sessionID = sessionID;
        this.statistics = statistics;
        this.manager = manager;
    }

    public NMContainerSession getContainerSession(ContainerSessionID containerSessionID) {
        NMContainerSession session = containerSessionMap.get(containerSessionID);
        if (session == null) {
            session = new NMContainerSession(containerSessionID, sessionID, statistics);
            containerSessionMap.put(containerSessionID, session);
        }
        return session;
    }

    public void destroySession(ContainerSessionID containerSessionID) throws RemoteException {
        log.debug("DThttp12 destroySessions NMsession");

        manager.destroyContainerSession(containerSessionID, sessionID);
        containerSessionMap.remove(containerSessionID);
    }

    public void destroyAllSessions() throws RemoteException {
        log.debug("DThttp12 destroyallSessions NMsession " + containerSessionMap.size());

        for (ContainerSessionID containerSessionID : containerSessionMap.keySet()) {
            manager.destroyContainerSession(containerSessionID, sessionID);
        }
        containerSessionMap.clear();
    }
}
