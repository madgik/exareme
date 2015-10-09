/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.dag;

import madgik.exareme.master.queryProcessor.decomposer.query.Column;

/**
 * @author dimitris
 */
public class PruningInfo {
    Node parent;
    Node child;
    PartitionCols pc;

    public PruningInfo(Node parent, Node child, PartitionCols pc) {
        this.parent = parent;
        this.child = child;
        this.pc = pc;
    }

    @Override public String toString() {
        return "PruningInfo{" + "pc=" + pc + '}';
    }

    public boolean isForColumn(Column c) {
        return this.pc.getColumns().contains(c);
    }


}
