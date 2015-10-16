package madgik.exareme.master.queryProcessor.analyzer.fanalyzer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

/**
 * @author jim
 */
public class StatQueries {
    public static final int LIMIT = 10000;
    public static final double LIMIT_FACTOR = 0.3;
    private static final int BLOB_SIZE = 1000000;
    private static final int NUM_SIZE = 8;
    private static final int MAX_STRING_SAMPLE = 20;
    public static final String SAMPLE = "_sample";

    private Connection con;

    public StatQueries(Connection con) {
        this.con = con;
    }

    public int countRows(String tableName) throws Exception {
        String countRows = "select count(*) as count  from `" + tableName + "`";
        Statement crStmt = con.createStatement();
        ResultSet crRs = crStmt.executeQuery(countRows);
        int count = 0;

        while (crRs.next()) {
            count = crRs.getInt("count");
        }
        crRs.close();
        crStmt.close();

        return count;
    }

    public int computeColumnSize(String columnName, int columnType, String table_sample)
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

    public MinMax computeMinMax(String tableName, String columnName) throws Exception {
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

    public int computeValOccurences(String tableName, String columnName, String value)
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

    public List<ValFreq> computeDistinctValuesFrequency(String table_sample, String columnName)
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

        // /

        // if(table_sample.equals("lineitem_sample")){

        // System.out.println("STAT_TABLE: " + table_sample + " COLUMN: " +
        // columnName);
        //
        // int s = 0;
        // for(ValFreq v : freqs)
        // s += v.getFreq();
        //
        // System.out.println("SUM: " + s);
        // }

        // /

        return freqs;
    }

    /* inner - helper classes */
    public final class MinMax {
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

    }


    public final class ValFreq {
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
}
