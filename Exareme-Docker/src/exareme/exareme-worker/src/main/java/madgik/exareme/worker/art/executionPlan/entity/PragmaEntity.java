/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.entity;

import java.io.Serializable;

/**
 * @author herald
 */
public class PragmaEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String pragmaName;
    public final String pragmaValue;

    public PragmaEntity(String name, String value) {
        this.pragmaName = name;
        this.pragmaValue = value;
    }
}
