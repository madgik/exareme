package madgik.exareme.worker.art.container.buffer.sync;

import madgik.exareme.worker.art.container.buffer.StreamBuffer;

import java.io.IOException;

/**
 * @author herald
 */
public class SynchronizedBuffer implements StreamBuffer {

    private final Object readLock = new Object();
    private final Object writeLock = new Object();
    private StreamBuffer buffer = null;

    public SynchronizedBuffer(StreamBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        synchronized (writeLock) {
            buffer.write(bytes, offset, length);
        }
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        synchronized (readLock) {
            return buffer.read(bytes, offset, length);
        }
    }

    @Override
    public void closeReader() throws IOException {
        synchronized (readLock) {
            buffer.closeReader();
        }
    }

    @Override
    public void closeWriter() throws IOException {
        synchronized (writeLock) {
            buffer.closeWriter();
        }
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
