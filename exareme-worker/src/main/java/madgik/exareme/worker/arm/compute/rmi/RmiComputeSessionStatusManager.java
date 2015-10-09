/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.worker.arm.compute.ComputeSessionStatusManager;
import madgik.exareme.worker.arm.compute.ComputeSessionStatusManagerProxy;
import madgik.exareme.worker.art.remote.RmiRemoteObject;

import java.rmi.RemoteException;
import java.util.UUID;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiComputeSessionStatusManager
    extends RmiRemoteObject<ComputeSessionStatusManagerProxy>
    implements ComputeSessionStatusManager {

    private EntityName regEntityName = null;

    public RmiComputeSessionStatusManager(EntityName regEntityName) throws RemoteException {
        super(NetUtil.getIPv4() + "_planSessionManager_" + UUID.randomUUID().toString());

        this.regEntityName = regEntityName;

        super.register();
    }

    public ComputeSessionStatusManagerProxy createProxy() throws RemoteException {
        return new RmiComputeSessionStatusManagerProxy(super.getRegEntryName(), regEntityName);
    }

    public void stopManager() throws RemoteException {
        super.unregister();
    }
}
