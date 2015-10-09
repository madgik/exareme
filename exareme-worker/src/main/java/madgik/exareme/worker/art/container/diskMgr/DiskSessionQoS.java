/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.diskMgr;

import java.io.Serializable;

/**
 * @author herald
 */
public class DiskSessionQoS implements Serializable {
    private static final long serialVersionUID = 1L;

    private int maxNumberOfFiles = 0;
    private long totalDiskSize = 0;

    public DiskSessionQoS(int maxNumberOfFiles, long totalDiskSize) {
        this.maxNumberOfFiles = maxNumberOfFiles;
        this.totalDiskSize = totalDiskSize;
    }

    public int getMaxNumberOfFiles() {
        return maxNumberOfFiles;
    }

    public long getTotalDiskSize() {
        return totalDiskSize;
    }
}
