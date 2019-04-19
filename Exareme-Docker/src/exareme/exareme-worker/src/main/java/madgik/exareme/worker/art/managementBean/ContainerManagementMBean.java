/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

import java.rmi.RemoteException;

/**
 * This is the ContainerManagementMBean interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ContainerManagementMBean {

    long getActiveConcreteOperators() throws RemoteException;

    long getActiveBuffers() throws RemoteException;

    long getActiveAdaptors() throws RemoteException;

    long getActiveSessions() throws RemoteException;

    long getPipePoolSize() throws RemoteException;

    long getPipePoolSessions() throws RemoteException;
}
