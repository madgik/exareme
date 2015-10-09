/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan;

/**
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class PlanExpressionConstants {

    public final static String BUFFERS = "buffers";
    public final static String BUFFER_LINKS = "links";
    public final static String CONTAINERS = "containers";
    public final static String OPERATORS = "operators";
    public final static String OPERATORNAME = "name";
    public final static String PRAGMA = "pragma";
    public final static String LOCATIONS = "locations";
    public final static String NAME = "name";
    public final static String IP = "IP";
    public final static String PORT = "port";
    public final static String CONTAINER = "container";
    public final static String FROM = "from";
    public final static String TO = "to";
    public final static String QOS = "QoS";
    public final static String OPERATOR = "operator";
    public final static String PARAMETERS = "parameters";
    public final static String QUERYSTRING = "queryString";
    public final static String VALUE = "value";
    public static String CONTAINERNAME = "container";
    public static String PRAGMANAME = "name";
    public static String PRAGMAVALUE = "value";
    public static String OPERATOR_LINKS = "op_links";
    public static String DATATRANSFERPORT = "data_transfer_port";

    private PlanExpressionConstants() {
        throw new RuntimeException("Cannot create instances of this class");
    }
}
