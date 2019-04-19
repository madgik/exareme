/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.estimator;

import madgik.exareme.master.queryProcessor.estimator.db.RelInfo;

/**
 * @author jim
 */
public final class NodeInfo {
    //    private final int nodeId;
    private double numberOfTuples;
    private double tupleLength; //in bytes
    private double responseTimeEstimation;
    private RelInfo resultRel;

    public NodeInfo(int nodeId, double numberOfTuples, double tupleLength, RelInfo resultRel) {
        //        this.nodeId = nodeId;
        this.numberOfTuples = numberOfTuples;
        this.tupleLength = tupleLength;
        this.resultRel = resultRel;
    }

    public NodeInfo() {

    }

    /*getters and setters*/
    //    public int getNodeId() {
    //        return nodeId;
    //    }

    public double getNumberOfTuples() {
        return numberOfTuples;
    }

    public double getTupleLength() {
        return tupleLength;
    }

    public double getResponseTimeEstimation() {
        return responseTimeEstimation;
    }

    public void setResponseTimeEstimation(double responseTimeEstimation) {
        this.responseTimeEstimation = responseTimeEstimation;
    }

    public void setNumberOfTuples(double numberOfTuples) {
        this.numberOfTuples = numberOfTuples;
    }

    public void setTupleLength(double tupleLength) {
        this.tupleLength = tupleLength;
    }

    public RelInfo getResultRel() {
        return resultRel;
    }

    public void setResultRel(RelInfo resultRel) {
        this.resultRel = resultRel;
    }


    /*interface methods*/
    public double outputRelSize() {
        return this.numberOfTuples * this.tupleLength;
    }
}
