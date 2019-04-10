/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.dag.Node;
import madgik.exareme.master.queryProcessor.decomposer.query.Column;

/**
 * @author dimitris
 */
public class MemoKey {
    private Node n;
    private Column c;

    public MemoKey(Node n, Column c) {
        this.n = n;
        this.c = c;
    }

    public Node getNode() {
        return this.n;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.n != null ? this.n.hashCode() : 0);
        hash = 31 * hash + (this.c != null ? this.c.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MemoKey other = (MemoKey) obj;
        if (this.n != other.n && (this.n == null || !this.n.equals(other.n))) {
            return false;
        }
        if (this.c != other.c && (this.c == null || !this.c.equals(other.c))) {
            return false;
        }
        return true;
    }

    public Column getColumn() {
        return this.c;
    }

    @Override
    public String toString() {
        return "MemoKey{" + "n=" + n.getObject().toString() + ", c=" + c + '}';
    }


}
