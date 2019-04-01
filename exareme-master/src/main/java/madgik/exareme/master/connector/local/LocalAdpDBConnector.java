/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.connector.local;

import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.connector.AdpDBConnector;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author heraldkllapi
 */
public class LocalAdpDBConnector implements AdpDBConnector {
    // TODO(herald): this 100 looks like a magik number :-)
    private static ExecutorService pool = Executors.newFixedThreadPool(100);

    public LocalAdpDBConnector() {
    }

    @Override
    public InputStream readTable(String tableName, Map<String, Object> alsoIncludeProps,
                                 AdpDBClientProperties props) throws RemoteException {
        try {
            PipedOutputStream out = new PipedOutputStream();
            pool.submit(new AdpDBTableReaderThread(tableName, alsoIncludeProps, props, out));
            return new PipedInputStream(out);
        } catch (Exception e) {
            throw new ServerException("Cannot read table: " + tableName, e);
        }
    }
}
