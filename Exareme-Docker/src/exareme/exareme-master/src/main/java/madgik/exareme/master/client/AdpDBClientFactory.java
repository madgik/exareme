/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.client;

import madgik.exareme.master.client.rmi.RmiAdpDBClient;
import madgik.exareme.master.engine.AdpDBManager;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class AdpDBClientFactory {

    public static AdpDBClient createDBClient(AdpDBManager manager, AdpDBClientProperties properties)
            throws RemoteException {
        return new RmiAdpDBClient(manager, properties);
    }

}
