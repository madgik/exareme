/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.monitor;

import madgik.exareme.common.art.AdaptorStatistics;
import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.worker.art.container.adaptor.WriteRmiStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.WriteRmiStreamAdaptorProxy;
import madgik.exareme.worker.art.remote.RetryPolicy;

import java.io.OutputStream;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class WriteRmiStreamAdaptorProxyMonitor implements WriteRmiStreamAdaptorProxy {

    private static final long serialVersionUID = 1L;
    private WriteRmiStreamAdaptorProxy proxy = null;
    private AdaptorStatistics adaptorStatistics = null;
    private ConcreteOperatorStatistics statistics = null;
    private OutputStreamMonitor monitor = null;

    public WriteRmiStreamAdaptorProxyMonitor(WriteRmiStreamAdaptorProxy proxy,
                                             AdaptorStatistics adaptorStatistics, ConcreteOperatorStatistics statistics) {
        this.proxy = proxy;
        this.adaptorStatistics = adaptorStatistics;
        this.statistics = statistics;
    }

    @Override
    public OutputStream getOutputStream() throws RemoteException {
        if (monitor == null) {
            monitor =
                    new OutputStreamMonitor(proxy.getOutputStream(), adaptorStatistics, statistics);
        }
        return monitor;
    }

    @Override
    public void close() throws RemoteException {
        proxy.close();
    }

    @Override
    public WriteRmiStreamAdaptor connect() throws RemoteException {
        return proxy.connect();
    }

    @Override
    public WriteRmiStreamAdaptor getRemoteObject() throws RemoteException {
        return proxy.getRemoteObject();
    }

    @Override
    public RetryPolicy getRetryPolicy() throws RemoteException {
        return proxy.getRetryPolicy();
    }
}
