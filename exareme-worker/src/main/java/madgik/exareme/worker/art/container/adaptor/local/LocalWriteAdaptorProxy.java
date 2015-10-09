///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.db.art.container.adaptor.local;
//
//import java.rmi.RemoteException;
//import madgik.exareme.db.art.datamodel.RecordGroup;
//import madgik.exareme.db.art.remote.RetryPolicy;
//import madgik.exareme.db.art.container.adaptor.WriteAdaptor;
//import madgik.exareme.db.art.container.adaptor.WriteAdaptorProxy;
//
///**
// *
// * @author EvaS <br>
// *      University of Athens /
// *      Department of Informatics and Telecommunications.
// * @since 1.0
// */
//public class LocalWriteAdaptorProxy implements WriteAdaptorProxy {
//
//    private LocalWriteAdaptor adaptor = null;
//
//    public LocalWriteAdaptorProxy(
//            LocalWriteAdaptor adaptor) {
//        this.adaptor = adaptor;
//    }
//
//    public void write(RecordGroup record) throws RemoteException {
//        adaptor.write(record);
//    }
//
//    public void close() throws RemoteException {
//        adaptor.close();
//    }
//
//    public WriteAdaptor connect() throws RemoteException {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public WriteAdaptor getRemoteObject() throws RemoteException {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public RetryPolicy getRetryPolicy() throws RemoteException {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//}
