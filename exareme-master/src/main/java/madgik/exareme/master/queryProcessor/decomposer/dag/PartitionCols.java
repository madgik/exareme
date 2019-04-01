/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.dag;

import madgik.exareme.master.queryProcessor.decomposer.query.Column;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author dimitris
 */
public class PartitionCols {
    private HashSet<Column> cols;

    public PartitionCols() {
        this.cols = new HashSet();
    }

    @Override
    public String toString() {
        return "PartitionCols{" + "cols=" + cols + '}';
    }

    public HashSet<Column> getCols() {
        return cols;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.cols != null ? this.cols.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final PartitionCols other = (PartitionCols) obj;

        if (this.cols != other.cols && (this.cols == null || !this.cols.equals(other.cols))) {
            return false;
        }
        return true;
    }


    public PartitionCols(List<Column> c) {
        this.cols = new HashSet();
        for (Column cl : c) {
            this.cols.add(cl);
        }
    }

    public void addColumn(Column c) {
        this.cols.add(c);
    }

    public boolean isEmpty() {
        return this.cols.isEmpty();
    }

    public boolean contains(Column c) {
        return this.cols.contains(c);
    }

    public Column getFirstCol() {
        return this.cols.iterator().next();
    }

    public void addColumns(Iterable<Column> allColumnRefs) {
        for (Column c : allColumnRefs) {
            this.cols.add(c);
        }
    }

    public Set<Column> getColumns() {
        return this.cols;
    }

    public void retainAll(PartitionCols nextPcs) {
        this.cols.retainAll(nextPcs.getColumns());
    }

}
