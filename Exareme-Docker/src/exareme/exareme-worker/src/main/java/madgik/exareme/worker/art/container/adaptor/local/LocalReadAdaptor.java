///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.db.art.container.adaptor.local;
//
//import java.rmi.RemoteException;
//import madgik.exareme.db.art.datamodel.RecordGroup;
//import madgik.exareme.db.art.container.adaptor.AdaptorMonitor;
//import madgik.exareme.db.art.container.adaptor.ReadAdaptor;
//import madgik.exareme.db.art.container.buffer.Buffer;
//import madgik.exareme.db.art.container.buffer.BufferID;
//
///**
// *
// * @author EvaS <br>
// *      University of Athens /
// *      Department of Informatics and Telecommunications.
// * @since 1.0
// */
//public class LocalReadAdaptor implements ReadAdaptor {
//
//  private Buffer buffer = null;
//
//  public LocalReadAdaptor(
//          Buffer rsb) throws RemoteException {
//    this.buffer = rsb;
//  }
//
//  public boolean hasNext() throws RemoteException {
//    return buffer.hasNext();
//  }
//
//  public RecordGroup readNext() throws RemoteException {
//    return buffer.getNext();
//  }
//
//  public AdaptorMonitor getMonitor() throws RemoteException {
//    throw new UnsupportedOperationException("Not supported yet.");
//  }
//
//  public BufferID getBufferProxy() throws RemoteException {
//    throw new UnsupportedOperationException("Not supported yet.");
//  }
//
//  public LocalReadAdaptorProxy createProxy() throws RemoteException {
//    return new LocalReadAdaptorProxy(this);
//  }
//
//  public String getRegEntryName() throws RemoteException {
//    return null;
//  }
//
//  public void close() throws RemoteException {
//    this.buffer.close();
//  }
//}
