package madgik.exareme.master.engine.iterations.scheduler.events.phaseCompletion;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.master.engine.iterations.scheduler.events.IterationsEvent;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
public class PhaseCompletionEvent extends IterationsEvent {
    private AdpDBQueryID queryID;
    private AdpDBStatus queryStatus;

    public PhaseCompletionEvent(AdpDBQueryID queryID, AdpDBStatus status) {
        super(null);
        this.queryID = queryID;
        this.queryStatus = status;
    }

    public AdpDBQueryID getAdpDBQueryID() {
        return queryID;
    }

    public AdpDBStatus getQueryStatus() {
        return queryStatus;
    }
}
