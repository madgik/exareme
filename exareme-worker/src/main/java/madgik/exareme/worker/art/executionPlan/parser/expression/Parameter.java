/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.parser.expression;

import java.io.Serializable;

/**
 * University of Athens / Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class Parameter implements Serializable {

    public String name;
    public String value;

    public Parameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Parameter guest = (Parameter) obj;
        //check strings

        if (!name.equals(guest.name)) {
            return false;
        }
        return value.equals(guest.value);
    }

    @Override public String toString() {
        return "{" + name + " : " + value + '}';
    }

}
