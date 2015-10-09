/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

import java.rmi.RemoteException;

/**
 * This is the RegistryManagmentMBean interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface RegistryManagmentMBean {

    int getRegisteredObjects() throws RemoteException;

    String getStorageInfo() throws RemoteException;

    String[] getRegisteredObjectsNames() throws RemoteException;
}
