/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

/**
 * @author dimitris
 */
public class BinaryOperand implements Operand {

    private Operand leftOp;
    private Operand rightOp;
    private String operator;

    @Override public List<Column> getAllColumnRefs() {
        List<Column> res = new ArrayList<Column>();
        for (Column c : leftOp.getAllColumnRefs()) {
            res.add(c);
        }
        for (Column c : rightOp.getAllColumnRefs()) {
            res.add(c);
        }
        return res;
    }

    public void setOperator(String op) {
        this.operator = op;
    }

    public String getOperator() {
        return this.operator;
    }

    public void setLeftOp(Operand op) {
        this.leftOp = op;
    }

    public void setRightOp(Operand op) {
        this.rightOp = op;
    }

    public Operand getLeftOp() {
        return this.leftOp;
    }

    public Operand getRightOp() {
        return this.rightOp;
    }

    @Override public String toString() {
        return "(" + leftOp.toString() + " " + operator + " " + rightOp.toString() + ")";
    }

    @Override public void changeColumn(Column oldCol, Column newCol) {
        if (leftOp.getClass().equals(Column.class)) {
            if (((Column) leftOp).equals(oldCol)) {
                leftOp = newCol;
            }
        } else {
            leftOp.changeColumn(oldCol, newCol);
        }

        if (rightOp.getClass().equals(Column.class)) {
            if (((Column) rightOp).equals(oldCol)) {
                rightOp = newCol;
            }
        } else {
            rightOp.changeColumn(oldCol, newCol);
        }
    }

    @Override public BinaryOperand clone() throws CloneNotSupportedException {
        BinaryOperand cloned = (BinaryOperand) super.clone();
        cloned.setLeftOp(leftOp.clone());
        cloned.setRightOp(rightOp.clone());
        return cloned;
    }

	@Override
	public HashCode getHashID() {
		Set<HashCode> codes=new HashSet<HashCode>();
		codes.add(this.leftOp.getHashID());
		codes.add(this.rightOp.getHashID());
		codes.add(Hashing.sha1().hashBytes(operator.getBytes()));
		return Hashing.combineOrdered(codes);
	}
}
