/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.registry.resourceStorage.MemoryResourceStorage;
import madgik.exareme.worker.art.registry.rmi.RmiArtRegistry;

import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class ArtRegistryFactory {

    private ArtRegistryFactory() {
    }

    public static ArtRegistry createRmiArtMemoryRegistry(EntityName regEntityName)
            throws RemoteException {
        try {
            return new RmiArtRegistry(new MemoryResourceStorage(), regEntityName);
        } catch (Exception ex) {
            throw new ServerException("Cannot create memory registry", ex);
        }
    }
}
