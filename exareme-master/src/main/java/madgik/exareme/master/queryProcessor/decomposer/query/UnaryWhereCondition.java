/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dimitris
 */
public class UnaryWhereCondition implements Operand {

    private boolean not;
    private int type;
    public static final int IS_NULL = 0;
    public static final int LIKE = 1;
    private Column onColumn;
    private Object o;

    public UnaryWhereCondition(int type, Column c, boolean n) {
        super();
        this.type = type;
        this.onColumn = c;
        this.not = n;
        this.o = new Object();
    }

    public UnaryWhereCondition(int type, Column c, boolean n, Object o) {
        super();
        this.type = type;
        this.onColumn = c;
        this.not = n;
        this.o = o;
    }

    public Column getColumn() {
        return onColumn;
    }

    public void setColumn(Column column) {
        this.onColumn = column;
    }

    public int getType() {
        return this.type;
    }

    public boolean getNot() {
        return this.not;
    }

    @Override public String toString() {
        switch (type) {
            case IS_NULL:
                return not ?
                    onColumn.tableAlias + "." + onColumn.columnName + " IS NOT NULL" :
                    onColumn.tableAlias + "." + onColumn.columnName + " IS NULL";
            case LIKE:
                return onColumn.tableAlias + "." + onColumn.columnName + " LIKE \'" + o.toString()
                    + "\'";
            default:
                return super.toString();
        }
    }

    @Override public List<Column> getAllColumnRefs() {
        List<Column> result = new ArrayList<Column>();
        result.add(onColumn);
        return result;
    }

    @Override public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.not ? 1 : 0);
        hash = 97 * hash + this.type;
        hash = 97 * hash + (this.onColumn != null ? this.onColumn.hashCode() : 0);
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UnaryWhereCondition other = (UnaryWhereCondition) obj;
        if (this.not != other.not) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (this.onColumn != other.onColumn && (this.onColumn == null || !this.onColumn
            .equals(other.onColumn))) {
            return false;
        }
        return true;
    }

    @Override public void changeColumn(Column oldCol, Column newCol) {
        if (onColumn.columnName.equals(oldCol.columnName) && onColumn.tableAlias
            .equals(oldCol.tableAlias)) {
            this.onColumn = newCol;
        }
    }

    @Override public UnaryWhereCondition clone() throws CloneNotSupportedException {
        UnaryWhereCondition cloned = (UnaryWhereCondition) super.clone();
        cloned.setColumn(this.getColumn().clone());
        return cloned;
    }



}
