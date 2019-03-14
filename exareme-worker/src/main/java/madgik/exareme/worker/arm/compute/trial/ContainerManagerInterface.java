/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.arm.compute.trial;

import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerStatus;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;

import java.rmi.RemoteException;


/**
 * @author Χρήστος
 */
public interface ContainerManagerInterface {
    void startManager() throws RemoteException;

    void stopManager() throws RemoteException;

    ActiveContainer[] getContainers(int number_of_containers, ArmComputeSessionID sessionID)
            throws RemoteException;

    ActiveContainer[] getContainers(int number_of_containers, ArmComputeSessionID sessionID,
                                    long duration_time) throws RemoteException;

    ActiveContainer[] getAtMostContainers(int number_of_containers, ArmComputeSessionID sessionID)
            throws RemoteException;

    ActiveContainer[] tryGetContainers(int number_of_containers, ArmComputeSessionID sessionID)
            throws RemoteException;

    void releaseContainers(ActiveContainer[] containers, ArmComputeSessionID sessionID)
            throws RemoteException;

    void closeSession(ArmComputeSessionID sessionID) throws RemoteException;

    ContainerManagerStatus getStatus() throws RemoteException;

    void stopContainer(ActiveContainer container, ArmComputeSessionID sessionID)
            throws RemoteException;

}
