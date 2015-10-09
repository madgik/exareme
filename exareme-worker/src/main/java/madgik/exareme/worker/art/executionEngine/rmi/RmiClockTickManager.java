/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.worker.art.container.ContainerID;
import madgik.exareme.worker.art.executionEngine.clockTickManager.ClockTickManager;
import madgik.exareme.worker.art.executionEngine.clockTickManager.ClockTickManagerInterface;
import madgik.exareme.worker.art.executionEngine.clockTickManager.ClockTickManagerProxy;
import madgik.exareme.worker.art.registry.ArtRegistryProxy;
import madgik.exareme.worker.art.remote.RmiRemoteObject;

import java.rmi.RemoteException;
import java.util.UUID;

/**
 * @author heraldkllapi
 */
public class RmiClockTickManager extends RmiRemoteObject<ClockTickManagerProxy>
    implements ClockTickManager {
    private ClockTickManagerInterface clockTickManagerInterface = null;
    private EntityName regEntityName = null;
    private ArtRegistryProxy registryProxy = null;

    public RmiClockTickManager(ClockTickManagerInterface clockTickManagerInterface,
        EntityName regEntityName, ArtRegistryProxy registryProxy) throws RemoteException {
        super(NetUtil.getIPv4() + "_clockTickManager_" + UUID.randomUUID().toString());

        this.clockTickManagerInterface = clockTickManagerInterface;
        this.regEntityName = regEntityName;
        this.registryProxy = registryProxy;
        super.register();
    }

    @Override public ClockTickManagerProxy createProxy() throws RemoteException {
        return new RmiClockTickManagerProxy(super.getRegEntryName(), regEntityName);
    }

    @Override
    public void containerWarningClockTick(ContainerID id, long timeToTick_ms, long quantumCount)
        throws RemoteException {
        clockTickManagerInterface.containerWarningClockTick(id, timeToTick_ms, quantumCount);
    }

    @Override public void containerClockTick(ContainerID id, long quantumCount)
        throws RemoteException {
        clockTickManagerInterface.containerClockTick(id, quantumCount);
    }

    @Override public void globalWarningClockTick(long timeToTick_ms, long quantumCount)
        throws RemoteException {
        clockTickManagerInterface.globalWarningClockTick(timeToTick_ms, quantumCount);
    }

    @Override public void globalClockTick(long quantumCount) throws RemoteException {
        clockTickManagerInterface.globalClockTick(quantumCount);
    }
}
