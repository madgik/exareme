/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.locking;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface AdpDBTablePartKey {
    void unlock() throws RemoteException;
}
