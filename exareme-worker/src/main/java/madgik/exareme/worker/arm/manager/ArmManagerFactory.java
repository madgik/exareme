/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.manager;

import madgik.exareme.worker.arm.manager.rmi.RmiArmManager;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class ArmManagerFactory {

    public static ArmManager createRmiArtManager() throws RemoteException {
        return new RmiArmManager();
    }
}
