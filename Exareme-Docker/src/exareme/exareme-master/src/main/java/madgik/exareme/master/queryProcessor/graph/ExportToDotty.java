///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.master.queryProcessor.graph;
//
//import java.util.HashMap;
//import java.util.List;
//import madgik.exareme.master.queryProcessor.optimizer.scheduler.OperatorAssignment;
//import madgik.exareme.master.queryProcessor.optimizer.scheduler.SchedulingResult;
//import madgik.exareme.utils.association.Pair;
//import org.apache.log4j.Logger;
//
///**
// *
// * @author Herald Kllapi <br>
// *      University of Athens /
// *      Department of Informatics and Telecommunications.
// * @since 1.0
// */
//public class ExportToDotty {
//
//  private static Logger log = Logger.getLogger(ExportToDotty.class);
//
//  public static String exportToDotty(ConcreteQueryGraph graph) {
//    StringBuilder sb = new StringBuilder(100);
//    sb.append("digraph G { \n");
//    for (Link link : graph.getLinks()) {
//      sb.append("\t").append(link.from.opID).append(" -> ").append(link.to.opID).append("\n");
//    }
//    sb.append("}");
//    return sb.toString();
//  }
//
//  public static String exportToDotty(ConcreteQueryGraph graph,
//                                     SchedulingResult schedule) {
//    HashMap<Integer, OperatorAssignment> assignments = new HashMap<Integer, OperatorAssignment>();
//    for (OperatorAssignment oa : schedule.operatorAssigments) {
//      if (oa == null) {
//        continue;
//      }
//      assignments.put(oa.getOpID(), oa);
//    }
//    StringBuilder sb = new StringBuilder(100);
//    sb.append("digraph G { \n");
//    for (Link link : graph.getLinks()) {
//      sb.append("\t").append(link.from.opID).append(" -> ").append(link.to.opID).append("\n");
//    }
//    for (ConcreteOperator cop : graph.getOperators()) {
//      OperatorAssignment oa = assignments.get(cop.opID);
//      if (oa == null) {
//        continue;
//      }
//      sb.append("\t" + oa.getOpID() +
//              " [label = \"" + oa.getOpID() + "/" + oa.container + "\"];" + "\n");
//
//    }
//    sb.append("}");
//    return sb.toString();
//  }
//
//  public static String exportToDotty(ConcreteQueryGraph graph,
//                                     List<Pair<Integer, Double>> rankings) {
//    StringBuilder sb = new StringBuilder(100);
//
//    sb.append("digraph G { \n");
//
//    sb.append("\t" + "node [style = filled]");
//
//    for (Link link : graph.getLinks()) {
//      sb.append("\t" + link.from.opID + " -> " + link.to.opID + "\n");
//    }
//
//    double max = 0.0;
//
//    for (Pair<Integer, Double> metrics : rankings) {
//      double s = metrics.b;
//      if (max < s) {
//        max = s;
//      }
//    }
//
//    for (Pair<Integer, Double> metrics : rankings) {
//      float weight = 1.0f - (float) (metrics.b / max);
//      int weight255 = (int)(weight * 255);
//      String hex = Integer.toHexString(weight255);
//      String hex2;
//
//      switch(hex.length()) {
//        case 1:
//          hex2 = "0" + hex;
//          break;
//        case 2:
//          hex2 = hex;
//          break;
//        default:
//          hex2 = hex.substring(0, 1);
//          break;
//      }
//
//      sb.append("\t" + metrics.a +
//              " [label = \"\", fillcolor=\"#" + hex2 + " " + hex2 + " " + hex2 + "\"];" + "\n");
//    }
//
//    sb.append("}");
//
//    return sb.toString();
//  }
//
//  private ExportToDotty() {
//  }
//}

/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.graph;

import madgik.exareme.master.queryProcessor.optimizer.scheduler.OperatorAssignment;
import madgik.exareme.master.queryProcessor.optimizer.scheduler.SchedulingResult;
import madgik.exareme.utils.association.Pair;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ExportToDotty {

    private static Logger log = Logger.getLogger(ExportToDotty.class);

    private ExportToDotty() {
    }

    public static String exportToDotty(ConcreteQueryGraph graph) {
        return exportToDotty(graph, false);
    }

    public static String exportToDotty(ConcreteQueryGraph graph, boolean useNames) {
        StringBuilder sb = new StringBuilder(100);
        sb.append("digraph G { \n");
        for (Link link : graph.getLinks()) {
            String from;
            String to;
            if (useNames) {
                from = link.from.operatorName;
                to = link.to.operatorName;
            } else {
                from = "" + link.from.opID;
                to = "" + link.to.opID;
            }
            sb.append("\t").append(from).append(" -> ").append(to).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    public static String exportToDotty(ConcreteQueryGraph graph, SchedulingResult schedule) {
        HashMap<Integer, OperatorAssignment> assignments =
                new HashMap<Integer, OperatorAssignment>();
        for (OperatorAssignment oa : schedule.operatorAssigments) {
            if (oa == null) {
                continue;
            }
            assignments.put(oa.getOpID(), oa);
        }
        StringBuilder sb = new StringBuilder(100);
        sb.append("digraph G { \n");
        for (Link link : graph.getLinks()) {
            sb.append("\t").append(link.from.opID).append(" -> ").append(link.to.opID).append("\n");
        }
        for (ConcreteOperator cop : graph.getOperators()) {
            OperatorAssignment oa = assignments.get(cop.opID);
            if (oa == null) {
                continue;
            }
            sb.append("\t" + oa.getOpID() +
                    " [label = \"" + oa.getOpID() + "/" + oa.container + "\"];" + "\n");

        }
        sb.append("}");
        return sb.toString();
    }

    public static String exportToDotty(ConcreteQueryGraph graph,
                                       List<Pair<Integer, Double>> rankings) {
        StringBuilder sb = new StringBuilder(100);

        sb.append("digraph G { \n");

        sb.append("\t" + "node [style = filled]");

        for (Link link : graph.getLinks()) {
            sb.append("\t" + link.from.opID + " -> " + link.to.opID + "\n");
        }

        double max = 0.0;

        for (Pair<Integer, Double> metrics : rankings) {
            double s = metrics.b;
            if (max < s) {
                max = s;
            }
        }

        for (Pair<Integer, Double> metrics : rankings) {
            float weight = 1.0f - (float) (metrics.b / max);
            int weight255 = (int) (weight * 255);
            String hex = Integer.toHexString(weight255);
            String hex2;

            switch (hex.length()) {
                case 1:
                    hex2 = "0" + hex;
                    break;
                case 2:
                    hex2 = hex;
                    break;
                default:
                    hex2 = hex.substring(0, 1);
                    break;
            }

            sb.append("\t" + metrics.a +
                    " [label = \"\", fillcolor=\"#" + hex2 + " " + hex2 + " " + hex2 + "\"];" + "\n");
        }

        sb.append("}");

        return sb.toString();
    }
}
