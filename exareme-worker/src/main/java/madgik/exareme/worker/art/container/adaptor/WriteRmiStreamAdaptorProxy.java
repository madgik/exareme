/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

import madgik.exareme.worker.art.remote.ObjectProxy;

import java.io.OutputStream;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface WriteRmiStreamAdaptorProxy
    extends ObjectProxy<WriteRmiStreamAdaptor>, AdaptorProxy {

    OutputStream getOutputStream() throws RemoteException;

    void close() throws RemoteException;
}
