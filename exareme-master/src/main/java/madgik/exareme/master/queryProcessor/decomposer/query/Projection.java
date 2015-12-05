/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

/**
 * @author dimitris
 */
public class Projection implements Operand {
    private List<Output> ops;
    private boolean distinct;

    public Projection(List<Output> operands) {
        this.ops = operands;
        this.distinct = false;
    }

    public Projection() {
        super();
        ops = new ArrayList<Output>();
    }

    public List<Column> getAllColumnRefs() {
        List<Column> res = new ArrayList<Column>();
        for (Output out : this.ops) {
            Operand o = out.getObject();
            for (Column c : o.getAllColumnRefs()) {
                res.add(c);
            }
        }
        return res;
    }

    public void setOperandAt(int i, Output o) {
        this.ops.set(i, o);
    }

    public void addOperand(Output o) {
        this.ops.add(o);
    }

    public List<Output> getOperands() {
        return this.ops;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PROJECT(");
        String sep = "";
        for (int i = 0; i < this.ops.size(); i++) {
            //multiway join
            sb.append(sep);
            sep = ", ";
            sb.append(ops.get(i).toString().replaceAll("\"", ""));
        }
        sb.append(")");

        return sb.toString();
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        // object must be Test at this point
        Projection other = (Projection) obj;
        if (other.getOperands().size() == this.ops.size()) {
            for (Output o : this.ops) {
                if (!other.getOperands().contains(o)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override public int hashCode() {
        int hash = 7;
        for (Output o : this.ops) {
            //Ignore table name if operand is column!!!!!!

            if(o.getObject() instanceof Column){
                Column c=(Column) o.getObject();
                hash=31 * hash + Objects.hashCode(o.getOutputName()) + Objects.hashCode(c.getName());

            }
            else{
            	hash=31 * hash + Objects.hashCode(o.getOutputName()) + Objects.hashCode(o.getObject());
            }
        }
        return hash;
    }

    public void changeColumn(Column oldCol, Column newCol) {
        for (Output out : this.getOperands()) {
            out.getObject().changeColumn(oldCol, newCol);
        }
    }

    @Override public Operand clone() throws CloneNotSupportedException {
        Projection cloned = new Projection();
        for (Output o : this.ops) {
            cloned.ops.add(o.clone());
        }
        return cloned;
    }


    public void setDistinct(boolean b) {
        this.distinct = b;

    }

    public boolean isDistinct() {
        return this.distinct;
    }
    
    @Override
	public HashCode getHashID() {
		Set<HashCode> codes=new HashSet<HashCode>();
		for(Output o:this.ops){
			codes.add(o.getHashID());
		}
		if(distinct){
			codes.add(Hashing.sha1().hashBytes("true".getBytes()));
		}
		else{
			codes.add(Hashing.sha1().hashBytes("false".getBytes()));
		}
		return Hashing.combineUnordered(codes);
	}
}
