/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema;

import java.io.Serializable;

/**
 * @author heraldkllapi
 */
public class Statistics implements Serializable {

    private static final long serialVersionUID = 1L;
    private String database = null;

    public Statistics(String database) {
        this.database = database;
    }


}
