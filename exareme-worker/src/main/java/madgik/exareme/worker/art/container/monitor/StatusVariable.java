/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.monitor;

import org.apache.log4j.Logger;

/**
 * A status variable is the main mechanism
 * used to report operator status.
 * <p/>
 * <pre>
 * Usage:
 *
 * StatusVariable sv = new StatusVariable("RecordCount", Integer.class);
 *
 * sv.setStatus(1);
 * sv.setStatus("1"); // Error
 *
 * </pre>
 *
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.2
 */
public class StatusVariable {

    private static Logger log = Logger.getLogger(StatusVariable.class);
    private String name = null;
    private Class svClass = null;
    private Object svStatus = null;
    private OperatorStatus opStat = null;
    private String threadID = null;

    /**
     * Constructs a status variable.
     *
     * @param varName The variable name.
     * @param svClass The variable class.
     */
    public StatusVariable(String varName, Class svClass) {
        this.name = varName;
        this.svClass = svClass;
    }

    /**
     * Constructs a status variable whith the specified status.
     *
     * @param varName The variable name.
     * @param svClass The variable class.
     * @param status  The variable value.
     */
    public StatusVariable(String varName, Class svClass, Object status) {
        this.name = varName;
        this.svClass = svClass;
        this.svStatus = status;

        threadID = "" + Thread.currentThread().getId();
    }

    /**
     * Get the variable name.
     *
     * @return The variable name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Register this status variable to the
     * operator status specified.
     *
     * @param opStat The operator status that the variable is
     *               registered to.
     */
    public void register(OperatorStatus opStat) {
        this.opStat = opStat;
    }

    /**
     * Get the variable value.
     *
     * @return The variable value.
     * @throws MonitorException
     */
    public Object getStatus() throws MonitorException {
        if (this.svStatus != null) {
            return this.svStatus;
        } else {
            throw new MonitorException("Status not exists!");
        }
    }

    /**
     * Set a new value for the variable.
     *
     * @param svStatus The new value.
     * @throws MonitorException
     */
    public void setStatus(Object svStatus) throws MonitorException {
        if (opStat == null) {
            throw new MonitorException("Variable not registered!");
        }

        if (svStatus.getClass() == svClass) {
            this.svStatus = svStatus;
            opStat.statusVariableChanged(name);
        } else {
            throw new MonitorException("Variable class exception. Expected " + svClass + " class!");
        }
    }

    /**
     * Get the operator thread id.
     *
     * @return The operator thread id.
     */
    public String getThreadID() {
        if (this.threadID == null) {
            return this.opStat.getThreadID();
        } else {
            return this.threadID;
        }
    }

    /**
     * Get the valiable class.
     *
     * @return The variable class.
     */
    public Class getVariableClass() {
        return this.svClass;
    }
}
