/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.bufferMgr.simple;

//import madgik.exareme.utils.properties.AdpProperties;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.utils.units.Metrics;
import madgik.exareme.worker.art.container.buffer.BufferID;
import madgik.exareme.worker.art.container.buffer.BufferQoS;
import madgik.exareme.worker.art.container.buffer.CombinedBuffer;
import madgik.exareme.worker.art.container.buffer.StreamBuffer;
import madgik.exareme.worker.art.container.bufferMgr.BMContainerSession;
import madgik.exareme.worker.art.container.bufferMgr.BMSession;
import madgik.exareme.worker.art.container.bufferMgr.BufferManagerInterface;
import madgik.exareme.worker.art.container.bufferMgr.BufferManagerStatus;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class SimpleBufferManager implements BufferManagerInterface {

    private static final Logger log = Logger.getLogger(SimpleBufferManager.class);
    private final long pipePoolSize;
    private HashMap<PlanSessionID, BMSession> sessionMap = new HashMap<PlanSessionID, BMSession>();
    private BufferManagerStatus status = null;
    private StatisticsManagerInterface statistics = null;
    private long maxCapacity = 0L;
    private long allocated = 0L;

    public SimpleBufferManager(BufferManagerStatus bufferStatus,
        StatisticsManagerInterface statistics) {
        this.status = bufferStatus;
        this.statistics = statistics;
        this.pipePoolSize =
            AdpProperties.getArtProps().getLong("art.container.pipePoolSize_mb") * Metrics.MB;

        this.maxCapacity = pipePoolSize;
        this.allocated = 0;

        this.status.getPipeSizeMeasurement().setMaxValue(maxCapacity);
    }

    private BMSession getSession(PlanSessionID sessionID) {
        BMSession session = sessionMap.get(sessionID);
        if (session == null) {
            session = new BMSession(sessionID, statistics, this);
            sessionMap.put(sessionID, session);

            this.status.getSessionMeasurement().changeActiveValue(1);
        }

        return session;
    }

    @Override public BufferID createBuffer(String bufferName, BufferQoS quality,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        int size = quality.getSizeMB() * Metrics.MB;
        if (allocated + size > maxCapacity) {
            //throw new AccessException("Not enough memory");
        }
        BMSession session = getSession(sessionID);
        BufferID id = session.getContainerSession(containerSessionID).
            createBuffer(bufferName, quality);
        allocated += size;
        this.status.getPipeSizeMeasurement().changeActiveValue(size);
        this.status.getPipeCountMeasurement().changeActiveValue(1);
        return id;
    }

    @Override
    public CombinedBuffer getLocalBuffer(BufferID id, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        BMSession session = getSession(sessionID);
        BMContainerSession cSession = session.getContainerSession(containerSessionID);
        CombinedBuffer buffer = cSession.getLocalBuffer(id);
        return buffer;
    }

    public StreamBuffer getLocalStreamBuffer(BufferID id, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        BMSession session = getSession(sessionID);
        BMContainerSession cSession = session.getContainerSession(containerSessionID);
        CombinedBuffer buffer = cSession.getLocalBuffer(id);
        return buffer.stream;
    }

    @Override public void destroyBuffer(BufferID id, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        BMSession session = getSession(sessionID);
        BMContainerSession cSession = session.getContainerSession(containerSessionID);
        long size = cSession.destroyBuffer(id);
        allocated -= size;
        this.status.getPipeSizeMeasurement().changeActiveValue(-size);
        this.status.getPipeCountMeasurement().changeActiveValue(-1);
    }

    @Override public void destroyContainerSession(ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        BMSession session = getSession(sessionID);
        if (session != null) {
            BMContainerSession cSession = session.getContainerSession(containerSessionID);
            Pair<Long, Long> sizeCount = cSession.destroySession();
            this.status.getPipeCountMeasurement().changeActiveValue(-sizeCount.b);
            allocated -= sizeCount.a;
            this.status.getPipeSizeMeasurement().changeActiveValue(-sizeCount.a);
        }
    }

    @Override public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        BMSession session = getSession(sessionID);
        if (session != null) {
            session.destroyAllSessions();
            this.status.getSessionMeasurement().changeActiveValue(-1);
        }
    }

    @Override public void destroyAllSessions() throws RemoteException {
        for (BMSession session : sessionMap.values()) {
            session.destroyAllSessions();
        }
        this.status.getSessionMeasurement().changeActiveValue(-sessionMap.size());
        sessionMap.clear();
    }
}
