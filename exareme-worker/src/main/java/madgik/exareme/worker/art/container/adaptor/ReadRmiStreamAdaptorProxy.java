/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

import madgik.exareme.worker.art.remote.ObjectProxy;

import java.io.InputStream;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface ReadRmiStreamAdaptorProxy extends ObjectProxy<ReadRmiStreamAdaptor>, AdaptorProxy {

    InputStream getInputStream() throws RemoteException;

    void close() throws RemoteException;
}
