/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.adaptor.ReadRmiStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.ReadRmiStreamAdaptorProxy;
import madgik.exareme.worker.art.container.buffer.StreamBuffer;
import madgik.exareme.worker.art.remote.RmiRemoteObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Arrays;

/**
 * @author herald
 */
public class RmiReadStreamAdaptor extends RmiRemoteObject<ReadRmiStreamAdaptorProxy>
        implements ReadRmiStreamAdaptor {

    private StreamBuffer buffer = null;
    private EntityName regEntityName = null;

    public RmiReadStreamAdaptor(String name, StreamBuffer buffer, EntityName regEntityName)
            throws RemoteException {
        super(name);
        this.buffer = buffer;
        this.regEntityName = regEntityName;
    }

    @Override
    public RmiReadStreamAdaptorProxy createProxy() throws RemoteException {
        super.register();
        return new RmiReadStreamAdaptorProxy(super.getRegEntryName(), regEntityName);
    }

    @Override
    public byte[] read(int length) throws RemoteException {
        try {
            byte[] bytes = new byte[length];
            int len = buffer.read(bytes, 0, length);

            // Sometimes read does not return length bytes.
            if (len >= 0) {
                if (len == length) {
                    return bytes;
                } else {
                    return Arrays.copyOf(bytes, len);
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new ServerException("Cannot read", e);
        }
    }

    @Override
    public void close() throws RemoteException {
        try {
            buffer.closeReader();
            super.unregister();
        } catch (IOException e) {
            throw new ServerException("Cannot close", e);
        }
    }
}
