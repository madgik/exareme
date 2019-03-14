/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.adaptor.ReadSocketStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.ReadSocketStreamAdaptorProxy;
import madgik.exareme.worker.art.container.netMgr.NetSession;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.AccessException;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class RmiReadSocketStreamAdaptorProxy extends RmiObjectProxy<ReadSocketStreamAdaptor>
        implements ReadSocketStreamAdaptorProxy {

    private static final long serialVersionUID = 1L;
    private InputStream input = null;
    private NetSession session = null;

    public RmiReadSocketStreamAdaptorProxy(String regEntryName, EntityName regEntityName)
            throws RemoteException {
        super(regEntryName, regEntityName);
    }

    @Override
    public void setNetSession(NetSession session) throws RemoteException {
        this.session = session;
    }

    @Override
    public InputStream getInputStream() throws RemoteException {
        EntityName netEntityName = null;
        try {
            netEntityName = this.getRemoteObject().getNetEntityName();
            input = session.openInputStream(netEntityName);
            return input;
        } catch (RemoteException e) {
            throw new RemoteException("Cannot open stream (" + netEntityName + ")", e);
        }

    }

    @Override
    public void close() throws RemoteException {
        try {
            if (input != null) {
                this.input.close();
                input = null;
            }
        } catch (java.lang.IllegalStateException e) {
            // Ignore this
        } catch (IOException e) {
            throw new AccessException("Cannot close stream", e);
        }
    }
}
