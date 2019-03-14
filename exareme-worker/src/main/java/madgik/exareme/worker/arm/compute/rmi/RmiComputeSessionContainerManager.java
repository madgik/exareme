/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.worker.arm.compute.ComputeSessionContainerManager;
import madgik.exareme.worker.arm.compute.ComputeSessionContainerManagerProxy;
import madgik.exareme.worker.arm.compute.cluster.PatternElement;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerInterface;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import madgik.exareme.worker.art.registry.ArtRegistryProxy;
import madgik.exareme.worker.art.remote.RmiRemoteObject;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiComputeSessionContainerManager
        extends RmiRemoteObject<ComputeSessionContainerManagerProxy>
        implements ComputeSessionContainerManager {

    private ContainerManagerInterface containerManagerInterface = null;
    private EntityName regEntityName = null;
    private ArtRegistryProxy registryProxy = null;

    public RmiComputeSessionContainerManager(ContainerManagerInterface containerManagerInterface,
                                             EntityName regEntityName, ArtRegistryProxy registryProxy) throws RemoteException {
        super(NetUtil.getIPv4() + "_planSessionManager_" + UUID.randomUUID().toString());

        this.containerManagerInterface = containerManagerInterface;
        this.regEntityName = regEntityName;
        this.registryProxy = registryProxy;

        super.register();
    }

    @Override
    public ComputeSessionContainerManagerProxy createProxy() throws RemoteException {
        return new RmiComputeSessionContainerManagerProxy(super.getRegEntryName(), regEntityName);
    }

    @Override
    public ActiveContainer[] getContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException {
        return containerManagerInterface.getContainers(numOfContainers, sessionID);
    }

    @Override
    public ActiveContainer[] tryGetContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException {
        return containerManagerInterface.tryGetContainers(numOfContainers, sessionID);
    }

    @Override
    public ActiveContainer[] getAtMostContainers(int numOfContainers, ArmComputeSessionID sessionID)
            throws RemoteException {
        return containerManagerInterface.getAtMostContainers(numOfContainers, sessionID);
    }

    @Override
    public void stopContainer(ActiveContainer container, ArmComputeSessionID sessionID)
            throws RemoteException {
        containerManagerInterface.stopContainer(container, sessionID);
    }

    @Override
    public void closeSession(ArmComputeSessionID sessionID) throws RemoteException {
        containerManagerInterface.closeSession(sessionID);
    }

    @Override
    public void stopManager() throws RemoteException {
        super.unregister();
    }

    @Override
    public ArrayList<Pair<PatternElement, ActiveContainer>> getAtMostContainers(
            ArmComputeSessionID sessionID) throws RemoteException {
        return containerManagerInterface.getAtMostContainers(sessionID);
    }

    @Override
    public void setPattern(ArrayList<PatternElement> pattern, ArmComputeSessionID sessionID)
            throws RemoteException {
        containerManagerInterface.setPattern(pattern, sessionID);
    }
}
