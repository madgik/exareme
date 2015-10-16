/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler;


import madgik.exareme.common.schema.QueryScript;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface AdpDBScheduler {

    AdpDBSchedulerInfo getInfo() throws RemoteException;

    QueryScriptStatus schedule(String query, String database) throws RemoteException;

    QueryScriptStatus schedule(QueryScript queryScript, String database) throws RemoteException;

    void stopScheduler() throws RemoteException;
}
