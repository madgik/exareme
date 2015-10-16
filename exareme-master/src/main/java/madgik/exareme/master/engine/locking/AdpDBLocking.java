/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.locking;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface AdpDBLocking {

    AdpDBTablePartKey getSharedKey(String database, String tableName, int part)
        throws RemoteException;

    AdpDBTablePartKey getExclusiveKey(String database, String tableName, int part)
        throws RemoteException;
}
