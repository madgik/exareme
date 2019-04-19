/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.estimator.histogram;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author jim
 */
public final class Histogram {
    public static final double MAX_HISTOGRAM_VALUE = Double.POSITIVE_INFINITY;
    public static final double MIN_HISTOGRAM_VALUE = Double.NEGATIVE_INFINITY;

    private NavigableMap<Double, Bucket> bucketIndex;

    /*default constructor*/
    public Histogram() {
        this.bucketIndex = new TreeMap<Double, Bucket>();
    }

    /*constructor*/
    public Histogram(NavigableMap<Double, Bucket> bucketIndex) {
        //preconditions
        checkNotNull(bucketIndex, "Histogram::Histogram: parameter <bucketIndex> is null");

        this.bucketIndex = bucketIndex;
    }

    /*copy constructor*/
    public Histogram(Histogram h) {
        //preconditions
        checkNotNull(h, "Histogram::Histogram: parameter <h> is null");

        this.bucketIndex = new TreeMap<Double, Bucket>();
        for (Map.Entry<Double, Bucket> entry : h.getBucketIndex().entrySet()) {
            if (!entry.getValue().equals(Bucket.FINAL_HISTOGRAM_BUCKET)) {
                this.bucketIndex.put(entry.getKey(), new Bucket(entry.getValue()));
            } else {
                this.bucketIndex.put(entry.getKey(), Bucket.FINAL_HISTOGRAM_BUCKET);
            }
        }

    }

    /*getters and setters*/
    public NavigableMap<Double, Bucket> getBucketIndex() {
        return bucketIndex;
    }

    public void setBucketIndex(NavigableMap<Double, Bucket> bucketIndex) {
        //preconditions
        checkNotNull(bucketIndex, "Histogram::setBucketIndex: parameter <bucketIndex> is null");

        this.bucketIndex = bucketIndex;
    }

    /*interface methods*/
    public double minValue() {
        return this.bucketIndex.firstKey();
    }

    public double maxValue() {
        return this.bucketIndex.lowerKey(this.bucketIndex.lastKey());
    }

    public double distinctValues() {
        double distinctValues = 0;
        for (Bucket b : this.bucketIndex.values()) {
            distinctValues += b.getDiffValues();
        }

        return distinctValues;
    }

    public double cardinality() {
        double rows = 0;
        for (Bucket b : this.bucketIndex.values()) {
            rows += b.getFrequency() * b.getDiffValues();
        }

        return rows;
    }

    public double estimateNumberOfTuples() {
        double tuples = 0;
        for (Bucket b : this.bucketIndex.values()) {
            tuples += b.estimateBucketNumberOfTuples();
            System.out.println(tuples);
        }

        return tuples;
    }

    public void equal(double value) {
        if (this.containsValue(value)) {
            Bucket b = this.bucketIndex.get(this.bucketIndex.floorKey(value));
            Bucket nb = new Bucket(b.getFrequency(), Bucket.SINGLE_BUCKET_DIFF_VAL);
            double appVal = this.approximateNextBucketValue(value);
            this.bucketIndex.clear();
            this.bucketIndex.put(value, nb);
            this.bucketIndex.put(appVal, Bucket.FINAL_HISTOGRAM_BUCKET);
        } else
            this.convertToTransparentHistogram();
    }

    public void notEqual(double value) {

        if (this.containsValue(value)) {
            System.out.println("Not yet implemented!!!");


        } else
            this.convertToTransparentHistogram();
    }
    //
    //
    //
    //        Histogram h = new Histogram(this);
    //
    //        if(this.bucketIndex.containsKey(value) && this.bucketIndex.lastKey() != value){
    //
    //            double upperBound = h.computeUpperBound(value);
    //            if(h.getBucketIndex().containsKey(value)){
    //                //ubdv: upper bound diff values
    //                double ubdv = h.computeNewBucketDiffValues(upperBound, false);
    //                Bucket nb = new Bucket(h.getBucketIndex().get(
    //                        h.getBucketIndex().floorKey(value)).getFrequency(),
    //                        ubdv);
    //                h.getBucketIndex().put(upperBound, nb);
    //                h.getBucketIndex().put(value, Bucket.HOLE_BUCKET);
    //            }
    //            else{
    //                //ubdv: upper bound diff values
    //                double ubdv = h.computeNewBucketDiffValues(upperBound, false);
    //                //fbdv: floor bucket diff values
    //                double fbdv = h.computeNewBucketDiffValues(value, true);
    //                Bucket nb = new Bucket(h.getBucketIndex().get(
    //                        h.getBucketIndex().ceilingKey(value)).getFrequency(),
    //                        ubdv);
    //                h.getBucketIndex().put(value, Bucket.HOLE_BUCKET);
    //
    //                h.getBucketIndex().put(upperBound, nb);
    //                h.getBucketIndex().get(h.getBucketIndex().lowerKey(value)).
    //                        setDiffValues(fbdv);
    //            }
    //
    //            return h;
    //        }
    //
    //        else
    //            return TRANSPARENT_HISTOGRAM;
    //    }

    public void greaterOrEqual(double value) {
        if (value >= this.getBucketIndex().lastKey())
            this.convertToTransparentHistogram();
        else if (this.containsValue(value))
            this.shrinkHistogramLeft(value);
    }


    public void greaterThan(double value) {
        if (value >= this.getBucketIndex().lastKey())
            this.convertToTransparentHistogram();
        else if (this.containsValue(value)) {
            this.greaterOrEqual(this.approximateNextBucketValue(value));
        }
    }


    public void lessThanValueEstimation(double value) {
        if (value < this.getBucketIndex().firstKey())
            this.convertToTransparentHistogram();
        else if (this.containsValue(value))
            this.shrinkHistogramRight(value);
    }


    public void lessOrEqualValueEstimation(double value) {
        if (value < this.getBucketIndex().firstKey())
            this.convertToTransparentHistogram();
        else if (this.containsValue(value)) {
            this.lessThanValueEstimation(this.approximateNextBucketValue(value));
        }
    }


    public void join(Histogram h2) {
        //preconditions
        checkNotNull(h2, "Histogram::joinHistogramsEstimation: parameter <h2> is null");

        if (this.isTransparentHistogram() && !h2.isTransparentHistogram())
            this.setBucketIndex(new TreeMap<Double, Bucket>(h2.getBucketIndex()));
        else if (h2.isTransparentHistogram() && !this.isTransparentHistogram())
            h2.setBucketIndex(new TreeMap<Double, Bucket>(this.getBucketIndex()));

        if (!existsIntersection(h2)) {
            System.out.println(this);
            System.out.println(h2);
            this.convertToTransparentHistogram();
        } else {
            Map<Double, Double> cbmap = this.combine(h2);
            for (Map.Entry<Double, Double> e : cbmap.entrySet())
                this.joinBuckets(h2, e.getKey(), e.getValue());
        }
    }


    //TODO
    //    @Override
    //    public void union(Histogram h2){
    //        //preconditions
    //        checkNotNull(h2, "Histogram::joinHistogramsEstimation: parameter <h2> is null");
    //
    //        if(!this.equals(h2) && h2.equals(this)){
    //
    //
    //
    //
    //
    //        }
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //    }


    public double numberOfTuples() {
        double not = 0.0;
        for (Map.Entry<Double, Bucket> entry : this.bucketIndex.entrySet())
            if (!entry.getValue().equals(Bucket.FINAL_HISTOGRAM_BUCKET))
                not += entry.getValue().estimateBucketNumberOfTuples();
        return not;
    }

    /*private-helper methods*/
    private void shrinkHistogramLeft(Double value) {
        if (this.getBucketIndex().containsKey(value))
            //                this.setBucketIndex(this.getBucketIndex().tailMap(value, true));
            this.setBucketIndex(
                    new TreeMap<Double, Bucket>(this.getBucketIndex().tailMap(value, true)));
        else {
            Bucket nb = new Bucket(this.bucketIndex.get(this.bucketIndex.
                    floorKey(value)).getFrequency(), this.computeRightSemiBucketDiffVals(value));
            this.bucketIndex.put(value, nb);
            //            this.bucketIndex = this.bucketIndex.tailMap(value, true);
            this.bucketIndex = new TreeMap<Double, Bucket>(this.bucketIndex.tailMap(value, true));

        }
    }

    private void shrinkHistogramRight(Double value) {
        if (!this.getBucketIndex().containsKey(value)) {
            this.bucketIndex.get(this.bucketIndex.lowerKey(value))
                    .setDiffValues(this.computeLeftSemiBucketDiffVals(value));
            this.bucketIndex.put(value, Bucket.FINAL_HISTOGRAM_BUCKET);
            //            this.setBucketIndex(this.getBucketIndex().headMap(value, true));
            this.setBucketIndex(
                    new TreeMap<Double, Bucket>(this.getBucketIndex().headMap(value, true)));
        } else {
            //            this.setBucketIndex(this.getBucketIndex().headMap(value, true));
            this.setBucketIndex(
                    new TreeMap<Double, Bucket>(this.getBucketIndex().headMap(value, true)));
            this.bucketIndex.put(value, Bucket.FINAL_HISTOGRAM_BUCKET);
        }
    }

    private void limitHistogram(double left, double right) {
        shrinkHistogramLeft(left);
        shrinkHistogramRight(right);

    }

    private double computeRightSemiBucketDiffVals(double value) {
        checkNotNull(this.bucketIndex.floorKey(value), "floor");
        checkNotNull(this.bucketIndex.ceilingKey(value), "ceiling");

        double fk = this.bucketIndex.floorKey(value);
        double ck = this.bucketIndex.ceilingKey(value);
        return ((ck - value) / (ck - fk)) * this.bucketIndex.get(fk).getDiffValues();
    }

    private double computeLeftSemiBucketDiffVals(double value) {
        double fk = this.bucketIndex.floorKey(value);
        double ck = this.bucketIndex.ceilingKey(value);
        return ((value - fk) / (ck - fk)) * this.bucketIndex.get(fk).getDiffValues();
    }

    private double approximateNextBucketValue(double value) {
        return value + Math.nextAfter(value, Double.MAX_VALUE);
    }

    private CommonHistogramsRange commonRange(Histogram h2) {
        //preconditions
        checkNotNull(h2, "Histogram::commonRange: parameter <h2> is null");

        double left = 0.0, right = 0.0;
        //finding common range        
        if (this.bucketIndex.firstKey() <= h2.getBucketIndex().firstKey()
                && this.bucketIndex.lastKey() >= h2.getBucketIndex().lastKey()) {
            left = h2.getBucketIndex().firstKey();
            right = h2.getBucketIndex().lastKey();
        } else if (h2.getBucketIndex().firstKey() <= this.bucketIndex.firstKey()
                && h2.getBucketIndex().lastKey() >= this.bucketIndex.
                lastKey()) {
            left = this.bucketIndex.firstKey();
            right = this.bucketIndex.lastKey();
        } else if (this.bucketIndex.firstKey() <= h2.getBucketIndex().firstKey()
                && this.bucketIndex.lastKey() <= h2.getBucketIndex().
                lastKey()) {
            left = h2.getBucketIndex().firstKey();
            right = this.bucketIndex.lastKey();
        } else if (h2.getBucketIndex().firstKey() <= this.bucketIndex.firstKey()
                && h2.getBucketIndex().lastKey() < this.bucketIndex.
                lastKey()) {
            left = this.bucketIndex.firstKey();
            right = h2.getBucketIndex().lastKey();
        } else if (this.bucketIndex.firstKey() >= h2.getBucketIndex().firstKey()
                && this.bucketIndex.lastKey() >= h2.getBucketIndex().
                lastKey()) {
            left = h2.getBucketIndex().firstKey();
            right = this.bucketIndex.lastKey();
        } else if (h2.getBucketIndex().firstKey() >= this.bucketIndex.firstKey()
                && h2.getBucketIndex().lastKey() >= this.bucketIndex.
                lastKey()) {
            left = this.bucketIndex.firstKey();
            right = h2.getBucketIndex().lastKey();
        }

        return new CommonHistogramsRange(left, right);
    }

    //    //try to concat histograms if posible
    //    //returns true in case of success, otherwise false
    //    //TODO: fix the = case
    //    private boolean apanedHistograms(Histogram h2){
    //        boolean isConcated = false;
    //
    //        if(this.getBucketIndex().firstKey() > h2.getBucketIndex().lastKey()){
    //            for(Double point : h2.bucketIndex.keySet()){
    //                Bucket b = h2.getBucketIndex().get(point);
    //                if(!b.equals(Bucket.FINAL_HISTOGRAM_BUCKET))
    //                    this.bucketIndex.put(point, b);
    //                else this.bucketIndex.put(point, Bucket.EMPTY_BUCKET);
    //            }
    //            isConcated = true;
    //        }
    //        else if(this.getBucketIndex().lastKey() < h2.getBucketIndex().firstKey()){
    //            double lastKey = this.bucketIndex.lastKey();
    //            this.bucketIndex.remove(lastKey);
    //            this.bucketIndex.put(lastKey, Bucket.EMPTY_BUCKET);
    //
    //            for(Double point : h2.bucketIndex.keySet()){
    //                Bucket b = h2.getBucketIndex().get(point);
    //                this.bucketIndex.put(point, b);
    //            }
    //            isConcated = true;
    //        }
    //        else if(this.getBucketIndex().firstKey().equals(h2.getBucketIndex().lastKey())){
    //            for(Double point : h2.bucketIndex.keySet()){
    //                Bucket b = h2.getBucketIndex().get(point);
    //                if(!b.equals(Bucket.FINAL_HISTOGRAM_BUCKET))
    //                    this.bucketIndex.put(point, b);
    //            }
    //            isConcated = true;
    //        }
    //        else if(this.getBucketIndex().lastKey().equals(h2.getBucketIndex().firstKey())){
    //            double lastKey = this.bucketIndex.lastKey();
    //            this.bucketIndex.remove(lastKey);
    //
    //            for(Double point : h2.bucketIndex.keySet()){
    //                Bucket b = h2.getBucketIndex().get(point);
    //                this.bucketIndex.put(point, b);
    //            }
    //            isConcated = true;
    //        }
    //
    //        return isConcated;
    //    }
    //

    //TODO
    //    private void mergeOnDelete(double value){
    //
    //    }

    private boolean existsIntersection(Histogram h2) {
        //preconditions
        checkNotNull(h2, "Histogram::existsIntersection: parameter <h2> is null");

        if (this.getBucketIndex().firstKey() > h2.getBucketIndex().lastKey()
                || this.getBucketIndex().lastKey() < h2.getBucketIndex().firstKey())
            return false;
        else
            return true;
    }

    private void joinBuckets(Histogram h2, double combiningBucketId, double combinerBucketId) {
        //preconditions
        checkNotNull(h2, "Histogram::joinBuckets: parameter <h2> is null");

        Bucket combiningBucket = this.getBucketIndex().get(combiningBucketId);
        Bucket combinerBucket = h2.getBucketIndex().get(combinerBucketId);

        if (combiningBucketId != this.bucketIndex.lastKey() && combinerBucketId != h2.bucketIndex
                .lastKey()) {

            double resultFreq = combiningBucket.getFrequency() * combinerBucket.getFrequency();

            double minCombinerBucketVal = combinerBucketId;
            double maxCombinerBucketVal = h2.getBucketIndex().higherKey(combinerBucketId);
            double minCombiningBucketVal = combiningBucketId;
            double maxCombiningBucketVal = this.getBucketIndex().higherKey(combiningBucketId);

            double combinerSubBucketDiffVals =
                    (maxCombiningBucketVal - minCombiningBucketVal) / (maxCombinerBucketVal
                            - minCombinerBucketVal) * combinerBucket.getDiffValues();


            double nodv = combiningBucket.getDiffValues();

            if (nodv > combinerSubBucketDiffVals)
                nodv = combinerSubBucketDiffVals;

            combiningBucket.setDiffValues(nodv);
            combiningBucket.setFrequency(resultFreq);
        }
    }

    private void unionBuckets(Histogram h2, double combiningBucketId, double combinerBucketId) {
        //preconditions
        checkNotNull(h2, "Histogram::unionBuckets: parameter <h2> is null");

        Bucket combiningBucket = this.getBucketIndex().get(combiningBucketId);
        Bucket combinerBucket = h2.getBucketIndex().get(combinerBucketId);


        if (combiningBucketId != this.bucketIndex.lastKey() && combinerBucketId != h2.bucketIndex
                .lastKey()) {
            double resultFreq = combiningBucket.getFrequency() + combinerBucket.getFrequency();

            double minCombinerBucketVal = combinerBucketId;
            double maxCombinerBucketVal = h2.getBucketIndex().higherKey(combinerBucketId);
            double minCombiningBucketVal = combiningBucketId;
            double maxCombiningBucketVal = this.getBucketIndex().higherKey(combiningBucketId);

            double combinerSubBucketDiffVals =
                    (maxCombiningBucketVal - minCombiningBucketVal) / (maxCombinerBucketVal
                            - minCombinerBucketVal) * combinerBucket.getDiffValues();


            double nodv = combiningBucket.getDiffValues();

            if (nodv < combinerSubBucketDiffVals)
                nodv = combinerSubBucketDiffVals;

            combiningBucket.setDiffValues(nodv);
            combiningBucket.setFrequency(resultFreq);

        }
    }
    //
    //    private void appendSubHistogram(Histogram rh, NavigableMap<Double, Bucket> subMap, boolean isLeftAppend){
    //        if(!isLeftAppend) rh.bucketIndex.remove(rh.bucketIndex.lastKey());
    //        for(Map.Entry<Double, Bucket> entry : subMap.entrySet())
    //            rh.getBucketIndex().put(entry.getKey(), entry.getValue());
    //    }

    public Map<Double, Double> combine(Histogram h2) {
        //preconditions
        checkNotNull(h2, "Histogram::merge: parameter <h2> is null");

        Map<Double, Double> combinedBucketsMap = new TreeMap<Double, Double>();
        CommonHistogramsRange range = this.commonRange(h2);
        this.limitHistogram(range.getLeft(), range.getRight());
        //System.out.println("LIMITED: " + this);
        h2.limitHistogram(range.getLeft(), range.getRight());
        double lastPoint = h2.getBucketIndex().firstKey();

        for (Map.Entry<Double, Bucket> entry2 : h2.getBucketIndex().
                entrySet()) {
            if (!entry2.getKey().equals(this.getBucketIndex().firstKey())) {

                if (!this.getBucketIndex().containsKey(entry2.getKey())) {
                    double nbdv = this.computeRightSemiBucketDiffVals(
                            entry2.getKey()); //nbdv: new bucket diff values
                    Bucket nb = new Bucket(this.getBucketIndex().
                            get(this.getBucketIndex().floorKey(entry2.
                                    getKey())).getFrequency(), nbdv);

                    this.getBucketIndex().get(this.getBucketIndex().
                            floorKey(entry2.getKey())).setDiffValues(this.
                            getBucketIndex().get(this.getBucketIndex().
                            floorKey(entry2.getKey())).getDiffValues() - nbdv);

                    this.getBucketIndex().put(entry2.getKey(), nb);
                }

                NavigableMap<Double, Bucket> submapView =
                        this.getBucketIndex().subMap(lastPoint, true, entry2.getKey(), false);

                double combinerBucketId = h2.getBucketIndex().lowerKey(entry2.getKey());

                for (Map.Entry<Double, Bucket> sentry : submapView.entrySet())
                    combinedBucketsMap.put(sentry.getKey(), combinerBucketId);

                lastPoint = entry2.getKey();
            }
        }

        combinedBucketsMap.put(this.getBucketIndex().lastKey(), h2.getBucketIndex().lastKey());
        return combinedBucketsMap;
    }

    private void convertToTransparentHistogram() {
        //        System.out.println("TRANSPARANTING...");
        //        System.out.println(this);

        this.bucketIndex.clear();

        //        System.out.println("\n\n" + this.bucketIndex);


        //        System.out.println(MIN_HISTOGRAM_VALUE + " " + Bucket.TRANSPARENT_BUCKET);


        this.bucketIndex.put(MIN_HISTOGRAM_VALUE, Bucket.TRANSPARENT_BUCKET);
        //        Bucket b = new Bucket(2.2, 3.2);
        //        System.out.println(b);
        //        this.bucketIndex.put(1.234, b);

        //        System.out.println("\n\n" + this.bucketIndex);

        this.bucketIndex.put(MAX_HISTOGRAM_VALUE, Bucket.FINAL_HISTOGRAM_BUCKET);


        //        System.out.println("=========================");
    }

    private boolean isTransparentHistogram() {
        if (this.bucketIndex.firstEntry().getValue().equals(Bucket.TRANSPARENT_BUCKET)
                && this.bucketIndex.size() == 2 &&
                this.bucketIndex.firstEntry().getValue().equals(Bucket.FINAL_HISTOGRAM_BUCKET))
            return true;
        else
            return false;
    }

    private boolean containsValue(double value) {
        if (value >= this.getBucketIndex().firstKey() &&
                value < this.getBucketIndex().lastKey() &&
                !isTransparentHistogram())
            return true;
        else
            return false;
    }

    /*standard methods*/
    @Override
    public String toString() {
        return "Histogram{" + "bucketIndex=" + bucketIndex + '}';
    }

    /*inner private-helper classes*/
    private final class CommonHistogramsRange {
        private final double left;
        private final double right;

        /*constructors*/
        public CommonHistogramsRange(double left, double right) {
            this.left = left;
            this.right = right;
        }

        /*getters and setters*/
        public double getLeft() {
            return left;
        }

        public double getRight() {
            return right;
        }

        /*standard methods*/
        @Override
        public String toString() {
            return "Range{" + "left=" + left + ", right=" + right + '}';
        }

    }


}
