/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema.expression;

import java.io.Serializable;

/**
 * @author herald
 */
public class SQLQuery implements Serializable {
    private static final long serialVersionUID = 1L;
    private Comments comments = null;
    private String sql = null;
    private String partsDefn = null;

    public SQLQuery() {
    }

    public Comments getComments() {
        return comments;
    }

    public void setComments(Comments comments) {
        this.comments = comments;
    }

    public String getPartsDefn() {
        return partsDefn;
    }

    public void setPartsDefn(String partitionDefn) {
        this.partsDefn = partitionDefn;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
