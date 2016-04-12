/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema;

import java.io.Serializable;

/**
 * @author herald
 */
public class Table implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name = null;
    private String sqlDefinition = null;
    private boolean temp = false;

    // The level of a table view is the
    private int level = -1;

    public Table(String name) {
        this.name = name.toLowerCase();
    }

    public Table(String name, String sql) {
        this.name = name.toLowerCase();
        this.sqlDefinition = sql.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String newName){
        this.name = newName;
    }

    public String getSqlDefinition() {
        return sqlDefinition;
    }

    public void setSqlDefinition(String sqlDefinition) {
        this.sqlDefinition = sqlDefinition.trim();
    }

    public boolean hasSQLDefinition() {
        return (sqlDefinition != null);
    }

    public boolean isTemp() {
        return temp;
    }

    public void setTemp(boolean temporary) {
        this.temp = temporary;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override public String toString() {
        return name + (temp ? " temp" : "");
    }
}
