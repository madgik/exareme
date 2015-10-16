/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.common.schema.Statistics;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.historicalData.AdpDBHistoricalQueryData;
import madgik.exareme.master.registry.Registry;

import java.rmi.RemoteException;

/**
 * The AdpDB optimizer computes the optimal execution plan of a query script.
 *
 * @author herald
 */
public interface AdpDBOptimizer {

    /* Optimize the script given the schema using the client properties. */
    AdpDBQueryExecutionPlan optimize(QueryScript script, Registry registry, Statistics stats,
        AdpDBHistoricalQueryData queryData, AdpDBQueryID queryID, AdpDBClientProperties props,
        boolean schedule, boolean validate) throws RemoteException;
}
