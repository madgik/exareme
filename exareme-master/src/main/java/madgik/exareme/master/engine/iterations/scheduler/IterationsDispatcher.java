package madgik.exareme.master.engine.iterations.scheduler;

import org.apache.log4j.Logger;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.app.engine.AdpDBStatus;

/**
 * Listener that submits
 * {@link madgik.exareme.master.engine.iterations.scheduler.events.phaseCompletion.PhaseCompletionEvent}
 * to the {@link IterationsScheduler}.
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class IterationsDispatcher implements AdpDBQueryListener {
    private static final Logger log = Logger.getLogger(IterationsDispatcher.class);
    private IterationsScheduler iterationsScheduler;

    private IterationsDispatcher() {
    }

    // Singleton related ------------------------------------------------------------------------
    private static final IterationsDispatcher instance = new IterationsDispatcher();

    public static IterationsDispatcher getInstance(IterationsScheduler iterationsScheduler) {
        if (instance.iterationsScheduler == null)
            instance.iterationsScheduler = iterationsScheduler;
        return instance;
    }

    // AdpDBQueryListener interface compliance --------------------------------------------------

    @Override
    public void statusChanged(AdpDBQueryID queryID, AdpDBStatus status) {
    }

    @Override
    public void terminated(AdpDBQueryID queryID, AdpDBStatus status) {
        iterationsScheduler.scheduleAlgorithmUpdate(queryID, status);
    }
}
