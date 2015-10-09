/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system;


import madgik.exareme.utils.units.Metrics;

/**
 * @author heraldkllapi
 */
public class GlobalTime {

    private static final double beginningOfTime = getCurrentSec();

    public static double getCurrentSec() {
        return (System.currentTimeMillis() / Metrics.MiliSec) - beginningOfTime;
    }
}
