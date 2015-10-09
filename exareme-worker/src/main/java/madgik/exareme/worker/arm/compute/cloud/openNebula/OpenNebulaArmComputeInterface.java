/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.cloud.openNebula;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.arm.compute.cluster.PatternElement;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerInterface;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerStatus;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import madgik.exareme.worker.art.manager.ArtManagerFactory;
import madgik.exareme.worker.art.manager.ContainerManager;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;

/**
 * @author herald
 */
public class OpenNebulaArmComputeInterface implements ContainerManagerInterface {

    private ContainerManagerStatus managerStatus = null;
    private ContainerManager manager = null;
    private boolean created = false;

    public OpenNebulaArmComputeInterface(ContainerManagerStatus managerStatus)
        throws RemoteException {
        this.managerStatus = managerStatus;

        try {
            this.manager = ArtManagerFactory.createRmiArtManager().getContainerManager();
        } catch (Exception e) {
            throw new ServerException("Cannot create local compute interface", e);
        }
    }

    @Override
    public ActiveContainer[] getContainers(int numOfContainers, ArmComputeSessionID sessionID)
        throws RemoteException {

        if (created == false) {
            manager.startContainer();

            ActiveContainer activeContainer =
                new ActiveContainer(0, manager.getContainer().createProxy().getEntityName(), 0);

            created = true;

            return new ActiveContainer[] {activeContainer};
        } else {
            throw new AccessException("Cannot create more than one container!");
        }
    }

    @Override public void stopContainer(ActiveContainer container, ArmComputeSessionID sessionID)
        throws RemoteException {
        manager.stopContainer();
    }

    @Override public ContainerManagerStatus getStatus() throws RemoteException {
        return managerStatus;
    }

    @Override public void startManager() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void stopManager() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void closeSession(ArmComputeSessionID sessionID) throws RemoteException {
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
    public void setPattern(ArrayList<PatternElement> pattern, ArmComputeSessionID sessionID)
        throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public ArrayList<Pair<PatternElement, ActiveContainer>> getAtMostContainers(
        ArmComputeSessionID sessionID) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
