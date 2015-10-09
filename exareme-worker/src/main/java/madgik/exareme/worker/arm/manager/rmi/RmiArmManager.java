/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.manager.rmi;

import madgik.exareme.utils.managementBean.ManagementUtil;
import madgik.exareme.worker.arm.manager.ArmComputeManager;
import madgik.exareme.worker.arm.manager.ArmManager;
import madgik.exareme.worker.arm.manager.ArmStorageManager;
import madgik.exareme.worker.art.managementBean.ArmManagerManagement;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class RmiArmManager implements ArmManager {

    private static ArmComputeManager armComputeManager = null;
    private static ArmStorageManager armStorageManager = null;
    private static Logger log = Logger.getLogger(RmiArmManager.class);

    public RmiArmManager() {
        armComputeManager = new RmiArmComputeManager(this);
        //        armStorageManager = new RmiArmStorageManager(this);
        try {
            ArmManagerManagement armManager = new ArmManagerManagement(this);
            ManagementUtil.registerMBean(armManager, "ArmManager");
            log.info("Art manager bean registered!");

            armComputeManager.startCompute();
            log.info("Compute manager started!");

            //      armStorageManager.startStorage();
            //      log.info("Storage manager started!");
        } catch (Exception e) {
            log.info("Art manager registration error (already running?)!", e);
        }
    }

    @Override public ArmComputeManager getComputeManager() throws RemoteException {
        return armComputeManager;
    }

    @Override public ArmStorageManager getStorageManager() throws RemoteException {
        //    return armStorageManager;
        throw new UnsupportedOperationException("Not support yet!");
    }

    @Override public void stopManager() throws RemoteException {
        armComputeManager.stopCompute();
        //    armStorageManager.stopStorage();
    }
}
