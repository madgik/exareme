/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptorMgr;

import madgik.exareme.worker.art.container.adaptorMgr.simple.SimpleAdaptorManager;
import madgik.exareme.worker.art.container.adaptorMgr.sync.SynchronizedAdaptorManager;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class AdaptorManagerFactory {

    private AdaptorManagerFactory() {
    }

    public static AdaptorManagerInterface createAdaptorManager() throws RemoteException {
        return new SynchronizedAdaptorManager(new SimpleAdaptorManager());
    }
}
