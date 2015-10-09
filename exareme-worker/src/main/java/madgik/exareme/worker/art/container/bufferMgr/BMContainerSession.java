/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.bufferMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.units.Metrics;
import madgik.exareme.worker.art.container.buffer.*;
import madgik.exareme.worker.art.container.buffer.monitor.BufferMonitor;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.HashMap;

/**
 * @author herald
 */
public class BMContainerSession implements Serializable {

    private static final Logger log = Logger.getLogger(BMContainerSession.class);

    private static final long serialVersionUID = 1L;
    private HashMap<BufferID, CombinedBuffer> bufferMap = new HashMap<BufferID, CombinedBuffer>();
    private ContainerSessionID containerSessionID = null;
    private PlanSessionID sessionID = null;
    private StatisticsManagerInterface statistics = null;
    private long bufferNum = 0;

    public BMContainerSession(ContainerSessionID containerSessionID, PlanSessionID sessionID,
        StatisticsManagerInterface statistics) {
        this.containerSessionID = containerSessionID;
        this.sessionID = sessionID;
        this.statistics = statistics;
    }

    public BufferID createBuffer(String bufferName, BufferQoS quality) throws RemoteException {

        try {
            BufferID bufferID = new BufferID(bufferNum);
            int size = quality.getSizeMB() * 10 * Metrics.KB; //TODO(jv) find sweetspot

            StreamBuffer streamBuffer = StreamBufferFactory.createStreamBuffer(size);
            SocketBuffer socketBuffer = StreamBufferFactory.createSocketBuffer(size);

            BufferMonitor monitor = new BufferMonitor(streamBuffer,
                statistics.getStatistics(containerSessionID, sessionID).
                    createBufferStatistics(bufferName));

            bufferMap.put(bufferID, new CombinedBuffer(socketBuffer, monitor));
            bufferNum++;
            return bufferID;
        } catch (IOException e) {
            throw new ServerException("Cannot create buffer", e);
        }
    }

    public CombinedBuffer getLocalBuffer(BufferID id) throws RemoteException {
        CombinedBuffer buffer = bufferMap.get(id);
        if (buffer == null) {
            throw new AccessException("Buffer not found: " + id.id);
        }
        return buffer;
    }

    public long destroyBuffer(BufferID id) throws RemoteException {

        CombinedBuffer buffer = bufferMap.remove(id);
        if (buffer == null) {
            throw new AccessException("Buffer not found: " + id.id);
        }
        try {
            buffer.socket.close();
            buffer.stream.clear();
            return buffer.stream.getSize();
        } catch (IOException e) {
            throw new ServerException("Cannot destroy buffer", e);
        }
    }

    public Pair<Long, Long> destroySession() throws RemoteException {

        try {
            Pair<Long, Long> sizeCount = new Pair<Long, Long>(0L, 0L);
            for (CombinedBuffer b : bufferMap.values()) {
                sizeCount.a += b.stream.getSize();
                sizeCount.b++;
                b.socket.close();

                b.stream.clear();
            }
            bufferMap.clear();
            return sizeCount;
        } catch (IOException e) {
            throw new ServerException("Cannot destroy session", e);
        }
    }
}
