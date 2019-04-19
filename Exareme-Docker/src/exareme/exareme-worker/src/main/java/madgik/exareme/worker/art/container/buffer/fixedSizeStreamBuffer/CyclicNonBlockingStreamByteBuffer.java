///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.db.art.container.buffer.fixedSizeStreamBuffer;
//
//import java.io.IOException;
//import java.util.concurrent.Semaphore;
//import madgik.exareme.db.art.container.buffer.StreamBuffer;
//import madgik.exareme.db.art.container.diskMgr.DMContainerSession;
//import org.apache.log4j.Logger;
//
///**
// *
// * @author herald
// */
//public class CyclicNonBlockingStreamByteBuffer implements StreamBuffer {
//
//  private static final Logger log = Logger.getLogger(CyclicNonBlockingStreamByteBuffer.class);
//
//  private Semaphore empty = null;
//  private Semaphore full = null;
//  private boolean closedReader = false;
//  private boolean closedWriter = false;
//  private byte[] data = null;
//  private int readIndex = 0;
//  private int writeIndex = 0;
//  private int maxSize = 0;
//  private int currentSize = 0;
//  private final Integer lock = new Integer(1);
//
//  public int semaphoreCalls = 0;
//
//  public CyclicNonBlockingStreamByteBuffer(int size) {
//
////	log.debug("SIZE=" + size);
//    log.debug("DThttp13 create cyclic bugf Reader");
//    this.maxSize = size;
//    this.data = new byte[size];
//
//    this.empty = new Semaphore(0);
//    this.full = new Semaphore(size);
//  }
//
//  public int getSize() {
//    return maxSize;
//  }
//
//  public void closeReader() throws IOException {
//    if (closedReader) {
//      return;
////		throw new AccessException("Already closed reader!");
//    }
//
//    log.debug("DThttp13 Closed Reader");
//
//    closedReader = true;
//    semaphoreCalls++;
//    full.release();
//    this.data = null;
//  }
//
//  public void closeWriter() throws IOException {
//    if (closedWriter) {
//      return;
////		throw new AccessException("Already closed writer!");
//    }
//
//    log.debug("DThttp13 Closed Reader");
//
//    closedWriter = true;
//    semaphoreCalls++;
//    empty.release();
//  }
//
//  public void write(byte[] bytes, int offset, int length) throws IOException {
//
//    if (closedWriter) {
//      throw new IOException("Pipe is closed!");
//    }
//
//    int current = 0;
//    while (true) {
//      try {
////		log.debug("W: Waiting");
//        semaphoreCalls++;
//        full.acquire();
//
//        if (closedReader) {
//          throw new IOException("Broken pipe!");
//        }
//
//        if (closedWriter) {
//          throw new IOException("Pipe is closed!");
//        }
//
//        /*  */
//        int acquired = 1;
//
//        synchronized (lock) {
//          int remaining = length - current - acquired;
//          int min = Math.min(remaining, maxSize - currentSize - 1);
////          System.err.println("W: MIN = " + min + " (" + (maxSize - currentSize - 1) + ")");
//          semaphoreCalls++;
//          if (full.tryAcquire(min) == false) {
//            //		    log.debug("W: ERROR tryAcquire");
//            throw new IOException("Error");
//          }
//          acquired += min;
//        }
//
//        for (int i = 0; i < acquired; i++) {
//          data[writeIndex] = bytes[offset + current + i];
//          writeIndex = (writeIndex + 1) % maxSize;
//        }
//
//        current += acquired;
//
//        synchronized (lock) {
//          currentSize += acquired;
//          semaphoreCalls++;
//          empty.release(acquired);
//        }
//
////		log.debug("W: " + acquired + " / " + 
////			current + " / " + length + " CURRENT SIZE: " + currentSize);
//        if (current == length) {
//          break;
//        }
//      } catch (Exception e) {
//        throw new IOException("", e);
//      }
//
////	log.debug("W: END: " + current);
//    }
//  }
//
//  public int read(byte[] bytes, int offset, int length) throws IOException {
//
//    if (closedReader) {
//      throw new IOException("Pipe is closed!");
//    }
//
//    if (closedWriter && (currentSize == 0)) {
////		log.debug("R: END 1 : -1");
//      return -1;
//    }
//
//    int current = 0;
//    while (true) {
//      try {
////		log.debug("R: Waiting");
//        semaphoreCalls++;
//        empty.acquire();
//
//        if (closedReader) {
//          throw new IOException("Broken pipe!");
//        }
//
//        if (closedWriter && (currentSize == 0)) {
//          break;
//        }
//
//        /*  */
//        int acquired = 1;
//
//        synchronized (lock) {
//          int remaining = length - current - acquired;
//          int min = Math.min(remaining, currentSize - 1);
//          //		log.debug("R: MIN = " + min + " (" + (currentSize - 1) + ")");
//          semaphoreCalls++;
//          if (empty.tryAcquire(min) == false) {
//            //		    log.debug("R: ERROR tryAcquire");
//            throw new IOException("Error");
//          }
//
//          acquired += min;
//        }
//
//        for (int i = 0; i < acquired; i++) {
//          bytes[offset + current + i] = data[readIndex];
//          readIndex = (readIndex + 1) % maxSize;
//        }
//
//        current += acquired;
//
//        synchronized (lock) {
//          currentSize -= acquired;
//          semaphoreCalls++;
//          full.release(acquired);
//        }
//
////		log.debug("R: " + acquired + " / " + 
////			current + " / " + length + " CURRENT SIZE: " + currentSize);
//        if (current == length) {
//          break;
//        }
//
//      } catch (Exception e) {
//        throw new IOException("", e);
//      }
//    }
//
////	log.debug("R: END 2 : " + current);
//    if (current == 0) {
//      return -1;
//    }
//
//    return current;
//  }
//}
