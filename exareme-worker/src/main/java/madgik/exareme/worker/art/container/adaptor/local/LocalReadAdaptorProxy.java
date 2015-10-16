///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.db.art.container.adaptor.local;
//
//import java.rmi.RemoteException;
//import madgik.exareme.db.art.datamodel.RecordGroup;
//import madgik.exareme.db.art.remote.RetryPolicy;
//import madgik.exareme.db.art.container.adaptor.ReadAdaptor;
//import madgik.exareme.db.art.container.adaptor.ReadAdaptorProxy;
//
///**
// *
// * @author EvaS <br>
// *      University of Athens /
// *      Department of Informatics and Telecommunications.
// * @since 1.0
// */
//public class LocalReadAdaptorProxy implements ReadAdaptorProxy {
//
//  private static final long serialVersionUID = 1L;
//  private LocalReadAdaptor adaptor = null;
//
//  public LocalReadAdaptorProxy(
//          LocalReadAdaptor adaptor) {
//    this.adaptor = adaptor;
//  }
//
//  public boolean hasNext() throws RemoteException {
//    return adaptor.hasNext();
//  }
//
//  public RecordGroup readNext() throws RemoteException {
//    return adaptor.readNext();
//  }
//
//  public ReadAdaptor connect() throws RemoteException {
//    throw new UnsupportedOperationException("Not supported.");
//  }
//
//  public ReadAdaptor getRemoteObject() throws RemoteException {
//    throw new UnsupportedOperationException("Not supported.");
//  }
//
//  public RetryPolicy getRetryPolicy() throws RemoteException {
//    throw new UnsupportedOperationException("Not supported.");
//  }
//
//  public void close() throws RemoteException {
//    this.adaptor.close();
//  }
//}
