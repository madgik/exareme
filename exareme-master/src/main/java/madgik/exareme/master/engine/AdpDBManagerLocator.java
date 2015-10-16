/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine;

/**
 * @author herald
 */
public class AdpDBManagerLocator {
    private static AdpDBManager manager = null;

    private AdpDBManagerLocator() {
    }

    public static AdpDBManager getDBManager() {
        return AdpDBManagerLocator.manager;
    }

    public static void setDBManager(AdpDBManager manager) {
        AdpDBManagerLocator.manager = manager;
    }
}
