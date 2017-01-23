package madgik.exareme.master.engine.iterations.state;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.iterations.handler.IterationsHandlerConstants;
import madgik.exareme.master.engine.iterations.handler.IterationsHandlerDFLUtils;
import madgik.exareme.master.engine.iterations.state.exceptions.IterationsStateFatalException;
import madgik.exareme.master.queryProcessor.composer.AlgorithmsProperties;
import madgik.exareme.master.queryProcessor.composer.ComposerConstants;

import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerConstants.iterationsConditionCheckColName;
import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerConstants.iterationsPropertyConditionQueryProvided;
import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerConstants.iterationsPropertyMaximumNumber;
import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerConstants.previousPhaseOutputTblVariableName;
import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerConstants.selectTerminationConditionValue;

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
    private final String iterationsDBPath;
    /**
     * Variable name and key of the {@code dflVariablesMap} to be used for DFL scripts variable
     * StrSubstitution.
     */
    final private String stepPhaseOutputTblVariableName;
    final private String termConditionPhaseOutputTblVariableName;
    // Iterations control-plane related fields [properties.json] --------------------------------
    private Boolean conditionQueryProvided;
    private Long maxIterationsNumber;

    // Iterations control-plane related fields [STATE] ------------------------------------------
    // An AdpDBClient is required per iterative algorithm.
    private AdpDBClient adpDBClient = null;

    // If this field's value is null, it signifies that the execution of the algorithm hasn't yet
    // started.
    private IterativeAlgorithmPhasesModel currentExecutionPhase;
    // Previous iterations number is required to check whether the caller of the "getter" of DFL
    // scripts, has already incremented current iterations number.
    private Long currentIterationsNumber, previousIterationsNumber;
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

    /**
     * Initializes the IterativeAlgorithmState object for the given algorithm.
     *
     * <p> Firstly, initializes the AdpDBClient (one per IterativeAlgorithm), generates {@code
     * algorithmProperties} Map and the DFL variables Map.
     * The latter is a mapping from variables (think templateStrings) used in step and finalize
     * iterative phases (i.e. {@code previousPhaseOutput} & {@code currentStepOutputTbl}) and
     * signify the table names to be used as output and input (providing context between phases).
     *
     * @param algorithmKey the key uniquely identifying the algorithm
     * @param algorithmProperties the algorithm properties of the algorithm
     * @param manager the AdpDBManager of the system
     * @throws IterationsStateFatalException if creation of the AdpDBClient fails with Remote
     * Exception
     */
    public IterativeAlgorithmState(
            String algorithmKey,
            AlgorithmsProperties.AlgorithmProperties algorithmProperties,
            AdpDBManager manager) {

        this.algorithmKey = algorithmKey;
        iterationsDBPath =
                ComposerConstants.mipAlgorithmsDemoWorkingDirectory + algorithmKey + "/"
                        + IterationsHandlerConstants.iterationsParameterIterDBValueSuffix;

        String database = ComposerConstants.mipAlgorithmsDemoWorkingDirectory + algorithmKey;
        try {
            createAdpDBClient(manager, database);
        } catch (RemoteException e) {
            String errMsg = "Failed to initialize " + AdpDBClient.class.getSimpleName()
                    + " for algorithm: " + algorithmKey;
            log.error(errMsg);
            throw new IterationsStateFatalException(errMsg, e, algorithmKey);
        }
        this.algorithmProperties = algorithmProperties;
        algorithmPropertiesMap =
                AlgorithmsProperties.AlgorithmProperties.toHashMap(algorithmProperties);
        setUpPropertyFields();

        // State related fields initialization
        currentExecutionPhase = null;
        stepPhaseOutputTblVariableName =
                IterationsHandlerDFLUtils.getStepPhaseOutputTblVariableName(algorithmKey);
        termConditionPhaseOutputTblVariableName =
                IterationsHandlerDFLUtils.getTermConditionPhaseOutputTblVariableName(algorithmKey);

        dflVariablesMap = new HashMap<>();
        // Initialize with init phase's output tbl name (it's always the first lookup)
        dflVariablesMap.put(
                IterationsHandlerConstants.previousPhaseOutputTblVariableName,
                IterationsHandlerDFLUtils.getInitPhaseOutputTblName(algorithmKey));
        dflVariablesMap.put(stepPhaseOutputTblVariableName, null);
        dflVariablesMap.put(termConditionPhaseOutputTblVariableName, null);
    }

    // IterativeAlgorithmState - Set property fields --------------------------------------------

    /**
     * Ensures that iterative properties in {@code properties.json} file are provided and that
     * their values are correct.
     * <p>IterationsStateFatalExceptions thrown from this method, don't need to specify the {@code
     * algorithmKey} parameter since the {@code IterativeAlgorithmState} hasn't been submitted to
     * the {@code IterationsStateManager}.
     */
    private void setUpPropertyFields() {
        // Ensure conditionQueryProvided is provided in properties.json, then ensure its value is
        // true/false and finally, set the corresponding field.
        final String iterationsConditionQueryProvidedValue =
                algorithmPropertiesMap.get(iterationsPropertyConditionQueryProvided);
        if (iterationsConditionQueryProvidedValue == null) {
            throw new IterationsStateFatalException("AlgorithmProperty \""
                    + iterationsPropertyConditionQueryProvided
                    + "\": is required [accepting: \"true/false\"]", null);
        }
        if (iterationsConditionQueryProvidedValue.equals(String.valueOf(true)))
            conditionQueryProvided = true;
        else if (iterationsConditionQueryProvidedValue.equals(String.valueOf(false)))
            conditionQueryProvided = false;
        else
            throw new IterationsStateFatalException("AlgorithmProperty \""
                    + iterationsPropertyConditionQueryProvided
                    + "\": Expected \"true/false\", found: "
                    + iterationsConditionQueryProvidedValue, null);

        // Ensure maxIterationsNumber is provided in properties.json, them ensure its value is
        // true/false and finally, set the corresponding field.
        final String iterationsMaxNumberVal =
                algorithmPropertiesMap.get(iterationsPropertyMaximumNumber);
        if (iterationsMaxNumberVal  == null) {
            throw new IterationsStateFatalException("AlgorithmProperty \""
                    + iterationsPropertyMaximumNumber
                    + "\": is required [accepting: \"long integer values\"]", null);
        }
        if (!iterationsMaxNumberVal.isEmpty()) {
            try {
                maxIterationsNumber = Long.parseLong(iterationsMaxNumberVal);
            } catch (NumberFormatException e) {
                throw new IterationsStateFatalException("IterativeAlgorithm property \""
                        + iterationsPropertyMaximumNumber
                        + "\": NaN [only accepted: long integer values]", null);
            }
        }
        else
            throw new IterationsStateFatalException("IterativeAlgorithm property \"" +
                    iterationsPropertyMaximumNumber
                    + "\": cannot be empty [only accepted: long integer values]", null);
    }

    // Pre-Execution phase ======================================================================

    /**
     * Initializes the {@link AdpDBClient} of the iterative algorithm.
     *
     * @param manager the manager needed for creating the client
     * @param database the database string of the iterative algorithm
     * @throws RemoteException if creation of AdpDBClient fails
     */
    private void createAdpDBClient(AdpDBManager manager, String database) throws RemoteException {
        AdpDBClientProperties clientProperties =
                new AdpDBClientProperties(database, "", "",
                        false, false, -1, 10);
        adpDBClient = AdpDBClientFactory.createDBClient(manager, clientProperties);
    }

    // Pre-Execution phase fields [Setters/Getters] ---------------------------------------------
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

    // Execution phase ==========================================================================
    // i.e. These methods must be called with the lock acquired.

    /**
     * Returns the DFL script of the given iterative phase.
     *
     * <p>
     * For step, termination condition and finalize phases it runs an strSubstitution for
     * replacing the previous output phase placeholder accordingly.<br>
     * <strong>Completes two tasks</strong>:<br>
     * <ol>
     * <li>generates DFL for requested phase</li>
     * <li>sets the {@code latestPhaseOutputTblName} in the {@code dflVariablesMap}</li>
     * </ol>
     *
     * <p>
     * <strong>Restrictions</strong>
     * <ol>
     * <li>Must be called with the lock of this instance acquired.</li>
     * <li>Must be called *AFTER* having called {@link
     * IterativeAlgorithmState#incrementIterationsNumber()} <b>in case of step phase only</b>.
     * </li>
     * </ol>
     *
     * @throws IterationsStateFatalException if previous and current iterations number match, which
     *                                       means that caller hasn't already increased the
     *                                       iterations current number (calling {@link
     *                                       IterativeAlgorithmState#incrementIterationsNumber()}.
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
                dflVariablesMap.put(IterationsHandlerConstants.previousPhaseOutputTblVariableName,
                        previousPhaseOutputTbl);
                dflVariablesMap.put(stepPhaseOutputTblVariableName, currentStepOutputTblName);

                dflScript = StrSubstitutor.replace(dflScripts[phase.ordinal()], dflVariablesMap);

                // Update previousPhaseOutputTbl name with currentStep's output tbl name
                dflVariablesMap.put(IterationsHandlerConstants.previousPhaseOutputTblVariableName,
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
     *
     * <p><b>Must be called with the lock of this instance acquired.</b>
     * Should be called after execution of a step phase.
     */
    public void incrementIterationsNumber() {
        ensureAcquiredLock();
        this.currentIterationsNumber++;
    }

    // Execution phase [Getters/Setters] --------------------------------------------------------
    /**
     * Retrieves the already created {@link AdpDBClient} for a new query submission.
     *
     * <p><b>Must be called with the lock of this instance acquired.</b>
     */
    public AdpDBClient getAdpDBClient() {
        ensureAcquiredLock();
        return adpDBClient;
    }

    /**
     * Retrieves the current iterations number.
     *
     * <p><b>Must be called with the lock of this instance acquired.</b>
     */
    public Long getCurrentIterationsNumber() {
        ensureAcquiredLock();
        return currentIterationsNumber;
    }

    /**
     * Retrieves the maximum iterations number.
     *
     * <p><b>Must be called with the lock of this instance acquired.</b>
     */
    public Long getMaxIterationsNumber() {
        ensureAcquiredLock();
        return maxIterationsNumber;
    }

    /**
     * Retrieves the current iterative algorithm phase.
     * <p><b>Must be called with the lock of this instance acquired.</b>
     *
     * @see IterativeAlgorithmPhasesModel
     */
    public IterativeAlgorithmPhasesModel getCurrentExecutionPhase() {
        ensureAcquiredLock();
        return currentExecutionPhase;
    }

    /**
     * Sets the iterative algorithm phase.
     * <p><b>Must be called with the lock of this instance acquired.</b>
     *
     * @param currentExecutionPhase the value of the current iterative algorithm phase
     */
    public void setCurrentExecutionPhase(IterativeAlgorithmPhasesModel currentExecutionPhase) {
        ensureAcquiredLock();
        this.currentExecutionPhase = currentExecutionPhase;
    }

    /**
     * Retrieves termination condition from iterationsDB of this algorithm.
     * <p><b>Must be called with the lock of this instance acquired.</b>
     *
     * @return true if the iterative algorithm should continue, false otherwise
     */
    public boolean readTerminationConditionValue() {
        ensureAcquiredLock();

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            String errMsg = "Could not find sqlite.JDBC driver class.";
            log.error(e);
            throw new IterationsStateFatalException(errMsg, e, algorithmKey);
        }

        Statement stmt = null;
        try (Connection conn =
                     DriverManager.getConnection("jdbc:sqlite:" + iterationsDBPath)) {
            stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(selectTerminationConditionValue);
            if (!resultSet.isBeforeFirst()) {
                String errMsg = "No data returned from iterationsDB condition check table for "
                        + toString();
                log.warn(errMsg);
                throw new IterationsStateFatalException(errMsg, algorithmKey);
            }
            if (resultSet.next()) {
                // Iterations control logic writes either 1 or 0 at the terminationConditionCheck
                // column.
                return resultSet.getInt(iterationsConditionCheckColName) == 1;
            }
        } catch (SQLException e) {
            String errMsg = "Failed to query for termination condition value for " + toString();
            log.error(errMsg);
            throw new IterationsStateFatalException(errMsg, e, algorithmKey);
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error("Failed to close iterationsDB select statement for: " + toString());
                }
            }
        }
        return false;
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
     *
     * @throws IterationsStateFatalException if previous and current iterations number match, which
     *                                       means that caller hasn't already increased the
     *                                       iterations current number (calling {@link
     *                                       IterativeAlgorithmState#incrementIterationsNumber()}.
     */
    private String generateStepPhaseCurrentOutputTbl() {
        if (currentIterationsNumber == null) {
            currentIterationsNumber = 0L;
            previousIterationsNumber = 0L;
        }
        else {
            if (currentIterationsNumber.equals(previousIterationsNumber)) {
                String errMsg = "Handler has called getter of DFL script, without having " +
                        "increased the iterations number first.";
                log.warn(errMsg);
                throw new IterationsStateFatalException(errMsg, algorithmKey);
            }
            else
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
}
