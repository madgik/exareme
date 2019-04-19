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
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ExecutionEngineManagementMBean {

    int getActiveExecutionPlans() throws RemoteException;

    String runPerformanceDataflow(int times, int parallelDataflows, String daxFilePath)
            throws RemoteException;

    int getTotalExecutedPlans() throws RemoteException;

    int getSuccessfullyExecutedPlans() throws RemoteException;
}
