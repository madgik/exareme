/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema;

import madgik.exareme.common.app.engine.DMQuery;
import madgik.exareme.common.schema.expression.SQLDropTable;

/**
 * @author herald
 */
public class DropTable extends DMQuery {

    private static final long serialVersionUID = 1L;
    private SQLDropTable parsedSQLDropTable = null;

    public DropTable(int id, SQLDropTable sqlDropTable) {
        super(id, sqlDropTable.getTable(), "drop table " + sqlDropTable.getTable(),
            sqlDropTable.getComments().toString());
        this.parsedSQLDropTable = sqlDropTable;
    }

    public SQLDropTable getParsedSQLDropTable() {
        return parsedSQLDropTable;
    }
}
