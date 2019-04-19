/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler.event.queryError;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.utils.eventProcessor.Event;

/**
 * @author herald
 */
public class QueryErrorEvent implements Event {

    private static final long serialVersionUID = 1L;

    public final Exception exception;
    public final AdpDBQueryID queryID;

    public QueryErrorEvent(Exception exception, AdpDBQueryID queryID) {
        this.exception = exception;
        this.queryID = queryID;
    }
}
