/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.master.queryProcessor.analyzer.builder;

import madgik.exareme.master.queryProcessor.analyzer.stat.Table;
import madgik.exareme.master.queryProcessor.estimator.db.AttrInfo;
import madgik.exareme.master.queryProcessor.estimator.db.RelInfo;
import madgik.exareme.master.queryProcessor.estimator.db.Schema;
import madgik.exareme.master.queryProcessor.estimator.histogram.Bucket;
import madgik.exareme.master.queryProcessor.estimator.histogram.Histogram;

import java.util.*;

/**
 * @author jim
 */
public class PrimitiveHistogram implements HistogramBuilder {

    @Override public Schema build(Map<String, Table> dbStats) {
        // System.out.println("=======================>" +
        // dbStats.get("lineitem").getColumnMap().get("l_partkey").getNumberOfDiffValues());
        // System.out.println("\n\n\n\n");
        // System.out.println("==================LLALALALALALALALALALALA=====================");
        // System.out.println(dbStats);
        // System.out.println("\n\n\n\n");
        // System.out.println("=======================================");
        Map<String, RelInfo> relMap = new HashMap<String, RelInfo>();
        Schema schema = new Schema("FULL_SCHEMA", relMap);

        double diffVals = 0;

        for (String t : dbStats.keySet()) {

            Map<String, AttrInfo> attrIndex = new HashMap<String, AttrInfo>();

            //System.out.println(dbStats.get(t).getColumnMap().keySet().size());

            for (String c : dbStats.get(t).getColumnMap().keySet()) {
                NavigableMap<Double, Bucket> bucketIndex = new TreeMap<Double, Bucket>();

                int count = dbStats.get(t).getNumberOfTuples();
                int nodv = dbStats.get(t).getColumnMap().get(c).getNumberOfDiffValues();
                // int limit = (int)Math.round(Stat.LIMIT_FACTOR *
                // dbStats.get(t).getNumberOfTuples()) + 1;
                int limit = 1000;

                // System.out.println(c);
                // System.out.println("count: " + count + " dv: " + nodv +
                // " limit: " + limit);

                // diffVals = ((double)nodv * (double)count) / ((double)limit);
                //System.out.println("diffVals estimation: " + diffVals);
                //System.out.println("nodv: " + nodv + "count: " + count
                //	+ "limit: " + limit);

                // Bucket b = new
                // Bucket(BuildUtil.computeMeanVal(dbStats.get(t).getColumnMap().get(c).getDiffValFreqMap()),
                // diffVals);
                Bucket b = new Bucket(((double) count) / (double) nodv, (double) nodv);

                bucketIndex
                    .put(Double.parseDouble(dbStats.get(t).getColumnMap().get(c).getMinValue()), b);
                bucketIndex.put(Math.nextAfter(
                    Double.parseDouble(dbStats.get(t).getColumnMap().get(c).getMaxValue()),
                    Double.MAX_VALUE), Bucket.FINAL_HISTOGRAM_BUCKET);

                Histogram h = new Histogram(bucketIndex);

                AttrInfo a = new AttrInfo(dbStats.get(t).getColumnMap().get(c).getColumnName(), h,
                    dbStats.get(t).getColumnMap().get(c).getColumnLength());

                attrIndex.put(a.getAttrName(), a);
            }
            Set<String> ha = new HashSet<String>();
            ha.add(dbStats.get(t).getPrimaryKey());
            RelInfo r = new RelInfo(dbStats.get(t).getTableName(), attrIndex,
                dbStats.get(t).getNumberOfTuples(), dbStats.get(t).getToupleSize(),
                RelInfo.DEFAULT_NUM_PARTITIONS, ha);

            relMap.put(r.getRelName(), r);
        }

        return schema;
    }

}
