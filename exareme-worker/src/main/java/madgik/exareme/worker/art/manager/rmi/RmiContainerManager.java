/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.manager.rmi;

import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.container.*;
import madgik.exareme.worker.art.manager.ContainerManager;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Logger;

import javax.activity.ActivityRequiredException;
import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiContainerManager implements ContainerManager {

    private final static Logger log = Logger.getLogger(RmiContainerManager.class);
    private static String containerName = null;
    private static ContainerID containerID = null;
    private static RmiArtManager artManager = null;
    private static Container container = null;
    private static ContainerProxy proxy = null;
    private static int dataTransferPort;

    public RmiContainerManager(String containerName, long cId, RmiArtManager artManager,
        int dataTransferPort) {
        RmiContainerManager.containerName = containerName;
        RmiContainerManager.containerID = new ContainerID(cId);
        RmiContainerManager.artManager = artManager;
        RmiContainerManager.dataTransferPort = dataTransferPort;
    }

    @Override public void stopContainer() throws RemoteException {
        container.stopContainer();
        ArtRegistryLocator.getArtRegistryProxy().removeContainer(proxy.getEntityName());
        container = null;
        proxy = null;
    }

    @Override public boolean isUp() {
        return (container != null);
    }

    @Override public Container getContainer() {
        return container;
    }

    @Override public void startContainer() throws RemoteException {
        if (artManager.getRegistryManager().isOnline()) {
            log.debug("Starting container...");
            // Choose here a different implmenentation of the container if needed
            String engineType = AdpProperties.getArtProps().getString("art.scheduler.mode");
            if (engineType == null) {
                engineType = "centralized";
            }
            if (engineType.equalsIgnoreCase("centralized")) {
                container = ContainerFactory.createRMIThreadContainer(containerName, containerID,
                    ArtRegistryLocator.getLocalRmiRegistryEntityName(), dataTransferPort);
            } else {
                throw new RemoteException("Mode not supported: " + engineType);
            }
            log.debug("Creating proxy ...");
            proxy = container.createProxy();

            ArtRegistryLocator.getArtRegistryProxy().registerContainer(proxy);
            ContainerLocator.setLocalContainer(proxy);
        } else {
            throw new ActivityRequiredException("Art Registry offline!");
        }
    }
}
