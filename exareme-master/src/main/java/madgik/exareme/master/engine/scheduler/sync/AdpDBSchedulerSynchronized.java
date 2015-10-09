/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler.sync;


import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.master.engine.scheduler.AdpDBScheduler;
import madgik.exareme.master.engine.scheduler.AdpDBSchedulerInfo;
import madgik.exareme.master.engine.scheduler.QueryScriptStatus;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class AdpDBSchedulerSynchronized implements AdpDBScheduler {

    private final AdpDBScheduler scheduler;

    public AdpDBSchedulerSynchronized(AdpDBScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public AdpDBSchedulerInfo getInfo() throws RemoteException {
        synchronized (scheduler) {
            return scheduler.getInfo();
        }
    }

    public QueryScriptStatus schedule(String query, String database) throws RemoteException {
        synchronized (scheduler) {
            return scheduler.schedule(query, database);
        }
    }

    public QueryScriptStatus schedule(QueryScript query, String database) throws RemoteException {
        synchronized (scheduler) {
            return scheduler.schedule(query, database);
        }
    }

    public void stopScheduler() throws RemoteException {
        synchronized (scheduler) {
            scheduler.stopScheduler();
        }
    }
}
