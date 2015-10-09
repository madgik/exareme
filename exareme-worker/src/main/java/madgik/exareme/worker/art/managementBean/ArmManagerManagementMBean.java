/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

import java.rmi.RemoteException;

/**
 * This is the ManagerManagementMBean interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ArmManagerManagementMBean {

    boolean isComputeOnline() throws RemoteException;

    String startCompute() throws RemoteException;

    String connectToCompute() throws RemoteException;

    boolean isStorageOnline() throws RemoteException;

    String startStorage() throws RemoteException;

    String connectToStorage() throws RemoteException;
}
