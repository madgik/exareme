//package madgik.exareme.db.art.executionPlan;
//
//import madgik.exareme.utils.file.FileUtil;
//
///**
// * @author herald
// */
//public class AnyContainerTest {
//    private static long bytes = 0;
//
//    public static void main(String[] args) throws Exception {
//        String planString =
//            FileUtil.readFile(ExecutionPlanImplTest.class.getResource("ArtPlanAny.txt"));
//        ExecutionPlanParser parser = new ExecutionPlanParser();
//        ExecutionPlan plan = parser.parse(planString);
//
//        for (String container : plan.iterateContainers()) {
//            System.out.println(container);
//        }
//    }
//}
