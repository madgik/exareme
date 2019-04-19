/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface AdpDBManager {

    AdpDBOptimizer getAdpDBOptimizer() throws RemoteException;

    AdpDBExecutor getAdpDBExecutor() throws RemoteException;

    AdpDBStatusManager getStatusManager() throws RemoteException;

    void stopManager() throws RemoteException;

}
