/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.locking.file;

import madgik.exareme.master.engine.locking.AdpDBTablePartKey;

import java.nio.channels.FileLock;
import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * @author herald
 */
public class AdpDBTablePartFileLock implements AdpDBTablePartKey {

    private FileLock lock = null;

    public AdpDBTablePartFileLock(FileLock lock) {
        this.lock = lock;
    }

    public void unlock() throws RemoteException {
        try {
            lock.release();
        } catch (Exception e) {
            throw new ServerException("Cannot release lock", e);
        }
    }
}
