/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

import java.rmi.RemoteException;

/**
 * This is the ExecutionEngineManagementMBean interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ArmComputeManagementMBean {

    int getActiveContainers() throws RemoteException;

    int getMaxContainers() throws RemoteException;
}
