/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema;

import madgik.exareme.common.app.engine.DMQuery;
import madgik.exareme.common.schema.expression.SQLBuildIndex;

import java.util.List;

/**
 * @author herald
 */
public class BuildIndex extends DMQuery {
    private static final long serialVersionUID = 1L;
    private SQLBuildIndex parsedSQLBuildIndex = null;

    public BuildIndex(int id, SQLBuildIndex sqlBuildIndex) {
        super(id, sqlBuildIndex.getTable(), "", sqlBuildIndex.getComments().toString());

        this.parsedSQLBuildIndex = sqlBuildIndex;

        StringBuilder query = new StringBuilder();
        query.append("create index ");
        query.append(sqlBuildIndex.getIndexName());
        query.append(" on ");
        query.append(sqlBuildIndex.getTable());
        query.append("(");

        List<String> columns = sqlBuildIndex.getColumns();

        for (int a = 0; a < columns.size(); ++a) {
            if (a == 0) {
                query.append(columns.get(a));
            } else {
                query.append(", ");
                query.append(columns.get(a));
            }
        }
        query.append(")");

        setQuery(query.toString());
    }

    public String getIndexName() {
        return parsedSQLBuildIndex.getIndexName();
    }

    public List<String> getColumns() {
        return parsedSQLBuildIndex.getColumns();
    }

    public SQLBuildIndex getParsedSQLBuildIndex() {
        return parsedSQLBuildIndex;
    }
}
