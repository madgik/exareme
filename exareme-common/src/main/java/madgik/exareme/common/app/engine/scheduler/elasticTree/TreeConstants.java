/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree;


import madgik.exareme.common.app.engine.scheduler.elasticTree.client.ExponentialSLA;
import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;
import madgik.exareme.utils.units.Metrics;

/**
 * @author heraldkllapi
 */
public class TreeConstants {
    public static final TreeConstants SETTINGS = new TreeConstants();

    // Generic
    public int RANDOM_SEED = 1;
    public int MAX_NUM_CONTAINERS = 50;
    public int MIN_NUM_CONTAINERS = 4;
    public int MIN_NUM_CONTAINERS_ROOT = 2;
    public int MIN_DATA_CONTAINERS = 16;
    public double TOTAL_TIME_SEC = 3.0 * Metrics.Hour;
    public double PHASE_TRANSITION_TIME = 1 * Metrics.Hour;
    public int VERBOSE_OUTPUT = 2;

    // Default operator execution time
    public double OPERATOR_DEFAULT_TIME_SEC = 10;
    public double OPERATOR_DEFAULT_DATA_MB = 0.01;

    // Generator
    public double QUERY_MEAN_ARRIVAL_TIME_SEC = 30;

    // SYSTEM - WITH STATIC TREE HEIGHT (3)
    public int[] STATIC_CONTAINERS_SMALL = new int[] {10, 4, 1};
    // + 16, 4, 1
    public int[] STATIC_CONTAINERS_MEDIUM = new int[] {26, 8, 2};
    // + 16, 4, 1
    public int[] STATIC_CONTAINERS_LARGE = new int[] {42, 12, 3};

    // SLAs
    public int CRITICAL_SLA = 0;
    public int HIGH_PRIORITY_SLA = 1;
    public int NORMAL_SLA = 2;
    public int BEST_EFFORT_SLA = 3;

    public SLA[] SLAS = new SLA[] {new ExponentialSLA(CRITICAL_SLA, 40, 20),
        new ExponentialSLA(HIGH_PRIORITY_SLA, 20, 40), new ExponentialSLA(NORMAL_SLA, 10, 80),
        new ExponentialSLA(BEST_EFFORT_SLA, 4, 1000)};

    // Topology
    public int MAX_TREE_HEIGHT = 3;

    public long ENABLE_SUPPLIER_AFTER_TIME = 600;
    // 0: run every time a dataflow finish execution
    public long RUN_SUPPLIER_EVERY = 0;

    // Windows - Used to smooth the marginal revenue
    public long HISTORICAL_WINDOW_SEC = 600;
    //  public long HISTORICAL_WINDOW_SEC = 30;
    public long PREDICTION_WINDOW_SEC = 300;
    //  public long PREDICTION_WINDOW_SEC = 15;

    public TreeConstants() {
    }
}
