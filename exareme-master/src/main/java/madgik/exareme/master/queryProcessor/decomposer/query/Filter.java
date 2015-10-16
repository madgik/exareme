/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query;

/**
 * @author heraldkllapi
 */
public class Filter {

    public String tableAlias = null;
    public String columnName = null;
    public String operator = null;
    public String value = null;

    @Override public String toString() {
        if (tableAlias != null) {
            return tableAlias + "." + columnName + " " + operator + " " + value;
        } else {
            return columnName + " " + operator + " " + value;
        }
    }
}
