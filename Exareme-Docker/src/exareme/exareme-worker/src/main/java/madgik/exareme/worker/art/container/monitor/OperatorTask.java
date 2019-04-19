/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.monitor;

import madgik.exareme.worker.art.concreteOperator.manager.SessionManager;

import java.rmi.RemoteException;
import java.util.Date;

/**
 * An operator task has a name, a start and an end! By default an operator has
 * only one task. The working task!
 * <p/>
 * An operator can have multiple tasks. For example the Merge operator has two
 * tasks, Left and Right Working.
 *
 * @author Herald Kllapi <br> University of Athens / Department of Informatics
 * and Telecommunications.
 * @since 1.0
 */
public class OperatorTask extends StatusVariable {
    private String name = null;
    private boolean start = false;
    private Date startDate = null;
    private boolean end = false;
    private Date endDate = null;
    private SessionManager sessionManager = null;

    /**
     * Create an operator task.
     *
     * @param name           Tha task name.
     * @param sessionManager
     */
    public OperatorTask(String name, SessionManager sessionManager) {
        super(name, Long.class);

        this.name = name;
        this.sessionManager = sessionManager;
    }

    /**
     * This is not supported for Tasks.
     *
     * @param svStatus The new value.
     * @throws MonitorException
     */
    @Override
    public void setStatus(Object svStatus) throws MonitorException {
        throw new MonitorException("Not supported!");
    }

    /**
     * Set the task start to current time.
     *
     * @throws MonitorException
     * @throws RemoteException
     */
    public void setStart() throws MonitorException, RemoteException {
        if (!start) {
            start = true;
            startDate = new Date();
            super.setStatus(startDate.getTime());
        }
    }

    /**
     * Set the task end to current time.
     *
     * @throws MonitorException
     * @throws RemoteException
     */
    public void setEnd() throws MonitorException, RemoteException {
        if (!end) {
            end = true;
            endDate = new Date();
            super.setStatus(endDate.getTime());
        }
    }
}
