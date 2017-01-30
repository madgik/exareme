package madgik.exareme.master.engine.iterations.handler;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.iterations.exceptions.IterationsFatalException;
import madgik.exareme.master.engine.iterations.scheduler.IterationsScheduler;
import madgik.exareme.master.engine.iterations.state.IterationsStateManager;
import madgik.exareme.master.engine.iterations.state.IterationsStateManagerImpl;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.master.queryProcessor.composer.AlgorithmsProperties;
import madgik.exareme.master.queryProcessor.composer.Composer;
import madgik.exareme.master.queryProcessor.composer.ComposerConstants;

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
    private IterationsStateManager iterationsStateManager = IterationsStateManagerImpl.getInstance();
    private IterationsScheduler iterationsScheduler = IterationsScheduler.getInstance();

    // Instance methods -------------------------------------------------------------------------

    /**
     * Submits a new iterative algorithm.
     *
     * <p> Generates required DFL scripts and submits initial query, while returning the new
     * algorithm's key. This can be used for status querying.
     *
     * @param adpDBManager        the AdpDBManager of the gateway
     * @param algorithmProperties the properties of this algorithm
     * @return the iterative algorithm state
     * @throws IterationsFatalException if it failed to initialize an AdpDBClient for the current
     *                                  algorithm
     * @see AlgorithmsProperties.AlgorithmProperties
     */
    public IterativeAlgorithmState handleNewIterativeAlgorithmRequest(
            AdpDBManager adpDBManager,
            AlgorithmsProperties.AlgorithmProperties algorithmProperties){

        // Generate algorithm key, the adpDBClient for this algorithm and a new
        // IterativeAlgorithmState
        String algorithmKey = generateAlgorithmKey(algorithmProperties);

        String database = ComposerConstants.mipAlgorithmsDemoWorkingDirectory + algorithmKey;
        AdpDBClient adpDBClient;
        try {
            AdpDBClientProperties clientProperties =
                    new AdpDBClientProperties(database, "", "",
                            false, false, -1, 10);
            adpDBClient = AdpDBClientFactory.createDBClient(adpDBManager, clientProperties);
        } catch (RemoteException e) {
            String errMsg = "Failed to initialize " + AdpDBClient.class.getSimpleName()
                    + " for algorithm: " + algorithmKey;
            log.error(errMsg);
            throw new IterationsFatalException(errMsg, e);
        }

        IterativeAlgorithmState iterativeAlgorithmState =
                new IterativeAlgorithmState(algorithmKey, algorithmProperties, adpDBClient);
        // --------------------------------------------------------------------------------------
        // Prepare DFL scripts
        iterativeAlgorithmState.setDflScripts(
                IterationsHandlerDFLUtils.prepareDFLScripts(
                algorithmKey, composer, algorithmProperties, iterativeAlgorithmState));

        // Only after DFL initialization, submit to IterationsStateManager and schedule it
        iterationsStateManager.submitIterativeAlgorithm(algorithmKey, iterativeAlgorithmState);
        iterationsScheduler.scheduleNewAlgorithm(algorithmKey);

        return iterativeAlgorithmState;
    }

    /**
     * Removes an {@link IterativeAlgorithmState} from
     * {@link madgik.exareme.master.engine.iterations.state.IterationsStateManager}
     * @param algorithmKey the algorithm's key (uniquely identifies an algorithm)
     */
    public void removeIterativeAlgorithmStateInstanceFromISM(String algorithmKey) {
        iterationsStateManager.removeIterativeAlgorithm(algorithmKey);
    }
}
