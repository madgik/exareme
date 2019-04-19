/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.local;

import madgik.exareme.worker.art.container.adaptor.WriteRmiStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.WriteRmiStreamAdaptorProxy;
import madgik.exareme.worker.art.remote.RetryPolicy;

import java.io.OutputStream;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class LocalWriteStreamAdaptorProxy implements WriteRmiStreamAdaptorProxy {

    private static final long serialVersionUID = 1L;
    private LocalWriteStreamAdaptor adaptor = null;

    public LocalWriteStreamAdaptorProxy(LocalWriteStreamAdaptor adaptor) {
        this.adaptor = adaptor;
    }

    public OutputStream getOutputStream() throws RemoteException {
        return adaptor.getOutput();
    }

    public WriteRmiStreamAdaptor connect() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public WriteRmiStreamAdaptor getRemoteObject() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RetryPolicy getRetryPolicy() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() throws RemoteException {
        adaptor.close();
    }
}
