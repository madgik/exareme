/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.adaptor.WriteSocketStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.WriteSocketStreamAdaptorProxy;
import madgik.exareme.worker.art.container.buffer.SocketBuffer;
import madgik.exareme.worker.art.remote.RmiRemoteObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * @author herald
 */
public class RmiWriteSocketStreamAdaptor extends RmiRemoteObject<WriteSocketStreamAdaptorProxy>
        implements WriteSocketStreamAdaptor {

    private SocketBuffer socket = null;
    private EntityName regEntityName = null;

    public RmiWriteSocketStreamAdaptor(String name, SocketBuffer socket, EntityName regEntityName)
            throws RemoteException {
        super(name);
        this.regEntityName = regEntityName;
        this.socket = socket;
    }

    @Override
    public EntityName getNetEntityName() throws RemoteException {
        return socket.getNetEntityName();
    }

    @Override
    public WriteSocketStreamAdaptorProxy createProxy() throws RemoteException {
        super.register();
        return new RmiWriteSocketStreamAdaptorProxy(super.getRegEntryName(), socket, regEntityName);
    }

    @Override
    public void close() throws RemoteException {
        try {
            super.unregister();
        } catch (IOException e) {
            throw new ServerException("Cannot close", e);
        }
    }
}
