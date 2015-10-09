/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry;

import madgik.exareme.common.art.entity.EntityName;
import org.apache.log4j.Logger;

import java.rmi.registry.Registry;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class ArtRegistryLocator {
    private static final Logger log = Logger.getLogger(ArtRegistryLocator.class);

    private static ArtRegistryProxy artRegistryProxy = null;
    private static Registry artRmiRegistry = null;
    private static Registry localRmiRegistry = null;
    private static EntityName localRmiRegistryEntityName = null;

    public static void setLocalRmiRegistry(Registry rmiRegistry,
        EntityName localRmiRegistryEntityName) {
        log.debug("Local RMI Registry : " + localRmiRegistryEntityName.getName() + " / "
            + localRmiRegistryEntityName.getIP() + "/ " + localRmiRegistryEntityName.getPort());

        ArtRegistryLocator.localRmiRegistry = rmiRegistry;
        ArtRegistryLocator.localRmiRegistryEntityName = localRmiRegistryEntityName;
    }

    public static ArtRegistryProxy getArtRegistryProxy() {
        return artRegistryProxy;
    }

    public static void setArtRegistryProxy(ArtRegistryProxy artRegistryProxy) {
        ArtRegistryLocator.artRegistryProxy = artRegistryProxy;
    }

    public static Registry getArtRmiRegistry() {
        return artRmiRegistry;
    }

    public static void setArtRmiRegistry(Registry rmiRegistry) {
        ArtRegistryLocator.artRmiRegistry = rmiRegistry;
    }

    public static Registry getLocalRmiRegistry() {
        return localRmiRegistry;
    }

    public static EntityName getLocalRmiRegistryEntityName() {
        return localRmiRegistryEntityName;
    }
}
