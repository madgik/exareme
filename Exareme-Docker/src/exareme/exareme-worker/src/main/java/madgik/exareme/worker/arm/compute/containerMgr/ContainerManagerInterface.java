/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.containerMgr;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.arm.compute.cluster.PatternElement;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author Herald Kllapi<br> herald@di.uoa.gr<br> University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ContainerManagerInterface {

    void startManager() throws RemoteException;

    void stopManager() throws RemoteException;

    void setPattern(ArrayList<PatternElement> pattern, ArmComputeSessionID sessionID)
            throws RemoteException;

    ActiveContainer[] getContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException;

    ActiveContainer[] tryGetContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException;

    ActiveContainer[] getAtMostContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException;

    ArrayList<Pair<PatternElement, ActiveContainer>> getAtMostContainers(
            ArmComputeSessionID sessionID) throws RemoteException;

    void stopContainer(ActiveContainer container, ArmComputeSessionID sessionID)
            throws RemoteException;

    void closeSession(ArmComputeSessionID sessionID) throws RemoteException;

    ContainerManagerStatus getStatus() throws RemoteException;
}
