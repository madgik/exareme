/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.estimator;

import java.io.Serializable;

/**
 * @author heraldkllapi
 */
public class EstimatorResult implements Serializable {

    public final String name;
    public final boolean foundInHistory;

    public EstimatorResult(String name, boolean foundInHistory) {
        this.name = name;
        this.foundInHistory = foundInHistory;
    }
}
