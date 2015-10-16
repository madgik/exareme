/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

import org.apache.log4j.Logger;

import java.io.OutputStream;
import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class WriteAdaptorWrapper {

    private static Logger log = Logger.getLogger(WriteAdaptorWrapper.class);
    private CombinedWriteAdaptorProxy out = null;

    public WriteAdaptorWrapper(CombinedWriteAdaptorProxy out) {
        this.out = out;
    }

    public OutputStream getOutputStream() throws RemoteException {
        if (AdaptorConstants.adaptorImpl == AdaptorImplType.socket) {
            return out.writeSocketStreamAdaptorProxy.getOutputStream();
        }
        return out.writeRmiStreamAdaptorProxy.getOutputStream();
    }

    public void close() throws RemoteException {
        if (AdaptorConstants.adaptorImpl == AdaptorImplType.socket) {
            out.writeSocketStreamAdaptorProxy.close();
        }
        out.writeRmiStreamAdaptorProxy.close();
    }
}
