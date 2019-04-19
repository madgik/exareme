/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author herald
 */
public class SQLBuildIndex extends SQLDMQuery {

    private static final long serialVersionUID = 1L;
    private String indexName = null;
    private ArrayList<String> columns = null;

    public SQLBuildIndex() {
        columns = new ArrayList<String>();
    }

    public void addColumn(String name) {
        this.columns.add(name);
    }

    public List<String> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
