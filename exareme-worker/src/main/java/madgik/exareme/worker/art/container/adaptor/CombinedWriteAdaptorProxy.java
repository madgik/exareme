/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class CombinedWriteAdaptorProxy implements Serializable {

    private static Logger log = Logger.getLogger(CombinedWriteAdaptorProxy.class);
    public WriteRmiStreamAdaptorProxy writeRmiStreamAdaptorProxy = null;
    public WriteSocketStreamAdaptorProxy writeSocketStreamAdaptorProxy = null;

    public CombinedWriteAdaptorProxy(WriteRmiStreamAdaptorProxy writeRmiStreamAdaptorProxy,
                                     WriteSocketStreamAdaptorProxy writeSocketStreamAdaptorProxy) {
        this.writeRmiStreamAdaptorProxy = writeRmiStreamAdaptorProxy;
        this.writeSocketStreamAdaptorProxy = writeSocketStreamAdaptorProxy;
    }

    public void close() throws RemoteException {
        log.debug("Closing rmi ...");
        writeRmiStreamAdaptorProxy.close();
        if (AdaptorConstants.adaptorImpl == AdaptorImplType.socket) {
            log.debug("Closing socket ...");
            writeSocketStreamAdaptorProxy.close();
        }
    }
}
