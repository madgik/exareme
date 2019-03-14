/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute;

import madgik.exareme.worker.art.remote.RemoteObject;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi
 * @since 1.0
 */
public interface ComputeSessionStatusManager
        extends RemoteObject<ComputeSessionStatusManagerProxy> {

    void stopManager() throws RemoteException;
}
