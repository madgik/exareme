/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.estimator.db;

import madgik.exareme.master.queryProcessor.estimator.histogram.Histogram;

/**
 * @author jim
 */
public class AttrInfo {
    private String attrName;        //attribute name
    private Histogram histogram;    //histogram for this column
    private int attrLength;         //length of attribute in bytes

    /*constructor*/
    public AttrInfo(String attrName, Histogram histogram, int attrLength) {
        this.attrName = attrName;
        this.histogram = histogram;
        this.attrLength = attrLength;
    }

    /*copy constructor*/
    public AttrInfo(AttrInfo attr) {
        this.attrName = attr.getAttrName();
        this.histogram = new Histogram(attr.getHistogram());
        this.attrLength = attr.getAttrLength();
    }

    /*getters and setters*/
    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public void setHistogram(Histogram histogram) {
        this.histogram = histogram;
    }

    public int getAttrLength() {
        return attrLength;
    }

    public void setAttrLength(int attrLength) {
        this.attrLength = attrLength;
    }

    /*interface methods*/
    public double density() {
        return this.histogram.distinctValues() / this.histogram.cardinality();
    }

    /*standard methods*/
    @Override
    public String toString() {
        return "Attribute{" + "attrName=" + attrName + ", histogram=" + histogram + ", attrLength="
                + attrLength + '}';
    }


}
