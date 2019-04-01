package madgik.exareme.master.queryProcessor.analyzer.fanalyzer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import madgik.exareme.master.queryProcessor.analyzer.builder.HistogramBuildMethod;
import madgik.exareme.master.queryProcessor.analyzer.dbstats.Gatherer;
import madgik.exareme.master.queryProcessor.analyzer.dbstats.StatBuilder;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;


/**
 * @author jim
 */
public class FederatedAnalyzer implements Analyzer {
    public static final String GATHER_JSON = "./files/gather/";
    public static final String TMP_SAMPLE_DIR = "./files/sample/";
    public static final String BUILD_JSON = "./files/build/";
    public static final String PYTHON_PATH = "/private/dimb/Python-2.7.3/python";
    public static final String MADIS_PATH = "/private/dimb/madisnew/src/mterm.py";

    private String ip;
    private int port;
    private String username;
    private String password;
    private String dbName;
    private Vendor vendor;
    private Connection con;
    public static Map<String, Set<String>> statCols;
    public static final Map<String, Integer> tableCount = new HashMap<String, Integer>();

    public FederatedAnalyzer() throws Exception {

        Properties prop = new Properties();
        InputStream input = new FileInputStream("./conf/credentials.properties");
        prop.load(input);

        this.ip = prop.getProperty("ip");
        this.port = Integer.parseInt(prop.getProperty("port"));
        this.username = prop.getProperty("username");
        this.password = prop.getProperty("password");
        this.dbName = prop.getProperty("dbname");

        String v = prop.getProperty("vendor");

        createConnection(v);
    }

    public FederatedAnalyzer(String ipAddress, int portNo, String user, String pass, String db,
                             String vendorName) throws Exception {

        this.ip = ipAddress;
        this.port = portNo;
        this.username = user;
        this.password = pass;
        this.dbName = db;
        createConnection(vendorName);
    }

    @Override
    public void analyzeAll() throws Exception {
        this.statCols = specifyTables();
        createSample();
        countRows();

        gatherStats();
        buildStats();
        deleteSamples();
        // this.con.close();
    }

    @Override
    public void analyzeTable(String tableName) throws Exception {
        this.statCols = specifyColumns(tableName);
        createSample();
        countRows();

        gatherStats();
        buildStats();
        deleteSamples();
        // this.con.close();
    }

    @Override
    public void analyzeAttrs(String tableName, Set<String> attrs) throws Exception {
        this.statCols = new HashMap<String, Set<String>>();
        this.statCols.put(tableName, attrs);
        createSample();
        countRows();

        gatherStats();
        buildStats();
        deleteSamples();
        // this.con.close();
    }

    /* private - helper methods */
    private Map<String, Set<String>> specifyTables() throws Exception {
        Map<String, Set<String>> info = new HashMap<String, Set<String>>();
        DatabaseMetaData dbmd = con.getMetaData(); // dtabase metadata object
        // listing tables and columns
        String catalog = null;
        String schemaPattern = null;
        String tableNamePattern = null;
        String[] types = null;
        String columnNamePattern = null;

        ResultSet resultTables = dbmd.getTables(catalog, schemaPattern, tableNamePattern, types);

        while (resultTables.next()) {
            String tableName = StringEscapeUtils.escapeJava(resultTables.getString(3));

            if (tableName.contains("_sample"))
                break;
            if (tableName.equals("sqlite_stat1"))
                continue;

            tableNamePattern = tableName;
            ResultSet resultColumns =
                    dbmd.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
            Set<String> columns = new HashSet<String>();

            while (resultColumns.next()) {
                String columnName = StringEscapeUtils.escapeJava(resultColumns.getString(4));
                columns.add(columnName);
            }
            info.put(tableName, columns);
        }

        return info;
    }

    private Map<String, Set<String>> specifyColumns(String tableName) throws Exception {
        Map<String, Set<String>> info = new HashMap<String, Set<String>>();
        DatabaseMetaData dbmd = con.getMetaData(); // dtabase metadata object

        // listing tables and columns
        String catalog = null;
        String schemaPattern = null;
        String columnNamePattern = null;
        String tname = tableName;
        if (tname.contains(".")) {
            schemaPattern = tname.split("\\.")[0];
            tname = tname.split("\\.")[1];
        }

        ResultSet resultColumns = dbmd.getColumns(catalog, schemaPattern, tname, columnNamePattern);
        Set<String> columns = new HashSet<String>();
        while (resultColumns.next()) {
            String columnName = StringEscapeUtils.escapeJava(resultColumns.getString(4));
            columns.add(columnName);
        }
        info.put(tableName, columns);
        resultColumns.close();
        return info;

    }

    private void createSample() throws Exception {
        System.out.println(this.statCols.keySet());
        System.out.println(this.statCols.keySet().size());

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
                    minQuery =
                            " (select * from " + tableName + " t where t." + c + " in (select min(t2."
                                    + c + ") from " + tableName + " t2) and ROWNUM<2) ";

                    maxQuery =
                            " (select * from " + tableName + " t where t." + c + " in (select max(t2."
                                    + c + ") from " + tableName + " t2) and ROWNUM<2) ";
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
                            "select * from (oracle jdbc:oracle:thin:@" + this.ip + ":" + this.port + ":"
                                    + this.dbName + " u:" + this.username + " p:" + this.password
                                    + "  select * from ( select * from   " + tableName
                                    + " order by dbms_random.value() ) where ROWNUM <= 1000  UNION ALL "
                                    + mm.toString() + " );";
                    break;
                case Mysql:
                    // sampleQuery =
                    // "select * from (mysql h:127.0.0.1 port:3306 u:root db:information_schema select * from `"
                    // + tableName + "` order by rand() limit 1000)";
                    if (this.password.isEmpty()) {
                        sampleQuery =
                                "select * from (mysql h:" + this.ip + " port:" + this.port + " u:"
                                        + this.username + " db:" + this.dbName + " (select * from "
                                        + tableName + " order by rand() limit 1000) UNION ALL " + mm
                                        .toString() + ");";

                    } else {
                        sampleQuery =
                                "select * from (mysql h:" + this.ip + " port:" + this.port + " u:"
                                        + this.username + " p:" + this.password + " db:" + this.dbName
                                        + " (select * from " + tableName
                                        + " order by rand() limit 1000) UNION ALL " + mm.toString() + " ;";
                    }
                    System.out.println("==========================");
                    System.out.println("==========================\n\n");
                    System.out.println(sampleQuery);
                    break;
                case Postgres:
                    // select * from (postgres h:127.0.0.1 port:5432 u:root p:rootpw
                    // db:testdb select 5 as num, 'test' as text);
                    sampleQuery =
                            "select * from (postgres h:" + this.ip + " port:" + this.port + " u:"
                                    + this.username + " p:" + this.password + " db:" + this.dbName
                                    + " (select * from " + tableName
                                    + " order by random() limit 1000) UNION ALL " + mm.toString() + ");";
                    // break;
            }

            String command =
                    "echo \"create table " + tableName + " as " + sampleQuery + "\" | " + PYTHON_PATH
                            + " " + MADIS_PATH + " " + TMP_SAMPLE_DIR + tableName + ".db";

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

    private void countRows() throws Exception {
        // this.tableCount = new HashMap<String, Integer>();
        for (String tableName : this.statCols.keySet()) {

            String countRows = "select count(*) as count  from " + tableName;
            Statement crStmt = this.con.createStatement();
            ResultSet crRs = crStmt.executeQuery(countRows);
            int count = 0;

            while (crRs.next()) {
                count = crRs.getInt("count");
            }
            crRs.close();
            crStmt.close();
            this.tableCount.put(tableName, count);
        }

    }

    public void deleteSamples() throws Exception {
        // String[] cmd = {"/bin/sh", "-c",
        // "rm ./files/sample/*; rm ./files/json/*"};
        // Process process = Runtime.getRuntime().exec(cmd);
        // process.waitFor();
    }

    private void gatherStats() throws Exception {
        for (String s : this.statCols.keySet()) {
            Gatherer g = new Gatherer(TMP_SAMPLE_DIR + s + ".db", s);
            g.gather(GATHER_JSON);
        }
    }

    private void buildStats() throws Exception {
        String[] db = this.statCols.keySet().toArray(new String[this.statCols.keySet().size()]);

        StatBuilder sb = new StatBuilder(BUILD_JSON, db, HistogramBuildMethod.Primitive);
        sb.build();
    }

    public void closeConnection() throws SQLException {
        this.con.close();
    }

    public void createConnection(String vendorName) throws Exception {
        if (vendorName.equals("Oracle")) {
            this.vendor = Vendor.Oracle;
            Class.forName("oracle.jdbc.driver.OracleDriver");

            con = DriverManager
                    .getConnection("jdbc:oracle:thin:@" + this.ip + ":" + this.port + ":" + this.dbName,
                            this.username, this.password);
        } else if (vendorName.equals("Mysql")) {
            this.vendor = Vendor.Mysql;
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager
                    .getConnection("jdbc:mysql://" + this.ip + ":" + this.port + "/" + this.dbName,
                            this.username, this.password);
        } else if (vendorName.equals("Postgres")) {
            this.vendor = Vendor.Postgres;
        }
    }

}
