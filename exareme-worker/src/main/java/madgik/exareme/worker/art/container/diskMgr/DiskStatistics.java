/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.diskMgr;

import java.io.Serializable;

/**
 * @author herald
 */
public class DiskStatistics implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String sessionName;
    private long bytes = 0;
    private long bytesRead = 0;
    private long bytesWrite = 0;

    public DiskStatistics(String sessionName) {
        this.sessionName = sessionName;
    }

    public void addBytes(int length) {
        if (length < 0) {
            return;
        }

        synchronized (sessionName) {
            bytes += length;
        }
    }

    public long getData() {
        synchronized (sessionName) {
            return bytes;
        }
    }

    public void addReadBytes(long bytes) {
        if (bytes < 0) {
            return;
        }

        synchronized (sessionName) {
            bytesRead += bytes;
        }
    }

    public void addWriteBytes(long bytes) {
        if (bytes < 0) {
            return;
        }

        synchronized (sessionName) {
            bytesWrite += bytes;
        }
    }

    public long getBytesRead() {
        synchronized (sessionName) {
            return bytesRead;
        }
    }

    public long getBytesWrite() {
        synchronized (sessionName) {
            return bytesWrite;
        }
    }
}
