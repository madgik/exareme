/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.remote;

import madgik.exareme.common.app.engine.AdpDBDMOperator;
import madgik.exareme.common.app.engine.AdpDBSelectOperator;
import madgik.exareme.common.consts.AdpDBArtPlanGeneratorConsts;
import madgik.exareme.common.optimizer.OperatorBehavior;
import madgik.exareme.common.schema.BuildIndex;
import madgik.exareme.common.schema.DropIndex;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.master.engine.parser.SemanticException;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;
import madgik.exareme.master.queryProcessor.graph.Link;
import madgik.exareme.master.queryProcessor.optimizer.scheduler.OperatorAssignment;
import madgik.exareme.utils.encoding.Base64Util;
import madgik.exareme.utils.properties.AdpDBProperties;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.executionPlan.parser.expression.Operator;
import madgik.exareme.worker.art.executionPlan.parser.expression.OperatorLink;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;
import madgik.exareme.worker.art.executionPlan.parser.expression.PlanExpression;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * @author herald
 * @author John Chronis
 * @author Vaggelis Nikolopoulos
 */
public class AdpDBArtPlanGenerator {

    // Characteristics
    public static final OperatorBehavior SELECT_BEHAVIOR = OperatorBehavior.store_and_forward;
    public static final OperatorBehavior DM_BEHAVIOR = OperatorBehavior.store_and_forward;
    public static final int SELECT_MEMORY =
            AdpDBProperties.getAdpDBProps().getInt("db.engine.memory.select");
    public static final int DM_MEMORY =
            AdpDBProperties.getAdpDBProps().getInt("db.engine.memory.dm");
    public static final int BUFFER_SIZE_MB =
            AdpDBProperties.getAdpDBProps().getInt("db.engine.memory.bufferMB");

    private static final Logger log = Logger.getLogger(AdpDBArtPlanGenerator.class);

    private AdpDBArtPlanGenerator() {
    }

    public static void generateJsonPlan(List<String> containers, AdpDBQueryExecutionPlan plan,
                                        HashMap<String, String> categoryMessageMap, PlanExpression planExpression,
                                        AdpDBClientProperties props) throws IOException {
        if (plan.getQueryOperators().isEmpty() == false) {
            generateRunQueryPlan(containers, plan, categoryMessageMap, planExpression, props);
            return;
        }
        if (plan.getDataManipulationOperators().isEmpty() == false) {
            generateDataManipulationQueryPlan(containers, plan, categoryMessageMap, planExpression);
            return;
        }
        throw new SemanticException("Plan is empty");
    }

    private static void generateRunQueryPlan(List<String> containers, AdpDBQueryExecutionPlan plan,
                                             Map<String, String> categoryMessageMap, PlanExpression planExpression,
                                             AdpDBClientProperties props) throws IOException {
        log.debug("Generating Query plan ... ");
        // Create operators
        for (ConcreteOperator cop : plan.getGraph().getOperators()) {

            OperatorAssignment oa = plan.getSchedulingResult().operatorAssigments.get(cop.opID);
            String container = containers.get(oa.container);

            AdpDBSelectOperator dbOperator = plan.getQueryOperators().get(cop.opID);
            //            dbOperator.printStatistics(cop.operatorName);
            if (dbOperator == null) {
                throw new SemanticException("DB Operator not found for " + oa.operatorName);
            }

            String opSerialized = Base64Util.encodeBase64(dbOperator);
            String operatorCode = null;
            String operatorCategory = "";
            switch (dbOperator.getType()) {
                case runQuery:
                    operatorCode = AdpDBArtPlanGeneratorConsts.EXECUTE_SELECT;
                    operatorCategory =
                            AdpDBArtPlanGeneratorConsts.EXECUTE_SELECT_CATEGORY + dbOperator
                                    .getOutputTables().iterator().next();
                    break;
                case tableUnionReplicator:
                    operatorCode = AdpDBArtPlanGeneratorConsts.TABLE_UNION_REPLICATOR;
                    operatorCategory =
                            AdpDBArtPlanGeneratorConsts.TABLE_UNION_REPLICATOR_CATEGORY + dbOperator
                                    .getOutputTables().iterator().next();
                    break;
                case tableInput:
                    operatorCode = AdpDBArtPlanGeneratorConsts.TABLE_UNION_REPLICATOR;
                    operatorCategory =
                            AdpDBArtPlanGeneratorConsts.TABLE_UNION_REPLICATOR_CATEGORY + dbOperator
                                    .getOutputTables().iterator().next();
                    break;
            }
            categoryMessageMap.put(operatorCategory,
                    dbOperator.getQuery().getComments() + dbOperator.getQuery().getQuery());

            LinkedList<Parameter> parameters = new LinkedList<>();
            parameters
                    .add(new Parameter(OperatorEntity.BEHAVIOR_PARAM, SELECT_BEHAVIOR.toString()));
            parameters.add(new Parameter(OperatorEntity.CATEGORY_PARAM, operatorCategory));
            parameters
                    .add(new Parameter((OperatorEntity.MEMORY_PARAM), String.valueOf(SELECT_MEMORY)));

            planExpression.addOperator(new Operator(cop.operatorName,
                    AdpDBArtPlanGeneratorConsts.LIB_PATH + "." + operatorCode, parameters,
                    "{" + opSerialized + "};", container, null));
        }

        for (Link link : plan.getGraph().getLinks()) {
            OperatorAssignment from =
                    plan.getSchedulingResult().operatorAssigments.get(link.from.opID);
            OperatorAssignment to = plan.getSchedulingResult().operatorAssigments.get(link.to.opID);

            AdpDBSelectOperator fromDbOp = plan.getQueryOperators().get(from.getOpID());
            AdpDBSelectOperator toDbOp = plan.getQueryOperators().get(to.getOpID());

            String fromC = containers.get(from.container);
            String toC = containers.get(to.container);
            //            log.debug("FromC :" + fromC + " toC :" + toC);
            String table = fromDbOp.getOutputTables().iterator().next();
            BitSet common = AdpDBSelectOperator.findCommonPartitions(fromDbOp, toDbOp, table);

            //            log.debug(
            //                "Partitions in common : ("
            //                    + from.operatorName + " -> "
            //                    + to.operatorName + ") :"
            //                    + common + " ( " + common.cardinality() +" )");
            if (common.cardinality() > 1) {
                throw new SemanticException("Operators do not have only one partition in common!");
            }
            int commonPartition = -1;
            if (common.cardinality() == 0) {
                if (props.isTreeEnabled() == false) {
                    throw new SemanticException(
                            "Operators do not have only one partition in common!");
                }
                List<Integer> outputs = fromDbOp.getOutputPartitions(table);
                if (outputs.size() != 1) {
                    throw new SemanticException("Tree reduction error!");
                }
                commonPartition = outputs.get(0);
            } else {
                commonPartition = common.nextSetBit(0);
            }


            // Connect the operators
            LinkedList<Parameter> parameters = new LinkedList<>();
            parameters.add(new Parameter(AdpDBArtPlanGeneratorConsts.BUFFER_TABLE_NAME, table));
            parameters.add(new Parameter(AdpDBArtPlanGeneratorConsts.BUFFER_TABLE_PART_NAME,
                    String.valueOf(commonPartition)));
            for (Operator op : planExpression.operatorList) {
                //                log.debug("**DEBUG-- " + op.operatorName);
                if (op.operatorName == from.operatorName) {
                    op.addLinkParam(to.operatorName, parameters);
                }
            }
            //            log.debug("adding Operator link, " + from.operatorName + " -> " + to.operatorName);
            planExpression.addOperatorConnect(
                    new OperatorLink(from.operatorName, to.operatorName, fromC, parameters));


        }

        //        log.debug(planExpression.toString());
    }

    private static void generateDataManipulationQueryPlan(List<String> containers,
                                                          AdpDBQueryExecutionPlan plan, Map<String, String> categoryMessageMap,
                                                          PlanExpression planExpression) throws IOException {
        log.debug("Generating DM plan ... ");
        for (ConcreteOperator cop : plan.getGraph().getOperators()) {
            OperatorAssignment oa = plan.getSchedulingResult().operatorAssigments.get(cop.opID);
            String container = containers.get(oa.container);

            AdpDBDMOperator dmOperator = plan.getDataManipulationOperators().get(cop.opID);
            if (dmOperator == null) {
                throw new SemanticException("DM Operator not found for " + oa.operatorName);
            }

            String opSerialized = Base64Util.encodeBase64(dmOperator);
            String operatorCategory = "";
            switch (dmOperator.getType()) {
                case buildIndex:
                    operatorCategory =
                            AdpDBArtPlanGeneratorConsts.BUILD_INDEX_CATEGORY + ((BuildIndex) dmOperator
                                    .getDMQuery()).getIndexName();
                    break;
                case dropIndex:
                    operatorCategory =
                            AdpDBArtPlanGeneratorConsts.DROP_INDEX_CATEGORY + ((DropIndex) dmOperator
                                    .getDMQuery()).getIndexName();
                    break;
                case dropTable:
                    operatorCategory =
                            AdpDBArtPlanGeneratorConsts.DROP_TABLE_CATEGORY + dmOperator.getDMQuery()
                                    .getTable();
                    break;
            }

            categoryMessageMap.put(operatorCategory,
                    dmOperator.getDMQuery().getComments() + dmOperator.getDMQuery().getQuery());

            LinkedList<Parameter> parameters = new LinkedList<>();
            parameters.add(new Parameter(OperatorEntity.BEHAVIOR_PARAM, DM_BEHAVIOR.toString()));
            parameters.add(new Parameter(OperatorEntity.CATEGORY_PARAM, operatorCategory));
            parameters.add(new Parameter(OperatorEntity.MEMORY_PARAM, String.valueOf(DM_MEMORY)));
            planExpression.addOperator(new Operator(cop.operatorName,
                    AdpDBArtPlanGeneratorConsts.LIB_PATH + "." + AdpDBArtPlanGeneratorConsts.EXECUTE_DM,
                    parameters, "{" + opSerialized + "};", container, null));

        }
    }

}
