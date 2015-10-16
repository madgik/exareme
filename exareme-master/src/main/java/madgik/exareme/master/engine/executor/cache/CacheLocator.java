/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.cache;


import madgik.exareme.utils.units.Metrics;

/**
 * @author heraldkllapi
 */
public class CacheLocator {
    // 8 GB cache
    private static final Cache cache = new Cache(10.0 * Metrics.GB / Metrics.MB);

    public static Cache getCache() {
        return cache;
    }
}
