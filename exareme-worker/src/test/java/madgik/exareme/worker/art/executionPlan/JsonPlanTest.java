package madgik.exareme.worker.art.executionPlan;

/**
 * Created by vagos on 3/10/15.
 */
public class JsonPlanTest {
    public static void main(String[] arg) {

        //        String artPlan = "container c('" + proxy.getEntityName().getName() + "', 1000); \n"
        //                + "operator op c('AdpDBNetReaderOperator', " + "database='" + registry.getDatabase()
        //                + "', " + "table='" + table.getName() + "', " + "part='" + p.getpNum() + "', "
        //                + "sendHeader='" + sendHeader + "', " + "ip='" + name.getIP() + "', " + "port='" + name
        //                .getPort() + "');";

        String jsonArtPlan = "{\n" +
            "  \"containers\": [\n" +
            "    {\n" +
            "      \"name\": \"c\",\n" +
            "      \"IP\":" + "\"192.129\"" + ",\n" +
            "      \"port\": \"1000\"\n" +
            "    }],\n" +
            "  \"operators\": [\n" +
            "    {\n" +
            "      \"name\": \"op\",\n" +
            "      \"container\": \"c1\",\n" +
            "      \"operator\": \"'madgik.adp.operatorLibrary.test.HelloWorld'\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";


        String artPlan = "container c('" + "19.168.1.1" + "', 1000); \n"
            + "operator op c('AdpDBNetReaderOperator', " + "database='" + "dbase" + "', "
            + "table='" + "tName" + "', " + "part='" + "0" + "', " + "sendHeader='" + "handler"
            + "', " + "ip='" + "127.0.0.1" + "', " + "port='" + "1099" + "');";

        ExecutionPlan plan = null;
        try {
            ExecutionPlanParser parser = new ExecutionPlanParser();
            plan = parser.parse(jsonArtPlan.toString());
            System.out.println("Plan: " + plan.getContainer("c1").getIP());

        } catch (SemanticError semanticError) {
            System.err.print("Error creating plan" + semanticError);
        }
    }

}
