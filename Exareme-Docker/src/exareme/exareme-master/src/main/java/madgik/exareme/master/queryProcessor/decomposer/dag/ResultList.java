/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.dag;

import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;
import madgik.exareme.master.queryProcessor.decomposer.query.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dimitris
 */
public class ResultList extends ArrayList<SQLQuery> {
    private Table lastTable;
    private SQLQuery current;
    private Map<String, String> baseTableTracker = new HashMap<String, String>();

    public void setLastTable(SQLQuery e) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).equals(e)) {
                String t = this.get(i).getTemporaryTableName();
                lastTable = new Table(t, t);//this.get(i).getTemporaryTableName();
                return;
            }
        }
    }

    public SQLQuery getCurrent() {
        return current;
    }

    public void setCurrent(SQLQuery current) {
        this.current = current;
    }

    public void setLastTable(Table t) {
        this.lastTable = t;
    }

    public Table getLastTable() {
        return this.lastTable;
    }

    @Override
    public boolean add(SQLQuery e) {
        if (this.contains(e)) {
            this.setLastTable(e);
            return true;
        } else {
            String t = e.getTemporaryTableName();
            this.lastTable = new Table(t, t);
            //this.lastTableName = e.getTemporaryTableName();
            return super.add(e);
        }
    }

    @Override
    public void add(int index, SQLQuery element) {
        String t = element.getTemporaryTableName();
        this.lastTable = new Table(t, t);//element.getTemporaryTableName();
        super.add(index, element);
    }

    public void trackBaseTableFromQuery(String base, String query) {
        this.baseTableTracker.put(base, query);
    }

    public String getQueryForBaseTable(String base) {
        return this.baseTableTracker.get(base);
    }

    public SQLQuery get(String query) {
        for (SQLQuery s : this) {
            if (s.getTemporaryTableName().equals(query)) {
                return s;
            }
        }
        return null;
    }


}
