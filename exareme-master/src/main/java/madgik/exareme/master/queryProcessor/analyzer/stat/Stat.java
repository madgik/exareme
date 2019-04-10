/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.master.queryProcessor.analyzer.stat;

import madgik.exareme.master.queryProcessor.analyzer.fanalyzer.OptiqueAnalyzer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * @author jim
 */
public class Stat implements StatExtractor {
    // public static final int LIMIT = 10000;
    // public static final double LIMIT_FACTOR = 0.3;
    private static final int BLOB_SIZE = 1000000;
    private static final int NUM_SIZE = 8;
    private static final int MAX_STRING_SAMPLE = 20;
    // public static final String SAMPLE = "_sample";
    private static final Logger log = Logger.getLogger(StatExtractor.class);

    private final Connection con;
    private String sch;

    public Stat(Connection con) {
        sch = "";
        this.con = con;
    }

    // schema map
    private Map<String, Table> schema = new HashMap<String, Table>();

    @Override
    public Map<String, Table> extractStats() throws Exception {

        DatabaseMetaData dbmd = con.getMetaData(); // dtabase metadata object

        // listing tables and columns
        String catalog = null;
        String schemaPattern = null;
        String tableNamePattern = null;
        String[] types = null;
        String columnNamePattern = null;

        ResultSet resultTables = dbmd.getTables(catalog, schemaPattern, tableNamePattern, types);
        log.debug("Starting extracting stats");
        while (resultTables.next()) {
            Map<String, Column> columnMap = new HashMap<String, Column>();
            String tableName = StringEscapeUtils.escapeJava(resultTables.getString(3));
            log.debug("Analyzing table " + tableName);

            int columnCount = resultTables.getMetaData().getColumnCount();
            int toupleSize = 0; // in bytes

            tableNamePattern = tableName;
            ResultSet resultColumns =
                    dbmd.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);

            int count = OptiqueAnalyzer.getCountFor(tableName, sch);

            if (count == 0) {
                log.debug("Empty table");
                continue;
            }


            while (resultColumns.next()) {

                String columnName = StringEscapeUtils.escapeJava(resultColumns.getString(4));
                int columnType = resultColumns.getInt(5);

                // computing column's size in bytes
                int columnSize = computeColumnSize(columnName, columnType, tableName);
                toupleSize += columnSize;

                // execute queries for numberOfDiffValues, minVal, maxVal
                Map<String, Integer> diffValFreqMap = new HashMap<String, Integer>();

                // computing column's min and max values
                MinMax mm = computeMinMax(tableName, columnName);
                String minVal = mm.getMin();
                String maxVal = mm.getMax();

                // /
                List<ValFreq> freqs = computeDistinctValuesFrequency(tableName, columnName);

                for (ValFreq k : freqs) {
                    diffValFreqMap.put(k.getVal(), k.getFreq());

                }

                // /add min max diff vals in the sampling values
                int minOcc = computeValOccurences(tableName, columnName, minVal);
                if (!diffValFreqMap.containsKey(minVal))
                    diffValFreqMap.put(minVal, minOcc);
                int maxOcc = computeValOccurences(tableName, columnName, maxVal);
                if (!diffValFreqMap.containsKey(maxVal))
                    diffValFreqMap.put(maxVal, maxOcc);

                int diffVals = diffValFreqMap.size();

                Column c = new Column(columnName, columnType, columnSize, diffVals, minVal, maxVal,
                        diffValFreqMap);
                columnMap.put(columnName, c);

            }

            ResultSet pkrs = dbmd.getExportedKeys("", "", tableName);
            String pkey = "DEFAULT_KEY";

            while (pkrs.next()) {
                pkey = pkrs.getString("PKCOLUMN_NAME");
                break;
            }

            Table t = new Table(tableName, columnCount, toupleSize, columnMap, count, pkey);
            schema.put(tableName, t);

        }

        return schema;

    }

    /* private-helper methods */
    private int computeColumnSize(String columnName, int columnType, String table_sample)
            throws Exception {
        int columnSize = 0;
        if (columnType == Types.INTEGER || columnType == Types.REAL || columnType == Types.DOUBLE
                || columnType == Types.DECIMAL || columnType == Types.FLOAT
                || columnType == Types.NUMERIC) {
            columnSize = NUM_SIZE;
        } else if (columnType == Types.VARCHAR) {
            String query0 =
                    "select max(length(`" + columnName + "`)) as length from (select `" + columnName
                            + "` from `" + table_sample + "`)" + " where `" + columnName
                            + "` is not null limit " + MAX_STRING_SAMPLE;

            Statement stmt0 = con.createStatement();
            ResultSet rs0 = stmt0.executeQuery(query0);

            while (rs0.next()) {
                columnSize = rs0.getInt("length");
            }
            rs0.close();
            stmt0.close();

        } else if (columnType == Types.BLOB)
            columnSize = BLOB_SIZE;

        return columnSize;
    }

    private MinMax computeMinMax(String tableName, String columnName) throws Exception {
        String query1 = "select min(`" + columnName + "`) as minVal, max(`" + columnName + "`) "
                + "as maxVal  from `" + tableName + "` where `" + columnName + "` is not null";

        String minVal = "", maxVal = "";

        Statement stmt1 = con.createStatement();
        ResultSet rs1 = stmt1.executeQuery(query1);
        while (rs1.next()) {
            minVal = rs1.getString("minVal");
            maxVal = rs1.getString("maxVal");
        }
        rs1.close();
        stmt1.close();

        return new MinMax(minVal, maxVal);
    }

    private int computeValOccurences(String tableName, String columnName, String value)
            throws Exception {
        String queryDf =
                "select count(*) as valCount " + "from `" + tableName + "` where `" + columnName
                        + "` is not null and  `" + columnName + "` = \"" + value + "\"";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(queryDf);
        int diffValCount = 0;
        while (rs.next()) {
            diffValCount = rs.getInt("valCount");
        }
        rs.close();
        stmt.close();

        return diffValCount;
    }

    private List<ValFreq> computeDistinctValuesFrequency(String table_sample, String columnName)
            throws Exception {
        List<ValFreq> freqs = new LinkedList<ValFreq>();

        String query = "select `" + columnName + "` as val, count(*) as freq from `" + table_sample
                + "` where `" + columnName + "` is not null group by `" + columnName + "`";

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            freqs.add(new ValFreq(rs.getString("val"), rs.getInt("freq")));
        }

        rs.close();
        stmt.close();

        return freqs;
    }

    /* inner - helper classes */
    private final class MinMax {
        private final String min;
        private final String max;

        public MinMax(String min, String max) {
            this.min = min;
            this.max = max;
        }

        public String getMin() {
            return min;
        }

        public String getMax() {
            return max;
        }

        @Override
        public String toString() {
            return "MinMax{" + "min=" + min + ", max=" + max + '}';
        }

    }


    private final class ValFreq {
        private final String val;
        private final int freq;

        public ValFreq(String val, int freq) {
            this.val = val;
            this.freq = freq;
        }

        public String getVal() {
            return val;
        }

        public int getFreq() {
            return freq;
        }

    }

    public void setSch(String s) {
        this.sch = s;
    }
}
