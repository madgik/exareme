/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine;

import madgik.exareme.common.schema.Query;

/**
 * @author herald
 */
public class DMQuery extends Query {
    private static final long serialVersionUID = 1L;
    private String table = null;

    public DMQuery(int id, String table, String query, String comments) {
        super(id, query, comments);
        this.table = table;
    }

    public String getTable() {
        return table;
    }
}
