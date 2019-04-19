/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler;

import java.rmi.RemoteException;
import java.util.List;

/**
 * @author herald
 */
public interface AdpDBSchedulerSelfManageable extends AdpDBScheduler {

    List<QueryScriptStatus> getAutoScheduledQueries() throws RemoteException;
}
