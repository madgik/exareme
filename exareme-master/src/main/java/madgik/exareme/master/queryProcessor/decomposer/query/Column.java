/**
 * Copyright MaDgIK Group 2010 - 2015.
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
 * @author heraldkllapi
 */
public class Column implements Operand {

    private String tableAlias = null;
    private String columnName = null;
    private String baseTable = null;

    public Column() {
        super();
    }

    public Column(String alias, String name) {
        this.tableAlias = alias;
        this.columnName = name;
    }
    
    public Column(String alias, String name, String base) {
        this.tableAlias = alias;
        this.columnName = name;
        this.baseTable = base;
    }

    public String getBaseTable() {
		return baseTable;
	}

	public void setBaseTable(String baseTable) {
		this.baseTable = baseTable;
	}

	@Override public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Column)) {
            return false;
        }
        Column otherCol = (Column) other;
        if (this.tableAlias == null) {
            return this.columnName.equals(otherCol.columnName);
        }
        return (this.columnName.equals(otherCol.columnName) && this.tableAlias
            .equals(otherCol.tableAlias));
    }

    @Override public int hashCode() {
       
        int hash = 31;
        String aliasUp=this.tableAlias.toUpperCase();
        String nameUp=this.columnName.toUpperCase();
        
        int last=aliasUp.charAt(aliasUp.length()-1)+19;
        last*=last;
        hash = 31*hash+last;
        hash = 89 * hash + aliasUp.hashCode();
        hash = 89 * hash + nameUp.hashCode();
        hash = 89 * hash +(new StringBuilder(aliasUp).reverse().toString()).hashCode();
        hash = 89 * hash +(new StringBuilder(nameUp).reverse().toString()).hashCode();
        return hash;
    }

    @Override public String toString() {
    	String table="";
    	String base="";
        if (tableAlias != null) {
            table= tableAlias + ".";
        }
        if (baseTable != null && table.startsWith("table")) {
        	base= baseTable + "_";
        }
        
        return table + base + columnName;
    }

    @Override public List<Column> getAllColumnRefs() {
        List<Column> res = new ArrayList<Column>();
        res.add(this);
        return res;
    }

    @Override public void changeColumn(Column oldCol, Column newCol) {
        if (this.columnName.equals(oldCol.columnName) && this.tableAlias
            .equals(oldCol.tableAlias)) {
            this.columnName = newCol.columnName;
            this.tableAlias = newCol.tableAlias;
            this.baseTable = newCol.baseTable;
        }
    }

    @Override public Column clone() throws CloneNotSupportedException {
        Column cloned = (Column) super.clone();
        return cloned;
    }
    
    public String getName(){
    	return this.columnName;
    }
    public String getAlias(){
    	return this.tableAlias;
    }

	public void setName(String string) {
		this.columnName=string;
	}

	public void setAlias(String tablename) {
		this.tableAlias=tablename;
		
	}

	@Override
	public HashCode getHashID() {
		List<HashCode> codes=new ArrayList<HashCode>();
		codes.add(Hashing.sha1().hashBytes(this.tableAlias.toUpperCase().getBytes()));
		codes.add(Hashing.sha1().hashBytes(this.columnName.toUpperCase().getBytes()));
		return Hashing.combineOrdered(codes);
	}
}
