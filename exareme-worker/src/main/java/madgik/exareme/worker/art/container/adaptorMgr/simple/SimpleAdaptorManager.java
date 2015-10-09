/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptorMgr.simple;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.utils.association.Triple;
import madgik.exareme.worker.art.container.adaptor.AdaptorID;
import madgik.exareme.worker.art.container.adaptor.CombinedReadAdaptor;
import madgik.exareme.worker.art.container.adaptor.CombinedWriteAdaptor;
import madgik.exareme.worker.art.container.adaptorMgr.AMContainerSession;
import madgik.exareme.worker.art.container.adaptorMgr.AMSession;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorManagerInterface;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class SimpleAdaptorManager implements AdaptorManagerInterface {

    private HashMap<PlanSessionID, AMSession> sessionMap = new HashMap<PlanSessionID, AMSession>();
    private long adaptorCount = 0;

    public SimpleAdaptorManager() {
    }

    private AMSession getSession(PlanSessionID sessionID) {
        AMSession session = sessionMap.get(sessionID);
        if (session == null) {
            session = new AMSession(sessionID);
            sessionMap.put(sessionID, session);
        }

        return session;
    }

    @Override public AdaptorID addReadAdaptor(CombinedReadAdaptor adaptor,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        AMSession session = getSession(sessionID);
        AMContainerSession cSession = session.getSession(containerSessionID);

        AdaptorID adaptorID = new AdaptorID(adaptorCount);
        adaptorCount++;

        cSession.addReadAdaptor(adaptorID, adaptor);

        return adaptorID;
    }

    @Override public AdaptorID addWriteAdaptor(CombinedWriteAdaptor adaptor,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        AMSession session = getSession(sessionID);
        AMContainerSession cSession = session.getSession(containerSessionID);

        AdaptorID adaptorID = new AdaptorID(adaptorCount);
        adaptorCount++;

        cSession.addWriteAdaptor(adaptorID, adaptor);

        return adaptorID;
    }

    @Override public CombinedReadAdaptor getReadAdaptor(AdaptorID adaptorID,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        AMSession session = getSession(sessionID);
        AMContainerSession cSession = session.getSession(containerSessionID);

        return cSession.getReadAdaptor(adaptorID);
    }

    @Override public CombinedWriteAdaptor getWriteAdaptor(AdaptorID adaptorID,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        AMSession session = getSession(sessionID);
        AMContainerSession cSession = session.getSession(containerSessionID);

        return cSession.getWriteAdaptor(adaptorID);
    }

    @Override
    public Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroyContainerSession(
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {

        AMSession session = getSession(sessionID);
        return session.destroySession(containerSessionID);
    }

    @Override
    public Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroySessions(
        PlanSessionID sessionID) throws RemoteException {

        AMSession session = getSession(sessionID);
        return session.destroyAllSessions();
    }

    @Override
    public Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroyAllSessions()
        throws RemoteException {

        Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> result =
            new Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>>(0L,
                new LinkedList<CombinedReadAdaptor>(), new LinkedList<CombinedWriteAdaptor>());

        for (AMSession session : sessionMap.values()) {
            Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> r =
                session.destroyAllSessions();

            result.a += r.a;
            result.b.addAll(r.b);
            result.c.addAll(r.c);
        }

        return result;
    }
}
