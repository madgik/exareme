/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.dag.Node;
import madgik.exareme.master.queryProcessor.decomposer.query.Column;
import madgik.exareme.master.queryProcessor.estimator.NodeCostEstimator;

/**
 * @author dimitris
 */
class CostEstimatorCaller {

    private NodeCostEstimator ce;

    public CostEstimatorCaller() {
        this.ce = new NodeCostEstimator();
    }

    public double getCostForOperator(Node n, Plan p, Node parent) {
        if (n.getOpCode() == Node.REPARTITION) {
            return 2;
        } else
            return 1.0;
     /*   if (n.getOpCode() == Node.REPARTITION) {
            return ce.estimateRepartition(parent, (Column) n.getObject(), n.getChildAt(0));
        } else if (n.getOpCode() == Node.JOIN) {
            return ce.estimateJoin(parent, (NonUnaryWhereCondition)n.getObject(), n.getChildAt(0), n.getChildAt(1));
        } else if (n.getOpCode() == Node.UNION) {
            return ce.estimateUnion(n);
        } else if (n.getOpCode() == Node.PROJECT) {
            return ce.estimateProjection(n);
        } else if (n.getOpCode() == Node.SELECT) {
            return ce.estimateFilter(parent, (Selection) n.getObject(), n.getChildAt(0));
        } else {
            return 0.0;
        }*/
    }

    double getCostForRepartition(Node e, Column c) {
        if (c == null) {
            return 0.0;
        }
        return 2.0;
        //return ce.estimateRepartition(e, c, new Node(Node.OR));
    }

    double getCostForOperator(Node o, SinglePlan e2Plan, Node e) {
        return 1.0;
    }

}
