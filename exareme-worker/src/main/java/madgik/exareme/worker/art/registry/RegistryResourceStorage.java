/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.registry.resourceStorage.RegistryResourceStorageStatus;

import java.rmi.RemoteException;
import java.util.Collection;

/**
 * This is the RegistryResourceStorage interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface RegistryResourceStorage {

    void store(Registerable r) throws RemoteException;

    Registerable retrieve(EntityName epr) throws RemoteException;

    void delete(EntityName epr) throws RemoteException;

    Collection<Registerable> retrieveAll(Registerable.Type type) throws RemoteException;

    Collection<Registerable> retrieveAll() throws RemoteException;

    RegistryResourceStorageStatus getResourceStorageStatus() throws RemoteException;

    String getInfo() throws RemoteException;
}
