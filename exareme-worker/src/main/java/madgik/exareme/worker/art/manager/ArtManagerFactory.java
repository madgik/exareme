/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.manager;

import madgik.exareme.worker.art.manager.rmi.RmiArtManager;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ArtManagerFactory {

    private ArtManagerFactory() {
        throw new RuntimeException("Cannot create instances of this class");
    }

    public static ArtManager createRmiArtManager() throws RemoteException {
        return createRmiArtManager(new ArtManagerProperties());
    }

    public static ArtManager createRmiArtManager(ArtManagerProperties managerProperties)
            throws RemoteException {
        return new RmiArtManager(managerProperties.getContainerName(),
                managerProperties.getContainerID(), managerProperties.getLocalRegistryPort(),
                managerProperties.getDataTransferPort());
    }
}
