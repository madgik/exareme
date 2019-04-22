/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.parser.expression;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class Destroy {

    public String containerName;
    public String objectName;

    public Destroy(String objectName, String containerName) {
        this.containerName = containerName;
        this.objectName = objectName;
    }
}
