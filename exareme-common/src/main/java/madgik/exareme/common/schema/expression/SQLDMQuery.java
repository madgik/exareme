/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema.expression;

/**
 * @author herald
 */
public class SQLDMQuery extends SQLQuery {

    private static final long serialVersionUID = 1L;
    private String table = null;

    public SQLDMQuery() {
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
