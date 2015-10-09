/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.query;

import java.util.List;

/**
 * @author dimitris
 */
public class CastOperand implements Operand {

    private Operand castOp;

    public Operand getCastOp() {
        return castOp;
    }

    @Override public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.castOp != null ? this.castOp.hashCode() : 0);
        hash = 97 * hash + (this.castType != null ? this.castType.hashCode() : 0);
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CastOperand other = (CastOperand) obj;
        return true;
    }

    private String castType;

    public void setCastType(String castType) {
        this.castType = castType;
    }

    public String getCastType() {
        return castType;
    }

    public CastOperand(Operand op, String t) {
        this.castOp = op;
        this.castType = t;
    }

    @Override public List<Column> getAllColumnRefs() {
        return castOp.getAllColumnRefs();
    }

    @Override public String toString() {
        return "cast(" + castOp.toString() + " as " + castType + ")";
    }

    @Override public void changeColumn(Column oldCol, Column newCol) {

        if (castOp.getClass().equals(Column.class)) {
            if (((Column) castOp).equals(oldCol)) {
                castOp = newCol;
            }
        } else {
            castOp.changeColumn(oldCol, newCol);
        }
    }

    @Override public CastOperand clone() throws CloneNotSupportedException {
        CastOperand cloned = (CastOperand) super.clone();
        cloned.setCastOp(this.castOp.clone());
        return cloned;
    }

    private void setCastOp(Operand op) {
        this.castOp = op;
    }
}
