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
public class CombinedReadAdaptorProxy implements Serializable {

    private static Logger log = Logger.getLogger(CombinedReadAdaptorProxy.class);
    public ReadRmiStreamAdaptorProxy readRmiStreamAdaptorProxy = null;
    public ReadSocketStreamAdaptorProxy readSocketStreamAdaptorProxy = null;

    public CombinedReadAdaptorProxy(ReadRmiStreamAdaptorProxy readStreamAdaptorProxy,
                                    ReadSocketStreamAdaptorProxy readStreamAdaptorProxy2) {
        this.readRmiStreamAdaptorProxy = readStreamAdaptorProxy;
        this.readSocketStreamAdaptorProxy = readStreamAdaptorProxy2;
    }

    public void close() throws RemoteException {
        log.debug("Closing rmi ...");
        readRmiStreamAdaptorProxy.close();
        if (AdaptorConstants.adaptorImpl == AdaptorImplType.socket) {
            log.debug("Closing socket ...");
            readSocketStreamAdaptorProxy.close();
        }
    }
}
