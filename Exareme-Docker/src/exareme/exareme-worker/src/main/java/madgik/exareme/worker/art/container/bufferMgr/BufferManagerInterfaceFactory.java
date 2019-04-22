/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.bufferMgr;

import madgik.exareme.worker.art.container.bufferMgr.simple.SimpleBufferManager;
import madgik.exareme.worker.art.container.bufferMgr.sync.SynchronizedBufferManager;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class BufferManagerInterfaceFactory {

    private BufferManagerInterfaceFactory() {
    }

    public static BufferManagerInterface createBufferManagerInterface(BufferManagerStatus status,
                                                                      StatisticsManagerInterface statistics) throws RemoteException {
        return new SynchronizedBufferManager(new SimpleBufferManager(status, statistics));
    }
}
