/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.remote.RemoteObject;

import java.rmi.RemoteException;
import java.util.Collection;

/**
 * This is the ArtRegistry interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}@di.uoa.gr<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ArtRegistry extends RemoteObject<ArtRegistryProxy> {

    void registerEntry(Registerable r) throws RemoteException;

    Registerable lookupEntry(EntityName epr) throws RemoteException;

    void removeEntry(EntityName epr) throws RemoteException;

    Collection<Registerable> list(Registerable.Type type) throws RemoteException;

    ArtRegistryStatus getStatus() throws RemoteException;

    String getStorageInfo() throws RemoteException;

    String[] getStoredObjectsNames() throws RemoteException;

    void stopArtRegistry() throws RemoteException;
}
