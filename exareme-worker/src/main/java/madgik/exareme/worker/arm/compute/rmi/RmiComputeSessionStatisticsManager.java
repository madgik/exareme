/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.worker.arm.compute.ComputeSessionStatisticsManager;
import madgik.exareme.worker.arm.compute.ComputeSessionStatisticsManagerProxy;
import madgik.exareme.worker.art.remote.RmiRemoteObject;

import java.rmi.RemoteException;
import java.util.UUID;

/**
 * @author herald
 */
public class RmiComputeSessionStatisticsManager
    extends RmiRemoteObject<ComputeSessionStatisticsManagerProxy>
    implements ComputeSessionStatisticsManager {

    private EntityName regEntityName = null;

    public RmiComputeSessionStatisticsManager(EntityName regEntityName) throws RemoteException {
        super(NetUtil.getIPv4() + "_planSessionStatisticsManager_" + UUID.randomUUID().toString());

        this.regEntityName = regEntityName;

        super.register();
    }

    public ComputeSessionStatisticsManagerProxy createProxy() throws RemoteException {
        return new RmiComputeSessionStatisticsManagerProxy(super.getRegEntryName(), regEntityName);
    }

    public void stopManager() throws RemoteException {
        super.unregister();
    }
}
