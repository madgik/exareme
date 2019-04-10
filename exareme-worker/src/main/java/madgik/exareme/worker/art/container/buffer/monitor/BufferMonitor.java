/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.buffer.monitor;

import madgik.exareme.common.art.BufferStatistics;
import madgik.exareme.worker.art.container.buffer.StreamBuffer;

import java.io.IOException;

/**
 * @author herald
 */
public class BufferMonitor implements StreamBuffer {

    private BufferStatistics stats = null;
    private StreamBuffer buffer = null;

    public BufferMonitor(StreamBuffer buffer, BufferStatistics stats) {
        this.stats = stats;
        this.buffer = buffer;
    }

    public StreamBuffer getBuffer() {
        return buffer;
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        buffer.write(bytes, offset, length);
        stats.addWriteBytes(length);
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        int len = buffer.read(bytes, offset, length);
        stats.addReadBytes(len);
        return len;
    }

    @Override
    public void closeReader() throws IOException {
        buffer.closeReader();
    }

    @Override
    public void closeWriter() throws IOException {
        buffer.closeWriter();
    }

    @Override
    public int getSize() throws IOException {
        return buffer.getSize();
    }

    @Override
    public void clear() {
        buffer.clear();
    }
}
