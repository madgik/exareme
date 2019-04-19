/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the object parameters.
 * <p/>
 * University of Athens / Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class Parameters implements Serializable, Iterable<Parameter> {

    private LinkedList<Parameter> parameters = new LinkedList<Parameter>();

    public Parameters() {
    }

    /**
     * Add a new parameter.
     *
     * @param p The parameter.
     */
    public void addParameter(Parameter p) {
        this.parameters.add(p);
    }

    /**
     * Iterates through the parameters.
     *
     * @return the parameter iterator.
     */
    public Iterator<Parameter> iterator() {
        return this.parameters.iterator();
    }

    /**
     * An object can have multiple paramaters with the same name. For that reason, a list is returned.
     *
     * @param paramName The parameter name.
     * @return The list with the parameters.
     */
    public List<Parameter> getParameter(String paramName) {
        LinkedList<Parameter> result = new LinkedList<Parameter>();

        Parameter param = new Parameter(paramName, "Not Needed!");

        for (Parameter p : parameters) {
            if (p.equals(param)) {
                result.add(p);
            }
        }

        return result;
    }

    public List<String> listParameterNames() {
        List<String> paramNames = new ArrayList<>();
        for (Parameter p : parameters) {
            paramNames.add(p.getName());
        }
        return paramNames;
    }
}
