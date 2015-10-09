/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.arm.compute.cluster.ClusterArmComputeInterface;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerInterface;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerStatus;
import madgik.exareme.worker.arm.compute.local.LocalArmComputeInterface;
import madgik.exareme.worker.arm.compute.rmi.RmiArmCompute;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.simple.ContainerManager;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;

/**
 * @author herald
 */
public class ArmComputeFactory {
    public static ArmCompute createRMILocal(EntityName regEntityName) throws RemoteException {
        try {
            ContainerManagerStatus managerStatus = new ContainerManagerStatus();

            ContainerManagerInterface computeInterface =
                new LocalArmComputeInterface(managerStatus);

            RmiArmCompute compute = new RmiArmCompute(computeInterface, regEntityName);
            computeInterface.startManager();
            return compute;
        } catch (Exception e) {
            throw new ServerException("Cannot create compute mediator!", e);
        }
    }

    public static ArmCompute createRMISimple(EntityName regEntityName) throws RemoteException {
        try {
            ContainerManagerInterface computeInterface = new ContainerManager(20, 1, false);
            RmiArmCompute compute = new RmiArmCompute(computeInterface, regEntityName);
            computeInterface.startManager();
            return compute;
        } catch (Exception e) {
            throw new ServerException("Cannot create compute mediator!", e);
        }
    }

    public static ArmCompute createRMICluster(EntityName regEntityName) throws RemoteException {
        try {
            ContainerProxy[] containers = ArtRegistryLocator.getArtRegistryProxy().getContainers();

            ArrayList<ActiveContainer> contNames = new ArrayList<ActiveContainer>();
            for (int cId = 0; cId < containers.length; ++cId) {
                contNames.add(new ActiveContainer(cId, containers[cId].getEntityName(), cId));
            }

            ContainerManagerInterface computeInterface =
                new ClusterArmComputeInterface(2, contNames);

            RmiArmCompute compute = new RmiArmCompute(computeInterface, regEntityName);
            computeInterface.startManager();
            return compute;
        } catch (Exception e) {
            throw new ServerException("Cannot create cluster compute mediator!", e);
        }
    }

    public static ArmCompute createRMICloud(EntityName regEntityName) throws RemoteException {
        try {
            ContainerManagerInterface computeInterface = null;
            //          new CloudArmComputeInterface(2, 10, null);
            RmiArmCompute compute = new RmiArmCompute(computeInterface, regEntityName);
            computeInterface.startManager();
            return compute;
        } catch (Exception e) {
            throw new ServerException("Cannot create cluster compute mediator!", e);
        }
    }
}
