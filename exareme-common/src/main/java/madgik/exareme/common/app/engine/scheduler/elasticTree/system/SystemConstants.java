/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system;


import madgik.exareme.common.optimizer.FinancialProperties;
import madgik.exareme.common.optimizer.RunTimeParameters;

/**
 * @author heraldkllapi
 */
public class SystemConstants {
    public static final SystemConstants SETTINGS = new SystemConstants();

    // Environment
    public int RANDOM_SEED = 0;

    public RunTimeParameters RUNTIME_PROPS = new RunTimeParameters();
    public FinancialProperties FIN_PROPS = new FinancialProperties();

    // This is actually (MAX - 1) because one session is used to monitoring
    public int MAX_CONCURENT_QUERIES = 6;
    public boolean FAIL_QUEUED_QUERIES = true;

    public SystemConstants() {
        //    RUNTIME_PROPS.network_speed__MB_SEC = 300;
        //    RUNTIME_PROPS.disk_throughput__MB_SEC = 400;
        //    RUNTIME_PROPS.quantum__SEC = 60;

        // Experiments @ Okeanos
        RUNTIME_PROPS.network_speed__MB_SEC = 10;
        RUNTIME_PROPS.disk_throughput__MB_SEC = 100;
        RUNTIME_PROPS.quantum__SEC = 300;
        //    RUNTIME_PROPS.quantum__SEC = 10;

        // Amazon cost (0.5 $ / VM hour)
        //    FIN_PROPS.timeQuantumCost = 0.5 * RUNTIME_PROPS.quantum__SEC / 3600.0;
        FIN_PROPS.timeQuantumCost = 5.0 * RUNTIME_PROPS.quantum__SEC / 3600.0;
        //    FIN_PROPS.timeQuantumCost = 0.5;
        // (0.1 $ / VM hour)
        //    FIN_PROPS.timeQuantumCost = 0.1;
        //    FIN_PROPS.timeQuantumCost = 0.2;
        //  Current Amazon cost: (0.03 * runTime.quantum__SEC * 12.0) / (365.0 * 24 * 3600.0)
        //    FIN_PROPS.storageCostPerMBPerQuantum =
        //        (0.03 * RUNTIME_PROPS.quantum__SEC * 12.0) / (365.0 * 24 * 3600.0);
        FIN_PROPS.storageCostPerMBPerQuantum = 0.0001;
    }
}
