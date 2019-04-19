/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.remote.RemoteObject;

import java.rmi.RemoteException;

/**
 * This is the read stream adaptor interface.
 *
 * @author Herald Kllapi <br>
 * herald@di.uoa.gr /
 * University of Athens
 * @since 1.0
 */
public interface ReadSocketStreamAdaptor
        extends Adaptor, RemoteObject<ReadSocketStreamAdaptorProxy> {

    EntityName getNetEntityName() throws RemoteException;

    void close() throws RemoteException;
}
