/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query;

import madgik.exareme.master.queryProcessor.decomposer.federation.DBInfoReaderDB;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

/**
 * @author heraldkllapi
 */
public class Table {

    private Boolean hasDBIdRemoved;
    private String name;
    private String alias;
    private HashCode hash=null;

    public Table() {
        hasDBIdRemoved = false;
        this.name = "";
        this.alias = "";
    }

    public Table(String n, String a) {
        hasDBIdRemoved = false;
        this.name = n;
        this.alias = a;
    }

    public boolean isFederated() {
        for (String id : DBInfoReaderDB.dbInfo.getAllDBIDs()) {
            if (getName().toUpperCase().startsWith(id.toUpperCase() + "_")) {
                return true;
            }
        }
        return false;
    }

    public String getlocalName() {
      /*  if(!this.hasDBIdRemoved){
        for (String id : DBInfoReaderDB.dbInfo.getAllDBIDs()) {
            if (getName().startsWith(id + "_")) {
                DB db=DBInfoReaderDB.dbInfo.getDB(id);
                return db.getSchema()+"."+getName().substring(id.length() + 1);
            }
        }}
*/
        return getName();

    }

    public String getDBName() {
        for (String id : DBInfoReaderDB.dbInfo.getAllDBIDs()) {
            if (getName().toUpperCase().startsWith(id.toUpperCase() + "_")) {
                return id;
            }
        }

        return null;
    }

    @Override public String toString() {
        return this.getlocalName() + " " + getAlias();
    }

    public String dotPrint() {
        return this.getlocalName();
    }

    public void removeDBIdFromAlias() {
        for (String id : DBInfoReaderDB.dbInfo.getAllDBIDs()) {
            if (getAlias().toUpperCase().startsWith(id.toUpperCase() + "_")) {
                setAlias(getAlias().substring(id.length() + 1));
                break;
            }
        }
    }

    @Override public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Table)) {
            return false;
        }
        Table otherT = (Table) other;
        if (this.getAlias() == null) {
            return this.getName().equals(otherT.getName());
        }
        return (this.getName().equals(otherT.getName()) && this.getAlias()
            .equals(otherT.getAlias()));
    }

    @Override public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.getAlias());
        hash = 89 * hash + Objects.hashCode(this.getName());
        return hash;
    }
    
    public int getHashId() {
        int hash = 31;
        String aliasUp=alias.toUpperCase();
        String nameUp=name.toUpperCase();
        
        int last=aliasUp.charAt(aliasUp.length()-1)+19;
        last*=last;
        hash = 31*hash+last;
        hash = 89 * hash + aliasUp.hashCode();
        hash = 89 * hash + nameUp.hashCode();
        hash = 89 * hash +(new StringBuilder(aliasUp).reverse().toString()).hashCode();
        hash = 89 * hash +(new StringBuilder(nameUp).reverse().toString()).hashCode();
        return hash;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
        hash=null;
    }

    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
        hash=null;
    }

    public boolean hasDBIdRemoved() {
        return this.hasDBIdRemoved;
    }

    public void setDBIdRemoved() {
        this.hasDBIdRemoved = true;
    }

	public HashCode getHashID() {
		if(hash==null){
		Set<HashCode> codes=new HashSet<HashCode>();
		codes.add(Hashing.sha1().hashBytes(alias.toUpperCase().getBytes()));
		codes.add(Hashing.sha1().hashBytes(name.toUpperCase().getBytes()));
		hash= Hashing.combineOrdered(codes);
		}
		return hash;
		
	}
}
