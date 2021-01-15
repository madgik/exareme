/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.managementBean.ManagementUtil;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.arm.compute.*;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerInterface;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import madgik.exareme.worker.art.managementBean.ArmComputeManagement;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import madgik.exareme.worker.art.registry.ArtRegistryProxy;
import madgik.exareme.worker.art.registry.updateDeamon.RegistryUpdateDeamon;
import madgik.exareme.worker.art.registry.updateDeamon.RegistryUpdateDeamonFactory;
import madgik.exareme.worker.art.remote.RmiRemoteObject;

import java.rmi.RemoteException;
import java.util.UUID;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiArmCompute extends RmiRemoteObject<ArmComputeProxy> implements ArmCompute {

    private final Object lock = new Object();
    private ArtRegistryProxy artRegistryProxy = null;
    private ArmComputeStatus computeStatus = null;
    private ContainerManagerInterface containerManagerInterface = null;
    private RmiComputeSessionContainerManager computeSessionContainerManager = null;
    private RmiComputeSessionReportManager computeSessionReportManager = null;
    private RmiComputeSessionStatisticsManager computeSessionStatisticsManager = null;
    private RmiComputeSessionStatusManager computeSessionStatusManager = null;
    private EntityName regEntityName = null;
    private RegistryUpdateDeamon registryUpdateDeamon = null;
    private long sessionCount = 0;

    public RmiArmCompute(ContainerManagerInterface containerManagerInterface,
                         EntityName regEntityName) throws Exception {
        super(NetUtil.getIPv4() + "_arm_compute_" + UUID.randomUUID().toString());

        this.regEntityName = regEntityName;
        this.artRegistryProxy = ArtRegistryLocator.getArtRegistryProxy();
        this.computeStatus = new ArmComputeStatus();
        this.containerManagerInterface = containerManagerInterface;

        this.computeSessionContainerManager =
                new RmiComputeSessionContainerManager(containerManagerInterface, regEntityName,
                        artRegistryProxy);

        this.computeSessionReportManager = new RmiComputeSessionReportManager(regEntityName);
        this.computeSessionStatisticsManager =
                new RmiComputeSessionStatisticsManager(regEntityName);
        this.computeSessionStatusManager = new RmiComputeSessionStatusManager(regEntityName);

        ArmComputeManagement engineManager = new ArmComputeManagement(this);
        ManagementUtil.registerMBean(engineManager, "ArmCompute");

        long lifeTime =
                AdpProperties.getArmProps().getLong("arm.compute.rmi.RmiArmCompute.lifetime");
        registryUpdateDeamon =
                RegistryUpdateDeamonFactory.createDeamon(this.createProxy(), (long) (0.75 * lifeTime));
        if(lifeTime != 0) {
            registryUpdateDeamon.startDeamon();
        }
    }

    @Override
    public ArmComputeProxy createProxy() throws RemoteException {
        super.register();
        return new RmiArmComputeProxy(super.getRegEntryName(), regEntityName);
    }

    @Override
    public ArmComputeSessionID createNewSession() throws RemoteException {
        synchronized (lock) {
            return new ArmComputeSessionID(sessionCount++);
        }
    }

    @Override
    public ComputeSessionContainerManagerProxy getComputeSessionContainerManagerProxy(
            ArmComputeSessionID sessionID) throws RemoteException {
        RmiComputeSessionContainerManagerProxy proxy =
                (RmiComputeSessionContainerManagerProxy) computeSessionContainerManager.createProxy();
        proxy.sessionID = sessionID;
        return proxy;
    }

    @Override
    public ComputeSessionReportManagerProxy getComputeSessionReportManagerProxy(
            ArmComputeSessionID sessionID) throws RemoteException {
        RmiComputeSessionReportManagerProxy proxy =
                (RmiComputeSessionReportManagerProxy) computeSessionReportManager.createProxy();
        proxy.sessionID = sessionID;
        return proxy;
    }

    @Override
    public ComputeSessionStatisticsManagerProxy getComputeSessionStatisticsManagerProxy(
            ArmComputeSessionID sessionID) throws RemoteException {
        RmiComputeSessionStatisticsManagerProxy proxy =
                (RmiComputeSessionStatisticsManagerProxy) computeSessionStatisticsManager.createProxy();
        proxy.sessionID = sessionID;
        return proxy;
    }

    @Override
    public ComputeSessionStatusManagerProxy getComputeSessionStatusManagerProxy(
            ArmComputeSessionID sessionID) throws RemoteException {
        RmiComputeSessionStatusManagerProxy proxy =
                (RmiComputeSessionStatusManagerProxy) computeSessionStatusManager.createProxy();
        proxy.sessionID = sessionID;
        return proxy;
    }

    @Override
    public synchronized void closeSession(ArmComputeSessionID sessionID)
            throws RemoteException {
        containerManagerInterface.closeSession(sessionID);
        // TODO(herald): fix the following
        //    computeSessionContainerManager.closeSession(sessionID);
    }

    public ArmComputeStatus getStatus() throws RemoteException {
        return computeStatus;
    }

    @Override
    public void stopArmCompute() throws RemoteException {
        registryUpdateDeamon.stopDeamon();
        containerManagerInterface.stopManager();

        super.unregister();
    }
}
