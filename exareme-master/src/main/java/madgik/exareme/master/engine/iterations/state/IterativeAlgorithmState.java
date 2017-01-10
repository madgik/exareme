package madgik.exareme.master.engine.iterations.state;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import madgik.exareme.master.engine.iterations.exceptions.IterationsFatalException;
import madgik.exareme.master.engine.iterations.handler.IterationsHandlerConstants;
import madgik.exareme.master.engine.iterations.handler.IterationsHandlerDFLUtils;
import madgik.exareme.master.engine.iterations.state.exceptions.IterationsStateFatalException;
import madgik.exareme.master.queryProcessor.composer.AlgorithmsProperties;

import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerConstants.iterationsPropertyConditionQueryProvided;
import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerConstants.iterationsPropertyMaximumNumber;
import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerConstants.previousPhaseOutputTblVariableName;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class IterativeAlgorithmState {
    private static final Logger log = Logger.getLogger(IterativeAlgorithmState.class);

    // Generic fields ---------------------------------------------------------------------------
    /**
     * Models the iterative algorithm phases/directory structure (in algorithms-dev repository).
     * <p>
     * Each enum field represents a phase of the iteration algorithm. Under each directory there
     * must be a multiple_local_global directory structure for each phase, <b>except for</b>
     * {@code termination_condition} phase, in which there must be a
     * {@code termination_condition.template.sql} file.
     */
    public enum IterativeAlgorithmPhasesModel {
        init,
        step,
        termination_condition,
        finalize
    }

    // Fields -----------------------------------------------------------------------------------
    private String algorithmKey;
    private AlgorithmsProperties.AlgorithmProperties algorithmProperties;
    private HashMap<String, String> algorithmPropertiesMap;
    private String[] dflScripts;
    /**
     * Variable name and key of the {@code dflVariablesMap} to be used for DFL scripts variable
     * StrSubstitution.
     */
    final private String stepPhaseOutputTblVariableName;
    // Iterations control-plane related fields [properties.json] --------------------------------
    private Boolean conditionQueryProvided;
    private Long maxIterationsNumber;

    // Iterations control-plane related fields [STATE] ------------------------------------------
    // If this field's value is null, it signifies that the execution of the algorithm hasn't yet
    // started.
    private IterativeAlgorithmPhasesModel currentExecutionPhase;
    private Long currentIterationsNumber;
    /**
     * Used in conjunction with StrSubstitutor to replace variables in DFL scripts.
     *
     * <p> Mapping contains: <br>
     *  1. {@code IterationsHandlerConstants.previousPhaseOutputTblVariableName -> latestPhaseOutputTblName}<br>
     *  2. {@code stepPhaseOutputTblVariableName -> currentStepPhaseOutputTblName}<br>
     */
    private Map<String, String> dflVariablesMap;

    // The lock will be used to ensure no data-races occur after the pre-algorithm-execution phase,
    // for the [STATE] fields above.
    private final ReentrantLock lock = new ReentrantLock();

    // Construction -----------------------------------------------------------------------------
    public IterativeAlgorithmState(String algorithmKey,
                            AlgorithmsProperties.AlgorithmProperties algorithmProperties) {
        this.algorithmKey = algorithmKey;
        this.algorithmProperties = algorithmProperties;
        algorithmPropertiesMap =
                AlgorithmsProperties.AlgorithmProperties.toHashMap(algorithmProperties);
        setUpPropertyFields();

        // State related fields initialization
        currentExecutionPhase = null;
        stepPhaseOutputTblVariableName = IterationsHandlerDFLUtils.getStepPhaseOutputTblVariableName(algorithmKey);

        dflVariablesMap = new HashMap<>();
        // Initialize with init phase's output tbl name (it's always the first lookup)
        dflVariablesMap.put(
                IterationsHandlerConstants.previousPhaseOutputTblVariableName,
                IterationsHandlerDFLUtils.getInitPhaseOutputTblName(algorithmKey));
        dflVariablesMap.put(stepPhaseOutputTblVariableName, null);
    }

    // IterativeAlgorithmState - Set property fields --------------------------------------------

    /**
     * Ensures that iterative properties in {@code properties.json} file are provided and that
     * their values are correct.
     */
    private void setUpPropertyFields() {
        // Ensure conditionQueryProvided is provided in properties.json, then ensure its value is
        // true/false and finally, set the corresponding field.
        final String iterationsConditionQueryProvidedValue =
                algorithmPropertiesMap.get(iterationsPropertyConditionQueryProvided);
        if (iterationsConditionQueryProvidedValue == null) {
            throw new IterationsStateFatalException("AlgorithmProperty \""
                    + iterationsPropertyConditionQueryProvided
                    + "\": is required [accepting: \"true/false\"");
        }
        if (iterationsConditionQueryProvidedValue.equals(String.valueOf(true)))
            conditionQueryProvided = true;
        else if (iterationsConditionQueryProvidedValue.equals(String.valueOf(false)))
            conditionQueryProvided = false;
        else
            throw new IterationsStateFatalException("AlgorithmProperty \""
                    + iterationsPropertyConditionQueryProvided
                    + "\": Expected \"true/false\", found: "
                    + iterationsConditionQueryProvidedValue);

        // Ensure maxIterationsNumber is provided in properties.json, them ensure its value is
        // true/false and finally, set the corresponding field.
        final String iterationsMaxNumberVal =
                algorithmPropertiesMap.get(iterationsPropertyMaximumNumber);
        if (iterationsMaxNumberVal  == null) {
            throw new IterationsStateFatalException("AlgorithmProperty \""
                    + iterationsPropertyMaximumNumber
                    + "\": is required [accepting: \"long integer values\"");
        }
        if (!iterationsMaxNumberVal.isEmpty()) {
            try {
                maxIterationsNumber = Long.parseLong(iterationsMaxNumberVal);
            } catch (NumberFormatException e) {
                throw new IterationsStateFatalException("IterativeAlgorithm property \""
                        + iterationsPropertyMaximumNumber
                        + "\": NaN [only accepted: long integer values]");
            }
        }
        else
            throw new IterationsStateFatalException("IterativeAlgorithm property \"" +
                    iterationsPropertyMaximumNumber
                    + "\": cannot be empty [only accepted: long integer values]");
    }

    // Setters/Getters --------------------------------------------------------------------------
    // Pre-Execution phase fields ======================
    public String getAlgorithmKey() {
        return algorithmKey;
    }

    public void setAlgorithmKey(String algorithmKey) {
        this.algorithmKey = algorithmKey;
    }

    public Boolean getConditionQueryProvided() {
        return conditionQueryProvided;
    }

    public void setDflScripts(String[] dflScripts) {
        this.dflScripts = dflScripts;
    }

    // Execution phase fields ==========================
    // i.e. These methods must be called with the lock acquired.

    /**
     * Returns the DFL script of the given iterative phase.
     *
     * <p> For step and finalize phases it runs an strSubstitution for replacing the previous
     * output phase placeholder accordingly.
     * <b>Completes two tasks</b>:<br>
     *     1. generates DFL for requested phase, <br>
     *     2. sets the {@code latestPhaseOutputTblName} in the {@code dflVariablesMap}.
     */
    public String getDFLScript(IterativeAlgorithmPhasesModel phase) {
        ensureAcquiredLock();
        String dflScript;
        // DFL for init and termination condition are already ("statically") generated, simply return
        // For other phases, substitute variables and return generated String
        switch (phase) {
            case init:
                dflScript = dflScripts[phase.ordinal()];
                break;
            case step:
                // Retrieve previousPhase outputTbl name & generate currentStep's outputTbl name
                String previousPhaseOutputTbl = dflVariablesMap.get(previousPhaseOutputTblVariableName);
                String currentStepOutputTblName = generateStepPhaseCurrentOutputTbl();
                dflVariablesMap.put(IterationsHandlerConstants.previousPhaseOutputTblVariableName,
                        previousPhaseOutputTbl);
                dflVariablesMap.put(stepPhaseOutputTblVariableName, currentStepOutputTblName);

                dflScript = StrSubstitutor.replace(dflScripts[phase.ordinal()], dflVariablesMap);

                // Update previousPhaseOutputTbl name with currentStep's output tbl name
                dflVariablesMap.put(IterationsHandlerConstants.previousPhaseOutputTblVariableName,
                        currentStepOutputTblName);
                break;
            case termination_condition:
                return dflScripts[phase.ordinal()];
            case finalize:
                dflScript = StrSubstitutor.replace(dflScripts[phase.ordinal()], dflVariablesMap);
                break;
                default:
                    throw new IterationsFatalException("IterativePhase: \"" + phase.name()
                            + "\" is not supported yet");
        }
        return dflScript;
    }

    /**
     * Retrieves the current iterations number.
     * <p>Must be called with the lock of this instance acquired.
     */
    public Long getCurrentIterationsNumber() {
        ensureAcquiredLock();
        return currentIterationsNumber;
    }

    /**
     * Increments the iterations counter of this algorithm.
     */
    public void incrementIterationsNumber() {
        ensureAcquiredLock();
        this.currentIterationsNumber++;
    }

    /**
     * Retrieves the current iterative algorithm phase.
     * <p><b>Must be called with the lock of this instance acquired.</b>
     * @see IterativeAlgorithmPhasesModel
     */
    public IterativeAlgorithmPhasesModel getCurrentExecutionPhase() {
        ensureAcquiredLock();
        return currentExecutionPhase;
    }

    /**
     * Sets the iterative algorithm phase.
     *
     * @param currentExecutionPhase the value of the current iterative algorithm phase
     */
    public void setCurrentExecutionPhase(IterativeAlgorithmPhasesModel currentExecutionPhase) {
        ensureAcquiredLock();
        this.currentExecutionPhase = currentExecutionPhase;
    }

    // Utilities --------------------------------------------------------------------------------
    /**
     * Tries to acquire the lock.
     * @return True if lock is successfully acquired, false otherwise
     */
    public boolean tryLock() {
        return lock.tryLock();
    }

    /**
     * Releases the lock(s) of the current thread.
     */
    public void releaseLock() {
        lock.unlock();
        if (lock.isLocked()) {
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
                log.debug(Thread.currentThread().getId()
                        + ": Lock acquired");
        }
    }

    @Override
    public String toString() {
        String currentStateMsg = currentExecutionPhase == null ?
                "Pre-Execution" : "CurrentPhase: " + currentExecutionPhase.name();
        return "IterativeStateAlgorithm{\"" +
                algorithmProperties.getName() + "\"} [" +
                currentStateMsg + "]";
    }

    /**
     * Generates the step phase's current outputTbl name in the format
     * {@code stepPhaseOutputTblVariableName_currentIterationNumber}.
     */
    private String generateStepPhaseCurrentOutputTbl() {
        if (currentIterationsNumber == null)
            currentIterationsNumber = 1L;
        return stepPhaseOutputTblVariableName + "_" + currentIterationsNumber;
    }
}
