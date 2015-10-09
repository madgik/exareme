/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.parameter;

import java.io.Serializable;

/**
 * This class represents an operator parameter.
 * Example:
 * <p/>
 * sql.Query {
 * Schema = "dbSchema"
 * } {
 * select ... from ... where ...
 * }
 * <p/>
 * Then a parameter with Name 'Schema' and value 'dbSchema' will be created.
 * <p/>
 * Herald Kllapi
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class Parameter implements Serializable {
    private static final long serialVersionUID = 2L;

    private String attrName;
    private String attrValue;

    /**
     * Creates a new Parameter.
     *
     * @param attrName  The parameter name.
     * @param attrValue The parameter value.
     */
    public Parameter(String attrName, String attrValue) {
        this.attrName = attrName;
        this.attrValue = attrValue;
    }

    /**
     * Get the parameter name.
     *
     * @return the parameter name.
     */
    public String getName() {
        return attrName;
    }

    /**
     * Get the parameter value.
     *
     * @return the parameter value.
     */
    public String getValue() {
        return attrValue;
    }

    /**
     * @param obj the parameter to compare.
     * @return true if obj has tha same name with this.
     */
    @Override public boolean equals(Object obj) {
        if (obj instanceof Parameter) {
            Parameter param = (Parameter) obj;
            return this.attrName.compareTo(param.getName()) == 0;
        }

        return false;
    }

    /**
     * To create the hash code the name and the value is used.
     *
     * @return the hash code of this object.
     */
    @Override public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.attrName != null ? this.attrName.hashCode() : 0);
        return hash;
    }
}
