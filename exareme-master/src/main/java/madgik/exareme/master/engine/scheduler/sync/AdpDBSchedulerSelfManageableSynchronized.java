/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler.sync;


import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.master.engine.scheduler.AdpDBSchedulerInfo;
import madgik.exareme.master.engine.scheduler.AdpDBSchedulerSelfManageable;
import madgik.exareme.master.engine.scheduler.QueryScriptStatus;

import java.rmi.RemoteException;
import java.util.List;

/**
 * @author herald
 */
public class AdpDBSchedulerSelfManageableSynchronized implements AdpDBSchedulerSelfManageable {

    private final AdpDBSchedulerSelfManageable scheduler;

    public AdpDBSchedulerSelfManageableSynchronized(AdpDBSchedulerSelfManageable scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public AdpDBSchedulerInfo getInfo() throws RemoteException {
        synchronized (scheduler) {
            return scheduler.getInfo();
        }
    }

    @Override
    public QueryScriptStatus schedule(String query, String database)
            throws RemoteException {
        synchronized (scheduler) {
            return scheduler.schedule(query, database);
        }
    }

    @Override
    public QueryScriptStatus schedule(QueryScript query, String database)
            throws RemoteException {
        synchronized (scheduler) {
            return scheduler.schedule(query, database);
        }
    }

    @Override
    public void stopScheduler() throws RemoteException {
        synchronized (scheduler) {
            scheduler.stopScheduler();
        }
    }

    @Override
    public List<QueryScriptStatus> getAutoScheduledQueries() throws RemoteException {
        synchronized (scheduler) {
            return scheduler.getAutoScheduledQueries();
        }
    }
}
