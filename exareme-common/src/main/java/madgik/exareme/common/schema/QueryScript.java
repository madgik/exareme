/* Copyright MaDgIK Group 2010 - 2015.
        */
package madgik.exareme.common.schema;

import madgik.exareme.common.app.engine.DMQuery;
import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;

import java.io.Serializable;
import java.util.*;


/**
 * @author herald
 */
public class QueryScript implements Serializable {
    private static final long serialVersionUID = 1L;
    private String database = null;
    private String mappings = null;
    private HashMap<String, PhysicalTable> tables = null;
    private ArrayList<Select> selectQueries = null;
    private ArrayList<DMQuery> dmQueries = null;
    private SLA sla;

    public QueryScript(String database, String mappings) {
        this.database = database;
        this.mappings = mappings;
        this.tables = new HashMap<>();
        this.selectQueries = new ArrayList<>();
        this.dmQueries = new ArrayList<>();
    }

    public String getDatabase() {
        return database;
    }

    public SLA getSla() {
        return sla;
    }

    public void setSla(SLA sla) {
        this.sla = sla;
    }

    public void addTable(PhysicalTable t) {
        tables.put(t.getName(), t);
    }

    public void addQuery(Select query) {
        query.setDatabaseDir(database);
        query.setMappings(mappings);
        this.selectQueries.add(query);
    }

    public void addBuildIndex(BuildIndex buildIndex) {
        buildIndex.setDatabaseDir(database);
        this.dmQueries.add(buildIndex);
    }

    public void addDropIndex(DropIndex dropIndex) {
        dropIndex.setDatabaseDir(database);
        this.dmQueries.add(dropIndex);
    }

    public void addDropTable(DropTable dropTable) {
        dropTable.setDatabaseDir(database);
        this.dmQueries.add(dropTable);
    }

    public Collection<PhysicalTable> getTables() {
        return tables.values();
    }

    public PhysicalTable getTable(String name) {
        return tables.get(name);
    }

    public List<Select> getSelectQueries() {
        return Collections.unmodifiableList(selectQueries);
    }

    public Select getTreeLeafQuery() {
        return selectQueries.get(0);
    }

    public Select getTreeInternalQuery() {
        return selectQueries.get(1);
    }

    public Select getTreeRootQuery() {
        return selectQueries.get(2);
    }

    public List<DMQuery> getDMQueries() {
        return Collections.unmodifiableList(dmQueries);
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Select s : selectQueries) {
            sb.append(s.getParsedSqlQuery().getSql());
        }
        return sb.toString();
    }
}
