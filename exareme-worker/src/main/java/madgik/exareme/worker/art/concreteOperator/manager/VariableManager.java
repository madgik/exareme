/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator.manager;

import madgik.exareme.worker.art.container.monitor.MonitorException;
import madgik.exareme.worker.art.container.monitor.OperatorStatus;
import madgik.exareme.worker.art.container.monitor.StatusVariable;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class VariableManager {

    private OperatorStatus operatorStatus;

    public VariableManager(SessionManager sessionManager) {
        this.operatorStatus = sessionManager.getOperatorStatus();
    }

    /**
     * This method registers a status variable to this operator.
     *
     * @param sv The status variable to be registered.
     * @throws MonitorException
     */
    public final void register(StatusVariable sv) throws MonitorException {
        operatorStatus.registerVariable(sv);
    }
}
