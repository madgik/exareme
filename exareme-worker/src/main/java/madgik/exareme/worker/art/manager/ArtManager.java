/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.manager;

import java.rmi.RemoteException;

/**
 * This is the ArtManager interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ArtManager {

    ArtRegistryManager getRegistryManager() throws RemoteException;

    ExecutionEngineManager getExecutionEngineManager() throws RemoteException;

    ContainerManager getContainerManager() throws RemoteException;

    void startGlobalQuantumClock() throws RemoteException;

    void stopManager() throws RemoteException;

    void stopManager(boolean force) throws RemoteException;
}
