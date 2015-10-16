/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler.event.optimize;

import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.master.engine.AdpDBOptimizer;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.master.engine.parser.AdpDBParser;
import madgik.exareme.master.engine.scheduler.QuerySchedulerState;
import madgik.exareme.master.engine.scheduler.QueryScriptState;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.utils.eventProcessor.EventHandler;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class OptimizeEventHandler implements EventHandler<OptimizeEvent> {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(OptimizeEventHandler.class);
    private QuerySchedulerState state = null;

    public OptimizeEventHandler(QuerySchedulerState schedulerState) {
        this.state = schedulerState;
    }

    @Override public void handle(OptimizeEvent event, EventProcessor proc) throws RemoteException {
        try {
            QueryScript script = event.queryScript;
            if (script == null) {
                log.debug("Parse query: " + event.queryID.getQueryID());
                state.setState(QueryScriptState.initializing);
                AdpDBParser parser = new AdpDBParser(event.schema.getDatabase());
                script = parser.parse(event.queryScriptString,
                    Registry.getInstance(event.schema.getDatabase()));
            }

            log.debug("Optimize query: " + event.queryID.getQueryID());
            state.setState(QueryScriptState.optimizing);
            AdpDBOptimizer optimizer = state.manager.getAdpDBOptimizer();
            AdpDBQueryExecutionPlan execPlan = optimizer
                .optimize(script, Registry.getInstance(event.schema.getDatabase()), event.stats,
                    event.queryData, event.queryID, null, true  /* schedule */,
                    true  /* validate */);
            state.setExecPlan(execPlan);
            log.debug("Schedule the query for execution: " + event.queryID.getQueryID());
            state.queryScheduler.schedule(execPlan);
            state.setState(QueryScriptState.ready);
        } catch (Exception e) {
            state.queryScheduler.error(e);
        }
    }
}
