package madgik.exareme.master.client.rmi;

import madgik.exareme.common.app.engine.*;
import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.ContainerSessionStatistics;
import madgik.exareme.common.schema.Index;
import madgik.exareme.common.schema.PhysicalTable;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.connector.DataSerialization;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.master.engine.parser.SemanticException;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.utils.chart.TimeFormat;
import madgik.exareme.utils.chart.TimeUnit;
import org.apache.log4j.Logger;

import java.io.InputStream;
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
    private TimeFormat timeF;
    private boolean finished;
    private InputStream result;

    public RmiAdpDBClientQueryStatus(AdpDBQueryID queryId, AdpDBClientProperties properties,
                                     AdpDBQueryExecutionPlan plan, AdpDBStatus status) {
        this.properties = properties;
        this.plan = plan;
        this.status = status;
        this.lastStatus = null;
        this.timeF = new TimeFormat(TimeUnit.min);
        this.finished = false;
        result = null;
    }


    /**
     * Checks if the query has finished or not by checking the AdpDBStatus.
     * In case it is finished it saves that information to the finished variable.
     **/
    @Override
    public boolean hasFinished() throws RemoteException {
        if (finished)
            return true;

        if (!status.hasFinished() && !status.hasError())
            return false;

        finished = true;
        return true;
    }

    @Override
    public AdpDBQueryID getQueryID() {
        return plan.getQueryID();
    }

    @Override
    public String getStatus() throws RemoteException {
        lastStatus = status.getStatistics().toString();
        return lastStatus;
    }

    @Override
    public boolean hasError() throws RemoteException {
        if (!finished)
            return false;
        return status.hasError();
    }

    @Override
    public String getError() throws RemoteException {
        return status.getLastException() == null ? "exception-is-empty" : status.getLastException().toString();
    }

    @Override
    public String getExecutionTime() throws RemoteException {
        long startTime = status.getStatistics().getAdpEngineStatistics().startTime;
        long endTime = status.getStatistics().getAdpEngineStatistics().endTime;
        return String.format("%s m", timeF.format(endTime - startTime));
    }

    @Override
    public void close() throws RemoteException {
        status.stopExecution();
    }

    @Override
    public void registerListener(AdpDBQueryListener listener) throws RemoteException {
        status.registerListener(listener);
    }

    @Override
    public InputStream getResult() throws RemoteException {
        return getResult(DataSerialization.ldjson);
    }

    /**
     *  Returns the result of a query.
     *
     *  It updates the registry first in order to bring the output of the table locally.
     *  Then the result can be read from the result returned. If the registry is not
     *  updated the result will have no data inside.
     *
     * @param ds is the format of the result
     * @return  the result that is saved in the first, usually the only one, output table
     * @throws RemoteException
     */
    @Override
    public InputStream getResult(DataSerialization ds) throws RemoteException {
        if (result == null) {
            updateRegistry();
            result = new RmiAdpDBClient(AdpDBManagerLocator.getDBManager(), properties)
                    .readTable(plan.getResultTables().get(0).getName(), ds);
        }
        return result;
    }

    /**
     * Fetches a table from the DBManager to the registry so it can be read afterwards.
     * If the table is in a different node it will be fetched from there.
     *
     * @throws RemoteException
     */
    private void updateRegistry() throws RemoteException {

        if (!status.hasError()) {
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

            // Adding result tables, indexes to schema.
            Registry registry = Registry.getInstance(properties.getDatabase());
            for (PhysicalTable resultTable : plan.getResultTables()) {
                if (!resultTable.getTable().hasSQLDefinition()) {
                    if (!resultTablesSQLDef.containsKey(resultTable.getName())) {
                        throw new SemanticException(
                                "Table definition not found: " + resultTable.getName());
                    }
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
