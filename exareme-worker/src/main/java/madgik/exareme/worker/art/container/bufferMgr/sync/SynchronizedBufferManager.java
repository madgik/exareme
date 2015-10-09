/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.bufferMgr.sync;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.container.buffer.BufferID;
import madgik.exareme.worker.art.container.buffer.BufferQoS;
import madgik.exareme.worker.art.container.buffer.CombinedBuffer;
import madgik.exareme.worker.art.container.bufferMgr.BufferManagerInterface;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class SynchronizedBufferManager implements BufferManagerInterface {

    private final Object lock = new Object();
    private BufferManagerInterface bufferManager = null;

    public SynchronizedBufferManager(BufferManagerInterface bufferManager) {
        this.bufferManager = bufferManager;
    }

    @Override public BufferID createBuffer(String bufferName, BufferQoS quality,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        synchronized (lock) {
            return bufferManager.createBuffer(bufferName, quality, containerSessionID, sessionID);
        }
    }

    @Override public void destroyBuffer(BufferID id, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        synchronized (lock) {
            bufferManager.destroyBuffer(id, containerSessionID, sessionID);
        }
    }

    @Override public void destroyContainerSession(ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        synchronized (lock) {
            bufferManager.destroyContainerSession(containerSessionID, sessionID);
        }
    }

    @Override
    public CombinedBuffer getLocalBuffer(BufferID id, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        synchronized (lock) {
            return bufferManager.getLocalBuffer(id, containerSessionID, sessionID);
        }
    }

    @Override public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        synchronized (lock) {
            bufferManager.destroySessions(sessionID);
        }
    }

    @Override public void destroyAllSessions() throws RemoteException {
        synchronized (lock) {
            bufferManager.destroyAllSessions();
        }
    }
}
