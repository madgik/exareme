/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute;

import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import madgik.exareme.worker.art.remote.RemoteObject;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 * herald@di.uoa.gr / University of Athens
 * @since 1.0
 */
public interface ArmCompute extends RemoteObject<ArmComputeProxy> {
    ArmComputeSessionID createNewSession() throws RemoteException;

    ComputeSessionContainerManagerProxy getComputeSessionContainerManagerProxy(
            ArmComputeSessionID sessionID) throws RemoteException;

    ComputeSessionReportManagerProxy getComputeSessionReportManagerProxy(
            ArmComputeSessionID sessionID) throws RemoteException;

    ComputeSessionStatisticsManagerProxy getComputeSessionStatisticsManagerProxy(
            ArmComputeSessionID sessionID) throws RemoteException;

    ComputeSessionStatusManagerProxy getComputeSessionStatusManagerProxy(
            ArmComputeSessionID sessionID) throws RemoteException;

    void closeSession(ArmComputeSessionID sessionID) throws RemoteException;

    void stopArmCompute() throws RemoteException;
}
