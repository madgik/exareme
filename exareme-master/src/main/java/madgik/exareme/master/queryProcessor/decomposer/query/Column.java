/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author heraldkllapi
 */
public class Column implements Operand {

    public String tableAlias = null;
    public String columnName = null;

    public Column() {
        super();
    }

    public Column(String alias, String name) {
        this.tableAlias = alias;
        this.columnName = name;
    }

    @Override public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Column)) {
            return false;
        }
        Column otherCol = (Column) other;
        if (this.tableAlias == null) {
            return this.columnName.equals(otherCol.columnName);
        }
        return (this.columnName.equals(otherCol.columnName) && this.tableAlias
            .equals(otherCol.tableAlias));
    }

    @Override public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.tableAlias);
        hash = 89 * hash + Objects.hashCode(this.columnName);
        return hash;
    }

    @Override public String toString() {
        if (tableAlias != null) {
            return tableAlias + "." + columnName;
        } else {
            return columnName;
        }
    }

    @Override public List<Column> getAllColumnRefs() {
        List<Column> res = new ArrayList<Column>();
        res.add(this);
        return res;
    }

    @Override public void changeColumn(Column oldCol, Column newCol) {
        if (this.columnName.equals(oldCol.columnName) && this.tableAlias
            .equals(oldCol.tableAlias)) {
            this.columnName = newCol.columnName;
            this.tableAlias = newCol.tableAlias;
        }
    }

    @Override public Column clone() throws CloneNotSupportedException {
        Column cloned = (Column) super.clone();
        return cloned;
    }
}
