/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.master.queryProcessor.analyzer.builder;

import madgik.exareme.master.queryProcessor.analyzer.stat.Column;
import madgik.exareme.master.queryProcessor.analyzer.stat.Table;
import madgik.exareme.master.queryProcessor.estimator.db.AttrInfo;
import madgik.exareme.master.queryProcessor.estimator.db.RelInfo;
import madgik.exareme.master.queryProcessor.estimator.db.Schema;
import madgik.exareme.master.queryProcessor.estimator.histogram.Bucket;
import madgik.exareme.master.queryProcessor.estimator.histogram.Histogram;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author jim
 */
public class EquiDepth implements HistogramBuilder {

    private static final double BUCKET_FACTOR = 0.2; // double 0..1

    @Override public Schema build(Map<String, Table> dbStats) {
        // System.out.println("=======================>" +
        // dbStats.get("lineitem").getColumnMap().get("l_partkey").getNumberOfDiffValues());
        Map<String, RelInfo> relMap = new HashMap<String, RelInfo>();
        Schema schema = new Schema("FULL_SCHEMA", relMap);

        for (Map.Entry<String, Table> t : dbStats.entrySet()) {
            Map<String, AttrInfo> attrIndex = new HashMap<String, AttrInfo>();

            // int totalSampledRows = (int)(t.getValue().getNumberOfTuples() *
            // Stat.LIMIT_FACTOR);
            int totalSampledRows = 1000;
            System.out.println("total_sampled_rows: " + totalSampledRows);

            for (Map.Entry<String, Column> c : t.getValue().getColumnMap().entrySet()) {

                NavigableMap<Double, Bucket> bucketIndex = new TreeMap<Double, Bucket>();

                TreeMap<Double, Integer> sdata =
                    sortData(t.getValue().getColumnMap().get(c.getKey()).getDiffValFreqMap());

                final double bucketDepth = (double) totalSampledRows * BUCKET_FACTOR;

                final double lastHistVal = sdata.lastKey();

                double cuttingPoint = sdata.firstKey();

                List<Double> sdataKeysList = new LinkedList<Double>(sdata.keySet());

                ListIterator<Double> it = sdataKeysList.listIterator();

                int curDepth = 0;
                int curDiffVal = 0;
                double curVal = 0;
                int curFreq = 0;

                ArrayList<Integer> freqs = new ArrayList<Integer>();

                while (it.hasNext()) {
                    curVal = it.next();
                    curDiffVal++;
                    curFreq = sdata.get(curVal);

                    curDepth += curFreq;
                    freqs.add(curFreq);

                    // /
                    if (c.getKey().equals("l_partkey")) {
                        System.out.println(
                            "==================> " + "curdepth: " + curDepth + " bucket_depth: "
                                + bucketDepth + " has_next: " + it.hasNext());
                    }

                    // /

                    if ((double) curDepth > bucketDepth || !it.hasNext()) {

                        bucketIndex.put(cuttingPoint, new Bucket(listMean(freqs), curDiffVal));
                        curDepth = 0;
                        curDiffVal = 0;
                        cuttingPoint = curVal;

                        freqs = new ArrayList<Integer>();
                    }

                }

                // scaling histogram
                double fscale;
                for (Entry<Double, Bucket> e : bucketIndex.entrySet()) {
                    fscale = ((e.getValue().estimateBucketNumberOfTuples() * (double) t.getValue()
                        .getNumberOfTuples()) / (double) totalSampledRows) / e.getValue()
                        .getDiffValues();
                    e.getValue().setFrequency(fscale);
                }

                bucketIndex.put(Math.nextAfter(lastHistVal, Double.MAX_VALUE),
                    Bucket.FINAL_HISTOGRAM_BUCKET);

                attrIndex.put(c.getKey(), new AttrInfo(c.getKey(), new Histogram(bucketIndex),
                    c.getValue().getColumnLength()));

                System.out.println(
                    "ATTR_NAME: " + c.getKey() + " NUM_BUCKETS: " + attrIndex.get(c.getKey())
                        .getHistogram().getBucketIndex().size() + " HIST_TUPLES: " + attrIndex
                        .get(c.getKey()).getHistogram().numberOfTuples());
            }

            Set<String> hs = new HashSet<String>();
            hs.add(t.getValue().getPrimaryKey());

            relMap.put(t.getKey(),
                new RelInfo(t.getKey(), attrIndex, dbStats.get(t.getKey()).getNumberOfTuples(),
                    t.getValue().getToupleSize(), RelInfo.DEFAULT_NUM_PARTITIONS, hs));

        }

        return new Schema("SCHEMA_DEPTH", relMap);
    }

    private TreeMap<Double, Integer> sortData(Map<String, Integer> diffValFreqMap) {

        TreeMap<Double, Integer> smap = new TreeMap<Double, Integer>();
        // smap.putAll(this.diffValFreqMap);

        for (Map.Entry<String, Integer> e : diffValFreqMap.entrySet())
            smap.put(Double.parseDouble(e.getKey()), e.getValue());

        return smap;
    }

    private double listMean(List<Integer> l) {
        int sum = 0;
        for (int i : l) {
            sum += i;
        }

        double mean = (double) sum / (double) l.size();
        return mean;
    }

}
