/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

import madgik.exareme.worker.art.container.netMgr.NetSession;
import madgik.exareme.worker.art.remote.ObjectProxy;

import java.io.InputStream;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface ReadSocketStreamAdaptorProxy
    extends ObjectProxy<ReadSocketStreamAdaptor>, AdaptorProxy {

    void setNetSession(NetSession manager) throws RemoteException;

    InputStream getInputStream() throws RemoteException;

    void close() throws RemoteException;
}
