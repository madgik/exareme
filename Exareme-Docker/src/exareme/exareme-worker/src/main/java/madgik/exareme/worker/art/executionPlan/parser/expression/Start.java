/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.parser.expression;

import java.io.Serializable;

/**
 * @author Herald Kllapi <br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class Start implements Serializable {

    private static final long serialVersionUID = 1L;
    public final String operatorName;
    public final String containerName;

    public Start(String operatorName, String containerName) {
        this.operatorName = operatorName;
        this.containerName = containerName;
    }
}
