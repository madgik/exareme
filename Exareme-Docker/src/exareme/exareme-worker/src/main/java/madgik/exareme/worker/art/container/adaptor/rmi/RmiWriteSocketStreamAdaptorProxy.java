/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.adaptor.WriteSocketStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.WriteSocketStreamAdaptorProxy;
import madgik.exareme.worker.art.container.buffer.SocketBuffer;
import madgik.exareme.worker.art.container.netMgr.NetSession;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

import java.io.OutputStream;
import java.rmi.AccessException;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class RmiWriteSocketStreamAdaptorProxy extends RmiObjectProxy<WriteSocketStreamAdaptor>
        implements WriteSocketStreamAdaptorProxy {
    private static final long serialVersionUID = 1L;

    private NetSession session = null;
    private OutputStream output = null;
    private SocketBuffer socket = null;

    public RmiWriteSocketStreamAdaptorProxy(String regEntryName, SocketBuffer socket,
                                            EntityName regEntityName) throws RemoteException {
        super(regEntryName, regEntityName);
        this.socket = socket;
    }

    @Override
    public void setNetSession(NetSession session) throws RemoteException {
        this.session = session;
    }

    @Override
    public OutputStream getOutputStream() throws RemoteException {
        try {
            output = session.openOutputStream(socket);
        } catch (Exception e) {
            throw new RemoteException("Cannot open stream", e);
        }
        return output;
    }

    @Override
    public void close() throws RemoteException {
        try {
            if (output != null) {
                this.output.close();
                output = null;
            }
        } catch (java.lang.IllegalStateException e) {
            // Ignore this
        } catch (Exception e) {
            throw new AccessException("Cannot close stream", e);
        }
    }
}
