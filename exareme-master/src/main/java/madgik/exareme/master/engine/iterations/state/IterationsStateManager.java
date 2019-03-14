package madgik.exareme.master.engine.iterations.state;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.master.engine.iterations.state.exceptions.IterationsStateFatalException;

/**
 * Iterations logic StateManager, mainly maps from either {@code AlgorithmKeys} or
 * {@link AdpDBQueryID} to {@code IterativeAlgorithmState} objects.
 *
 * <p>The {@code IterationsStateManager} provides a central location for querying and retrieving
 * state regarding iterative algorithms and their execution.
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 * @see AdpDBQueryID
 */
public interface IterationsStateManager {
    /**
     * Submits a new iterative algorithm to the {@code IterationsStateManager}.
     *
     * @param algorithmKey            the algorithm key, that uniquely identifies the given
     *                                IterativeAlgorithmState, not null
     * @param iterativeAlgorithmState the newly created {@code IterativeAlgorithmState} object, not
     *                                null
     * @throws IterationsStateFatalException If an entry with the same algorithmKey already exists.
     * @throws IllegalArgumentException      If either one of {@code algorithmKey} and {@code
     *                                       IterativeAlgorithmState} is null.
     * @see IterativeAlgorithmState
     */
    void submitIterativeAlgorithm(String algorithmKey,
                                  IterativeAlgorithmState iterativeAlgorithmState);

    /**
     * Removes an {@code IterativeAlgorithmState} object from the {@code IterationsStateManager}
     * using the specified {@code algorithmKey}.
     *
     * @param algorithmKey the algorithm key, that uniquely identifies {@code
     *                     IterativeAlgorithmState}, not null
     * @return the {@code IterativeAlgorithmState} object that was removed, or null if the object
     * does not exist.
     * @throws IllegalArgumentException If {@code algorithmKey} is null.
     */
    IterativeAlgorithmState removeIterativeAlgorithm(String algorithmKey);

    /**
     * Retrieves the {@code IterativeAlgorithmState} object from the {@code IterationsStateManager}.
     *
     * @param algorithmKey the algorithm key, that uniquely identifies {@code
     *                     IterativeAlgorithmState}, not null
     * @return the {@code IterativeAlgorithmState} object that was paired with the given {@code
     * algorithmKey}, or null if the object does not exist.<br> The client code must handle the case
     * of returning null, according to its context.
     * @throws IllegalArgumentException If {@code algorithmKey} is null.
     */
    IterativeAlgorithmState getIterativeAlgorithm(String algorithmKey);

    /**
     * Submits a new query for the algorithm designated by {@code algorithmKey}.
     *
     * @param algorithmKey the algorithm key, not null
     * @param queryID      the queryID retrieved by the AdpDBClientQueryStatus, not null
     * @throws IllegalArgumentException      If either of {@code algorithmKey} and {@code queryID}
     *                                       is null.
     * @throws IterationsStateFatalException If a query with the same ID already exists.
     * @see AdpDBQueryID
     * @see madgik.exareme.master.client.AdpDBClientQueryStatus
     */
    void submitQueryForIterativeAlgorithm(String algorithmKey, AdpDBQueryID queryID);

    /**
     * Removes an entry from the mapping of {@code queryID}s to {@code algorithmKey}s.
     *
     * @param queryID the queryID to be deleted
     * @throws IllegalArgumentException If {@code queryID} is null.
     */
    void removeQueryOfIterativeAlgorithm(AdpDBQueryID queryID);

    /**
     * Retrieves the {@code IterativeAlgorithmState} object from the {@code IterationsStateManager}
     * using the specified {@code queryID}.
     *
     * @param queryID the queryID which has already been submitted to the {@code
     *                IterationsStateManager}
     * @return the {@code IterativeAlgorithmState} object that was paired with the given {@code
     * algorithmKey}, which was tied to the given {@code queryID}. <br> Otherwise, null is returned
     * if either {@code queryID} does not map to an {@code algorithmKey} or {@code algorithmKey}
     * does not map to an {@code IterativeAlgorithmState}. <br> The client code must handle the case
     * of returning null, according to its context.
     * @throws IllegalArgumentException If {@code queryID} is null.
     */
    IterativeAlgorithmState getIterativeAlgorithm(AdpDBQueryID queryID);
}
