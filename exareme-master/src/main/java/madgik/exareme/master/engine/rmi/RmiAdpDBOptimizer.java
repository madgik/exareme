/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.rmi;

import madgik.exareme.common.app.engine.AdpDBOperatorType;
import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBSelectOperator;
import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.common.schema.Statistics;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBOptimizer;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.master.engine.AdpDBStatusManager;
import madgik.exareme.master.engine.historicalData.AdpDBHistoricalQueryData;
import madgik.exareme.master.engine.parser.SemanticException;
import madgik.exareme.master.engine.queryCache.AdpDBQueryCache;
import madgik.exareme.master.queryProcessor.graph.*;
import madgik.exareme.master.queryProcessor.optimizer.scheduler.OperatorAssignment;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.worker.art.manager.ArtManager;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author herald
 */
public class RmiAdpDBOptimizer implements AdpDBOptimizer {
    private static final Logger log = Logger.getLogger(RmiAdpDBOptimizer.class);
    private final RmiAdpDBSelectOptimizer queryOptimizer;
    private final RmiAdpDBDMOptimizer dataManipulationOptimizer;
    private AdpDBStatusManager statusManager = null;
    private ArtManager manager = null;
    private AdpDBQueryCache queryCache = null;

    public RmiAdpDBOptimizer(AdpDBStatusManager statusManager, ArtManager manager,
                             AdpDBQueryCache queryCache) {
        this.statusManager = statusManager;
        this.manager = manager;
        this.queryCache = queryCache;
        queryOptimizer = new RmiAdpDBSelectOptimizer(statusManager, manager);
        dataManipulationOptimizer = new RmiAdpDBDMOptimizer(statusManager, manager);
    }

    static void mapPreviousPlanToCurrent(QueryScript script, Registry registry,
                                         AdpDBQueryExecutionPlan plan) {

    }

    static void postProcessGraph(InputData input, StateData state, AdpDBClientProperties props)
            throws SemanticException {
        // Remove the replicator operators if they are not needed
        for (ConcreteOperator cop : state.graph.getOperators()) {
            AdpDBSelectOperator dbOp = state.dbOps.get(cop.opID);
            if (dbOp.getType() != AdpDBOperatorType.tableUnionReplicator) {
                continue;
            }
            Collection<Link> outputs = state.graph.getOutputLinks(cop.opID);
            // Keep the replicator if the result must be saved.
            if (state.resultTableName != null) {
                if (dbOp.getOutputTables().contains(state.resultTableName)) {
                    continue;
                }
            }
            // Keep the table replication if the outputs are more than one.
            if (outputs.size() > 1) {
                continue;
            }
            // Remove the operator if the input is comming from only one operator and there is no output.
            if (outputs.isEmpty()) {
                Collection<Link> inputs = state.graph.getInputLinks(cop.opID);
                // Remove the replicator operators that are not connected to anyone (this happens when not
                // all partitions are used).
                if (inputs.isEmpty()) {
                    state.graph.removeOperator(cop);
                }
                if (inputs.size() == 1) {
                    Link in = inputs.iterator().next();
                    state.graph.removeLink(in);
                    state.graph.removeOperator(cop);
                }
                continue;
            }
            // NOTE: There is only one output here.
            // Remove the link
            Link out = outputs.iterator().next();
            ConcreteOperator consumer = out.to;
            state.graph.removeLink(out);
            ArrayList<Link> inputs = new ArrayList<>(state.graph.getInputLinks(cop.opID));
            for (Link in : inputs) {
                state.graph.removeLink(in);
                in.to = consumer;
                in.data.setToOpID(consumer.opID);
                state.graph.addLink(in);
            }
            // Update the db operator inputs
            AdpDBSelectOperator consumerDbOp = state.dbOps.get(consumer.opID);
            // Clear all inputs that are outputs of table union replicator operator
            for (String tableName : dbOp.getOutputTables()) {
                consumerDbOp.clearInputs(tableName, dbOp);
            }
            // Add all the inputs of table union replicator operator
            consumerDbOp.addToInputsAllInputsOf(dbOp);
            state.graph.removeOperator(cop);
        }
    }

    static void removeInputTableGravity(InputData input, StateData state,
                                        AdpDBClientProperties props) throws SemanticException {
        for (ConcreteOperator cop : state.graph.getOperators()) {
            AdpDBSelectOperator dbOp = state.dbOps.get(cop.opID);
            // Remove table input if the consumer is in the same container.
            if (dbOp.getType() != AdpDBOperatorType.tableInput) {
                continue;
            }
            OperatorAssignment tableOA = state.result.operatorAssigments.get(cop.opID);
            boolean deleteTableInput = true;
            ArrayList<Link> toDelete = new ArrayList<>();
            for (Link out : state.graph.getOutputLinks(cop.opID)) {
                OperatorAssignment consumerOA = state.result.operatorAssigments.get(out.to.opID);
                if (tableOA.container == consumerOA.container) {
                    toDelete.add(out);
                    // Update the db operator inputs
                    AdpDBSelectOperator consumerDbOp = state.dbOps.get(consumerOA.getOpID());
                    // Clear all inputs that are outputs of table operator
                    for (String tableName : dbOp.getOutputTables()) {
                        consumerDbOp.clearInputs(tableName, dbOp);
                    }
                    // Add all the outputs of table operator
                    consumerDbOp.addToInputsAllOutputsOf(dbOp);
                } else {
                    deleteTableInput = false;
                }
            }
            for (Link del : toDelete) {
                state.graph.removeLink(del);
                del.to.removeFileInputData(del.data.name + ".file");
            }
            if (deleteTableInput) {
                state.graph.removeOperator(cop);
            }
        }
    }

    static void validateGraph(InputData input, StateData state, AdpDBClientProperties props)
            throws SemanticException {
        log.debug("Checking if the db operators are properly updated ... ");
        HashSet<String> operatorNames = new HashSet<>();
        for (ConcreteOperator cop : state.graph.getOperators()) {
            AdpDBSelectOperator dbOp = state.dbOps.get(cop.opID);
            if (operatorNames.add(cop.operatorName) == false) {
                throw new SemanticException("Operator Already Exists: " + cop.operatorName);
            }

            //      if (dbOp.getInputTables().isEmpty()
            //              && dbOp.getQuery().getParsedSqlQuery().getInputDataPattern() != DataPattern.external) {
            //        throw new SemanticException("No input found: " + cop.operatorName);
            //      }

            if (dbOp.getType() != AdpDBOperatorType.runQuery) {
                continue;
            }
            int inSize = state.graph.getInputLinks(cop.opID).size();
            int dbInSize = dbOp.getTotalInputs();
            if (inSize != dbInSize) {
                throw new SemanticException(
                        cop.operatorName + " inputs do not match: " + inSize + " != " + dbInSize);
            }
            int outSize = state.graph.getOutputLinks(cop.opID).size();
            int dbOutSize = dbOp.getTotalOutputs();
            if (inSize != dbInSize) {
                throw new SemanticException(
                        cop.operatorName + " outputs do not match: " + outSize + " != " + dbOutSize);
            }
        }
    }

    static void estimateTimeAndData(InputData input, StateData state, AdpDBClientProperties props) {
    }

    static void annotateWithHistoricalData(ConcreteQueryGraph graph,
                                           AdpDBHistoricalQueryData queryData, ArrayList<AdpDBSelectOperator> dbOperators,
                                           AdpDBClientProperties props) {
        if (queryData == null) {
            return;
        }
        for (ConcreteOperator op : graph.getOperators()) {
            ConcreteOperator histOp = queryData.getOperator(op.operatorName);
            if (histOp != null) {
                op.runTime_SEC = histOp.runTime_SEC;
            } else {
                log.debug("Operator NOT found in history: '" + op.operatorName + "'");
            }
        }
        for (Link link : graph.getLinks()) {
            LinkData linkData =
                    queryData.getLinkData(link.data.name, link.from.operatorName, link.to.operatorName);
            if (linkData != null && linkData.size_MB > 0) {
                link.data.size_MB = linkData.size_MB;
            } else {
                log.debug(
                        "Link NOT found in history: '" + link.from.operatorName + "[" + link.data.name
                                + "]" + link.to.operatorName + "'");
            }
        }
    }

    static void addLocalFileData(ConcreteQueryGraph graph) {
        for (Link link : graph.getLinks()) {
            LocalFileData lfd = new LocalFileData(link.data.name + ".file", link.data.size_MB);
            link.from.addFileOutputData(lfd);
            link.to.addFileInputData(lfd);
        }
    }

    private static void logState(StateData state, boolean die) {
        for (AdpDBSelectOperator op : state.dbOps) {
            op.printStatistics(op.getOutputTables().iterator().next());
        }
        if (die) {
            throw new RuntimeException("Hohoho!");
        }
    }

    @Override
    public AdpDBQueryExecutionPlan optimize(QueryScript script, Registry registry, Statistics stats,
                                            AdpDBHistoricalQueryData queryData, AdpDBQueryID queryID, AdpDBClientProperties props,
                                            boolean schedule, boolean validate) throws RemoteException {
        AdpDBQueryExecutionPlan plan = queryCache.getPlan(script, registry);
        if (plan != null) {
            mapPreviousPlanToCurrent(script, registry, plan);
            return plan;
        }
        int numOfSelectQueries = script.getSelectQueries().size();
        int numOfDMQueries = script.getDMQueries().size();
        if (numOfSelectQueries == 0 && numOfDMQueries == 0) {
            throw new SemanticException("Query script is empty!");
        }
        if (numOfSelectQueries > 0 && numOfDMQueries > 0) {
            throw new SemanticException(
                    "Not supported yet: script with both queries and data manipulation");
        }
        // Create input and state
        int numContaieners = -1;
        if (props != null) {
            numContaieners = props.getMaxNumberOfContainers();
        }
        InputData input =
                new InputData(script, registry.getSchema(), stats, queryData, queryID, numContaieners,
                        schedule, validate);
        if (numOfSelectQueries > 0) {
            plan = queryOptimizer.optimize(input, props);
        } else {
            plan = dataManipulationOptimizer.optimize(input, props);
        }
        queryCache.addPlan(script, registry, plan);
        return plan;
    }
}
