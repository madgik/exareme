/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.adaptor.ReadRmiStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.ReadRmiStreamAdaptorProxy;
import madgik.exareme.worker.art.container.adaptor.StreamFactory;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * @author herald
 */
public class RmiReadStreamAdaptorProxy extends RmiObjectProxy<ReadRmiStreamAdaptor>
    implements ReadRmiStreamAdaptorProxy {

    private static final long serialVersionUID = 1L;
    private InputStream input = null;

    public RmiReadStreamAdaptorProxy(String regEntryName, EntityName regEntityName)
        throws RemoteException {
        super(regEntryName, regEntityName);
    }

    @Override public InputStream getInputStream() throws RemoteException {
        if (input == null) {
            try {
                this.input = StreamFactory.createInputStream(this.getRemoteObject());
            } catch (IOException e) {
                throw new ServerException("Cannot create input stream", e);
            }
        }
        return input;
    }

    @Override public void close() throws RemoteException {
        try {
            if (input != null) {
                this.input.close();
            }
        } catch (Exception e) {
            throw new AccessException("Cannot close stream", e);
        }
    }
}
