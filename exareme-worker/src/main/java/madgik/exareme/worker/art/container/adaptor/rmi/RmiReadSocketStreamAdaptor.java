/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.adaptor.ReadSocketStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.ReadSocketStreamAdaptorProxy;
import madgik.exareme.worker.art.remote.RmiRemoteObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * @author herald
 */
public class RmiReadSocketStreamAdaptor extends RmiRemoteObject<ReadSocketStreamAdaptorProxy>
    implements ReadSocketStreamAdaptor {

    private EntityName netEntityName = null;
    private EntityName regEntityName = null;

    public RmiReadSocketStreamAdaptor(String name, EntityName netEntityName,
        EntityName regEntityName) throws RemoteException {
        super(name);
        this.netEntityName = netEntityName;
        this.regEntityName = regEntityName;
    }

    @Override public EntityName getNetEntityName() throws RemoteException {
        return netEntityName;
    }

    @Override public RmiReadSocketStreamAdaptorProxy createProxy() throws RemoteException {
        super.register();
        return new RmiReadSocketStreamAdaptorProxy(super.getRegEntryName(), regEntityName);
    }

    @Override public void close() throws RemoteException {
        try {
            super.unregister();
        } catch (IOException e) {
            throw new ServerException("Cannot close", e);
        }
    }
}
