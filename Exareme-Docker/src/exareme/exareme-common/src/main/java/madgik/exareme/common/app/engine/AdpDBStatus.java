/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine;

import java.rmi.RemoteException;
import java.util.List;

/**
 * @author herald
 */
public interface AdpDBStatus {

    /**
     * @return the id of the session.
     */
    int getId();

    /**
     * @return
     */
    AdpDBQueryID getQueryID();

    /**
     * @return true if the session has finished.
     * @throws RemoteException
     */
    boolean hasFinished() throws RemoteException;

    /**
     * @return true if the session has exception.
     * @throws RemoteException
     */
    boolean hasError() throws RemoteException;

    /**
     * @param listener the query listener.
     * @throws RemoteException
     */
    void registerListener(AdpDBQueryListener listener) throws RemoteException;

    /**
     * Force the termination of the execution.
     *
     * @throws RemoteException
     */
    void stopExecution() throws RemoteException;

    /**
     * @return the most recent exception. Null if no exception exists.
     * @throws RemoteException if something goes wrong.
     */
    Exception getLastException() throws RemoteException;

    /**
     * Return at most k recent exceptions.
     *
     * @param k the maximum number of exceptions.
     * @return a list with the exceptions.
     * @throws RemoteException if something goes wrong.
     */
    List<Exception> getExceptions(int k) throws RemoteException;

    /**
     * The statistics may be incomplete if the job has not finished.
     *
     * @return the statistics.
     * @throws RemoteException if something goes wrong.
     */
    AdpDBStatistics getStatistics() throws RemoteException;

    /**
     * Return a status that can be serialized. Further updates will not be visible by the object.
     *
     * @return a serializable status.
     * @throws RemoteException
     */
    AdpDBStatus createSerializableStatus() throws RemoteException;
}
