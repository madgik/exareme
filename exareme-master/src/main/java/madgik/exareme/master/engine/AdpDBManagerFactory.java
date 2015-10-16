/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine;

import madgik.exareme.master.engine.rmi.RmiAdpDBManager;
import madgik.exareme.worker.art.manager.ArtManager;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class AdpDBManagerFactory {

    private AdpDBManagerFactory() {
    }

    public static AdpDBManager createManager(String artRegistry, int artRegistryPort, int dtPort)
        throws RemoteException {
        return createRmiManager(artRegistry, artRegistryPort, dtPort);
    }

    public static AdpDBManager createRmiManager(String artRegistry, int artRegistryPort, int dtPort)
        throws RemoteException {
        return new RmiAdpDBManager(artRegistry, artRegistryPort, dtPort);
    }

    public static AdpDBManager createRmiManager(ArtManager artManager) throws RemoteException {
        return new RmiAdpDBManager(artManager);
    }
}
