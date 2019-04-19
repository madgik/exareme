/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler.event.scheduleQuery;


import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.utils.eventProcessor.Event;

/**
 * @author herald
 */
public class ScheduleEvent implements Event {

    private static final long serialVersionUID = 1L;
    public final AdpDBQueryExecutionPlan execPlan;
    public final AdpDBQueryID queryID;

    public ScheduleEvent(AdpDBQueryExecutionPlan execPlan, AdpDBQueryID queryID) {
        this.execPlan = execPlan;
        this.queryID = queryID;
    }
}
