/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.rmi;

import madgik.exareme.common.optimizer.OperatorBehavior;
import madgik.exareme.master.engine.executor.remote.AdpDBArtPlanGenerator;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;
import madgik.exareme.master.queryProcessor.graph.Link;
import madgik.exareme.master.queryProcessor.graph.LinkData;

/**
 * @author herald
 */
public class ConcreteGraphFactory {

    private ConcreteGraphFactory() {
        throw new RuntimeException("Cannot create instances of this class");
    }

    public static ConcreteOperator createTableReader(String name) {
        return getOrCreateOperator("I_" + name, 20.0, 0.2, AdpDBArtPlanGenerator.SELECT_MEMORY,
                OperatorBehavior.pipeline);
    }

    public static ConcreteOperator createTableTransferReplicator(String name) {
        return getOrCreateOperator("TR_" + name, 20.0, 0.5, AdpDBArtPlanGenerator.SELECT_MEMORY,
                OperatorBehavior.pipeline);
    }

    public static ConcreteOperator createRunQuery(String name) {
        return getOrCreateOperator("R_" + name, 200.0, 1.0, AdpDBArtPlanGenerator.SELECT_MEMORY,
                OperatorBehavior.store_and_forward);
    }

    public static ConcreteOperator createBuildIndex(String name) {
        return getOrCreateOperator("IDX_" + name, 50.0, 1.0, AdpDBArtPlanGenerator.DM_MEMORY,
                OperatorBehavior.store_and_forward);
    }

    public static ConcreteOperator createDropIndex(String name) {
        return getOrCreateOperator("D_IDX_" + name, 10.0, 1.0, AdpDBArtPlanGenerator.DM_MEMORY,
                OperatorBehavior.store_and_forward);
    }

    public static ConcreteOperator createDropTable(String name) {
        return getOrCreateOperator("D_TBL_" + name, 10.0, 1.0, AdpDBArtPlanGenerator.DM_MEMORY,
                OperatorBehavior.store_and_forward);
    }

    public static Link createLink(String name, ConcreteOperator from, ConcreteOperator to) {
        // THe following very BAD. Just doing it now for the experiments.
        double size_MB = 1.0;
        if (from.operatorName.startsWith("I_")) {
            // Set data to be 2GB per partition.
            size_MB = 2000.0;
            // The following is temporary
            if (from.operatorName.startsWith("I_customer")) {
                size_MB = 50;
            }
            if (from.operatorName.startsWith("I_lineitem")) {
                size_MB = 1550;
            }
            if (from.operatorName.startsWith("I_nation")) {
                size_MB = 0.1;
            }
            if (from.operatorName.startsWith("I_orders")) {
                size_MB = 330;
            }
            if (from.operatorName.startsWith("I_part")) {
                size_MB = 50;
            }
            if (from.operatorName.startsWith("I_partsupp")) {
                size_MB = 240;
            }
            if (from.operatorName.startsWith("I_region")) {
                size_MB = 0.1;
            }
            if (from.operatorName.startsWith("I_supplier")) {
                size_MB = 24;
            }
        }
        LinkData linkData = new LinkData(name, size_MB);
        Link link = new Link(from, to, linkData);
        linkData.updateLinks(link);
        from.addOutputData(linkData);
        to.addInputData(linkData);
        return link;
    }

    private static ConcreteOperator getOrCreateOperator(String name, double runTime_SEC,
                                                        double cpuUtilization, int memory_MB, OperatorBehavior behavior) {
        return new ConcreteOperator(name, runTime_SEC, cpuUtilization, memory_MB, behavior);
    }
}
