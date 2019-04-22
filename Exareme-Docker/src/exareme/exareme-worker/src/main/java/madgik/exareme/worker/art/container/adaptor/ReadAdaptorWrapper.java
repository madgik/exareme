/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

import java.io.InputStream;
import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class ReadAdaptorWrapper {

    private CombinedReadAdaptorProxy in = null;

    public ReadAdaptorWrapper(CombinedReadAdaptorProxy in) {
        this.in = in;
    }

    public InputStream getInputStream() throws RemoteException {
        if (AdaptorConstants.adaptorImpl == AdaptorImplType.socket) {
            return in.readSocketStreamAdaptorProxy.getInputStream();
        }
        return in.readRmiStreamAdaptorProxy.getInputStream();
    }

    public void close() throws RemoteException {
        if (AdaptorConstants.adaptorImpl == AdaptorImplType.socket) {
            in.readSocketStreamAdaptorProxy.close();
        }
        in.readRmiStreamAdaptorProxy.close();
    }
}
