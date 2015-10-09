/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.remote.RemoteObject;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface WriteSocketStreamAdaptor
    extends Adaptor, RemoteObject<WriteSocketStreamAdaptorProxy> {

    EntityName getNetEntityName() throws RemoteException;

    void close() throws RemoteException;
}
