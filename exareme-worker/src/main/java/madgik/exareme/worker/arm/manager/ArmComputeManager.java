/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.manager;

import madgik.exareme.worker.arm.compute.ArmCompute;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface ArmComputeManager {

    ArmCompute getCompute() throws RemoteException;

    void startCompute() throws RemoteException;

    void stopCompute() throws RemoteException;

    boolean isUp() throws RemoteException;
}
