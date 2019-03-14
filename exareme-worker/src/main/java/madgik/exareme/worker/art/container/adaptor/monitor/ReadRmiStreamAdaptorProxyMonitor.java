/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.monitor;

import madgik.exareme.common.art.AdaptorStatistics;
import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.worker.art.container.adaptor.ReadRmiStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.ReadRmiStreamAdaptorProxy;
import madgik.exareme.worker.art.remote.RetryPolicy;

import java.io.InputStream;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class ReadRmiStreamAdaptorProxyMonitor implements ReadRmiStreamAdaptorProxy {

    private static final long serialVersionUID = 1L;
    private ReadRmiStreamAdaptorProxy proxy = null;
    private AdaptorStatistics adaptorStatistics = null;
    private ConcreteOperatorStatistics statistics = null;
    private InputStreamMonitor streamMonitor = null;

    public ReadRmiStreamAdaptorProxyMonitor(ReadRmiStreamAdaptorProxy proxy,
                                            AdaptorStatistics adaptorStatistics, ConcreteOperatorStatistics statistics) {
        this.proxy = proxy;
        this.adaptorStatistics = adaptorStatistics;
        this.statistics = statistics;
    }

    @Override
    public InputStream getInputStream() throws RemoteException {
        if (streamMonitor == null) {
            streamMonitor =
                    new InputStreamMonitor(proxy.getInputStream(), adaptorStatistics, statistics);
        }
        return streamMonitor;
    }

    @Override
    public void close() throws RemoteException {
        proxy.close();
    }

    @Override
    public ReadRmiStreamAdaptor connect() throws RemoteException {
        return proxy.connect();
    }

    @Override
    public ReadRmiStreamAdaptor getRemoteObject() throws RemoteException {
        return proxy.getRemoteObject();
    }

    @Override
    public RetryPolicy getRetryPolicy() throws RemoteException {
        return proxy.getRetryPolicy();
    }
}
