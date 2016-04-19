package madgik.exareme.master.client.rmi;

import madgik.exareme.common.app.engine.*;
import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.ContainerSessionStatistics;
import madgik.exareme.common.schema.Index;
import madgik.exareme.common.schema.PhysicalTable;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.master.engine.parser.SemanticException;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.utils.chart.TimeFormat;
import madgik.exareme.utils.chart.TimeUnit;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * @author alex
 */
public class RmiAdpDBClientQueryStatus implements AdpDBClientQueryStatus {
    private final static Logger log = Logger.getLogger(AdpDBClientQueryStatus.class);

    private AdpDBClientProperties properties;
    private AdpDBQueryExecutionPlan plan;
    private AdpDBStatus status;
    private String lastStatus;
    private String resultTableName;
    private TimeFormat timeF;
    private boolean finished;

    public RmiAdpDBClientQueryStatus(AdpDBQueryID queryId, AdpDBClientProperties properties,
        AdpDBQueryExecutionPlan plan, AdpDBStatus status) {
        this.properties = properties;
        this.plan = plan;
        this.status = status;
        this.resultTableName = null;
        this.lastStatus = null;
        this.timeF = new TimeFormat(TimeUnit.min);
        this.finished = false;
    }

    @Override public boolean hasFinished() throws RemoteException {
        if (finished) {
            return true;
        }
        if (status.hasFinished() == false && status.hasError() == false) {
            return false;
        }
        updateRegistry();
        finished = true;
        return true;
    }

    @Override public AdpDBQueryID getQueryID() {
        return plan.getQueryID();
    }

    @Override public String getStatus() throws RemoteException {
        lastStatus = status.getStatistics().toString();
        return lastStatus;
    }

    @Override public String getStatusIfChanged() throws RemoteException {
        String currentStatus = status.getStatistics().toString();
        if (!currentStatus.equals(lastStatus)) {
            lastStatus = currentStatus;
            return lastStatus;
        }
        return null;
    }

    @Override public boolean hasError() throws RemoteException {
        if (!finished)
            return false;
        return status.hasError();
    }

    @Override public String getError() throws RemoteException {
        return status.getLastException() == null ? "exception-is-empty" :status.getLastException().toString();
    }

    @Override public String getExecutionTime() throws RemoteException {
        long startTime = status.getStatistics().getAdpEngineStatistics().startTime;
        long endTime = status.getStatistics().getAdpEngineStatistics().endTime;
        return String.format("%s m", timeF.format(endTime - startTime));
    }

    @Override public void close() throws RemoteException {
        status.stopExecution();
    }

    @Override public void registerListener(AdpDBQueryListener listener) throws RemoteException {
        status.registerListener(listener);
    }

    private void updateRegistry() throws RemoteException {
        if (status.hasError() == false) {

            // update registry
            HashMap<String, String> resultTablesSQLDef = new HashMap<>();
            // containers
            for (ContainerSessionStatistics containerStat : status.getStatistics()
                .getAdpEngineStatistics().containerStats) {
                // operators
                for (ConcreteOperatorStatistics operatorStatistics : containerStat.operators) {
                    ExecuteQueryExitMessage exitMessage =
                        (ExecuteQueryExitMessage) operatorStatistics.getExitMessage();
                    // get
                    if (exitMessage != null) {
                        resultTablesSQLDef.put(exitMessage.outTableInfo.getTableName(),
                            exitMessage.outTableInfo.getSQLDefinition());
                    }
                }
            }

            //      log.debug(
            //          ConsoleFactory.getDefaultStatisticsFormat().format(
            //              status.getStatistics().getAdpEngineStatistics(),
            //              plan.getGraph(),
            //              RmiAdpDBSelectScheduler.runTimeParams
            //          )
            //      );

            // Adding result tables, indexes to schema.
            Registry registry = Registry.getInstance(properties.getDatabase());
            for (PhysicalTable resultTable : plan.getResultTables()) {
                if (resultTable.getTable().hasSQLDefinition() == false) {
                    if (resultTablesSQLDef.containsKey(resultTable.getName()) == false) {
                        throw new SemanticException(
                            "Table definition not found: " + resultTable.getName());
                    }
                    resultTableName = resultTable.getName();
                    String sqlDef = resultTablesSQLDef.get(resultTable.getName());
                    resultTable.getTable().setSqlDefinition(sqlDef);
                }
                registry.addPhysicalTable(resultTable);
            }

            for (Index index : plan.getBuildIndexes()) {
                registry.addIndex(index);
            }

            //Drop tables
            for (AdpDBDMOperator dmOP : plan.getDataManipulationOperators()) {
                if (dmOP.getType().equals(AdpDBOperatorType.dropTable)) {
                    registry.removePhysicalTable(dmOP.getDMQuery().getTable());
                }
            }
            log.debug("Registry updated.");
        }
    }
}
