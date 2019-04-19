/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine;

import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 * @author herald
 */
public class AdpDBDMOperator implements Serializable {
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(AdpDBDMOperator.class);
    private AdpDBOperatorType type = null;
    private DMQuery dm = null;
    private int part = -1;

    public AdpDBDMOperator(AdpDBOperatorType t, DMQuery dm) {
        this.type = t;
        this.dm = dm;
    }

    public AdpDBOperatorType getType() {
        return type;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public DMQuery getDMQuery() {
        return dm;
    }

    public void setDMQuery(DMQuery dm) {
        this.dm = dm;
    }
}
