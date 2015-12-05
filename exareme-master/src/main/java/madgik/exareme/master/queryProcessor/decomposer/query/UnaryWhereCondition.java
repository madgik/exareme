/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

/**
 * @author dimitris
 */
public class UnaryWhereCondition implements Operand {

    private boolean not;
    private int type;
    public static final int IS_NULL = 0;
    public static final int LIKE = 1;
    private Operand onColumn;
    private String o;
    private HashCode hash=null;

    public UnaryWhereCondition(int type, Operand c, boolean n) {
        super();
        this.type = type;
        this.onColumn = c;
        this.not = n;
        this.o="";
    }
    
    public UnaryWhereCondition(int type, Operand c, boolean n, String o) {
        super();
        this.type = type;
        this.onColumn = c;
        this.not = n;
        this.o = o;
    }

    public Operand getOperand() {
        return onColumn;
    }

    public void setOperand(Operand column) {
        this.onColumn = column;
        hash=null;
    }

    public int getType() {
        return this.type;
    }

    public boolean getNot() {
        return this.not;
    }

    @Override public String toString() {
        switch (type) {
            case IS_NULL:
                return not ? onColumn.toString() + " IS NOT NULL" : onColumn.toString()  + " IS NULL";
            case LIKE:
                return onColumn.toString()  + " LIKE \'" + o +"\'";
            default:
                return super.toString();
        }
    }

    @Override public List<Column> getAllColumnRefs() {
        return onColumn.getAllColumnRefs();
    }

    @Override public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.not ? 1 : 0);
        hash = 97 * hash + this.type;
        hash = 97 * hash + this.o.hashCode();
        hash = 97 * hash + (this.onColumn != null ? this.onColumn.hashCode() : 0);
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UnaryWhereCondition other = (UnaryWhereCondition) obj;
        if (this.not != other.not) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (this.onColumn != other.onColumn && (this.onColumn == null || !this.onColumn
            .equals(other.onColumn))) {
            return false;
        }
        if(this.o != other.o){
        	return false;
        }
        return true;
    }

    @Override
    public void changeColumn(Column oldCol, Column newCol) {
    	hash=null;
    	if(onColumn instanceof Column){
    		Column c=(Column)onColumn;
        if (c.getName().equals(oldCol.getName()) && c.getAlias().equals(oldCol.getAlias())) {
            this.onColumn=newCol;
        }
    	}
    	else{
    		onColumn.changeColumn(oldCol, newCol);
    	}
    }
    
    @Override
    public UnaryWhereCondition clone() throws CloneNotSupportedException {
        UnaryWhereCondition cloned = (UnaryWhereCondition)super.clone();
    cloned.setOperand(this.getOperand().clone());
    cloned.setObject(this.getObject());
    cloned.hash=hash;
    return cloned;
    }

    public void setObject(String object) {
		this.o=object;
		
	}

	public String getObject() {
		return o;
	}
	
	@Override
	public HashCode getHashID() {
		if(hash==null){
		Set<HashCode> codes=new HashSet<HashCode>();
		codes.add(onColumn.getHashID());
		codes.add(Hashing.sha1().hashInt(this.type));
		codes.add(Hashing.sha1().hashBytes(o.toUpperCase().getBytes()));
		if(not){
			codes.add(Hashing.sha1().hashBytes("true".getBytes()));
		}
		else{
			codes.add(Hashing.sha1().hashBytes("false".getBytes()));
		}
		hash=Hashing.combineOrdered(codes);}
		return hash;
	}
    
}
