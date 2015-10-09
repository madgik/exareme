package madgik.exareme.worker.art.container.buffer.safe;

import madgik.exareme.worker.art.container.buffer.StreamBuffer;

import java.io.IOException;

/**
 * @author herald
 */
public class ThreadSafeBuffer implements StreamBuffer {

    private StreamBuffer buffer = null;

    private long readerThreadID = -1;
    private String readerThreadName = null;
    private long writerThreadID = -1;
    private String writerThreadName = null;

    public ThreadSafeBuffer(StreamBuffer buffer) {
        this.buffer = buffer;
    }

    @Override public void write(byte[] bytes, int offset, int length) throws IOException {
        if (writerThreadID < 0) {
            writerThreadID = Thread.currentThread().getId();
            writerThreadName = Thread.currentThread().getName();
        }

        if (writerThreadID != Thread.currentThread().getId()) {
            throw new IOException(
                "Pipe violation: " + writerThreadID + " != " + Thread.currentThread().getId() + ""
                    + " / " + writerThreadName + " != " + Thread.currentThread().getName());
        }

        buffer.write(bytes, offset, length);
    }

    @Override public int read(byte[] bytes, int offset, int length) throws IOException {
        if (readerThreadID < 0) {
            readerThreadID = Thread.currentThread().getId();
            readerThreadName = Thread.currentThread().getName();
        }

        if (readerThreadID != Thread.currentThread().getId()) {
            throw new IOException(
                "Pipe violation: " + readerThreadID + " != " + Thread.currentThread().getId() + ""
                    + " / " + readerThreadName + " != " + Thread.currentThread().getName());
        }

        return buffer.read(bytes, offset, length);
    }

    @Override public void closeReader() throws IOException {
        if (readerThreadID < 0) {
            readerThreadID = Thread.currentThread().getId();
            readerThreadName = Thread.currentThread().getName();
        }

        if (readerThreadID != Thread.currentThread().getId()) {
            throw new IOException(
                "Pipe violation: " + readerThreadID + " != " + Thread.currentThread().getId() + ""
                    + " / " + readerThreadName + " != " + Thread.currentThread().getName());
        }

        buffer.closeReader();
    }

    @Override public void closeWriter() throws IOException {
        if (writerThreadID < 0) {
            writerThreadID = Thread.currentThread().getId();
            writerThreadName = Thread.currentThread().getName();
        }

        if (writerThreadID != Thread.currentThread().getId()) {
            throw new IOException(
                "Pipe violation: " + writerThreadID + " != " + Thread.currentThread().getId() + ""
                    + " / " + writerThreadName + " != " + Thread.currentThread().getName());
        }

        buffer.closeWriter();
    }

    @Override public int getSize() throws IOException {
        return buffer.getSize();
    }

    @Override public void clear() {
        buffer.clear();
    }
}
