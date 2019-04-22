package madgik.exareme.master.queryProcessor.analyzer.fanalyzer;

import madgik.exareme.master.queryProcessor.analyzer.builder.HistogramBuildMethod;
import madgik.exareme.master.queryProcessor.analyzer.dbstats.Gatherer;
import madgik.exareme.master.queryProcessor.analyzer.dbstats.StatBuilder;
import madgik.exareme.master.queryProcessor.analyzer.stat.Table;
import madgik.exareme.master.queryProcessor.estimator.db.Schema;
import madgik.exareme.utils.properties.AdpProperties;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author jim
 */
public class OptiqueAnalyzer {
    public static final String GATHER_JSON = "";
    public static final String TMP_SAMPLE_DIR = "dbstats/";
    public static final String BUILD_JSON = "";
    public static final String PYTHON_PATH =
            AdpProperties.getSystemProperties().getString("EXAREME_PYTHON");
    public static final String MADIS_PATH =
            AdpProperties.getSystemProperties().getString("EXAREME_MADIS");

    private static final Logger log = Logger.getLogger(OptiqueAnalyzer.class);

    public static Map<String, Set<String>> statCols;
    // public static final Map<String, Integer> tableCount = new HashMap<String,
    // Integer>();
    private static String madis;
    private Vendor vendor;
    private String dbPath;
    private String schema;

    public OptiqueAnalyzer(String m, String jdbc, String db, String s) throws Exception {
        this.dbPath = db;
        this.schema = s;
        if (!this.dbPath.endsWith("/")) {
            dbPath = dbPath + "/";
        }
        this.madis = m;
        if (jdbc.contains("oracle")) {
            this.vendor = Vendor.Oracle;
        } else if (jdbc.contains("mysql")) {
            this.vendor = Vendor.Mysql;
        } else if (jdbc.contains("postgresql")) {
            this.vendor = Vendor.Postgres;
        }
    }

    public Schema analyzeAttrs(String tableName, Set<String> attrs) throws Exception {
        this.statCols = new HashMap<String, Set<String>>();
        this.statCols.put(tableName, attrs);
        createSample();
        // countRows();

        Map<String, Table> sch = gatherStats();
        Schema res = buildStats(sch);
        deleteSamples();
        return res;
        // this.con.close();
    }

    private void createSample() throws Exception {
        // System.out.println(this.statCols.keySet());
        // System.out.println(this.statCols.keySet().size());

        for (String tableName : this.statCols.keySet()) {

            // if(!tableName.equals("COLLATIONS")) continue;

            StringBuilder mm = new StringBuilder();

            int i = 0;
            for (String c : this.statCols.get(tableName)) {
                String minQuery =
                        " (select * from " + tableName + " t where t." + c + " in (select min(t2." + c
                                + ") from " + tableName + " t2) limit 1) ";

                String maxQuery =
                        " (select * from " + tableName + " t where t." + c + " in (select max(t2." + c
                                + ") from " + tableName + " t2) limit 1) ";

                if (this.vendor == Vendor.Oracle) {
                    minQuery = " (select * from " + schema + "." + tableName + " t where t." + c
                            + " in (select min(t2." + c + ") from " + schema + "." + tableName
                            + " t2) and ROWNUM<2) ";

                    maxQuery = " (select * from " + schema + "." + tableName + " t where t." + c
                            + " in (select max(t2." + c + ") from " + schema + "." + tableName
                            + " t2) and ROWNUM<2) ";
                } else if (this.vendor == Vendor.Postgres) {
                    minQuery = " (select * from " + tableName + " t where t.\"" + c
                            + "\" in (select min(t2.\"" + c + "\") from " + tableName
                            + " t2) limit 1) ";

                    maxQuery = " (select * from " + tableName + " t where t.\"" + c
                            + "\" in (select max(t2.\"" + c + "\") from " + tableName
                            + " t2) limit 1) ";
                }

                mm.append(minQuery).append(" UNION ALL ").append(maxQuery);

                if (i < this.statCols.get(tableName).size() - 1)
                    mm.append(" UNION ALL ");
                i++;

            }

            String sampleQuery = "";
            switch (this.vendor) {
                case Oracle:
                    sampleQuery =
                            "select * from (" + madis + "  select * from ( select * from   " + schema
                                    + "." + tableName
                                    + " order by dbms_random.value() ) where ROWNUM <= 1000  UNION ALL "
                                    + mm.toString() + " );";
                    break;
                case Mysql:
                    // sampleQuery =
                    // "select * from (mysql h:127.0.0.1 port:3306 u:root
                    // db:information_schema select * from `"
                    // + tableName + "` order by rand() limit 1000)";
                    sampleQuery = "select * from (" + madis + " (select * from " + tableName
                            + " order by rand() limit 1000) UNION ALL " + mm.toString() + ");";

                    // System.out.println("==========================");
                    // System.out.println("==========================\n\n");
                    // System.out.println(sampleQuery);
                    break;
                case Postgres:
                    // select * from (postgres h:127.0.0.1 port:5432 u:root p:rootpw
                    // db:testdb select 5 as num, 'test' as text);
                    sampleQuery = "select * from (" + madis + " (select * from " + tableName
                            + " order by random() limit 1000) UNION ALL " + mm.toString() + ");";
                    // break;
            }

            String command =
                    "echo \"create table " + tableName + " as " + sampleQuery + "\" | " + PYTHON_PATH
                            + " " + MADIS_PATH + " " + dbPath + TMP_SAMPLE_DIR + tableName + ".db";
            File file = new File(dbPath + TMP_SAMPLE_DIR + tableName + ".db");
            if (file.exists()) {
                file.delete();
            }
            File statDir = new File(dbPath + TMP_SAMPLE_DIR);
            if (!statDir.exists()) {
                statDir.mkdir();
            }

            // hugeCommand.append(command);
            // if(j < this.statCols.keySet().size() - 1)
            // hugeCommand.append(" ; ");

            String[] cmd = {"/bin/sh", "-c", command};
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            BufferedReader dbr =
                    new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = dbr.readLine()) != null) {
                System.out.println(s);
            }

            //
            System.out.println(command);
            System.out.println("ENDED: " + tableName);
            System.out.println(sampleQuery);

            // gatherStats();

            // break;
        }

        // System.out.println(hugeCommand.toString());
        // String[] cmd = {"/bin/sh", "-c", hugeCommand.toString()};
        // Process process = Runtime.getRuntime().exec(cmd);
        // process.waitFor();

    }

    public void deleteSamples() throws Exception {
        // String[] cmd = {"/bin/sh", "-c",
        // "rm ./files/sample/*; rm ./files/json/*"};
        // Process process = Runtime.getRuntime().exec(cmd);
        // process.waitFor();
    }

    private Map<String, Table> gatherStats() throws Exception {
        // for (String s : this.statCols.keySet()) {
        String s = this.statCols.keySet().iterator().next();
        Gatherer g = new Gatherer(dbPath + TMP_SAMPLE_DIR + s + ".db", s);
        if (this.vendor == Vendor.Oracle) {
            g.setSch(this.schema);
        }
        return g.gather(dbPath);
        // }
    }

    private Schema buildStats(Map<String, Table> schema) throws Exception {
        String[] db = this.statCols.keySet().toArray(new String[this.statCols.keySet().size()]);

        StatBuilder sb = new StatBuilder(db, HistogramBuildMethod.Primitive, schema);
        return sb.build();
    }

    public static int getCountFor(String tableName, String schema) throws Exception {
        String command =
                "echo \"select * from (" + madis + " select count(*) from " + schema + "." + tableName
                        + "); " + "\" | " + PYTHON_PATH + " " + MADIS_PATH + " ";
        String[] cmd = {"/bin/sh", "-c", command};
        log.debug("executing:" + command);
        Process process = Runtime.getRuntime().exec(cmd);
        process.waitFor();
        BufferedReader dbr = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String s;
        int result = 0;
        while ((s = dbr.readLine()) != null) {
            log.debug(s);
            String n = s.replace("]", "").replace("[", "");
            n = n.replaceAll("\"", "");
            try {
                result = Integer.parseInt(n);
            } catch (Exception e) {
                continue;
            }
        }
        dbr.close();
        BufferedReader dbr2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((s = dbr2.readLine()) != null) {
            log.error(s);
        }
        dbr2.close();
        return result;
    }

}
