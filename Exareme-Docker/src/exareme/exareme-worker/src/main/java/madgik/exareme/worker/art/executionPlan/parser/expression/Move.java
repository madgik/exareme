/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.parser.expression;

import java.io.Serializable;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class Move implements Serializable {

    public String fromContainerName;
    public String toContainerName;
    public String operatorName;

    public Move(String operatorName, String fromContainerName, String toContainerName) {
        this.operatorName = operatorName;
        this.fromContainerName = fromContainerName;
        this.toContainerName = toContainerName;
    }
}
