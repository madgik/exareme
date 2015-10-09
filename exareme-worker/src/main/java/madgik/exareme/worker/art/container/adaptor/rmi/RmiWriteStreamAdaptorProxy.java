/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.adaptor.StreamFactory;
import madgik.exareme.worker.art.container.adaptor.WriteRmiStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.WriteRmiStreamAdaptorProxy;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * @author herald
 */
public class RmiWriteStreamAdaptorProxy extends RmiObjectProxy<WriteRmiStreamAdaptor>
    implements WriteRmiStreamAdaptorProxy {
    private static final long serialVersionUID = 1L;

    private OutputStream output = null;

    public RmiWriteStreamAdaptorProxy(String regEntryName, EntityName regEntityName)
        throws RemoteException {
        super(regEntryName, regEntityName);
    }

    @Override public OutputStream getOutputStream() throws RemoteException {
        if (output == null) {
            try {
                output = StreamFactory.createOutputStream(this.getRemoteObject());
            } catch (IOException e) {
                throw new ServerException("Cannot create output stream", e);
            }
        }
        return output;
    }

    @Override public void close() throws RemoteException {
        try {
            if (output != null) {
                this.output.close();
            }
        } catch (Exception e) {
            throw new AccessException("Cannot close stream", e);
        }
    }
}
