/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author herald
 */
public class SQLScript implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<SQLSelect> queries = null;
    private ArrayList<SQLBuildIndex> buildIndexes = null;
    private ArrayList<SQLDropTable> dropTables = null;
    private ArrayList<SQLDropIndex> dropIndexes = null;

    // Views
    private List<SQLSelect> queriesView = null;
    private List<SQLBuildIndex> buildIndexesView = null;
    private List<SQLDropTable> dropTablesView = null;
    private List<SQLDropIndex> dropIndexesView = null;

    public SQLScript() {
        queries = new ArrayList<SQLSelect>();
        queriesView = Collections.unmodifiableList(queries);

        buildIndexes = new ArrayList<SQLBuildIndex>();
        buildIndexesView = Collections.unmodifiableList(buildIndexes);

        dropTables = new ArrayList<SQLDropTable>();
        dropTablesView = Collections.unmodifiableList(dropTables);

        dropIndexes = new ArrayList<SQLDropIndex>();
        dropIndexesView = Collections.unmodifiableList(dropIndexes);
    }

    public void addSelect(SQLSelect query) {
        queries.add(query);
    }

    public void addBuildIndex(SQLBuildIndex buildIndex) {
        buildIndexes.add(buildIndex);
    }

    public void addDropTable(SQLDropTable dropTable) {
        dropTables.add(dropTable);
    }

    public void addDropIndex(SQLDropIndex dropIndex) {
        dropIndexes.add(dropIndex);
    }

    public List<SQLSelect> getQueries() {
        return queriesView;
    }

    public List<SQLBuildIndex> getBuildIndexes() {
        return buildIndexesView;
    }

    public List<SQLDropTable> getDropTables() {
        return dropTablesView;
    }

    public List<SQLDropIndex> getDropIndexes() {
        return dropIndexesView;
    }
}
