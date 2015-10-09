/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.manager.rmi;

import madgik.exareme.worker.arm.compute.ArmCompute;
import madgik.exareme.worker.arm.compute.ArmComputeFactory;
import madgik.exareme.worker.arm.compute.ArmComputeLocator;
import madgik.exareme.worker.arm.compute.ArmComputeProxy;
import madgik.exareme.worker.arm.manager.ArmComputeManager;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;

import javax.activity.ActivityRequiredException;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class RmiArmComputeManager implements ArmComputeManager {
    private RmiArmManager armManager = null;
    private ArmCompute compute = null;

    public RmiArmComputeManager(RmiArmManager armManager) {
        this.armManager = armManager;
    }

    @Override public ArmCompute getCompute() throws RemoteException {
        return compute;
    }

    @Override public void startCompute() throws RemoteException {
        try {
            compute = ArmComputeFactory
                .createRMICluster(ArtRegistryLocator.getLocalRmiRegistryEntityName());
            ArmComputeProxy proxy = compute.createProxy();
            ArtRegistryLocator.getArtRegistryProxy().registerComputeMediator(proxy);
            ArmComputeLocator.setArmCompute(proxy);
        } catch (Exception e) {
            throw new ActivityRequiredException("Cannot start compute mediator!", e);
        }
    }

    @Override public void stopCompute() throws RemoteException {
        compute.stopArmCompute();
    }

    @Override public boolean isUp() throws RemoteException {
        return compute != null;
    }
}
