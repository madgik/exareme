package madgik.exareme.worker.art.executionPlan;//package madgik.exareme.db.art.executionPlan;
//
//import madgik.exareme.utils.file.FileUtil;
//
//import java.io.IOException;
//import java.io.ObjectOutputStream;
//import java.io.OutputStream;
//
///**
// * @author herald
// */
//public class SerializationPerformanceTest {
//    private static long bytes = 0;
//
//    public static void main(String[] args) throws Exception {
//        String planString =
//            FileUtil.readFile(ExecutionPlanImplTest.class.getResource("ArtPlan.txt"));
//        ExecutionPlanParser parser = new ExecutionPlanParser();
//        ExecutionPlan plan = (EditableExecutionPlan) parser.parse(planString);
//
//        ObjectOutputStream outStream = new ObjectOutputStream(new OutputStream() {
//
//            @Override public void write(int b) throws IOException {
//                bytes++;
//            }
//        });
//
//        long times = 50000;
//
//        long start = System.currentTimeMillis();
//        for (long i = 0; i < times; ++i) {
//            outStream.writeUnshared(plan);
//        }
//        long end = System.currentTimeMillis();
//
//        System.out.println((times / ((end - start) / 1000.0)) + " objects / sec");
//        System.out.println(bytes + " bytes");
//    }
//}
