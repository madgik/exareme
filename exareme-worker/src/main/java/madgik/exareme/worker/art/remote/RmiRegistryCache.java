/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiRegistryCache {

    private static Map<EntityName, Registry> registryMap =
            Collections.synchronizedMap(new HashMap<EntityName, Registry>());

    private RmiRegistryCache() {
    }

    public synchronized static Registry getRegistry(EntityName entityName) throws RemoteException {
        Registry reg = registryMap.get(entityName);
        if (reg == null) {
            if (ArtRegistryLocator.getLocalRmiRegistryEntityName().equals(entityName)) {
                reg = ArtRegistryLocator.getLocalRmiRegistry();
            } else {
                reg = LocateRegistry.getRegistry(entityName.getIP(), entityName.getPort());
            }
            registryMap.put(entityName, reg);
        }
        return reg;
    }
}
