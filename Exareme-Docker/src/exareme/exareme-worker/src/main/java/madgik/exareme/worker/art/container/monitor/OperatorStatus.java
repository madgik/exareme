/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.monitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An operator status contains the operator's
 * status variable. Only a registered status variable
 * can be sent.
 *
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.2
 */
public class OperatorStatus {

    private Map<String, StatusVariable> variablesMap = null;
    private String operatorThread = null;

    /**
     * Costruct an operator status. The
     * status variables are sent using the
     * specified logger.
     */
    public OperatorStatus() {
        this.variablesMap = Collections.synchronizedMap(new HashMap<String, StatusVariable>());
    }

    /**
     * Registers a variable to this operator status.
     *
     * @param statVar The operator status variable.
     * @throws MonitorException
     */
    public void registerVariable(StatusVariable statVar) throws MonitorException {
        if (variablesMap.get(statVar.getName()) == null) {
            variablesMap.put(statVar.getName(), statVar);
            statVar.register(this);

        } else {
            throw new MonitorException("Variable already registered!");
        }
    }

    /**
     * Indicates that a status variable has changed.
     *
     * @param statVariable The status variable name.
     * @throws MonitorException
     */
    public void statusVariableChanged(String statVariable) throws MonitorException {

        StatusVariable sv = null;
        if ((sv = variablesMap.get(statVariable)) != null) {
            if (this.operatorThread == null) {
                this.operatorThread = "" + Thread.currentThread().getId();
            }

            //      logger.variable(sv.toVariableMessage());
        } else {
            throw new MonitorException("Variable not registered!");
        }
    }

    /**
     * Get the thread id of the operator.
     *
     * @return The thread id of the operator.
     */
    public String getThreadID() {
        return this.operatorThread;
    }
}
