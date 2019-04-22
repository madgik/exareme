/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.app.engine.AdpDBStatistics;
import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * @author herald
 */
public interface AdpDBStatusManager {

    AdpDBStatus createNewStatus(AdpDBQueryID queryID, ExecutionEngineSessionPlan sessionPlan,
                                Map<String, String> categoryMessageMap) throws RemoteException;

    /**
     * Create new status.
     *
     * @param sessionPlan        the session plan
     * @param graph              the graph of computation
     * @param categoryMessageMap
     * @return the newly created status.
     * @throws RemoteException if something goes wrong.
     */
    AdpDBStatus createNewStatus(ExecutionEngineSessionPlan sessionPlan, ConcreteQueryGraph graph,
                                Map<String, String> categoryMessageMap) throws RemoteException;

    /**
     * @param statusId the status id.
     * @return true if the job has finished.
     */
    boolean hasFinished(int statusId);

    void setFinished(int statusId);

    void stopExecution(int statusId) throws RemoteException;

    /**
     * @param statusId the status id.
     * @return true if the job has error.
     */
    boolean hasError(int statusId);

    void setError(int statusId, Exception exception);

    void registerListener(AdpDBQueryListener listener, int statusId) throws RemoteException;

    /**
     * @param statusId the status id.
     * @return the most recent exception. Null if no exception exists.
     */
    Exception getLastException(int statusId);

    /**
     * Return at most k recent exceptions.
     *
     * @param k        the maximum number of exceptions.
     * @param statusId
     * @return a list with the exceptions.
     */
    List<Exception> getExceptions(int k, int statusId);

    void addException(Exception exception, int statusId);

    /**
     * The statistics may be incomplete if the job has not finished.
     *
     * @param statusId the status id.
     * @return the statistics.
     */
    AdpDBStatistics getStatistics(int statusId);

    /**
     * Updates the current statistics adding the values of the given one.
     *
     * @param delta    the difference.
     * @param statusId the status id.
     */
    void updateWith(AdpDBStatistics delta, int statusId);
}
