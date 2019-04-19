/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.locking;

import madgik.exareme.master.engine.locking.file.AdpDBFileLocking;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class AdpDBLockingFactory {

    private AdpDBLockingFactory() {
        throw new RuntimeException("Cannot create instances of this class!");
    }

    public static AdpDBLocking createFileLocking() throws RemoteException {
        return new AdpDBFileLocking();
    }
}
