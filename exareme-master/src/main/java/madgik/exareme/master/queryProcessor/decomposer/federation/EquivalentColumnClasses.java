/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.dag.PartitionCols;
import madgik.exareme.master.queryProcessor.decomposer.query.Column;
import madgik.exareme.master.queryProcessor.decomposer.query.NonUnaryWhereCondition;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dimitris
 */
public class EquivalentColumnClasses {
    private Map<PartitionCols, Boolean> classes;
    private PartitionCols deliverdPart;

    public EquivalentColumnClasses(Map<PartitionCols, Boolean> c) {
        this.classes = c;
    }

    public EquivalentColumnClasses() {
        this.classes = new HashMap<PartitionCols, Boolean>();
    }

    public PartitionCols getClassForColumn(Column c) {
        for (PartitionCols pc : this.classes.keySet()) {
            if (pc.contains(c)) {
                return pc;
            }
        }
        PartitionCols pc = new PartitionCols();
        pc.addColumn(c);
        return pc;
    }

    public boolean partitionHistoryContains(Column c) {
        for (PartitionCols pc : this.classes.keySet()) {
            if (pc.contains(c)) {
                return this.classes.get(pc);
            }
        }
        return false;
    }


    public void setClassRepartitioned(Column c, boolean setLast) {
        for (PartitionCols pc : this.classes.keySet()) {

            if (pc.contains(c)) {
                this.classes.put(pc, Boolean.TRUE);
                if (setLast) {
                    deliverdPart = pc;
                }
                return;
            }
        }
        PartitionCols newPc = new PartitionCols();
        newPc.addColumn(c);
        if (setLast) {
            deliverdPart = newPc;
        }
        this.classes.put(newPc, Boolean.TRUE);
        // System.out.println("eq class not found!");
    }

    void mergePartitionRecords(NonUnaryWhereCondition join) {
        PartitionCols l = this.getClassForColumn(join.getLeftOp().getAllColumnRefs().get(0));
        PartitionCols r = this.getClassForColumn(join.getRightOp().getAllColumnRefs().get(0));
        Boolean lIsRepartitioned = Boolean.FALSE;
        Boolean rIsRepartitioned = Boolean.FALSE;
        if (this.classes.containsKey(l)) {
            lIsRepartitioned = this.classes.get(l);
        }
        this.classes.remove(l);
        if (this.classes.containsKey(r)) {
            rIsRepartitioned = this.classes.get(r);
        }
        PartitionCols res = new PartitionCols();
        this.classes.remove(r);
        for (Column c : l.getColumns()) {
            res.addColumn(c);
        }
        for (Column c : r.getColumns()) {
            res.addColumn(c);
        }
        this.classes.put(res, rIsRepartitioned || lIsRepartitioned);

        if (deliverdPart == l || deliverdPart == r) {
            deliverdPart = res;
        }


    }

    public EquivalentColumnClasses shallowCopy() {
        Map<PartitionCols, Boolean> copy = new HashMap<PartitionCols, Boolean>();
        for (PartitionCols pc : this.classes.keySet()) {
            copy.put(pc, classes.get(pc));
        }
        return new EquivalentColumnClasses(copy);
    }

    public PartitionCols getLast() {
        return deliverdPart;
    }

    public void copyFrom(EquivalentColumnClasses e2RecordCloned) {
        this.classes.clear();
        for (PartitionCols pc : e2RecordCloned.classes.keySet()) {
            this.classes.put(pc, e2RecordCloned.classes.get(pc));

        }
        this.deliverdPart = e2RecordCloned.deliverdPart;
    }

    public void addClassesFrom(EquivalentColumnClasses other) {
      /*  for(PartitionCols pc:other.classes.keySet()){
            if(!this.classes.containsKey(pc)){
                for(Column c:pc.getColumns()){
                    PartitionCols pc2=this.getClassForColumn(c);
                    if(!pc2.isEmpty()){
                        
                    }
                }
            }
            else{
                this.classes.put(pc, other.classes.get(pc));
            }
        }*/
    }

    public void setLastPartitioned(PartitionCols returnedPt) {
        this.deliverdPart = returnedPt;
    }


}
