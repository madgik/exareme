/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.bufferMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * @author herald
 */
public class BMSession implements Serializable {
    private static final org.apache.log4j.Logger log =
        org.apache.log4j.Logger.getLogger(BMSession.class);

    private static final long serialVersionUID = 1L;
    private HashMap<ContainerSessionID, BMContainerSession> containerSessionMap =
        new HashMap<ContainerSessionID, BMContainerSession>();
    private StatisticsManagerInterface statistics = null;
    private PlanSessionID sessionID = null;
    private BufferManagerInterface manager = null;

    public BMSession(PlanSessionID sessionID, StatisticsManagerInterface statistics,
        BufferManagerInterface manager) {
        this.statistics = statistics;
        this.sessionID = sessionID;
        this.manager = manager;
    }

    public BMContainerSession getContainerSession(ContainerSessionID containerSessionID) {
        BMContainerSession session = containerSessionMap.get(containerSessionID);
        if (session == null) {
            session = new BMContainerSession(containerSessionID, sessionID, statistics);
            containerSessionMap.put(containerSessionID, session);
        }
        return session;
    }

    public void destroySession(ContainerSessionID containerSessionID) throws RemoteException {
        manager.destroyContainerSession(containerSessionID, sessionID);
        containerSessionMap.remove(containerSessionID);
    }

    public void destroyAllSessions() throws RemoteException {
        for (ContainerSessionID containerSessionID : containerSessionMap.keySet()) {
            manager.destroyContainerSession(containerSessionID, sessionID);
        }
        containerSessionMap.clear();
    }
}
