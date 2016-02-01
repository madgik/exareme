/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

/**
 * @author heraldkllapi
 */
public class Function implements Operand {

    private String functionName;
    private List<Operand> params = new ArrayList<Operand>();
    //public String outputName;
    private boolean isDistinct = false;

    @Override public String toString() {
        StringBuilder output = new StringBuilder();
        output.append(functionName);
        output.append("(");
        String separator = "";
        if (isDistinct) {
            output.append("distinct ");
        }
        for (Operand p : params) {

            output.append(separator);
            output.append(p.toString());

            separator = ", ";
        }
        output.append(")");
        return output.toString();
    }

    @Override public ArrayList<Column> getAllColumnRefs() {
        ArrayList<Column> cols = new ArrayList<Column>();
        for (Operand p : this.params) {
            for (Column c : p.getAllColumnRefs()) {
                cols.add(c);
            }
        }
        return cols;
    }

    public List<Operand> getParameters() {
        return params;
    }

    public void addParameter(Operand functionParameter) {
        this.params.add(functionParameter);
    }

    public void setFunctionName(String n) {
        this.functionName = n;
    }

    public void setResultDistinct(boolean d) {
        this.isDistinct = d;
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public boolean getResultDistinct() {
        return this.isDistinct;
    }

    @Override public void changeColumn(Column oldCol, Column newCol) {
        for (int i = 0; i < params.size(); i++) {
            Operand o = params.get(i);
            if (o.getClass().equals(Column.class)) {
                if (((Column) o).equals(oldCol)) {
                    params.set(i, newCol);
                }
            } else {
                o.changeColumn(oldCol, newCol);
            }

        }
    }

    public void setParams(List<Operand> pars) {
        this.params = pars;
    }

    @Override public Function clone() throws CloneNotSupportedException {
        Function cloned = (Function) super.clone();
        cloned.setParams(new ArrayList<Operand>());
        for (Operand o : this.params) {
            cloned.addParameter(o.clone());
        }
        return cloned;
    }

    @Override public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.functionName != null ? this.functionName.hashCode() : 0);
        hash = 23 * hash + (this.params != null ? this.params.hashCode() : 0);
        hash = 23 * hash + (this.isDistinct ? 1 : 0);
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Function other = (Function) obj;
        if ((this.functionName == null) ?
            (other.functionName != null) :
            !this.functionName.equals(other.functionName)) {
            return false;
        }
        if (this.params != other.params && (this.params == null || !this.params
            .equals(other.params))) {
            return false;
        }
        if (this.isDistinct != other.isDistinct) {
            return false;
        }
        return true;
    }

	@Override
	public HashCode getHashID() {
		List<HashCode> codes=new ArrayList<HashCode>();
		for(Operand o:this.params){
			codes.add(o.getHashID());
		}
		codes.add(Hashing.sha1().hashBytes(functionName.toUpperCase().getBytes()));
		if(isDistinct){
			codes.add(Hashing.sha1().hashBytes("true".getBytes()));
		}
		else{
			codes.add(Hashing.sha1().hashBytes("false".getBytes()));
		}
		return Hashing.combineOrdered(codes);
	}
}
