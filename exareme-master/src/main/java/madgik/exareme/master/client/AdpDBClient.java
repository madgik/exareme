/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.client;

import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.master.engine.dflSegment.Segment;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.List;


/**
 * @author alex
 * @since 0.1
 */
public interface AdpDBClient {

    /**
     * @param queryScript
     * @return
     * @throws RemoteException
     */
    String explain(String queryScript, String exportMode) throws RemoteException;

    /**
     * @param queryID
     * @param queryScript
     * @return
     * @throws RemoteException
     */
    AdpDBClientQueryStatus query(String queryID, String queryScript) throws RemoteException;

    AdpDBClientQueryStatus query(String queryID, QueryScript script) throws RemoteException;

    AdpDBClientQueryStatus iquery(String queryID, String queryScript) throws RemoteException;

    AdpDBClientQueryStatus query(String queryID, List<Segment> segments) throws RemoteException;

    /**
     * @param queryID
     * @param queryScript
     * @param listener
     * @return
     * @throws RemoteException
     */
    AdpDBClientQueryStatus aquery(String queryID, String queryScript, AdpDBQueryListener listener)
        throws RemoteException;

    /**
     * @param tableName
     * @return
     * @throws RemoteException
     */
    InputStream readTable(String tableName) throws RemoteException;

}
