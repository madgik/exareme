/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

import java.rmi.RemoteException;

/**
 * The bean that manages the ART properties.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface PropertiesManagementMBean {

    boolean getCachingStatus() throws RemoteException;

    int getRegisterCacheSize() throws RemoteException;

    int getMessageEventDumpPeriod() throws RemoteException;

    long getContainerLifetime() throws RemoteException;

    long getRmiExecutionEngineLifetime() throws RemoteException;
}
