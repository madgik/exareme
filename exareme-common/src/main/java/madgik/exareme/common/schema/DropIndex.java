/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema;

import madgik.exareme.common.app.engine.DMQuery;
import madgik.exareme.common.schema.expression.SQLDropIndex;

/**
 * @author herald
 */
public class DropIndex extends DMQuery {
    private static final long serialVersionUID = 1L;
    private SQLDropIndex parsedSQLDropIndex = null;

    public DropIndex(int id, SQLDropIndex sqlDropIndex) {
        super(id, sqlDropIndex.getTable(), "drop index " + sqlDropIndex.getIndexName(),
            sqlDropIndex.getComments().toString());
        this.parsedSQLDropIndex = sqlDropIndex;
    }

    public String getIndexName() {
        return parsedSQLDropIndex.getIndexName();
    }

    public SQLDropIndex getParsedSQLDropIndex() {
        return parsedSQLDropIndex;
    }
}
