/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.manager;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface ArmManager {

    ArmComputeManager getComputeManager() throws RemoteException;

    ArmStorageManager getStorageManager() throws RemoteException;

    void stopManager() throws RemoteException;
}
