/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.manager.rmi;

import madgik.exareme.worker.arm.manager.ArmStorageManager;
import madgik.exareme.worker.arm.storage.ArmStorage;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class RmiArmStorageManager implements ArmStorageManager {

    private RmiArmManager armManager = null;

    public RmiArmStorageManager(RmiArmManager armManager) {
        this.armManager = armManager;
    }

    @Override public ArmStorage getStorage() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void startStorage() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override public void stopStorage() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override public boolean isUp() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
