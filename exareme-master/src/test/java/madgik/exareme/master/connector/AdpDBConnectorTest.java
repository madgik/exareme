package madgik.exareme.master.connector;//package madgik.exareme.master.connector;
//
//import madgik.exareme.common.schema.PhysicalTable;
//import madgik.exareme.common.schema.Table;
//import madgik.exareme.master.engine.AdpDBManager;
//import madgik.exareme.master.engine.AdpDBManagerFactory;
//import madgik.exareme.master.registry.Registry;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;

//public class AdpDBConnectorTest {
//    private static Logger log = Logger.getLogger(AdpDBConnectorUtil.class);
//
//    private AdpDBManager manager;
//
//    @Before public void setUp() throws Exception {
//        log.info("Set up...");
//        manager = AdpDBManagerFactory.createManager("127.0.0.1", 1098, 8088);
//    }
//
//    @After public void tearDown() throws Exception {
//        log.info("Tear down...");
//        manager.stopManager();
//    }
//
//    @Test public void testReadTable() throws Exception {
//        log.info("Test...");
//        Logger.getRootLogger().setLevel(Level.ALL);
//        AdpDBConnectorUtil
//            .readRemoteTablePart(Registry.getInstance("/tmp/db/client-test-1423497619793"),
//                new PhysicalTable(new Table("emp")), null, null, System.out);
//
//    }
//}
