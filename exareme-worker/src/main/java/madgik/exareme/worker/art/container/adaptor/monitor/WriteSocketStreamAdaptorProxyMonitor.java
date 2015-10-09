/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.monitor;

import madgik.exareme.common.art.AdaptorStatistics;
import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.worker.art.container.adaptor.WriteSocketStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.WriteSocketStreamAdaptorProxy;
import madgik.exareme.worker.art.container.netMgr.NetSession;
import madgik.exareme.worker.art.remote.RetryPolicy;

import java.io.OutputStream;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class WriteSocketStreamAdaptorProxyMonitor implements WriteSocketStreamAdaptorProxy {

    private static final long serialVersionUID = 1L;
    private WriteSocketStreamAdaptorProxy proxy = null;
    private AdaptorStatistics adaptorStatistics = null;
    private ConcreteOperatorStatistics statistics = null;
    private OutputStreamMonitor monitor = null;

    public WriteSocketStreamAdaptorProxyMonitor(WriteSocketStreamAdaptorProxy proxy,
        AdaptorStatistics adaptorStatistics, ConcreteOperatorStatistics statistics) {
        this.proxy = proxy;
        this.adaptorStatistics = adaptorStatistics;
        this.statistics = statistics;
    }

    @Override public OutputStream getOutputStream() throws RemoteException {
        if (monitor == null) {
            monitor =
                new OutputStreamMonitor(proxy.getOutputStream(), adaptorStatistics, statistics);
        }
        return monitor;
    }

    @Override public void close() throws RemoteException {
        proxy.close();
    }

    @Override public RetryPolicy getRetryPolicy() throws RemoteException {
        return proxy.getRetryPolicy();
    }

    @Override public void setNetSession(NetSession manager) throws RemoteException {
        proxy.setNetSession(manager);
    }

    @Override public WriteSocketStreamAdaptor connect() throws RemoteException {
        return proxy.connect();
    }

    @Override public WriteSocketStreamAdaptor getRemoteObject() throws RemoteException {
        return proxy.getRemoteObject();
    }
}
