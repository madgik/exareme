/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

//package madgik.exareme.db.art.managementBean;

import madgik.exareme.worker.arm.manager.ArmComputeManager;
import madgik.exareme.worker.arm.manager.ArmManager;
import madgik.exareme.worker.arm.manager.ArmStorageManager;

import java.rmi.RemoteException;


/**
 * @author Herald Kllapi <br> University of Athens / Department of Informatics
 *         and Telecommunications.
 * @since 1.0
 */
public class ArmManagerManagement
    implements madgik.exareme.worker.art.managementBean.ArmManagerManagementMBean {
    private ArmManager manager = null;
    private ArmComputeManager armCompute = null;
    private ArmStorageManager armStorage = null;

    public ArmManagerManagement(ArmManager manager) throws RemoteException {
        this.manager = manager;
        this.armCompute = manager.getComputeManager();
        //    this.armStorage = manager.getStorageManager();
    }

    @Override public boolean isComputeOnline() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public String startCompute() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public String connectToCompute() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public boolean isStorageOnline() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public String startStorage() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public String connectToStorage() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
