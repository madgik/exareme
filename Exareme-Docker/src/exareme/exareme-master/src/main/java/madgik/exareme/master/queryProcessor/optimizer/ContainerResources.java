/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer;

import java.io.Serializable;

/**
 * @author herald
 */
public class ContainerResources implements Serializable {
    /**
     * Container memory (in MB)
     * TODO: this is a percentage! not MB
     */
    public double container_memory__MB = 1.0;
    /**
     * Container CPU (MAX CPU utilization)
     * Default: 1.0 (1 CPU)
     */
    public double container_CPU = 1.0;
    /**
     * Container disk size. LRU is used when the disk is full.
     * Default: 500 GB
     */
    public double containerDisk__MB = 1.0;

    public ContainerResources() {
    }
}
