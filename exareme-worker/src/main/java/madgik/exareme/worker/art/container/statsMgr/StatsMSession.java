/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.statsMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author herald
 */
public class StatsMSession implements Serializable {

    private static final long serialVersionUID = 1L;
    private HashMap<ContainerSessionID, StatsMContainerSession> sessionStatsMap =
        new HashMap<ContainerSessionID, StatsMContainerSession>();
    private PlanSessionID sessionID = null;
    private String containerName = null;

    public StatsMSession(PlanSessionID sessionID, String containerName) {
        this.sessionID = sessionID;
        this.containerName = containerName;
    }

    public StatsMContainerSession getSession(ContainerSessionID containerSessionID) {
        StatsMContainerSession session = sessionStatsMap.get(containerSessionID);
        if (session == null) {
            session = new StatsMContainerSession(containerSessionID, sessionID, containerName);
            sessionStatsMap.put(containerSessionID, session);
        }
        return session;
    }

    public void destroySession(ContainerSessionID sessionID) {
        StatsMContainerSession session = sessionStatsMap.remove(sessionID);
        session.destroySession();
    }

    public void destroyAllSessions() {
        for (StatsMContainerSession session : sessionStatsMap.values()) {
            session.destroySession();
        }
        sessionStatsMap.clear();
    }
}
