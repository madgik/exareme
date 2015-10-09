/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.netMgr.session;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.buffer.SocketBuffer;
import madgik.exareme.worker.art.container.netMgr.NetSession;

import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class NetSessionSynchronized implements NetSession {

    private static final long serialVersionUID = 1L;
    private final NetSession session;

    public NetSessionSynchronized(NetSession session) {
        this.session = session;
    }

    @Override public InputStream openInputStream(EntityName netAddress) throws RemoteException {
        synchronized (session) {
            return session.openInputStream(netAddress);
        }
    }

    @Override public OutputStream openOutputStream(SocketBuffer socket) throws RemoteException {
        synchronized (session) {
            return session.openOutputStream(socket);
        }
    }

    @Override public InputStream openInputStream(SocketBuffer socket) throws RemoteException {
        synchronized (session) {
            return session.openInputStream(socket);
        }
    }

    @Override public OutputStream openOutputStream(EntityName netAddress) throws RemoteException {
        synchronized (session) {
            return session.openOutputStream(netAddress);
        }
    }
}
