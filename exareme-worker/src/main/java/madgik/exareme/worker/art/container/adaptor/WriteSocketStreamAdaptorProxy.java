/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

import madgik.exareme.worker.art.container.netMgr.NetSession;
import madgik.exareme.worker.art.remote.ObjectProxy;

import java.io.OutputStream;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface WriteSocketStreamAdaptorProxy
    extends ObjectProxy<WriteSocketStreamAdaptor>, AdaptorProxy {

    void setNetSession(NetSession manager) throws RemoteException;

    OutputStream getOutputStream() throws RemoteException;

    void close() throws RemoteException;
}
