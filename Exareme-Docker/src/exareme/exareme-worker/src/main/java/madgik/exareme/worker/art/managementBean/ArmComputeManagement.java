/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

import madgik.exareme.worker.arm.compute.ArmCompute;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ArmComputeManagement implements ArmComputeManagementMBean {

    private ArmCompute compute = null;

    public ArmComputeManagement(ArmCompute compute) {
        this.compute = compute;
    }

    public int getActiveContainers() throws RemoteException {
        return 0;
    }

    public int getMaxContainers() throws RemoteException {
        return 0;
    }
}
