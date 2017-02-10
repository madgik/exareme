/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.worker.art.executionPlan.deserializers.ContainerDeserialiser;
import madgik.exareme.worker.art.executionPlan.deserializers.ExecutionPlanDeserialiser;
import madgik.exareme.worker.art.executionPlan.deserializers.OperatorDeserialiser;
import madgik.exareme.worker.art.executionPlan.parser.expression.*;
import madgik.exareme.worker.art.manager.ArtManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.*;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author johnchronis
 */
public class JsonParserTest {

    static ArtManager manager = null;

    public JsonParserTest() {
    }

    @BeforeClass public static void setUpClass() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
    }

    @AfterClass public static void tearDownClass() {
    }

    @Before public void setUp() {
    }

    @After public void tearDown() {
    }

    @Test public void compareHelloWorld() throws Exception {
        System.out.println("HelloWorldDemo/////////////////////////////////////////");
        compareExpressions("demo/art/HelloWorldDemo");
    }

    @Test public void compareQuery() throws Exception {
        System.out.println("Query/////////////////////////////////////////");
        compareExpressions("demo/art/Query");
    }

    @Test public void compareQuery2() throws Exception {
        System.out.println("Query2/////////////////////////////////////////");
        compareExpressions("demo/art/Query2");
    }

    public void compareExpressions(String filename) throws Exception {

    /*read and proccess .ep file*/
  /*  String planEP = FileUtil.readFile(new File(filename + ".ep"));
    System.out.println(planEP);

    ByteArrayInputStream stream = new ByteArrayInputStream(planEP.getBytes());
    ExecutionPlanQueryParser parser = new ExecutionPlanQueryParser(stream);
    PlanExpression expressionEP = parser.parse();*/

    /*read and proccess .json file*/
        String planJson = FileUtil.readFile(new File(JsonParserTest.class.getClassLoader()
                        .getResource("madgik/exareme/worker/art/HelloWorldDemo.json").getFile()));
        //String planJson = FileUtil.readFile(new File(filename + ".json"));
        System.out.println(planJson);

        final GsonBuilder gsonBuilder = new GsonBuilder();

        //        gsonBuilder.registerTypeAdapter(Buffer.class, new BufferDeserialiser());
        //        gsonBuilder.registerTypeAdapter(BufferLink.class, new BufferLinkDeserialiser());
        gsonBuilder.registerTypeAdapter(Container.class, new ContainerDeserialiser());
        gsonBuilder.registerTypeAdapter(PlanExpression.class, new ExecutionPlanDeserialiser());
        gsonBuilder.registerTypeAdapter(Operator.class, new OperatorDeserialiser());

        final Gson gson = gsonBuilder.create();

        Reader reader = new StringReader(planJson);
        final PlanExpression expressionJson = gson.fromJson(reader, PlanExpression.class);

        printExpression(expressionJson);

    /*compare the two expressions*/
  /*  assertEquals(expressionEP.bufferConnectList.size(), expressionJson.bufferConnectList.size());
    assertEquals(expressionEP.bufferList.size(), expressionJson.bufferList.size());
    assertEquals(expressionEP.containersList.size(), expressionJson.containersList.size());
    assertEquals(expressionEP.operatorList.size(), expressionJson.operatorList.size());
    assertEquals(expressionEP.pragmaList.size(), expressionJson.pragmaList.size());
    assertEquals(expressionEP.stateLinkList.size(), expressionJson.stateLinkList.size());
    assertEquals(expressionEP.stateList.size(), expressionJson.stateList.size());
    assertEquals(expressionEP.switchConnectList.size(), expressionJson.switchConnectList.size());
    assertEquals(expressionEP.switchList.size(), expressionJson.switchList.size());

    for (BufferLink bflink : expressionEP.bufferConnectList) {
      assertTrue(expressionJson.bufferConnectList.contains(bflink));
    }

    for (Buffer buffer : expressionEP.bufferList) {
      assertTrue(expressionJson.bufferList.contains(buffer));
    }

    for (madgik.exareme.db.art.executionPlan.parser.expression.Container cont : expressionEP.containersList) {
      assertTrue(expressionJson.containersList.contains(cont));
    }

    for (Operator op : expressionEP.operatorList) {
      assertTrue(expressionJson.operatorList.contains(op));
    }

    for (Pragma pragma : expressionEP.pragmaList) {
      assertTrue(expressionJson.pragmaList.contains(pragma));
    }

    for (StateLink statelink : expressionEP.stateLinkList) {
      assertTrue(expressionJson.stateLinkList.contains(statelink));
    }

    for (State state : expressionEP.stateList) {
      assertTrue(expressionJson.stateList.contains(state));
    }

    for (SwitchLink switchlink : expressionEP.switchConnectList) {
      assertTrue(expressionJson.switchConnectList.contains(switchlink));
    }

    for (Switch swtch : expressionEP.switchList) {
      assertTrue(expressionJson.switchList.contains(swtch));
    }*/

    }

    public void printExpression(PlanExpression pe) {
        System.out.println("bufferlinks");
        //        for (BufferLink bflink : pe.bufferConnectList) {
        //            System.out.println(bflink.containerName);
        //        }
        System.out.println("buffers");
        //        for (Buffer buffer : pe.bufferList) {
        //            System.out.println(buffer.bufferName);
        //        }
        System.out.println("containers");
        for (Container cont : pe.containersList) {
            System.out.println(cont.name);
        }
        System.out.println("operators");
        for (Operator op : pe.operatorList) {
            System.out.println(op.operatorName);

        }
        System.out.println("pragmas");
        for (Pragma pragma : pe.pragmaList) {
            System.out.println(pragma.pragmaName);
        }
        System.out.println("statelinks");
        for (StateLink statelink : pe.stateLinkList) {
            System.out.println(statelink.stateName);
        }
        System.out.println("states");
        for (State state : pe.stateList) {
            System.out.println(state.stateName);
        }
        System.out.println("switchlinks");
        for (SwitchLink switchlink : pe.switchConnectList) {
            System.out.println(switchlink.containerName);
        }
        System.out.println("switches");
        for (Switch swtch : pe.switchList) {
            System.out.println(swtch.switchName);
        }
    }

  /*@Test
   public void compareExecutionPlans() throws Exception {
   ExecutionPlanParser planParser = new ExecutionPlanParser();

   /*read and proccess .ep file*//*
   String planEP = FileUtil.readFile(new File("demo/art/HelloWorldDemo.ep"));
   System.out.println(planEP);

   ExecutionPlan executionPlanEP = planParser.parse(planEP);

   /*read and proccess .json file*//*
   String planJson = FileUtil.readFile(new File("demo/art/HelloWorldDemo.json"));
   ExecutionPlan executionPlanJson = planParser.parsej(planJson);

   /*compare the two produced PlanExpressions*//*
   }
    

   /*public void compareExecutionPlans(ExecutionPlan A, ExecutionPlan B) throws SemanticError {

   //containers
   assertEquals(A.getContainerCount(), B.getContainerCount());
   for (String container : A.iterateContainers()) {
   EntityName entityNameA = A.getContainer(container);
   EntityName entityNameB = B.getContainer(container);
   assertNotNull(entityNameA);
   assertNotNull(entityNameB);
   assertEquals(entityNameA.getName(), entityNameB.getName());
   assertEquals(entityNameA.getIP(), entityNameB.getIP());
   assertEquals(entityNameA.getPort(), entityNameB.getPort());
   }

   //operators
   assertEquals(A.getOperatorCount(), B.getOperatorCount());
   for (OperatorEntity opA : A.iterateOperators()) {
   OperatorEntity opB = B.getOperator(opA.operatorName);
   assertNotNull(opA);
   assertNotNull(opB);
   compareOperatorEntity(opA, opB);

   List<OperatorEntity> AFromList = new ArrayList<OperatorEntity>();
   List<OperatorEntity> AToList = new ArrayList<OperatorEntity>();
   List<OperatorEntity> BFromList = new ArrayList<OperatorEntity>();
   List<OperatorEntity> BToList = new ArrayList<OperatorEntity>();

   for (OperatorEntity Afrom : A.getFromLinks(opA)) {
   AFromList.add(Afrom);
   }
   for (OperatorEntity Ato : A.getToLinks(opA)) {
   AToList.add(Ato);
   }
   for (OperatorEntity Bfrom : A.getFromLinks(opA)) {
   assertTrue.
   }
   for (OperatorEntity Bto : A.getFromLinks(opA)) {
   BToList.add(Bto);
   }
            
   assertEquals(AFromList, BFromList);
   assertEquals(AToList, BToList);

   }
        
        
   assertEquals(A.getPragmaCount(), B.getPragmaCount());
   for (PragmaEntity pragma : A.iteratePragmas()) {
   PragmaEntity pragmaA = A.getPragma(pragma.pragmaName);
   PragmaEntity pragmaB = B.getPragma(pragma.pragmaName);
   assertNotNull(pragmaA);
   assertNotNull(pragmaB);
   assertEquals(pragmaA.pragmaName, pragmaB.pragmaName);
   assertEquals(pragmaA.pragmaValue, pragmaB.pragmaValue);
   }

   assertEquals(A.getBufferLinkCount(), B.getBufferLinkCount());
   String from, to;
   for (BufferLinkEntity bflink : A.iterateBufferLinks()) {
   if (bflink.type.equals(BufferLinkEntity.ConnectDirection.reader)) {
   from = bflink. ;
   to = ; 
   }

   BufferLinkEntity bflinkA = A.getBufferLink(bflink.);
   BufferLinkEntity bflinkB = B.getBufferLink(bflink);
   assertNotNull(pragmaA);
   assertNotNull(pragmaB);
   assertEquals(pragmaA.pragmaName, pragmaB.pragmaName);
   assertEquals(pragmaA.pragmaValue, pragmaB.pragmaValue);
   }

   }

   private void compareOperatorEntity(OperatorEntity opA, OperatorEntity opB) {
   assertEquals(opA.operator, opB.operator);
   assertEquals(opA.operatorName, opB.operatorName);
   assertEquals(opA.queryString, opB.queryString);
   assertEquals(opA.containerName, opB.containerName);
   }*/


}
