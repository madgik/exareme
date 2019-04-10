/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.estimator.histogram;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author jim
 */
public final class Bucket {
    public static final double SINGLE_BUCKET_DIFF_VAL = 1.0;
    public static final double EMPTY_BUCKET_FREQ = 0.0;
    public static final double EMPTY_BUCKET_DIFF_VAL = 0.0;
    public static final double TRANSPARENT_FREQ = 1.0;
    public static final double MIN_BUCKET_WIDTH = Double.MIN_NORMAL;
    public static final double MAX_BUCKET_DIFF_VALS = Double.POSITIVE_INFINITY;
    public static final Bucket FINAL_HISTOGRAM_BUCKET =
            new Bucket(EMPTY_BUCKET_FREQ, EMPTY_BUCKET_DIFF_VAL);
    public static final Bucket HOLE_BUCKET = new Bucket(EMPTY_BUCKET_FREQ, SINGLE_BUCKET_DIFF_VAL);
    public static final Bucket EMPTY_BUCKET = new Bucket(EMPTY_BUCKET_FREQ, EMPTY_BUCKET_DIFF_VAL);
    public static final Bucket TRANSPARENT_BUCKET =
            new Bucket(TRANSPARENT_FREQ, MAX_BUCKET_DIFF_VALS);

    private double frequency;
    private double diffValues;

    /*constructor*/
    public Bucket(double frequency, double diffValues) {
        this.frequency = frequency;
        this.diffValues = diffValues;
    }

    /*copy constructor*/
    public Bucket(Bucket b) {
        //preconditions
        checkNotNull(b, "Bucket::Bucket: parameter b is null");

        this.frequency = b.getFrequency();
        this.diffValues = b.getDiffValues();
    }

    /*getters and setters*/
    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public double getDiffValues() {
        return diffValues;
    }

    public void setDiffValues(double diffValues) {
        this.diffValues = diffValues;
    }

    /*interface methods*/
    public double estimateBucketNumberOfTuples() {
        return this.diffValues * this.frequency;
    }

    /*standard methods*/
    @Override
    public String toString() {
        return "Bucket{" + "frequency=" + frequency + ", diffValues=" + diffValues + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Bucket other = (Bucket) obj;
        if (Double.doubleToLongBits(this.frequency) != Double.doubleToLongBits(other.frequency)) {
            return false;
        }
        if (Double.doubleToLongBits(this.diffValues) != Double.doubleToLongBits(other.diffValues)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.frequency) ^ (
                Double.doubleToLongBits(this.frequency) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.diffValues) ^ (
                Double.doubleToLongBits(this.diffValues) >>> 32));
        return hash;
    }


}
