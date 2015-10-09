///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.db.art.container.adaptor.local;
//
//import java.rmi.RemoteException;
//import madgik.exareme.db.art.datamodel.RecordGroup;
//import madgik.exareme.db.art.container.adaptor.AdaptorMonitor;
//import madgik.exareme.db.art.container.adaptor.WriteAdaptor;
//import madgik.exareme.db.art.container.adaptor.WriteAdaptorProxy;
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
//public class LocalWriteAdaptor  implements WriteAdaptor {
//
//    private Buffer buffer = null;
//
//    public LocalWriteAdaptor(
//            Buffer rsb) throws RemoteException {
//        this.buffer = rsb;
//    }
//
//    public void write(RecordGroup record) throws RemoteException {
//        buffer.append(record);
//    }
//
//    public void close() throws RemoteException {
//        buffer.close();
//    }
//
//    public AdaptorMonitor getMonitor() throws RemoteException {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public BufferID getBufferProxy() throws RemoteException {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public WriteAdaptorProxy createProxy() throws RemoteException {
//        return new  LocalWriteAdaptorProxy(this);
//    }
//
//    public String getRegEntryName() throws RemoteException {
//	return null;
//    }
//}
