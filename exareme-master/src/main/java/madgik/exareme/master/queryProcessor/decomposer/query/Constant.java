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
public class Constant implements Operand {

    private Object value;
    private boolean isArithmetic;

    public Object getValue() {
        return value;
    }

    public Constant() {
        super();
        this.value = new Object();
        this.isArithmetic = false;
    }

    public Constant(Object constant) {
        this.value = constant;
        this.isArithmetic = false;
    }

    @Override public String toString() {
        return value.toString();
    }

    @Override public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Constant other = (Constant) obj;
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
            return false;
        }
        return true;
    }

    public void setValue(Object v) {
        this.value = v;
    }

    @Override public List<Column> getAllColumnRefs() {
        return new ArrayList<Column>();
    }

    @Override public void changeColumn(Column oldCol, Column newCol) {
    }

    @Override public Constant clone() throws CloneNotSupportedException {
        Constant cloned = (Constant) super.clone();
        return cloned;
    }

    void setArithmetic(boolean b) {
        this.isArithmetic = true;
    }

    public boolean isArithmetic() {
        return isArithmetic;
    }

}
