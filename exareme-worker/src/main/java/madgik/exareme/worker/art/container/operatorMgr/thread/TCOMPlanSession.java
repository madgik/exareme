/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.operatorMgr.thread;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.container.jobQueue.JobQueueInterface;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author herald
 */
public class TCOMPlanSession {

    JobQueueInterface jobQueueInterface;
    private HashMap<ContainerSessionID, TCOMContainerSession> sessionMap =
        new HashMap<ContainerSessionID, TCOMContainerSession>();
    private PlanSessionID sessionID = null;

    public TCOMPlanSession(PlanSessionID sessionID, JobQueueInterface jobQueueInterface) {
        this.sessionID = sessionID;
        this.jobQueueInterface = jobQueueInterface;
    }

    public TCOMContainerSession getSession(ContainerSessionID containerSessionID) {
        TCOMContainerSession session = sessionMap.get(containerSessionID);
        if (session == null) {
            session = new TCOMContainerSession(containerSessionID, sessionID, jobQueueInterface);
            sessionMap.put(containerSessionID, session);
        }
        return session;
    }

    public int destroySession(ContainerSessionID containerSessionID) throws RemoteException {
        // TODO(DSD): free resources? to kanei i destroy session
        TCOMContainerSession session = sessionMap.remove(containerSessionID);
        return session.destroySession();
    }

    public int destroySession() throws RemoteException {
        // TODO(DSD): free resources? to kanei i destroy session
        LinkedList<ContainerSessionID> ids =
            new LinkedList<ContainerSessionID>(sessionMap.keySet());
        int count = 0;
        for (ContainerSessionID id : ids) {
            count += destroySession(id);
        }
        return count;
    }
}
