/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler.event.querySuccess;

import madgik.exareme.common.app.engine.ExecuteQueryExitMessage;
import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.ContainerSessionStatistics;
import madgik.exareme.common.schema.Index;
import madgik.exareme.common.schema.PhysicalTable;
import madgik.exareme.master.engine.parser.SemanticException;
import madgik.exareme.master.engine.scheduler.QuerySchedulerState;
import madgik.exareme.master.engine.scheduler.QueryScriptState;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.utils.eventProcessor.EventHandler;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

/**
 * @author herald
 */
public class QuerySuccessEventHandler implements EventHandler<QuerySuccessEvent> {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(QuerySuccessEventHandler.class);
    private QuerySchedulerState state = null;

    public QuerySuccessEventHandler(QuerySchedulerState schedulerState) {
        this.state = schedulerState;
    }

    public void handle(QuerySuccessEvent event, EventProcessor proc) throws RemoteException {
        try {
            log.info("Query success: " + event.queryID.getQueryID());

            HashMap<String, String> tableSQLDefs = new HashMap<String, String>();
            List<ContainerSessionStatistics> containerStats =
                    event.status.getStatistics().getAdpEngineStatistics().containerStats;
            for (ContainerSessionStatistics css : containerStats) {
                for (ConcreteOperatorStatistics cos : css.operators) {
                    if (cos.getExitMessage() != null) {
                        ExecuteQueryExitMessage msg =
                                (ExecuteQueryExitMessage) cos.getExitMessage();
                        tableSQLDefs.put(msg.outTableInfo.getTableName(),
                                msg.outTableInfo.getSQLDefinition());
                    }
                }
            }

            log.info("Adding result tables to the schema ...");
            for (PhysicalTable pt : event.execPlan.getResultTables()) {
                if (pt.getTable().hasSQLDefinition() == false) {
                    log.info("Adding definition for table: " + pt.getName());
                    String sqlDef = tableSQLDefs.get(pt.getName());
                    if (sqlDef == null) {
                        throw new SemanticException("Table definition not found: " + pt.getName());
                    }

                    pt.getTable().setSqlDefinition(sqlDef);
                }
                Registry.getInstance(event.execPlan.getSchema().getDatabase()).addPhysicalTable(pt);
            }

            log.info("Adding indexes to the schema ...");
            for (Index idx : event.execPlan.getBuildIndexes()) {
                log.info("Adding index: " + idx.toString());
                Registry.getInstance(event.execPlan.getSchema().getDatabase()).addIndex(idx);
            }

            log.info("Updating schema ...");
            state.setState(QueryScriptState.success);
        } catch (Exception e) {
            state.queryScheduler.error(e);
        }
    }
}
