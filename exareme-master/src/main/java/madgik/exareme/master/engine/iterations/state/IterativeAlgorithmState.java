package madgik.exareme.master.engine.iterations.state;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import madgik.exareme.master.engine.iterations.state.exceptions.IterationsStateFatalException;
import madgik.exareme.master.queryProcessor.composer.AlgorithmsProperties;

import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerConstants.iterationsPropertyConditionQueryProvided;
import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerConstants.iterationsPropertyMaximumNumber;

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
    private String workingDirectory;

    // Iterations control-plane related fields [properties.json] --------------------------------
    private Boolean conditionQueryProvided;
    private Long maxIterationsNumber;

    // Iterations control-plane related fields [STATE] ------------------------------------------
    // If this field's value is null, it signifies that the execution of the algorithm hasn't yet
    // started.
    private IterativeAlgorithmPhasesModel currentExecutionPhase;
    private Long currentIterationsNumber;
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

    public void setConditionQueryProvided(Boolean conditionQueryProvided) {
        this.conditionQueryProvided = conditionQueryProvided;
    }

    public Long getMaxIterationsNumber() {
        return maxIterationsNumber;
    }

    public void setDflScripts(String[] dflScripts) {
        this.dflScripts = dflScripts;
    }

    /**
     * Returns the DFL script of a specific phase ({@code phase}).
     */
    public String getDFLScript(IterativeAlgorithmPhasesModel phase) {
        return dflScripts[phase.ordinal()];
    }
    // Execution phase fields ==========================
    // i.e. These methods must be called with the lock acquired.

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
        this.currentIterationsNumber = currentIterationsNumber;
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
                "Pre-Execution initialization" : "CurrentPhase: " + currentExecutionPhase.name();
        return "IterativeStateAlgorithm{\"" +
                algorithmProperties.getName() + "} [" +
                currentStateMsg;
    }
}
