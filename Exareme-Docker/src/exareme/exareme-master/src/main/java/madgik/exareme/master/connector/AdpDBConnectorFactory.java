/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.connector;

import madgik.exareme.master.connector.local.LocalAdpDBConnector;
import madgik.exareme.master.connector.rmi.RmiAdpDBConnector;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class AdpDBConnectorFactory {

    public static AdpDBConnector createAdpDBConnector() throws RemoteException {
        return createAdpDBRmiConnector();
    }

    private static AdpDBConnector createAdpDBLocalConnector() throws RemoteException {
        return new LocalAdpDBConnector();
    }

    private static AdpDBConnector createAdpDBRmiConnector() throws RemoteException {
        return new RmiAdpDBConnector();
    }
}
