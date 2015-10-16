/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.art;

import java.io.Serializable;

/**
 * @author herald
 */
public class AdaptorStatistics implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String name;
    private final String from;
    private final String to;
    private long bytes = 0;
    private double bytesPerSecond = 0.0;
    private boolean remote = false;

    public AdaptorStatistics(String name, String from, String to) {
        this.name = name;
        this.from = from;
        this.to = to;
    }

    public void setRemote() {
        synchronized (from) {
            remote = true;
        }
    }

    public void setLocal() {
        synchronized (from) {
            remote = false;
        }
    }

    public boolean isRemote() {
        synchronized (from) {
            return remote;
        }
    }

    public void addBytes(int length) {
        if (length <= 0) {
            return;
        }

        synchronized (from) {
            bytes += length;
        }
    }

    public long getBytes() {
        synchronized (from) {
            return bytes;
        }
    }

    public double getBytesPerSecond() {
        synchronized (from) {
            return bytesPerSecond;
        }
    }

    public String getName() {
        return name;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
