/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.simple;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.arm.compute.cluster.PatternElement;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerInterface;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerStatus;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author Xristos
 */
public class SimpleArmComputeInterface implements ContainerManagerInterface {

    public SimpleArmComputeInterface(EntityName regEntityName,
                                     ContainerManagerStatus managerStatus) {

    }

    @Override
    public void startManager() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stopManager() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ActiveContainer[] getContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ActiveContainer[] tryGetContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ActiveContainer[] getAtMostContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stopContainer(ActiveContainer container, ArmComputeSessionID sessionID)
            throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void closeSession(ArmComputeSessionID sessionID) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ContainerManagerStatus getStatus() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setPattern(ArrayList<PatternElement> pattern, ArmComputeSessionID sessionID)
            throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<Pair<PatternElement, ActiveContainer>> getAtMostContainers(
            ArmComputeSessionID sessionID) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
