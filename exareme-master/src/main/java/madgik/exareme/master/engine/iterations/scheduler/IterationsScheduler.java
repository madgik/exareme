package madgik.exareme.master.engine.iterations.scheduler;

import org.apache.log4j.Logger;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBStatus;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class IterationsScheduler {
    private static final Logger log = Logger.getLogger(IterationsScheduler.class);

    private IterationsSchedulerState iterationsSchedulerState;

    private IterationsScheduler() {
        iterationsSchedulerState = new IterationsSchedulerState(this);
    }

    // Singleton specific -----------------------------------------------------------------------
    private static final IterationsScheduler instance = new IterationsScheduler();

    public static IterationsScheduler getInstance() {
        return instance;
    }

    // Public API -------------------------------------------------------------------------------
    /**
     * Called by the {@link madgik.exareme.master.engine.iterations.handler.IterationsHandler} to
     * schedule a new iterative algorithm execution.
     *
     * @param algorithmKey the algorithm's key
     */
    public synchronized void scheduleNewAlgorithm(String algorithmKey) {
        if (log.isDebugEnabled())
            log.debug("Scheduling new iterative algorithm [" + algorithmKey + "]");
    }

    /**
     * Called via the {@link IterationsDispatcher} object to schedule an algorithm's phase
     * completion handling.
     *
     * @param queryID the currently completed query's ID
     * @param status  the currently completed query's status
     */
    public synchronized void scheduleAlgorithmUpdate(AdpDBQueryID queryID, AdpDBStatus status) {
        if (log.isDebugEnabled())
            log.debug("Query [" + queryID.getQueryID() + "] terminated, scheduling a phase " +
                    "completion event");
    }
}
