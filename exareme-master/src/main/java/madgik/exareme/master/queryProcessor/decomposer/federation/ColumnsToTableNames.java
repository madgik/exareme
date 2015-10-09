/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.query.Column;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author dimitris
 */
public class ColumnsToTableNames<T> extends HashMap<String, T> {

    public ColumnsToTableNames() {
        super();
    }

    public void putColumnInTable(Column c, T t) {
        this.put(c.tableAlias + ":" + c.columnName, t);
    }

    public T getTablenameForColumn(Column c) {
        return this.get(c.tableAlias + ":" + c.columnName);
    }

    public boolean containsColumn(Column c) {
        return this.containsKey(c.tableAlias + ":" + c.columnName);
    }

    public List<Column> getAllColumns() {
        List<Column> result = new ArrayList<Column>();
        for (String c : this.keySet()) {
            result.add(new Column(c.split(":")[0], c.split(":")[1]));
        }
        return result;
    }

    void changeColumns(T toChange, T join) {
        for (String s : this.keySet()) {
            if (get(s).equals(toChange)) {
                put(s, join);
            }
        }
    }
}
