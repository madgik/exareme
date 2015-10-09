/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote;

import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @param <T>
 * @since 1.0
 */
public abstract class RmiRemoteObject<T> implements RemoteObject<T> {

    private static Logger log = Logger.getLogger(RmiRemoteObject.class);
    private boolean isRegistered = false;
    private String regEntryName = null;

    public RmiRemoteObject(String regEntryName) throws RemoteException {
        this.regEntryName = regEntryName;
    }

    @Override public String getRegEntryName() {
        return regEntryName;
    }

    protected final void register() throws RemoteException {
        if (isRegistered == false) {
            try {
                RmiRegistryHelper.bind(this);
            } catch (Exception e) {
                throw new RemoteException("Cannot register object", e);
            }

            log.trace(
                "Registered in " + ArtRegistryLocator.getLocalRmiRegistryEntityName().getIP() + ":"
                    + ArtRegistryLocator.getLocalRmiRegistryEntityName().getPort() + " / "
                    + ArtRegistryLocator.getLocalRmiRegistryEntityName().getDataTransferPort()
                    + " / " + regEntryName);
            isRegistered = true;
        }
    }

    public final void unregister() throws RemoteException {
        if (isRegistered == true) {
            try {
                RmiRegistryHelper.unbind(this);
            } catch (Exception e) {
                throw new RemoteException("Cannot unregister object", e);
            }

            log.trace(
                "Unregistered from " + ArtRegistryLocator.getLocalRmiRegistryEntityName().getIP()
                    + ":" + ArtRegistryLocator.getLocalRmiRegistryEntityName().getPort() + " / "
                    + regEntryName);
            isRegistered = true;
        }
    }
}
