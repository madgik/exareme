/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.arm.compute.cluster.PatternElement;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import madgik.exareme.worker.art.remote.RemoteObject;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author Herald Kllapi <br> University of Athens / Department of Informatics
 * and Telecommunications.
 * @since 1.0
 */
public interface ComputeSessionContainerManager
        extends RemoteObject<ComputeSessionContainerManagerProxy> {
    ActiveContainer[] getContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException;

    ActiveContainer[] tryGetContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException;

    ActiveContainer[] getAtMostContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException;

    void stopContainer(ActiveContainer container, ArmComputeSessionID sessionID)
            throws RemoteException;

    void closeSession(ArmComputeSessionID sessionID) throws RemoteException;

    void stopManager() throws RemoteException;

    ArrayList<Pair<PatternElement, ActiveContainer>> getAtMostContainers(
            ArmComputeSessionID sessionID) throws RemoteException;

    void setPattern(ArrayList<PatternElement> pattern, ArmComputeSessionID sessionID)
            throws RemoteException;
}
