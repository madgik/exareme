package madgik.exareme.master.engine.iterations.state;

import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.engine.iterations.handler.IterationsConstants;
import madgik.exareme.master.engine.iterations.handler.IterationsHandlerDFLUtils;
import madgik.exareme.master.engine.iterations.state.exceptions.IterationsStateFatalException;
import madgik.exareme.master.queryProcessor.HBP.AlgorithmProperties;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.http.nio.IOControl;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static madgik.exareme.master.engine.iterations.handler.IterationsConstants.previousPhaseOutputTblVariableName;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
public class IterativeAlgorithmState {
    private static final Logger log = Logger.getLogger(IterativeAlgorithmState.class);

    /**
     * Models the iterative algorithm phases/directory structure (in algorithms-dev repository).
     * Each enum field represents a phase of the iteration algorithm. Under each directory there
     * must be a multiple_local_global directory structure for each phase, except for
     * termination_condition phase, in which there must be a
     * termination_condition.template.sql file.
     */
    public enum IterativeAlgorithmPhasesModel {
        init,
        step,
        termination_condition,
        finalize
    }

    // Fields -----------------------------------------------------------------------------------
    private String algorithmKey;
    private AlgorithmProperties algorithmProperties;
    private String[] dflScripts;

    /**
     * Variable name and key of the dflVariablesMap to be used for DFL scripts variable
     * StrSubstitution.
     */
    final private String stepPhaseOutputTblVariableName;
    final private String termConditionPhaseOutputTblVariableName;
    /**
     * Used in conjunction with StrSubstitutor to replace variables in DFL scripts.
     * Mapping contains:
     * 1. IterationsConstants.previousPhaseOutputTblVariableName -> latestPhaseOutputTblName
     * 2. stepPhaseOutputTblVariableName -> currentStepPhaseOutputTblName
     */
    private Map<String, String> dflVariablesMap;   // TODO Remove completely and replace with separate variables


    // Iterations control-plane related fields [STATE] ------------------------------------------
    // An AdpDBClient is required per iterative algorithm.
    private AdpDBClient adpDBClient = null;
    // Required for notifying algorithm completion so as to initiate final result response.
    private IOControl ioctrl;
    // Set to false during execution phase, set to true on completion.
    private Boolean algorithmCompleted;
    // Set to true to signify necessity for error response.
    private Boolean algorithmHasError;
    // Message when error occured.
    private String algorithmError;
    // Query status of iteration phase, to be used for obtaining response data and termination_condition phase result.
    private AdpDBClientQueryStatus adpDBClientQueryStatus;

    // If this field's value is null, it signifies that the execution of the algorithm hasn't yet started.
    private IterativeAlgorithmPhasesModel currentExecutionPhase;

    // Previous iterations number is required to check whether the caller of the "getter" of DFL
    // scripts, has already incremented current iterations number.
    private Long currentIterationsNumber, previousIterationsNumber;

    // The lock will be used to ensure no data-races occur after the pre-algorithm-execution phase,
    // for the [STATE] fields above.
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Initializes the IterativeAlgorithmState object for the given algorithm.
     * <p>
     * Firstly, initializes the AdpDBClient (one per IterativeAlgorithm), generates
     * algorithmProperties and the DFL variables Map.
     * The latter is a mapping from variables (think templateStrings) used in step and finalize
     * iterative phases (i.e. previousPhaseOutput & currentStepOutputTbl) and
     * signify the table names to be used as output and input (providing context between phases),
     * in order to avoid "table already exists" error.
     *
     * @param algorithmKey        the key uniquely identifying the algorithm
     * @param algorithmProperties the algorithm properties of the algorithm
     * @param adpDBClient         the AdpDBClient to be used for the current algorithm execution
     * @throws IterationsStateFatalException if creation of the AdpDBClient fails with Remote
     *                                       Exception
     */
    public IterativeAlgorithmState(
            String algorithmKey,
            AlgorithmProperties algorithmProperties,
            AdpDBClient adpDBClient) {

        this.algorithmKey = algorithmKey;
        this.adpDBClient = adpDBClient;
        this.algorithmProperties = algorithmProperties;

        // State related fields initialization
        algorithmCompleted = false;
        algorithmHasError = false;
        currentExecutionPhase = null;
        algorithmError = null;

        // DFL modification variables
        stepPhaseOutputTblVariableName =
                IterationsHandlerDFLUtils.getStepPhaseOutputTblVariableName(algorithmKey);
        termConditionPhaseOutputTblVariableName =
                IterationsHandlerDFLUtils.getTermConditionPhaseOutputTblVariableName(algorithmKey);

        dflVariablesMap = new HashMap<>();
        // Initialize with init phase's output tbl name (it's always the first lookup)
        dflVariablesMap.put(
                IterationsConstants.previousPhaseOutputTblVariableName,
                IterationsHandlerDFLUtils.getInitPhaseOutputTblName(algorithmKey));
        dflVariablesMap.put(stepPhaseOutputTblVariableName, null);
        dflVariablesMap.put(termConditionPhaseOutputTblVariableName, null);
    }

    // Pre-Execution phase ======================================================================
    // Pre-Execution phase fields [Setters/Getters] ---------------------------------------------
    public String getAlgorithmKey() {
        return algorithmKey;
    }

    public void setAlgorithmKey(String algorithmKey) {
        this.algorithmKey = algorithmKey;
    }

    public void setDflScripts(String[] dflScripts) {
        this.dflScripts = dflScripts;
    }

    // Execution phase ==========================================================================
    // i.e. These methods must be called with the lock acquired.

    /**
     * Returns the DFL script for the given iterative phase.
     * <p>
     * For init phase simply returns the generated DFL script.
     * For step, termination condition and finalize phases it runs an strSubstitution for
     * replacing the previous output phase placeholder accordingly.
     * Completes two tasks:
     * 1) generates DFL for requested phase
     * 2) sets the latestPhaseOutputTblName in the dflVariablesMap
     * <p>
     * Restrictions:
     * 1) Must be called with the lock of this instance acquired.
     * 2) In case of step phase then it must be called *AFTER* having called incrementIterationsNumber().
     *
     * @throws IterationsStateFatalException if previous and current iterations number match, which
     *                                       means that caller hasn't already increased the
     *                                       iterations current number.
     */
    public String getDFLScript(IterativeAlgorithmPhasesModel phase) {
        ensureAcquiredLock();

        String dflScript;
        // DFL for init is already ("statically") generated.
        // For other phases, substitute variables and return generated String.
        switch (phase) {
            case init:
                dflScript = dflScripts[phase.ordinal()];
                break;
            case step:
                // Retrieve previousPhase outputTbl name & generate currentStep's outputTbl name
                String previousPhaseOutputTbl =
                        dflVariablesMap.get(previousPhaseOutputTblVariableName);
                String currentStepOutputTblName = generateStepPhaseCurrentOutputTbl();
                dflVariablesMap.put(IterationsConstants.previousPhaseOutputTblVariableName,
                        previousPhaseOutputTbl);
                dflVariablesMap.put(stepPhaseOutputTblVariableName, currentStepOutputTblName);

                dflScript = StrSubstitutor.replace(dflScripts[phase.ordinal()], dflVariablesMap);

                // Update previousPhaseOutputTbl name with currentStep's output tbl name
                dflVariablesMap.put(IterationsConstants.previousPhaseOutputTblVariableName,
                        currentStepOutputTblName);
                break;

            case termination_condition:
                dflVariablesMap.put(
                        termConditionPhaseOutputTblVariableName,
                        generateTermCondPhaseCurrentOutputTbl());
                dflScript = StrSubstitutor.replace(dflScripts[phase.ordinal()], dflVariablesMap);
                break;

            case finalize:
                dflScript = StrSubstitutor.replace(dflScripts[phase.ordinal()], dflVariablesMap);
                break;

            default:
                releaseLock();
                throw new IterationsStateFatalException("IterativePhase: \"" + phase.name()
                        + "\" is not supported yet", algorithmKey);
        }
        return dflScript;
    }

    /**
     * Increments the iterations counter of this algorithm.
     * Should be called after execution of a step phase.
     * (Must be called with the lock of this instance acquired.)
     */
    public void incrementIterationsNumber() {
        ensureAcquiredLock();
        this.currentIterationsNumber++;
    }

    // Execution phase [Getters/Setters] --------------------------------------------------------

    /**
     * Retrieves the already created AdpDBClient for a new query submission.
     * (Must be called with the lock of this instance acquired.)
     */
    public AdpDBClient getAdpDBClient() {
        ensureAcquiredLock();
        return adpDBClient;
    }

    /**
     * Retrieves the current iterations number.
     * (Must be called with the lock of this instance acquired.)
     */
    public Long getCurrentIterationsNumber() {
        ensureAcquiredLock();
        return currentIterationsNumber;
    }

    /**
     * Retrieves the current iterative algorithm phase.
     * (Must be called with the lock of this instance acquired.)
     */
    public IterativeAlgorithmPhasesModel getCurrentExecutionPhase() {
        ensureAcquiredLock();
        return currentExecutionPhase;
    }

    /**
     * Sets the iterative algorithm phase.
     * (Must be called with the lock of this instance acquired.)
     *
     * @param currentExecutionPhase the value of the current iterative algorithm phase
     */
    public void setCurrentExecutionPhase(IterativeAlgorithmPhasesModel currentExecutionPhase) {
        ensureAcquiredLock();
        this.currentExecutionPhase = currentExecutionPhase;
    }

    /**
     * Sets ioctrl provided by NIO via an
     * {@link org.apache.http.nio.entity.HttpAsyncContentProducer}, i.e.
     * {@link madgik.exareme.master.engine.iterations.handler.NIterativeAlgorithmResultEntity}.
     */
    public void setIoctrl(IOControl ioctrl) {
        ensureAcquiredLock();
        this.ioctrl = ioctrl;
    }

    /**
     * Signifies algorithm completion by setting the algorithmCompleted field to true
     * and triggering notification on IOCtrl for generating algorithm's response.
     * (Must be called with the lock of this instance acquired.)
     * (Must solely be called after execution phase.)
     */
    public void signifyAlgorithmCompletion() {
        ensureAcquiredLock();

        if (!currentExecutionPhase.equals(IterativeAlgorithmPhasesModel.finalize)) {
            String errMsg = "Attempt to signify algorithm completion before "
                    + IterativeAlgorithmPhasesModel.finalize + " phase.";
            log.error(errMsg);
            throw new IterationsStateFatalException(errMsg, algorithmKey);
        }

        algorithmCompleted = true;
        ioctrl.requestOutput();
    }

    /**
     * Signifies algorithm's execution error by setting the algorithmHasError field to true
     * and triggering notification on IOCtrl for generating algorithm's erroneous response.
     * (Must be called with the lock of this instance acquired.)
     */
    public void signifyAlgorithmError(String result) {
        ensureAcquiredLock();
        algorithmCompleted = false;
        algorithmHasError = true;
        algorithmError = result;
        ioctrl.requestOutput();
    }

    /**
     * (Must be called with the lock of this instance acquired.)
     */
    public Boolean getAlgorithmCompleted() {
        ensureAcquiredLock();
        return algorithmCompleted;
    }

    /**
     * (Must be called with the lock of this instance acquired.)
     */
    public Boolean getAlgorithmHasError() {
        ensureAcquiredLock();
        return algorithmHasError;
    }

    /**
     * (Must be called with the lock of this instance acquired.)
     */
    public String getAlgorithmError() {
        ensureAcquiredLock();
        return algorithmError;
    }

    /**
     * Retrieves query status of iteration phase.
     * (Must be called with the lock of this instance acquired.)
     * To be called after phase completion.
     *
     * @throws IterationsStateFatalException if called before algorithm completion.
     */
    public AdpDBClientQueryStatus getAdpDBClientQueryStatus() {
        ensureAcquiredLock();
        return adpDBClientQueryStatus;
    }

    /**
     * Sets query status of finalize query.
     * (Must be called with the lock of this instance acquired.)
     * To be called after submission of finalize phase query.
     *
     * @throws IterationsStateFatalException if called before finalize phase execution.
     */
    public void setAdpDBClientQueryStatus(AdpDBClientQueryStatus adpDBClientQueryStatus) {
        ensureAcquiredLock();
        this.adpDBClientQueryStatus = adpDBClientQueryStatus;
    }

    // Utilities --------------------------------------------------------------------------------

    /**
     * Tries to acquire the lock.
     *
     * @return True if lock is successfully acquired, false otherwise
     */
    public boolean tryLock() {
        return lock.tryLock();
    }

    /**
     * Locks this instance.
     */
    public void lock() {
        lock.lock();
    }

    /**
     * Releases the lock(s) of the current thread.
     */
    public void releaseLock() {
        lock.unlock();
        if (lock.isHeldByCurrentThread()) {
            // Unlikely to happen except for programming error.
            log.warn(Thread.currentThread().getId() + ": Lock counter > 1, releasing all locks");
            while (lock.isLocked())
                lock.unlock();
        }
    }

    /**
     * Ensures the lock is acquired, if not it logs a warning message, and then tries to lock.
     */
    private void ensureAcquiredLock() {
        if (!lock.isHeldByCurrentThread()) {
            log.warn(Thread.currentThread().getId()
                    + ": Using " + IterativeAlgorithmState.class.getName()
                    + " method (for manipulation of algorithm execution fields) with no lock");
            lock.lock();
            if (log.isDebugEnabled())
                log.debug(Thread.currentThread().getId() + ": Lock acquired");
        }
    }

    /**
     * Generates the step phase's current outputTbl name in the format
     * stepPhaseOutputTblVariableName_currentIterationNumber.
     *
     * @throws IterationsStateFatalException if previous and current iterations number match, which
     *                                       means that caller hasn't already increased the
     *                                       iterations current number.
     */
    private String generateStepPhaseCurrentOutputTbl() {
        if (currentIterationsNumber == null) {
            currentIterationsNumber = 0L;
            previousIterationsNumber = 0L;
        } else {
            if (currentIterationsNumber.equals(previousIterationsNumber)) {
                String errMsg = "Handler has called getter of DFL script, without having " +
                        "increased the iterations number first.";
                log.warn(errMsg);
                throw new IterationsStateFatalException(errMsg, algorithmKey);
            } else
                previousIterationsNumber = currentIterationsNumber;
        }
        return stepPhaseOutputTblVariableName + "_" + currentIterationsNumber;
    }

    /**
     * Generates the termination condition phase's current outputTbl name.
     */
    private String generateTermCondPhaseCurrentOutputTbl() {
        // Reducing iterations number by 1, so as to have consistency between stepOutputTblName and
        // terminationConditionOutputTblName (matching ending numbers).
        return termConditionPhaseOutputTblVariableName + "_" + (currentIterationsNumber - 1);
    }

    @Override
    public String toString() {
        String currentStateMsg = currentExecutionPhase == null ?
                "Pre-Execution" : "CurrentPhase: " + currentExecutionPhase.name();
        return "IterativeStateAlgorithm{\"" +
                algorithmProperties.getName() + "\"} [" +
                currentStateMsg + "]";
    }
}
