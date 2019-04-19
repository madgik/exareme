/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.estimator.db;

import madgik.exareme.master.queryProcessor.estimator.histogram.Bucket;
import madgik.exareme.master.queryProcessor.estimator.histogram.Histogram;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author jim
 */
public class RelInfo {
    public static final int DEFAULT_NUM_PARTITIONS = 0;
    private String relName;
    private Map<String, AttrInfo> attrIndex;
    private double numberOfTuples;
    private int tupleLength;
    private int numberOfPartitions;
    private Set<String> hashAttr;

    /*constructor*/
    public RelInfo(String relName, Map<String, AttrInfo> attrIndex, double numberOfTuples,
                   int tupleLength, int numberOfPartitions, Set<String> hashAttr) {

        this.relName = relName;
        this.attrIndex = attrIndex;
        this.numberOfTuples = numberOfTuples;
        this.tupleLength = tupleLength;
        this.numberOfPartitions = numberOfPartitions;
        this.hashAttr = hashAttr;
    }

    /*copy constructor*/
    public RelInfo(RelInfo rel) {
        this.relName = rel.getRelName();
        this.numberOfTuples = rel.getNumberOfTuples();
        this.tupleLength = rel.getTupleLength();
        this.numberOfPartitions = rel.getNumberOfPartitions();
        this.attrIndex = new HashMap<String, AttrInfo>();
        this.hashAttr = new HashSet<String>(rel.getHashAttr());

        for (Map.Entry<String, AttrInfo> e : rel.getAttrIndex().entrySet()) {
            this.attrIndex.put(e.getKey(), new AttrInfo(e.getValue()));
        }
    }

    /*getters and seters*/
    public String getRelName() {
        return relName;
    }

    public void setRelName(String relName) {
        this.relName = relName;
    }

    public Map<String, AttrInfo> getAttrIndex() {
        return attrIndex;
    }

    public void setAttrIndex(Map<String, AttrInfo> attrIndex) {
        this.attrIndex = attrIndex;
    }

    public double getNumberOfTuples() {
        return numberOfTuples;
    }

    public void setNumberOfTuples(double numberOfTuples) {
        this.numberOfTuples = numberOfTuples;
    }

    public int getTupleLength() {
        return tupleLength;
    }

    public void setTupleLength(int tupleLength) {
        this.tupleLength = tupleLength;
    }

    public int getNumberOfPartitions() {
        return numberOfPartitions;
    }

    public void setNumberOfPartitions(int numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
    }

    public Set<String> getHashAttr() {
        return hashAttr;
    }

    public void setHashAttr(Set<String> hashAttr) {
        this.hashAttr = hashAttr;
    }


    public double totalRelSize() {
        return this.numberOfTuples * this.tupleLength;
    }


    /*standard methods*/
    @Override
    public String toString() {
        return "Relation{" + "relName=" + relName + ", attrIndex=" + attrIndex +
                ", numberOfTuples=" + numberOfTuples + ", tupleLength=" +
                tupleLength + ", numberOfPartitions=" + numberOfPartitions + ", hashAttr=" + hashAttr
                + '}';
    }

    /*interface methods*/
    //it keeps only essential projected attributes an eliminates the others. It also dicreases the tupleLength.
    public void eliminteRedundantAttributes(Set<String> projections) {
        Set<String> remove = new HashSet<String>();

        for (String k : this.attrIndex.keySet()) {
            if (!projections.contains(k))
                remove.add(k);
        }

        for (String k : remove)
            this.attrIndex.remove(k);

        //eliminate redudancy from hashing attributes set
        //        for(String k : remove){
        //            if(this.hashAttr.contains(k))
        //                this.hashAttr.remove(k);
        //        }

        refreshTupleLength();
    }


    public void adjustRelation(String attrName, Histogram h) {
        //difference of the number of records between two tables
        double recTableDiff;
        //percentage of general increasing/decreasing of the rest histograms
        double percentage;
        double numOfTuples = h.numberOfTuples();

        for (AttrInfo attr : this.attrIndex.values()) {
            if (!attr.getAttrName().equals(attrName)) {
                recTableDiff = numOfTuples - attr.getHistogram().numberOfTuples();
                percentage = recTableDiff / attr.getHistogram().numberOfTuples();

                if (h.getBucketIndex().isEmpty()) {
                    for (AttrInfo attr2 : this.attrIndex.values()) {
                        attr2.getHistogram().getBucketIndex()
                                .clear();//////////////////change to default histogram
                    }
                    break;
                } else {
                    for (Map.Entry<Double, Bucket> entry : attr.getHistogram().
                            getBucketIndex().entrySet()) {
                        if (!entry.getValue().equals(Bucket.FINAL_HISTOGRAM_BUCKET))
                            entry.getValue().setFrequency(
                                    entry.getValue().getFrequency() + percentage * entry.getValue()
                                            .getFrequency());
                    }
                }
            }
        }

        //fix new numberOfTUples variable when the histograms changes
        this.numberOfTuples = numOfTuples;
    }


    /*private-helper methods*/
    private void refreshTupleLength() {
        int tl = 0;

        for (AttrInfo a : this.attrIndex.values()) {
            tl += a.getAttrLength();
        }

        this.tupleLength = tl;
    }


}
