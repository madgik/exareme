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
public class NonUnaryWhereCondition implements Operand {

    private List<Operand> ops;
    // private Operand leftOp;
    //private Operand rightOp;
    String operator;

    public NonUnaryWhereCondition() {
        super();
        ops = new ArrayList<Operand>();
    }

    public NonUnaryWhereCondition(Operand left, Operand right, String operator) {
        ops = new ArrayList<Operand>();
        this.ops.add(left);
        this.ops.add(right);
        this.operator = operator;
    }

    public NonUnaryWhereCondition(List<Operand> operands, String operator) {
        this.ops = operands;
        this.operator = operator;
    }

    @Override public List<Column> getAllColumnRefs() {
        List<Column> res = new ArrayList<Column>();
        for (Operand o : this.ops) {
            for (Column c : o.getAllColumnRefs()) {
                res.add(c);
            }
        }
        return res;
    }

    public void setOperator(String op) {
        this.operator = op;
    }

    public String getOperator() {
        return this.operator;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        if (operator.equalsIgnoreCase("or")) {
            sb.append("(");
        }
        sb.append(this.ops.get(0).toString());

        sb.append(" ");
        sb.append(operator);
        sb.append(" ");
        sb.append(ops.get(1).toString());
        for (int i = 2; i < this.ops.size(); i++) {
            //multiway join
            sb.append(" AND ");
            sb.append(ops.get(i - 1).toString());
            sb.append(operator);
            sb.append(" ");
            sb.append(ops.get(i).toString());
        }
        if (operator.equalsIgnoreCase("or")) {
            sb.append(")");
        }
        return sb.toString();
    }

    public List<Operand> getOperands() {
        return this.ops;
    }

    public Operand getLeftOp() {
        return this.ops.get(0);
    }

    public Operand getRightOp() {
        return this.ops.get(1);
    }

    public Operand getOp(int i) {
        return this.ops.get(i);
    }

    public void setLeftOp(Operand op) {
        if (this.ops.isEmpty()) {
            this.ops.add(op);
        } else {
            this.ops.set(0, op);
        }
    }

    public void setRightOp(Operand op) {
        if (this.ops.isEmpty()) {
            this.setLeftOp(new Column("temp", "temp"));
            this.ops.add(op);
        } else if (this.ops.size() == 1) {
            this.ops.add(op);
        } else {
            this.ops.set(1, op);
        }
    }

    @Override public NonUnaryWhereCondition clone() throws CloneNotSupportedException {
        NonUnaryWhereCondition cloned = (NonUnaryWhereCondition) super.clone();
        List<Operand> opsCloned = new ArrayList<Operand>();
        for (Operand o : this.ops) {
            opsCloned.add(o.clone());
        }
        cloned.ops = opsCloned;
        return cloned;
    }

    public void setOperandAt(int i, Operand op) {
        this.ops.set(i, op);
    }

    public void addOperand(Operand op) {
        this.ops.add(op);
    }

    @Override public int hashCode() {
        int hash = 3;
        if (this.operator.equals("=")) {
            //in join commutativity does not affect the result
            for (Operand o : this.ops) {
                hash += (o != null ? o.hashCode() : 0);
            }
        } else {
            //int test=this.ops.hashCode();
            hash = 43 * hash + (this.ops != null ? this.ops.hashCode() : 0);
        }
        hash = 43 * hash + (this.operator != null ? this.operator.hashCode() : 0);
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NonUnaryWhereCondition other = (NonUnaryWhereCondition) obj;
        if (this.ops != other.ops && (this.ops == null || !this.ops.equals(other.ops))) {
            return false;
        }
        if ((this.operator == null) ?
            (other.operator != null) :
            !this.operator.equals(other.operator)) {
            return false;
        }
        return true;
    }

    @Override public void changeColumn(Column oldCol, Column newCol) {
        for (int i = 0; i < this.ops.size(); i++) {
            Operand o = this.ops.get(i);
            if (o.getClass().equals(Column.class)) {
                if (((Column) o).equals(oldCol)) {
                    this.ops.set(i, newCol);
                }
            } else {
                o.changeColumn(oldCol, newCol);
            }
        }
    }
}
