/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote;

import madgik.exareme.common.art.entity.EntityName;
import org.apache.log4j.Logger;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @param <T>
 * @since 1.0
 */
public abstract class RmiObjectProxy<T> implements ObjectProxy<T> {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(RmiObjectProxy.class);
    private EntityName regEntityName = null;
    private String regEntryName = null;
    private transient T remoteObject = null;
    private transient boolean isConnected = false;

    public RmiObjectProxy(String regEntryName, EntityName regEntityName) {
        this.regEntryName = regEntryName;
        this.regEntityName = regEntityName;
    }

    @Override
    public synchronized T connect() throws RemoteException {
        int tries = 0;
        while (true) {
            try {
                log.trace("Connecting to (" + tries + ") " +
                        regEntityName.getIP() + ":" + regEntityName.getPort() + " ...");
                tries++;
                Registry registry = RmiRegistryCache.getRegistry(regEntityName);
                remoteObject = (T) registry.lookup(regEntryName);
                isConnected = true;
                return remoteObject;
            } catch (Exception e) {
                log.error("Cannot connect to " +
                        regEntityName.getIP() + ":" + regEntityName.getPort() + " ...", e);
                if (getRetryPolicy().getRetryTimesPolicy().retry(e, tries) == false) {
                    break;
                }

                try {
                    Thread.sleep(getRetryPolicy().getRetryTimeInterval().getTime(tries));
                } catch (Exception ee) {
                    throw new AccessException("Cannot connect", ee);
                }
            }
        }
        throw new RemoteException(
                "Cannot connect to " + regEntityName.getIP() + ":" + regEntityName.getPort());
    }

    @Override
    public T getRemoteObject() throws RemoteException {
        if (isConnected == false) {
            connect();
        }
        return remoteObject;
    }

    @Override
    public RetryPolicy getRetryPolicy() throws RemoteException {
        return RetryPolicyFactory.defaultRetryPolicy();
    }
}
