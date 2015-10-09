/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine;

/**
 * @author herald
 */
public class AdpDBStatusManagerLocator {
    private static AdpDBStatusManager statusMgr = null;

    private AdpDBStatusManagerLocator() {
    }

    public static AdpDBStatusManager getStatusManager() {
        return AdpDBStatusManagerLocator.statusMgr;
    }

    public static void setStatusManager(AdpDBStatusManager statusMgr) {
        AdpDBStatusManagerLocator.statusMgr = statusMgr;
    }
}
