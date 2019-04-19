/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.manager;

import madgik.exareme.worker.arm.storage.ArmStorage;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface ArmStorageManager {
    ArmStorage getStorage() throws RemoteException;

    void startStorage() throws RemoteException;

    void stopStorage() throws RemoteException;

    boolean isUp() throws RemoteException;
}
