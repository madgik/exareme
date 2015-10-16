/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.graph;

import madgik.exareme.common.optimizer.OperatorBehavior;
import madgik.exareme.master.queryProcessor.optimizer.scheduler.OperatorAssignment;
import madgik.exareme.master.queryProcessor.optimizer.scheduler.SchedulingResult;
import madgik.exareme.utils.units.Metrics;
import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ExportToArtQL {
    private static Logger log = Logger.getLogger(ExportToArtQL.class);

    public static String exportToArtQL(ConcreteQueryGraph queryGraph, SchedulingResult schedule) {
        return exportToArtQL(queryGraph, schedule, null);
    }

    public static String exportToArtQL(ConcreteQueryGraph queryGraph, SchedulingResult schedule,
        LinkedList<String> containerNames) {
        StringBuilder containers = new StringBuilder(Metrics.KB);
        StringBuilder operators = new StringBuilder(Metrics.KB);
        StringBuilder buffers = new StringBuilder(Metrics.KB);
        StringBuilder links = new StringBuilder(Metrics.KB);

        LinkedHashMap<Integer, String> containerMap =
            new LinkedHashMap<>(schedule.getStatistics().getContainersUsed());

        LinkedHashMap<String, OperatorAssignment> operatorAssigmentMap =
            new LinkedHashMap<>(queryGraph.getNumOfOperators());

        for (OperatorAssignment oa : schedule.operatorAssigments) {
            operatorAssigmentMap.put(oa.operatorName, oa);
        }

    /* Operators */
        for (ConcreteOperator op : queryGraph.getOperators()) {
            OperatorAssignment oa = operatorAssigmentMap.get(op.operatorName);

      /* Add the containers */
            String containerName = "C" + oa.container;
            containerMap.put(oa.container, containerName);

            operators.append("instantiate " + oa.operatorName + " " + containerName + "(\n");
            if (oa.behavior == OperatorBehavior.store_and_forward) {
                operators
                    .append("\t'madgik.exareme.db.operatorLibrary.artificialWorkload.AWmimoSF',\n");
            } else {
                operators
                    .append("\t'madgik.exareme.db.operatorLibrary.artificialWorkload.AWmimoPL',\n");
            }
            for (LinkData link : op.outputDataArray) {
                operators.append("\tout='(" + link.name + "," + link.size_MB + "MB)',\n");
            }

            for (LinkData link : op.inputDataArray) {
                operators.append("\tin='(" + link.name + "," + link.size_MB + "MB)',\n");
            }
            operators.append("\tmemory='" + oa.memory + "',\n");
            operators.append("\tcpu='" + oa.cpuUtil + "',\n");
            operators.append("\ttime='" + op.runTime_SEC + "',\n");
            operators.append("\tcpuTime='" + (op.runTime_SEC * oa.cpuUtil) + "',\n");
            operators.append("\tbehavior='" + op.behavior + "');\n");

      /* TODO: All other parameters */
        }

    /* Buffers and Links */
        int bNum = 0;
        for (Link link : queryGraph.getLinks()) {
            OperatorAssignment fromOA = operatorAssigmentMap.get(link.from.operatorName);
            OperatorAssignment toOA = operatorAssigmentMap.get(link.to.operatorName);

      /* Create the buffer to producer */
            String fromContainerName = "C" + fromOA.container;
            String toContainerName = "C" + toOA.container;
            String bufferName = "b" + bNum;

            buffers.append("create " + bufferName + " " + fromContainerName + "('3');\n");
            bNum++;

      /* Create links */
            links.append(
                "connect " + fromContainerName + "(" + fromOA.operatorName + ", " + bufferName
                    + ");\n");
            links.append("connect " + toContainerName + "(" + bufferName + ", " + toOA.operatorName
                + ");\n");
        }

    /* Ignore Outputs */
        for (ConcreteOperator op : queryGraph.getOperators()) {
            //      if (queryGraph.fromLinkMap.get(op.opID).size() == 0) {
            if (queryGraph.getOutputLinks(op.opID).isEmpty()) {
                // this operator has no one connected to it's outputs
                OperatorAssignment oa = operatorAssigmentMap.get(op.operatorName);
                String containerName = "C" + oa.container;

                // irnore all outputs
                for (LinkData out : op.outputDataArray) {
                    String nullName = oa.operatorName + "_" + out.name + "_NULL";
                    operators.append("instantiate " + nullName + " " + containerName + "(\n");
                    operators.append("\t'madgik.exareme.db.operatorLibrary.builtin.Null',\n");
                    operators.append("\tin='(" + out.name + "," + out.size_MB + "MB)',\n");
                    operators.append("\tmemory='" + 0 + "',\n");
                    operators.append("\tmemoryPercentage='" + 0 + "',\n");
                    operators.append("\tcpu='" + 0.0 + "',\n");
                    operators.append("\ttime='" + 0.0 + "',\n");
                    operators.append("\tcpuTime='" + 0.0 + "',\n");
                    operators.append("\tbehavior='" + OperatorBehavior.pipeline + "');\n");

                    String bufferName = "b" + bNum;
                    buffers.append("create " + bufferName + " " + containerName + "('3');\n");
                    bNum++;

                    links.append(
                        "connect " + containerName + "(" + oa.operatorName + ", " + bufferName
                            + ");\n");

                    links.append(
                        "connect " + containerName + "(" + bufferName + ", " + nullName + ");\n");
                }
            }
        }

    /* Containers */
        int cNum = 0;
        for (Integer cId : containerMap.keySet()) {
            if (containerNames != null) {
                containers.append("container " + containerMap.get(cId) +
                    " ('" + containerNames.get(cNum) + "', 1099);\n");
            } else {
                containers.append("container " + containerMap.get(cId) +
                    " ('$C" + cNum + "', 1099);\n");
            }
            cNum++;
        }

        StringBuilder sb = new StringBuilder(4192);
        sb.append(containers + "\n");
        sb.append(operators + "\n");
        sb.append(buffers + "\n");
        sb.append(links + "\n");

        return sb.toString();
    }
}
