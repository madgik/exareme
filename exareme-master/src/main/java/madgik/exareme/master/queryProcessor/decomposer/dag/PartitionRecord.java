/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.dag;

import madgik.exareme.master.queryProcessor.decomposer.query.Column;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dimitris
 */
public class PartitionRecord {
    private Set<PartitionCols> record;

    public PartitionRecord() {
        record = new HashSet<PartitionCols>();
    }

    void add(PartitionCols s) {
        this.record.add(s);
    }

    Iterable<PartitionCols> getPartitionCols() {
        //TODO: remove this! implement functionality in class
        return record;
    }

    public boolean contains(PartitionCols ptned) {
        return this.contains(ptned);
    }

    public PartitionCols getPartitionColsFor(Column c) {
        for (PartitionCols pc : record) {
            if (pc.contains(c)) {
                return pc;
            }
        }
        return null;
    }

    public boolean isDisjointWith(PartitionCols other) {
        for (PartitionCols pc : record) {
            if (!Collections.disjoint(pc.getColumns(), other.getColumns())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "PartitionRecord{" + "record=" + record + '}';
    }

}
