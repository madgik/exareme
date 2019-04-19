/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry;

import madgik.exareme.worker.art.registry.resourceStorage.RegistryResourceStorageStatus;

/**
 * @author dimitris
 */
public class ArtRegistryStatus {
    private RegistryResourceStorageStatus registryResourceStorageStatus;

    public ArtRegistryStatus(RegistryResourceStorageStatus st) {
        registryResourceStorageStatus = st;
    }

    public int getRegisteredObjects() {
        return registryResourceStorageStatus.getStoredObjects();
    }
}
