/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute;

import madgik.exareme.worker.art.remote.RemoteObject;

import java.rmi.RemoteException;


/**
 * @author herald
 */
public interface ComputeSessionStatisticsManager
    extends RemoteObject<ComputeSessionStatisticsManagerProxy> {

    void stopManager() throws RemoteException;
}
