/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.manager;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionEngine.ExecutionEngine;

import java.rmi.RemoteException;

/**
 * This is the ExecutionEngineManager interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ExecutionEngineManager {

    ExecutionEngine getExecutionEngine();

    void startExecutionEngine() throws RemoteException;

    void stopExecutionEngine() throws RemoteException;

    void stopExecutionEngine(boolean force) throws RemoteException;

    void connectToExecutionEngine(EntityName epr) throws RemoteException;

    void connectToExecutionEngine() throws RemoteException;

    boolean isOnline();
}
