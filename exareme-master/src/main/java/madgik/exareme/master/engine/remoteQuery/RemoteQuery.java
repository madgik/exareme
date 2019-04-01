/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery;

import madgik.exareme.master.engine.remoteQuery.impl.cache.CacheAlgorithm;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CachedDataInfo;
import madgik.exareme.worker.art.concreteOperator.manager.ProcessManager;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public interface RemoteQuery {

    /*
     * Request for the execution of a query and assign a listener for
     * response
     *
     * @param   server      the server in which the query will be executed
     * @param   query       the query which will be executed
     * @param   listener    the listener which the server call to inform
     *                      the client that the query results are available
     * @param   procManager TODO
     * @param   table       the tableName of the output results
     */
    public void schedule(ServerInfo server, String query, RemoteQueryListener listener,
                         ProcessManager procManager, String table) throws RemoteException, IOException;

    /*
     * Request for the execution of a query and assign a listener for
     * response, given a stale restriction
     *
     * @param   server      the server in which the query will be executed
     * @param   query       the query which will be executed
     * @param   listener    the listener which the server call to inform
     *                      the client that the query results are available
     * @param   procManager TODO
     * @param   table       the tableName of the output results
     * @param   staleLimit  the time limit which is acceptable for in cache
     *                      query results, to be valid for the request
     */
    public void schedule(ServerInfo server, String query, RemoteQueryListener listener,
                         ProcessManager procManager, String table, double staleLimit)
            throws RemoteException, IOException;

    /*
     * Function which is called by the client to notify the server
     * that it no longer needs the query results
     *
     * @param   query       the query for which the request was done
     * @param   info        the information of the used data
     */
    public void finished(String query, CachedDataInfo info) throws RemoteException;

    /*
     * Function which changes the desirable replacement algorithm
     * and the storageSize
     */
    public void setReplacementAlgorithm(CacheAlgorithm algorithm, int storageSize);

    /*
     * Function which is called so as to close the server
     */
    public void close();

    public void printData();
}
