/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.worker.art.executionPlan.parser.expression.PlanExpression;

import java.rmi.RemoteException;


/**
 * The adp db executor that schedules the jobs.
 *
 * @author Herald Kllapi <br>
 * herald@di.uoa.gr /
 * University of Athens
 * @since 1.0
 */
public interface AdpDBExecutor {

    /**
     * Execute the script.
     *
     * @param script     the script to be executed.
     * @param properties
     * @return a status that can be queried to monitor the status.
     * @throws RemoteException if something goes wrong.
     */
    AdpDBStatus executeScript(AdpDBQueryExecutionPlan script, AdpDBClientProperties properties)
            throws RemoteException;

    void registerListener(AdpDBQueryListener listener, AdpDBQueryID queryID) throws RemoteException;

    /**
     * Stop the adp db executor.
     *
     * @throws RemoteException
     */
    void stop() throws RemoteException;

    /**
     * Returns the final JSON plan.
     *
     * @param script     the script to be explained.
     * @param properties
     * @return The JSON plan.
     * @throws RemoteException if something goes wrong.
     */
    String getJSONPlan(AdpDBQueryExecutionPlan script, AdpDBClientProperties properties)
            throws RemoteException;

    PlanExpression getExecPlan(AdpDBQueryExecutionPlan script, AdpDBClientProperties properties)
            throws RemoteException;


}
