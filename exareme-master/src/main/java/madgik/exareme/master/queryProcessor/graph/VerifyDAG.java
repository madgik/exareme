/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.graph;

/**
 * @author herald
 */
public class VerifyDAG {

    private static short WHITE = 0;
    private static short GREY = 1;
    private static short BLACK = 2;

    private VerifyDAG() {
    }

    public static boolean isDAG(ConcreteQueryGraph graph) {
        if (graph.getNumOfOperators() == 0) {
            return true;
        }
        if (graph.getNumOfLinks() == 0) {
            return true;
        }

        short[] status = new short[graph.getNumOfOperators()];
        for (ConcreteOperator op : graph.getOperators()) {
            if (status[op.opID] == WHITE) {
                if (visit(graph, op, status)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean visit(ConcreteQueryGraph graph, ConcreteOperator op, short[] status) {
        status[op.opID] = GREY;
        for (Link link : graph.getOutputLinks(op.opID)) {
            if (status[link.to.opID] == GREY) {
                return true;
            } else {
                if (status[link.to.opID] == WHITE) {
                    if (visit(graph, link.to, status)) {
                        return true;
                    }
                }
            }
        }
        status[op.opID] = BLACK;
        return false;
    }
}
