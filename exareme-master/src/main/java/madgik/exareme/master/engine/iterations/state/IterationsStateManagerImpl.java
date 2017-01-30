package madgik.exareme.master.engine.iterations.state;

import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.master.engine.iterations.state.exceptions.IterationsStateFatalException;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class IterationsStateManagerImpl implements IterationsStateManager {
    private static final Logger log = Logger.getLogger(IterationsStateManagerImpl.class);

    // Mapping from algorithmKey -> IterativeAlgorithmState.
    private ConcurrentHashMap<String, IterativeAlgorithmState> iterativeAlgorithmMapping;

    // Mapping from QueryID -> AlgorithmKey.
    // To be used from listeners, so as to access the algorithm's state.
    private ConcurrentHashMap<AdpDBQueryID, String> queryIdToAlgorithmKeyMapping;

    // Singleton related ------------------------------------------------------------------------
    private IterationsStateManagerImpl() {
        iterativeAlgorithmMapping = new ConcurrentHashMap<>();
        queryIdToAlgorithmKeyMapping = new ConcurrentHashMap<>();
    }

    private static IterationsStateManagerImpl instance = new IterationsStateManagerImpl();

    public static IterationsStateManager getInstance() {
        return instance;
    }
    // ------------------------------------------------------------------------------------------

    // IterationsStateManager API ---------------------------------------------------------------
    // Related to algorithmKey -> IterativeAlgorithmState mapping -------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void submitIterativeAlgorithm(String algorithmKey, IterativeAlgorithmState iterativeAlgorithmState) {
        if (algorithmKey == null || iterativeAlgorithmState == null) {
            String errMsg = algorithmKey == null ? "algorithmKey " : "";
            errMsg += iterativeAlgorithmState == null ?
                    (!errMsg.isEmpty() ? " or iterativeAlgorithmState " : "iterativeAlgorithmState ")
                    : "";
            errMsg += "cannot be null.";
            throw new IllegalArgumentException(errMsg);
        }

        IterativeAlgorithmState dummyIterAlgorithmState = iterativeAlgorithmMapping.get(algorithmKey);
        if (dummyIterAlgorithmState != null) {
            String errMsg = "An IterativeAlgorithmState entry ("
                    + dummyIterAlgorithmState.toString() + ") tied to the " +
                    "specified algorithmKey (\"" + algorithmKey + "\" already exists.";
            throw new IterationsStateFatalException(errMsg, null);
        }

        iterativeAlgorithmMapping.put(algorithmKey, iterativeAlgorithmState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IterativeAlgorithmState removeIterativeAlgorithm(String algorithmKey) {
        if (algorithmKey == null)
            throw new IllegalArgumentException("Attempt to remove IterativeAlgorithm from " +
                    this.getClass().getSimpleName() + ": algorithmKey cannot be null.");

        return iterativeAlgorithmMapping.remove(algorithmKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IterativeAlgorithmState getIterativeAlgorithm(String algorithmKey) {
        if (algorithmKey == null)
            throw new IllegalArgumentException("Attempt to retrieve IterativeAlgorithm from " +
                    this.getClass().getSimpleName() + ": algorithmKey cannot be null.");

        return iterativeAlgorithmMapping.get(algorithmKey);
    }

    // Related to queryID -> algorithmKey mapping -----------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void submitQueryForIterativeAlgorithm(String algorithmKey, AdpDBQueryID queryID) {
        if (algorithmKey == null || queryID == null) {
            String msg = algorithmKey == null ? "algorithmKey " : "";
            msg += queryID == null ?
                    (!msg.isEmpty() ? " or queryID " : "queryID ")
                    : "";
            msg += "cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (iterativeAlgorithmMapping.get(algorithmKey) == null)
            throw new IterationsStateFatalException("An algorithmKey (\"" + algorithmKey + "\") entry " +
                    "tied to the specified queryID (\"" + queryID + "\") already exists.", null);

        queryIdToAlgorithmKeyMapping.put(queryID, algorithmKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeQueryOfIterativeAlgorithm(AdpDBQueryID queryID) {
        if (queryID == null)
            throw new IllegalArgumentException("Attempt to retrieve IterativeAlgorithm from " +
                    this.getClass().getSimpleName() + ": queryID cannot be null.");

        queryIdToAlgorithmKeyMapping.remove(queryID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IterativeAlgorithmState getIterativeAlgorithm(AdpDBQueryID queryID) {
        if (queryID == null)
            throw new IllegalArgumentException("Attempt to retrieve IterativeAlgorithm from " +
                    this.getClass().getSimpleName() + ": queryID cannot be null.");

        String algorithmKey = queryIdToAlgorithmKeyMapping.get(queryID);
        if (algorithmKey == null)
            return null;
        return iterativeAlgorithmMapping.get(algorithmKey);
    }
}
