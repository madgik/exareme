/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.local;

import madgik.exareme.worker.art.container.adaptor.ReadRmiStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.ReadRmiStreamAdaptorProxy;
import madgik.exareme.worker.art.remote.RetryPolicy;

import java.io.InputStream;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class LocalReadStreamAdaptorProxy implements ReadRmiStreamAdaptorProxy {

    private static final long serialVersionUID = 1L;
    private LocalReadStreamAdaptor adaptor = null;

    public LocalReadStreamAdaptorProxy(LocalReadStreamAdaptor adaptor) {
        this.adaptor = adaptor;
    }

    public InputStream getInputStream() throws RemoteException {
        return adaptor.getInput();
    }

    public ReadRmiStreamAdaptor connect() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ReadRmiStreamAdaptor getRemoteObject() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RetryPolicy getRetryPolicy() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() throws RemoteException {
        adaptor.close();
    }
}
