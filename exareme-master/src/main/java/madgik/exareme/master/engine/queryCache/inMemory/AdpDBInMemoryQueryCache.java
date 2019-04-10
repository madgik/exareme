/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.queryCache.inMemory;

import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.master.engine.queryCache.AdpDBQueryCache;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.utils.serialization.SerializationUtil;

import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * @author heraldkllapi
 */
public class AdpDBInMemoryQueryCache implements AdpDBQueryCache {
    private HashMap<String, AdpDBQueryExecutionPlan> cache = null;

    public AdpDBInMemoryQueryCache() {
        cache = new HashMap<String, AdpDBQueryExecutionPlan>();
    }

    @Override
    public void addPlan(QueryScript script, Registry registry, AdpDBQueryExecutionPlan plan)
            throws RemoteException {
        //    cache.put(script.toString(), SerializationUtil.deepCopy(plan));
    }

    @Override
    public AdpDBQueryExecutionPlan getPlan(QueryScript script, Registry registry)
            throws RemoteException {
        if (true) {
            return null;
        }
        AdpDBQueryExecutionPlan plan = cache.get(script.toString());
        return (plan == null) ? null : SerializationUtil.deepCopy(plan);
    }
}
