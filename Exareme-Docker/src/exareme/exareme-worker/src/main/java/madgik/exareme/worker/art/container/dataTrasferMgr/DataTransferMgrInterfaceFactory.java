/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.dataTrasferMgr;

import madgik.exareme.worker.art.container.dataTrasferMgr.sync.SynchronizedDataTransferMgr;

import java.rmi.RemoteException;

/**
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class DataTransferMgrInterfaceFactory {

    public static DataTransferMgrInterface createDataTransferManagerDTPInterface(int port)
            throws RemoteException {
        return new SynchronizedDataTransferMgr(new DataTransferMgr(port));
    }


}
