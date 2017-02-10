package madgik.exareme.worker.art.executionPlan;//package madgik.exareme.db.art.executionPlan;
//
//import junit.framework.TestCase;
//import madgik.exareme.db.art.executionPlan.entity.BufferEntity;
//import madgik.exareme.db.art.executionPlan.entity.BufferLinkEntity;
//import madgik.exareme.db.art.executionPlan.entity.OperatorEntity;
//import madgik.exareme.db.art.executionPlan.parser.expression.Buffer;
//import madgik.exareme.db.art.executionPlan.parser.expression.BufferLink;
//import madgik.exareme.db.art.executionPlan.parser.expression.Operator;
//import madgik.exareme.db.art.executionPlan.parser.expression.Parameter;
//import madgik.exareme.utils.file.FileUtil;
//import madgik.exareme.utils.serialization.SerializationUtil;
//
//import java.util.LinkedList;
//
///**
// * @author herald
// */
//public class ExecutionPlanImplTest extends TestCase {
//
//    private static ExecutionPlan plan = null;
//
//    public ExecutionPlanImplTest() {
//    }
//
//    @Override public void setUp() throws Exception {
//        String planString =
//            FileUtil.readFile(ExecutionPlanImplTest.class.getResource("ArtPlan.txt"));
//        ExecutionPlanParser parser = new ExecutionPlanParser();
//        plan = (EditableExecutionPlan) parser.parse(planString);
//    }
//
//    public void testReadOnly() throws Exception {
//        // The number of links is twice the size of buffers
//        testGraph(plan);
//    }
//
//    public void testEditable() throws Exception {
//        EditableExecutionPlan ePlan = (EditableExecutionPlan) SerializationUtil.deepCopy(plan);
//
//        for (OperatorEntity from : plan.iterateOperators()) {
//            for (OperatorEntity to : plan.getToLinks(from)) {
//                for (BufferEntity buffer : plan.getBuffers(from.operatorName, to.operatorName)) {
//                    ePlan.removeBufferLink(from.operatorName, buffer.bufferName);
//                    {
//                        Operator newOp = new Operator(buffer.bufferName + ".FROM_OP", "'a'",
//                            new LinkedList<Parameter>(), "", from.containerName);
//                        ePlan.addOperator(newOp);
//
//                        Buffer newBuffer =
//                            new Buffer(buffer.bufferName + ".FROM_B", "'a'", buffer.containerName,
//                                new LinkedList<Parameter>());
//                        ePlan.addBuffer(newBuffer);
//
//                        ePlan.addBufferLink(new BufferLink(from.operatorName, newBuffer.bufferName,
//                                from.containerName, new LinkedList<Parameter>()));
//                        ePlan.addBufferLink(new BufferLink(newBuffer.bufferName, newOp.operatorName,
//                                from.containerName, new LinkedList<Parameter>()));
//                        ePlan.addBufferLink(new BufferLink(newOp.operatorName, buffer.bufferName,
//                                from.containerName, new LinkedList<Parameter>()));
//                    }
//
//                    ePlan.removeBufferLink(buffer.bufferName, to.operatorName);
//                    {
//                        Operator newOp = new Operator(buffer.bufferName + ".TO_OP", "'a'",
//                            new LinkedList<Parameter>(), "", to.containerName);
//                        ePlan.addOperator(newOp);
//
//                        Buffer newBuffer =
//                            new Buffer(buffer.bufferName + ".RO_B", "'a'", buffer.containerName,
//                                new LinkedList<Parameter>());
//                        ePlan.addBuffer(newBuffer);
//
//                        ePlan.addBufferLink(new BufferLink(buffer.bufferName, newOp.operatorName,
//                            from.containerName, new LinkedList<Parameter>()));
//                        ePlan.addBufferLink(new BufferLink(newOp.operatorName, newBuffer.bufferName,
//                            to.containerName, new LinkedList<Parameter>()));
//                        ePlan.addBufferLink(
//                            new BufferLink(newBuffer.bufferName, to.operatorName, to.containerName,
//                                new LinkedList<Parameter>()));
//                    }
//                }
//            }
//        }
//
//        testGraph(ePlan);
//    }
//
//    private void testGraph(ExecutionPlan plan) throws Exception {
//        // The number of links is twice the size of buffers
//        assertEquals(2 * plan.getBufferCount(), plan.getBufferLinkCount());
//
//        // Every buffer is connected to only one reader and only one writer
//        for (OperatorEntity op : plan.iterateOperators()) {
//            OperatorEntity operator = plan.getOperator(op.operatorName);
//            assertEquals(operator, op);
//
//            for (OperatorEntity to : plan.getToLinks(op)) {
//                for (BufferEntity buffer : plan.getFromBuffers(to)) {
//                    BufferLinkEntity link2 = plan.getBufferLink(buffer.bufferName, to.operatorName);
//                    assertEquals(link2.operatorEntity, to);
//                }
//
//                for (OperatorEntity from : plan.getFromLinks(to)) {
//                    for (BufferEntity buffer : plan.getToBuffers(from)) {
//                        BufferLinkEntity link1 =
//                            plan.getBufferLink(from.operatorName, buffer.bufferName);
//                        assertEquals(link1.operatorEntity, from);
//                    }
//
//                    for (BufferEntity buffer : plan
//                        .getBuffers(from.operatorName, to.operatorName)) {
//                        BufferLinkEntity link1 =
//                            plan.getBufferLink(from.operatorName, buffer.bufferName);
//                        BufferLinkEntity link2 =
//                            plan.getBufferLink(buffer.bufferName, to.operatorName);
//
//                        assertEquals(link1.bufferEntity, link2.bufferEntity);
//                        assertEquals(link1.operatorEntity, from);
//                        assertEquals(link2.operatorEntity, to);
//                    }
//                }
//            }
//        }
//    }
//}
