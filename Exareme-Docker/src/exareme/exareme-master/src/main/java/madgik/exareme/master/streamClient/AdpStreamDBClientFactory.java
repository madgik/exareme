/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.streamClient;

import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.streamClient.rmi.RmiOptiqueStreamAdpDBClient;

import java.rmi.RemoteException;

/**
 * @author Christoforos Svingos
 */
public class AdpStreamDBClientFactory {

    public static AdpStreamDBClient createOptiqueStreamDBClient(AdpDBManager manager,
                                                                AdpDBClientProperties properties) throws RemoteException {
        return new RmiOptiqueStreamAdpDBClient(manager, properties);
    }

}
