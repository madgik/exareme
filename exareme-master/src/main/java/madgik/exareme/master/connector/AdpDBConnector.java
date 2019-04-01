/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.connector;

import madgik.exareme.master.client.AdpDBClientProperties;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * @author heraldkllapi
 */
public interface AdpDBConnector {

    // Returns a stream of json records.
    InputStream readTable(String tableName, Map<String, Object> alsoIncludeProps,
                          AdpDBClientProperties props) throws RemoteException;
}
