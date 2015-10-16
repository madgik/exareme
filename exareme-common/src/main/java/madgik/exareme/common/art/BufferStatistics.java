/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.art;

import java.io.Serializable;

/**
 * @author herald
 */
public class BufferStatistics implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String bufferName;
    private long dataWrite = 0;
    private long dataRead = 0;

    public BufferStatistics(String bufferName) {
        this.bufferName = bufferName;
    }

    public void addWriteBytes(int length) {
        if (length < 0) {
            return;
        }

        synchronized (bufferName) {
            dataWrite += length;
        }
    }

    public void addReadBytes(int length) {
        if (length < 0) {
            return;
        }

        synchronized (bufferName) {
            dataRead += length;
        }
    }

    public long getDataRead() {
        synchronized (bufferName) {
            return dataRead;
        }
    }

    public long getDataWrite() {
        synchronized (bufferName) {
            return dataWrite;
        }
    }

    public String getBufferName() {
        return bufferName;
    }
}
