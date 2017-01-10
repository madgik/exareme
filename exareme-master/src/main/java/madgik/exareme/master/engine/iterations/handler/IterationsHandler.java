package madgik.exareme.master.engine.iterations.handler;

import org.apache.log4j.Logger;

import madgik.exareme.master.engine.iterations.state.IterationsStateManagerImpl;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.master.queryProcessor.composer.AlgorithmsProperties;
import madgik.exareme.master.queryProcessor.composer.Composer;

import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerUtils.generateAlgorithmKey;

/**
 * Handles iteration requests
 *
 * <p> This involves either submitting a new iterative algorithm or querying the status of an
 * already running one.
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class IterationsHandler {
    private static final Logger log = Logger.getLogger(IterationsHandler.class);

    // Singleton specific -----------------------------------------------------------------------
    private IterationsHandler() {
    }

    private static final IterationsHandler instance = new IterationsHandler();

    public static IterationsHandler getInstance() {
        return instance;
    }

    // Instance fields --------------------------------------------------------------------------
    private Composer composer = Composer.getInstance();
    private IterationsStateManagerImpl iterationsStateManager = IterationsStateManagerImpl.getInstance();

    // Instance methods -------------------------------------------------------------------------
    /**
     * Submits a new iterative algorithm.
     *
     * <p> Generates required DFL scripts and submits initial query, while returning the new
     * algorithm's key. This can be used for status querying.
     *
     * @param algorithmProperties the properties of this algorithm
     * @return the iterative algorithm's key
     *
     * @see AlgorithmsProperties.AlgorithmProperties
     */
    public String handleNewIterativeAlgorithmRequest(
            AlgorithmsProperties.AlgorithmProperties algorithmProperties){

        // Generate algorithm key and a new IterativeAlgorithmState object
        // via the iterationsStateManager.
        String algorithmKey = generateAlgorithmKey(algorithmProperties);
        IterativeAlgorithmState iterativeAlgorithmState =
                new IterativeAlgorithmState(algorithmKey, algorithmProperties);
        iterationsStateManager.submitIterativeAlgorithm(algorithmKey, iterativeAlgorithmState);
        // --------------------------------------------------------------------------------------
        // Prepare DFL scripts
        iterativeAlgorithmState.setDflScripts(
                IterationsHandlerDFLUtils.prepareDFLScripts(
                algorithmKey, composer, algorithmProperties, iterativeAlgorithmState));

        // WIP -------

        return null;
    }
}
