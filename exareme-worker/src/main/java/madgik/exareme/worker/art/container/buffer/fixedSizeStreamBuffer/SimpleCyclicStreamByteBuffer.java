package madgik.exareme.worker.art.container.buffer.fixedSizeStreamBuffer;

import madgik.exareme.worker.art.container.buffer.StreamBuffer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 * @author herald
 */
public class SimpleCyclicStreamByteBuffer implements StreamBuffer {

    private static final Logger log = Logger.getLogger(SimpleCyclicStreamByteBuffer.class);
    private int size = 0;
    private boolean readerClosed = false;
    private boolean writerClosed = false;
    private byte[] data = null;
    private int readIndex = 0;
    private int writeIndex = 0;
    private Phase phase = Phase.write;
    private Semaphore full = new Semaphore(1);
    private Semaphore empty = new Semaphore(0);

    //  private static Logger log = Logger.getLogger(SimpleCyclicStreamByteBuffer.class);
    public SimpleCyclicStreamByteBuffer(int size) {
        //    log.setLevel(Level.OFF);
        this.size = size;
        this.data = new byte[size];


    }

    @Override
    public void clear() {
    }

    public void write(byte[] bytes, int offset, int length) throws IOException {
        try {
            //      log.debug("Trying to acquire write ... " + Thread.currentThread().getId());
            full.acquire();
            //      log.debug("Got write! "  + Thread.currentThread().getId());
        } catch (InterruptedException ex) {
            throw new IOException("full.acquire()", ex);
        }

        if (readerClosed) {
            throw new IOException("Broken pipe (reader closed).");
        }

        int toWrite = Math.min(length, data.length - writeIndex);
        System.arraycopy(bytes, offset, data, writeIndex, toWrite);
        writeIndex += toWrite;

        //    log.debug("Wrote " + toWrite + " / " + length + " - " +  + Thread.currentThread().getId());
        //    log.debug("W: " + (writeIndex - toWrite) + " -> " + writeIndex + " - " + Thread.currentThread().getId());
        if (writeIndex < data.length) {
            //      log.debug("Writing again! " + Thread.currentThread().getId());
            full.release();
        } else {
            //      log.debug("Release read! " + Thread.currentThread().getId());
            readIndex = 0;
            phase = Phase.read;
            empty.release();
        }

        // Recursive call with the rest.
        if (toWrite < length) {
            write(bytes, offset + toWrite, length - toWrite);
        }
    }

    public int read(byte[] bytes, int offset, int length) throws IOException {
        try {
            //      log.debug("Trying to acquire read ... " + Thread.currentThread().getId());
            empty.acquire();
            //      log.debug("Got read! "  + Thread.currentThread().getId());
        } catch (InterruptedException ex) {
            throw new IOException("empty.acquire()", ex);
        }

        if ((writeIndex == 0) && writerClosed) {
            //      log.debug("No more data ... " + Thread.currentThread().getId());
            return -1;
        }

        int toRead = Math.min(length, writeIndex - readIndex);
        System.arraycopy(data, readIndex, bytes, offset, toRead);
        readIndex += toRead;

        //    log.debug("R: " + (readIndex - toRead) + " -> " + readIndex + " - " + Thread.currentThread().getId());
        if (readIndex < writeIndex) {
            //      log.debug("Reading again! " + Thread.currentThread().getId());
            empty.release();
        } else {
            //      log.debug("Release write! " + Thread.currentThread().getId());
            writeIndex = 0;
            phase = Phase.write;
            full.release();
        }

        //    log.debug("Read " + toRead + "/" + length + " - " + Thread.currentThread().getId());
        return toRead;
    }

    public void closeReader() throws IOException {

        if (readerClosed) {
            //      throw new IOException("Already closed!");
            return;
        }

        //    log.debug("Close reader - release write! " + Thread.currentThread().getId());
        readerClosed = true;
        full.release();
        //    this.data = null;
    }

    public void closeWriter() throws IOException {
        if (writerClosed) {
            //      throw new IOException("Already closed!");
            return;
        }

        writerClosed = true;

        // If the buffer is not full, the empty semaphore has to be released twice.
        if (phase == Phase.write) {
            readIndex = 0;

            //      log.debug("Close writer - release read 2 times! " + Thread.currentThread().getId());
            empty.release();
        }

        //    log.debug("Close writer - release read! " + Thread.currentThread().getId());
        empty.release();
    }

    public int getSize() throws IOException {
        return size;
    }

    enum Phase {

        read,
        write
    }
}
