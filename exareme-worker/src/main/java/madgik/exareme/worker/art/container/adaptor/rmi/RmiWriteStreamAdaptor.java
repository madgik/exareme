/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.adaptor.WriteRmiStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.WriteRmiStreamAdaptorProxy;
import madgik.exareme.worker.art.container.buffer.StreamBuffer;
import madgik.exareme.worker.art.remote.RmiRemoteObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * @author herald
 */
public class RmiWriteStreamAdaptor extends RmiRemoteObject<WriteRmiStreamAdaptorProxy>
    implements WriteRmiStreamAdaptor {

    private StreamBuffer buffer = null;
    private EntityName regEntityName = null;

    public RmiWriteStreamAdaptor(String name, StreamBuffer buffer, EntityName regEntityName)
        throws RemoteException {
        super(name);
        this.buffer = buffer;
        this.regEntityName = regEntityName;
    }

    @Override public WriteRmiStreamAdaptorProxy createProxy() throws RemoteException {
        super.register();
        return new RmiWriteStreamAdaptorProxy(super.getRegEntryName(), regEntityName);
    }

    @Override public void write(byte[] bytes) throws RemoteException {
        try {
            buffer.write(bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new ServerException("Cannot write", e);
        }
    }

    @Override public void write(byte[] bytes, int offset, int length) throws RemoteException {
        try {
            buffer.write(bytes, offset, length);
        } catch (IOException e) {
            throw new ServerException("Cannot write", e);
        }
    }

    @Override public void close() throws RemoteException {
        try {
            buffer.closeWriter();
            super.unregister();
        } catch (IOException e) {
            throw new ServerException("Cannot close", e);
        }
    }
}
