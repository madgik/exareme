/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.locking;

/**
 * @author herald
 */
public class AdpDBLockingLocator {

    private static AdpDBLocking lock = null;

    private AdpDBLockingLocator() {
    }

    public static AdpDBLocking getAdpDBLocking() {
        return lock;
    }

    public static void setAdpDBLocking(AdpDBLocking lock) {
        AdpDBLockingLocator.lock = lock;
    }
}
