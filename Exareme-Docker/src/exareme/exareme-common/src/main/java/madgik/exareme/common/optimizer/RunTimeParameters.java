/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.optimizer;

import java.io.Serializable;

/**
 * @author Herald Kllapi
 * @since 1.0
 */
public class RunTimeParameters implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Quantum size (in seconds)
     * Default: 1 hour
     */
    public double quantum__SEC = 3600.0;

    /**
     * Network speed (in MB/sec)
     * Default: 100 MB/sec (~1GBit)
     */
    public double network_speed__MB_SEC = 100.0;

    /**
     * Disk throughput in (MB/sec)
     * Default: 200 MB/sec (SSD)
     */
    public double disk_throughput__MB_SEC = 200.0;

    /**
     * CPU utilization of the data transfer operator
     * Default: 0.1 (10%)
     */
    public double data_transfer_CPU = 0.1;

    /**
     * Memory needs of the data transfer operator
     * Default: 1%
     * TODO: Change this to integer in the range [0, 100]
     */
    public double data_transfer_memory__MB = 0.01;

    public RunTimeParameters() {
    }

    public RunTimeParameters(RunTimeParameters params) {
        this.quantum__SEC = params.quantum__SEC;
        this.network_speed__MB_SEC = params.network_speed__MB_SEC;
        this.disk_throughput__MB_SEC = params.disk_throughput__MB_SEC;
        this.data_transfer_CPU = params.data_transfer_CPU;
        this.data_transfer_memory__MB = params.data_transfer_memory__MB;
    }
}
