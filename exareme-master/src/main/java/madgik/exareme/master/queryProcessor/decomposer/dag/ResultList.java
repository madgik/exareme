/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.dag;

import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;

import java.util.ArrayList;

/**
 * @author dimitris
 */
public class ResultList extends ArrayList<SQLQuery> {
    private String lastTableName;
    private SQLQuery current;

    public void setLastTableName(SQLQuery e) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).equals(e)) {
                lastTableName = this.get(i).getTemporaryTableName();
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

    public void setLastTableName(String t) {
        this.lastTableName = t;
    }

    public String getLastTableName() {
        return this.lastTableName;
    }

    @Override public boolean add(SQLQuery e) {
        if (this.contains(e)) {
            this.setLastTableName(e);
            return true;
        } else {
            this.lastTableName = e.getTemporaryTableName();
            return super.add(e);
        }
    }

    @Override public void add(int index, SQLQuery element) {
        this.lastTableName = element.getTemporaryTableName();
        super.add(index, element);
    }


}
