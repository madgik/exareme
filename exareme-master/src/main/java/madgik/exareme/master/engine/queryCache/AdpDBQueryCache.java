/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.queryCache;

import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.master.registry.Registry;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public interface AdpDBQueryCache {

    void addPlan(QueryScript script, Registry registry, AdpDBQueryExecutionPlan plan)
            throws RemoteException;

    AdpDBQueryExecutionPlan getPlan(QueryScript script, Registry registry) throws RemoteException;
}
