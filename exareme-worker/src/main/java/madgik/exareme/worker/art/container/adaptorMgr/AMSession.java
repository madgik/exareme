/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptorMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.association.Triple;
import madgik.exareme.worker.art.container.adaptor.CombinedReadAdaptor;
import madgik.exareme.worker.art.container.adaptor.CombinedWriteAdaptor;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author herald
 */
public class AMSession implements Serializable {
    private static final org.apache.log4j.Logger log =
        org.apache.log4j.Logger.getLogger(AMSession.class);

    private static final long serialVersionUID = 1L;
    private HashMap<ContainerSessionID, AMContainerSession> sessionMap =
        new HashMap<ContainerSessionID, AMContainerSession>();
    private PlanSessionID sessionID = null;

    public AMSession(PlanSessionID sessionID) {
        this.sessionID = sessionID;
    }

    public AMContainerSession getSession(ContainerSessionID containerSessionID)
        throws RemoteException {
        AMContainerSession session = sessionMap.get(containerSessionID);
        if (session == null) {
            session = new AMContainerSession(containerSessionID, sessionID);
            sessionMap.put(containerSessionID, session);
        }

        return session;
    }

    public Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroySession(
        ContainerSessionID containerSessionID) throws RemoteException {
        AMContainerSession session = sessionMap.remove(containerSessionID);
        if (session == null) {
            // return empty set.
            return new Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>>(0L,
                new LinkedList<CombinedReadAdaptor>(), new LinkedList<CombinedWriteAdaptor>());
        }

        Pair<List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> r = session.destroySession();

        return new Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>>(1L, r.a,
            r.b);
    }

    public Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroyAllSessions()
        throws RemoteException {

        Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> result =
            new Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>>(0L,
                new LinkedList<CombinedReadAdaptor>(), new LinkedList<CombinedWriteAdaptor>());
        for (AMContainerSession session : sessionMap.values()) {
            Pair<List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> r =
                session.destroySession();
            result.a++;
            result.b.addAll(r.a);
            result.c.addAll(r.b);
        }
        sessionMap.clear();
        return result;
    }
}
