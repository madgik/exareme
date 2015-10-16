/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator.manager;

import madgik.exareme.worker.art.parameter.Parameter;
import madgik.exareme.worker.art.parameter.Parameters;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Herald Kllapi <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ParameterManager {

    /**
     * The operator parameters. For example the schema of the sql operator.
     */
    protected Parameters params = null;
    protected String queryString = null;
    protected Map<String, LinkedList<madgik.exareme.worker.art.executionPlan.parser.expression.Parameter>>
        outputParameters;

    public ParameterManager(SessionManager sessionManager) {
    }

    /**
     * Get the operator parameters.
     *
     * @return the operator parameters.
     */
    public final Parameters getParameters() {
        return this.params;
    }

    /**
     * Set the operator parameters.
     *
     * @param params the parameters.
     */
    public final void setParameters(Parameters params) {
        if (this.params == null) {
            this.params = params;
        }
    }

    public final List<Parameter> getParameter(String name) {
        return params.getParameter(name);
    }

    public final List<String> listParameterNames() {
        return params.listParameterNames();
    }

    public final String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public final void setOutputParameters(
        Map<String, LinkedList<madgik.exareme.worker.art.executionPlan.parser.expression.Parameter>> outParams) {
        if (outputParameters == null) {
            outputParameters = outParams;
        }
    }

    public final Set<String> getOutOperators() {
        return outputParameters.keySet();
    }

    public final LinkedList<madgik.exareme.worker.art.executionPlan.parser.expression.Parameter> getOutOperatorParameters(
        String opname) {
        return outputParameters.get(opname);
    }
}
