/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.buffer.fixedSizeStreamBuffer;

import madgik.exareme.worker.art.container.buffer.StreamBuffer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author herald
 */
public class CyclicStreamByteBuffer implements StreamBuffer {
    private static final Logger log = Logger.getLogger(CyclicStreamByteBuffer.class);

    private Semaphore empty = null;
    private Semaphore full = null;
    private boolean closedReader = false;
    private boolean closedWriter = false;
    private ReentrantLock lock = null;
    private byte[] data = null;
    private int readIndex = 0;
    private int writeIndex = 0;
    private int maxSize = 0;
    private int currentSize = 0;

    public CyclicStreamByteBuffer(int size) {

        //	log.debug("SIZE=" + size);
        log.debug("DThttp34");
        this.maxSize = size;
        this.data = new byte[size];

        this.empty = new Semaphore(0);
        this.full = new Semaphore(size);
        this.lock = new ReentrantLock();
    }

    public int getSize() {
        return maxSize;
    }

    public void closeReader() throws IOException {
        lock.lock();
        try {
            if (closedReader) {
                return;
                //		throw new AccessException("Already closed reader!");
            }

            //	    log.debug("Closed Reader");
            closedReader = true;
            full.release();
        } finally {
            lock.unlock();
        }
    }

    public void closeWriter() throws IOException {
        lock.lock();
        try {
            if (closedWriter) {
                return;
                //		throw new AccessException("Already closed writer!");
            }

            //	    log.debug("Closed Writer");
            closedWriter = true;
            empty.release();
        } finally {
            lock.unlock();
        }
    }

    public void write(byte[] bytes, int offset, int length) throws IOException {
        lock.lock();
        try {
            if (closedWriter) {
                throw new IOException("Pipe is closed!");
            }
        } finally {
            lock.unlock();
        }

        int current = 0;
        while (true) {
            try {
                //		log.debug("W: Waiting");
                full.acquire();
                lock.lock();

                if (closedReader) {
                    throw new IOException("Broken pipe!");
                }

                if (closedWriter) {
                    throw new IOException("Pipe is closed!");
                }

                /*  */
                int acquired = 1;
                int remaining = length - current - acquired;
                int min = Math.min(remaining, maxSize - currentSize - 1);
                //		log.debug("W: MIN = " + min + " (" + (maxSize - currentSize - 1) + ")");
                if (full.tryAcquire(min) == false) {
                    //		    log.debug("W: ERROR tryAcquire");
                    throw new IOException("Error");
                }
                acquired += min;

                for (int i = 0; i < acquired; i++) {
                    data[writeIndex] = bytes[offset + current + i];
                    writeIndex = (writeIndex + 1) % maxSize;
                }

                currentSize += acquired;
                current += acquired;

                empty.release(acquired);

                //		log.debug("W: " + acquired + " / " +
                //			current + " / " + length + " CURRENT SIZE: " + currentSize);
                if (current == length) {
                    break;
                }
            } catch (Exception e) {
                throw new IOException("", e);
            } finally {
                lock.unlock();
            }
        }

        //	log.debug("W: END: " + current);
    }

    public int read(byte[] bytes, int offset, int length) throws IOException {

        lock.lock();
        try {
            if (closedReader) {
                throw new IOException("Pipe is closed!");
            }

            if (closedWriter && (currentSize == 0)) {
                //		log.debug("R: END 1 : -1");
                return -1;
            }
        } finally {
            lock.unlock();
        }

        int current = 0;
        while (true) {
            try {
                //		log.debug("R: Waiting");
                empty.acquire();
                lock.lock();

                if (closedReader) {
                    throw new IOException("Broken pipe!");
                }

                if (closedWriter && (currentSize == 0)) {
                    break;
                }

                /*  */
                int acquired = 1;
                int remaining = length - current - acquired;
                int min = Math.min(remaining, currentSize - 1);
                //		log.debug("R: MIN = " + min + " (" + (currentSize - 1) + ")");
                if (empty.tryAcquire(min) == false) {
                    //		    log.debug("R: ERROR tryAcquire");
                    throw new IOException("Error");
                }

                acquired += min;

                for (int i = 0; i < acquired; i++) {
                    bytes[offset + current + i] = data[readIndex];
                    readIndex = (readIndex + 1) % maxSize;
                }

                current += acquired;
                currentSize -= acquired;

                full.release(acquired);

                //		log.debug("R: " + acquired + " / " +
                //			current + " / " + length + " CURRENT SIZE: " + currentSize);
                if (current == length) {
                    break;
                }

            } catch (Exception e) {
                throw new IOException("", e);
            } finally {
                lock.unlock();
            }
        }

        //	log.debug("R: END 2 : " + current);
        if (current == 0) {
            return -1;
        }

        return current;
    }

    @Override
    public void clear() {
        data = null;
    }
}
