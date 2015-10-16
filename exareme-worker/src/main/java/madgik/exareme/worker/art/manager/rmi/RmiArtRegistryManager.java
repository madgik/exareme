/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.manager.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.worker.art.manager.ArtRegistryManager;
import madgik.exareme.worker.art.registry.ArtRegistry;
import madgik.exareme.worker.art.registry.ArtRegistryFactory;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiArtRegistryManager implements ArtRegistryManager {
    private static int localRegistryPort = 0;
    private static RmiArtManager artManager = null;
    private static ArtRegistry artRegistry = null;
    private static Registry artRmiRegistry = null;
    private static Registry localRmiRegistry = null;
    private static EntityName localRegEntityName = null;
    private static boolean isLocalRegistry = false;
    private static Logger log = Logger.getLogger(RmiArtRegistryManager.class);

    public RmiArtRegistryManager(int localRegistryPort, RmiArtManager artManager) {
        log.info("RmiArtRegistryManager created");
        RmiArtRegistryManager.localRegistryPort = localRegistryPort;
        RmiArtRegistryManager.artManager = artManager;
    }

    public EntityName getRmiRegistryName() {
        return localRegEntityName;
    }

    @Override public boolean isOnline() throws RemoteException {
        return (artRmiRegistry != null);
    }

    @Override public ArtRegistry getRegistry() throws RemoteException {
        return artRegistry;
    }

    @Override public void startArtRegistry() throws RemoteException {
        try {
            artRmiRegistry = LocateRegistry.createRegistry(localRegistryPort);
        } catch (Exception e) {
            log.error("Rmi Registry already running on localhost", e);
            artRmiRegistry = LocateRegistry.getRegistry(localRegistryPort);
        }
        localRmiRegistry = artRmiRegistry;
        localRegEntityName = new EntityName("RmiRegistry", NetUtil.getIPv4(), localRegistryPort);

        ArtRegistryLocator.setArtRmiRegistry(artRmiRegistry);
        ArtRegistryLocator.setLocalRmiRegistry(localRmiRegistry, localRegEntityName);

        artRegistry = ArtRegistryFactory.createRmiArtMemoryRegistry(localRegEntityName);
        ArtRegistryLocator.setArtRegistryProxy(artRegistry.createProxy());

        isLocalRegistry = true;
    }

    @Override public void connectToRegistry(EntityName name) throws RemoteException {
        try {
            artRmiRegistry = LocateRegistry.getRegistry(name.getIP(), name.getPort());
            try {
                localRmiRegistry = LocateRegistry.createRegistry(localRegistryPort);
            } catch (Exception e) {
                log.error("Rmi Registry already running on localhost", e);
                localRmiRegistry = LocateRegistry.getRegistry(localRegistryPort);
            }
            localRegEntityName =
                new EntityName("RmiRegistry", NetUtil.getIPv4(), localRegistryPort);

            ArtRegistryLocator.setArtRmiRegistry(artRmiRegistry);
            ArtRegistryLocator.setLocalRmiRegistry(localRmiRegistry, localRegEntityName);

            log.debug("Registry IP: " + name.getIP());

            artRegistry = (ArtRegistry) artRmiRegistry.lookup(name.getName());
            ArtRegistryLocator.setArtRegistryProxy(artRegistry.createProxy());
            isLocalRegistry = false;
        } catch (Exception e) {
            throw new RemoteException("Cannot connect to registry", e);
        }
    }

    @Override public void stopArtRegistry() throws RemoteException {
        if (isLocalRegistry) {
            artRegistry.stopArtRegistry();
        }
    }
}
