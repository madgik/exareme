/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema.expression;

/**
 * @author herald
 */
public class SQLDropIndex extends SQLDMQuery {

    private static final long serialVersionUID = 1L;
    private String indexName = null;

    public SQLDropIndex() {
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
