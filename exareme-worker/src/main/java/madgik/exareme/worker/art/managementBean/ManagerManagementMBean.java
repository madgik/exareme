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
public interface ManagerManagementMBean {

    boolean isRegistryOnline() throws RemoteException;

    String startRegistry() throws RemoteException;

    String stopRegistry() throws RemoteException;

    String connectToRegistry(String ip, int port) throws RemoteException;

    boolean isExecutionEngineOnline() throws RemoteException;

    String startExecutionEngine() throws RemoteException;

    String stopExecutionEngine() throws RemoteException;

    String connectToExecutionEngine() throws RemoteException;

    String getExecutionEngineRegEntryName() throws RemoteException;

    boolean isContainerUp() throws RemoteException;

    String startContainer() throws RemoteException;

    String stopContainer() throws RemoteException;

    String getContainerRegEntryName() throws RemoteException;
}
