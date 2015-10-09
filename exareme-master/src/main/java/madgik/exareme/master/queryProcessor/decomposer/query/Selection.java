/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author dimitris
 */
public class Selection implements Operand {

    private Set<Operand> ops;

    public Selection(Set<Operand> operands) {
        this.ops = operands;
    }

    public Selection() {
        super();
        ops = new HashSet<Operand>();
    }


    public List<Column> getAllColumnRefs() {
        List<Column> res = new ArrayList<Column>();
        for (Operand o : this.ops) {
            for (Column c : o.getAllColumnRefs()) {
                res.add(c);
            }
        }
        return res;
    }

    ///   public void addOperand(Operand op) {
    //       this.ops.add(op);
    //   }

    public void addOperand(Operand op) {
        this.ops.add(op);
    }

    public Set<Operand> getOperands() {
        return this.ops;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();

        // sb.append("SELECT(");
        String sep = "";
        for (Operand o : this.ops) {
            //   for (int i = 0; i < this.ops.size(); i++) {
            //multiway join
            sb.append(sep);
            sep = ", ";
            sb.append(o.toString().replaceAll("\"", ""));
        }

        //     sb.append(")");

        return sb.toString();
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        Selection other = (Selection) obj;
        if (other.ops.size() == this.ops.size()) {

            for (Operand o : this.ops) {
                if (!other.ops.contains(o)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override public int hashCode() {

        int hash = 7;
        //  for(Operand o:this.ops){
        hash = 31 * hash + this.ops.hashCode();
        //  }
        if (this.ops.size() == 1) {
            // System.out.println(this.ops.hashCode());
        }
        return hash;
    }

    @Override public void changeColumn(Column oldCol, Column newCol) {
        throw new UnsupportedOperationException(
            "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public Operand clone() throws CloneNotSupportedException {
        Selection cloned = new Selection();
        for (Operand o : this.ops) {
            cloned.ops.add(o.clone());
        }
        return cloned;
    }


}
