//package madgik.exareme.master.app.usecase;
//
//import madgik.exareme.master.client.AdpDBClient;
//import madgik.exareme.master.client.AdpDBClientProperties;
//import madgik.exareme.master.client.AdpDBClientQueryStatus;
//import madgik.exareme.master.app.cluster.ExaremeCluster;
//import madgik.exareme.master.app.cluster.ExaremeClusterFactory;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.IOUtils;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.File;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//
///**
// * @author alex
// */
//public class UseCaseHB {
//    private static final Logger log = Logger.getLogger(UseCaseHB.class);
//
//    private String dbpath = "/tmp/db/hb/";
//    private String loadQuery = null;
//    private String localQuery = null;
//    private String globalQuery = null;
//
//    @Before public void setUp() throws Exception {
//        Logger.getRootLogger().setLevel(Level.INFO);
//        log.debug("---- SETUP ----");
//
//        loadQuery = String.format(
//            new String(
//                Files.readAllBytes(
//                    Paths.get(UseCaseHB.class.getResource("load_hospital.sql").getFile())))
//            , UseCaseHB.class.getResource("Hospitals.tsv").getFile());
//        log.debug("\n" + loadQuery);
//        log.debug("---- SETUP ----");
//    }
//
//    @Test public void testQuery() throws Exception {
//
//        ExaremeCluster miniCluster = ExaremeClusterFactory.createMiniCluster(1099, 8088, 2);
//        miniCluster.start();
//
//        AdpDBClientProperties properties = new AdpDBClientProperties(dbpath);
//
//        AdpDBClient dbClient = miniCluster.getExaremeClusterClient(properties);
//
//        AdpDBClientQueryStatus queryResult = dbClient.query("load", loadQuery);
//        if (queryResult.hasException()){
//            throw new RuntimeException(queryResult.getException());
//        }
//        String load =
//            "distributed create table hospital_local to 3 on rid as\n"
//            + "select * \n"
//            + "from hospital;\n";
//        queryResult = dbClient.query("load", load);
//        if (queryResult.hasException()){
//            throw new RuntimeException(queryResult.getException());
//        }
//
//        String query =
//                  "distributed create temporary table hospital_sums to 1 as direct\n"
//                + " select\n"
//                      + "  colname,\n"
//                      + "  FSUM(FARITH('*', val, val)) as S2,\n"
//                      + "  FSUM(val) as S1,\n" + "  count(val) as N\n"
//                + "from (\n"
//                      + "select *\n"
//                      + "from hospital_local\n"
//                      + "where val <> 'NA'\n"
//                      + ")\n"
//                + "group by  colname;\n"
//                + "distributed create table attr_stats as\n"
//                + "select\n"
//                      + "  colname,\n"
//                      + "  FARITH('/',S1A,NA) as avgvalue,\n"
//                      + "  SQROOT(\n"
//                      + "    FARITH('/', '-', '*', NA, S2A, '*', S1A, S1A, '*', NA, '-', NA, 1)\n"
//                      + "  ) as stdvalue\n"
//                + "from ("
//                      + "select\n"
//                      + "  colname,\n"
//                      + "  FSUM(S2) as S2A,\n"
//                      + "  FSUM(S1) as S1A,\n"
//                      + "  SUM(N) as NA\n"
//                      + "from hospital_sums\n"
//                      + "group by colname"
//                      + ");";
//
//        log.info("\n"+query);
//        queryResult = dbClient.query("query", query);
//        if (queryResult.hasException()){
//            throw new RuntimeException(queryResult.getException());
//        }
//        log.info(IOUtils.toString(dbClient.readTable("attr_stats")));
//        miniCluster.stop(true);
//    }
//
//    @After public void tearDown() throws Exception {
//        log.debug("---- CLEAN ----");
//        FileUtils.deleteDirectory(new File(dbpath));
//        log.debug("---- CLEAN ----");
//
//    }
//}
