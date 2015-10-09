/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.manager;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.registry.ArtRegistry;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ArtRegistryManager {

    ArtRegistry getRegistry() throws RemoteException;

    void startArtRegistry() throws RemoteException;

    void stopArtRegistry() throws RemoteException;

    void connectToRegistry(EntityName name) throws RemoteException;

    boolean isOnline() throws RemoteException;
}
