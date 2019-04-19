///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package madgik.exareme.master.engine.registry;
//
//import com.google.gson.Gson;
//import madgik.exareme.common.schema.Index;
//import madgik.exareme.common.schema.Partition;
//import madgik.exareme.common.schema.PhysicalTable;
//import madgik.exareme.common.schema.Table;
//import madgik.exareme.master.registry.Registry;
//import org.junit.*;
//
//import java.util.Collection;
//import java.util.List;
//
///**
// * @author Christoforos Svingos
// */
//public class RegistryTest {
//
//    public RegistryTest() {
//    }
//
//    @BeforeClass public static void setUpClass() {
//    }
//
//    @AfterClass public static void tearDownClass() {
//    }
//
//    @Before public void setUp() {
//    }
//
//    @After public void tearDown() {
//    }
//
//    /**
//     * Test of getDatabase method, of class XrsRegistry.
//     */
//    @Test public void testGetDatabase() {
//        System.out.println("getDatabase");
//        Registry instance = null;
//        String expResult = "";
//        String result = instance.getDatabase();
//        Assert.assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        Assert.fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTableDefinitions method, of class XrsRegistry.
//     */
//    @Test public void testGetTableDefinitions() {
//        System.out.println("getTableDefinitions");
//        Registry instance = null;
//        List<String> expResult = null;
//        List<String> result = instance.getTableDefinitions();
//        Assert.assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        Assert.fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addPhysicalTable, getPhysicalTable and removePhysicalTable methods,
//     * of class XrsRegistry.
//     */
//    @Test public void testPhysicalTableOperations() {
//        System.out.println("testPhysicalTableOperations");
//        Registry instance = Registry.getInstance("/home/xrs/Desktop");
//        PhysicalTable[] physicalTables = new PhysicalTable[2];
//
//    /* Create and insert PhysicalTable objects */
//
//        for (int i = 0; i < physicalTables.length; ++i) {
//            // Create table
//            Table table = new Table("test_table_" + i);
//            table.setSqlDefinition("Select * from test_table_" + i + ";");
//
//            // Create partitions
//            Partition partition0 = new Partition(table.getName(), 0);
//            partition0.addLocation("127.0.0.1");
//            partition0.addLocation("127.0.0.2");
//            partition0.addPartitionColumn("field1");
//            partition0.addPartitionColumn("field2");
//
//            Partition partition1 = new Partition(table.getName(), 1);
//            partition1.addLocation("127.0.0.2");
//            partition1.addLocation("127.0.0.3");
//            partition1.addPartitionColumn("field1");
//            partition1.addPartitionColumn("field2");
//
//            Partition partition2 = new Partition(table.getName(), 2);
//            partition2.addLocation("127.0.0.1");
//            partition2.addLocation("127.0.0.3");
//            partition2.addPartitionColumn("field1");
//            partition2.addPartitionColumn("field2");
//
//            // Create indexes
//            Index index0 = new Index(table.getName(), "field1", "test_table_" + i + "_index0");
//            index0.addPartition(0);
//            index0.addPartition(1);
//
//            Index index1 = new Index(table.getName(), "field1", "test_table__" + i + "_index1");
//            index1.addPartition(0);
//            index1.addPartition(1);
//
//            // Create Physical Table
//            physicalTables[i] = new PhysicalTable(table);
//            physicalTables[i].addPartition(partition0);
//            physicalTables[i].addPartition(partition1);
//            physicalTables[i].addPartition(partition2);
//            physicalTables[i].addIndex(index0);
//            physicalTables[i].addIndex(index1);
//
//            instance.addPhysicalTable(physicalTables[i]);
//        }
//
//        Gson gson = new Gson();
//
//        for (int i = 0; i < physicalTables.length; ++i) {
//
//            // For Debug
//      /*
//      System.out.println("Original PhysicalTable must be equal with Registry PhysicalTable");
//      System.out.println("Original PhysicalTable: " + gson.toJson(physicalTables[i]));
//      System.out.println("Registry PhysicalTable: " +
//                         gson.toJson(instance.getPhysicalTable(physicalTables[i].getName())));
//      */
//
//            Assert.assertEquals("Original PhysicalTable must be equal with Registry PhysicalTable",
//                gson.toJson(physicalTables[i]),
//                gson.toJson(instance.getPhysicalTable(physicalTables[i].getName())));
//
//            instance.removePhysicalTable(physicalTables[i].getName());
//            Assert.assertEquals("Not Exists Physical Table must return NULL",
//                instance.getPhysicalTable(physicalTables[i].getName()), null);
//        }
//    }
//
//    /**
//     * Test of containsPhysicalTable method, of class XrsRegistry.
//     */
//    @Test public void testContainsPhysicalTable() {
//        System.out.println("containsPhysicalTable");
//        String name = "";
//        Registry instance = null;
//        boolean expResult = false;
//        boolean result = instance.containsPhysicalTable(name);
//        Assert.assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        Assert.fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPhysicalTables method, of class XrsRegistry.
//     */
//    @Test public void testGetPhysicalTables() {
//        System.out.println("getPhysicalTables");
//        Registry instance = null;
//        Collection<PhysicalTable> expResult = null;
//        Collection<PhysicalTable> result = instance.getPhysicalTables();
//        Assert.assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        Assert.fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addIndex method, of class XrsRegistry.
//     */
//    @Test public void testAddIndex() {
//        System.out.println("addIndex");
//        Index idx = null;
//        Registry instance = null;
//        instance.addIndex(idx);
//        // TODO review the generated test code and remove the default call to fail.
//        Assert.fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setMappings method, of class XrsRegistry.
//     */
//    @Test public void testSetMappings() {
//        System.out.println("setMappings");
//        String mappings = "";
//        Registry instance = null;
//        instance.setMappings(mappings);
//        // TODO review the generated test code and remove the default call to fail.
//        Assert.fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getMappings method, of class XrsRegistry.
//     */
//    @Test public void testGetMappings() {
//        System.out.println("getMappings");
//        Registry instance = null;
//        String expResult = "";
//        String result = instance.getMappings();
//        Assert.assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        Assert.fail("The test case is a prototype.");
//    }
//
//}
