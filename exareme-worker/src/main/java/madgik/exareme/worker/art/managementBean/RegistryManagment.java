/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

import madgik.exareme.worker.art.registry.ArtRegistry;

import java.rmi.RemoteException;

/**
 * @author dimitris
 */
public class RegistryManagment implements RegistryManagmentMBean {

    private ArtRegistry artRegistry;

    public RegistryManagment(ArtRegistry artRegistry) {
        this.artRegistry = artRegistry;
    }

    public int getRegisteredObjects() throws RemoteException {
        return artRegistry.getStatus().getRegisteredObjects();
    }

    public String getStorageInfo() throws RemoteException {
        return artRegistry.getStorageInfo();
    }

    public String[] getRegisteredObjectsNames() throws RemoteException {
        return artRegistry.getStoredObjectsNames();
    }

}
