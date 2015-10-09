package madgik.exareme.worker.art.container.buffer.fixedSizeStreamBuffer;

import madgik.exareme.worker.art.container.buffer.StreamBuffer;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Semaphore;

/**
 * @author herald
 */
public class PipeStreamByteBuffer implements StreamBuffer {

    private int size = 0;
    private PipedInputStream inPipe = null;
    private PipedOutputStream outPipe = null;
    private Semaphore waitForWriter = null;
    private boolean readerClosed = false;
    private boolean writerClosed = false;
    //  private static Logger log = Logger.getLogger(PipeStreamByteBuffer.class);

    public PipeStreamByteBuffer(int size) throws IOException {
        inPipe = new PipedInputStream(size);
        outPipe = new PipedOutputStream(inPipe);
        waitForWriter = new Semaphore(0);

        //    log.debug("Pipe created!");
    }

    public void write(byte[] bytes, int offset, int length) throws IOException {
        outPipe.write(bytes, offset, length);
        //    log.debug("Wrote " + length);
    }

    public int read(byte[] bytes, int offset, int length) throws IOException {
        int actual = inPipe.read(bytes, offset, length);
        //    log.debug("Read " + actual + "/" + length);
        return actual;
    }

    public void closeReader() throws IOException {
        if (readerClosed) {
            //      throw new IOException("Already closed!");
            return;
        }

        //    if (!writerClosed) {
        //      throw new IOException("Writer not closed");
        //    }

        try {
            //      log.debug("Waiting for writer to close ... " + this.toString() + " (" +
            //              Thread.currentThread().getId() + ")");
            waitForWriter.acquire();
        } catch (InterruptedException e) {
            throw new IOException("Cannot wait for writer to close", e);
        }
        //    log.debug("writer closed!");

        inPipe.close();
        readerClosed = true;
    }

    public void closeWriter() throws IOException {

        if (writerClosed) {
            //      throw new IOException("Already closed!");
            return;
        }

        outPipe.close();
        writerClosed = true;
        waitForWriter.release();

        //    log.debug("writer closed!");
    }

    public int getSize() throws IOException {
        return size;
    }

    @Override public void clear() {

    }
}
