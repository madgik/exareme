/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator.manager;

import madgik.exareme.worker.art.container.monitor.MonitorException;
import madgik.exareme.worker.art.container.monitor.OperatorStatus;
import madgik.exareme.worker.art.container.monitor.OperatorTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class TaskManager {

    private Map<String, OperatorTask> tasksMap = null;
    private OperatorStatus operatorStatus;
    private SessionManager sessionManager = null;

    public TaskManager(SessionManager sessionManager) {
        this.tasksMap = Collections.synchronizedMap(new HashMap<String, OperatorTask>());
        this.operatorStatus = sessionManager.getOperatorStatus();
        this.sessionManager = sessionManager;
    }

    /**
     * Returns a task.
     *
     * @param taskName The task name.
     * @return The task with name 'taskName'.
     */
    public final OperatorTask getTask(String taskName) {
        return this.tasksMap.get(taskName);
    }

    /**
     * This method creates a task.
     *
     * @param name
     * @throws MonitorException
     */
    public final void createTask(String name) throws MonitorException {
        OperatorTask task = new OperatorTask(name, sessionManager);

        operatorStatus.registerVariable(task);

        this.tasksMap.put(name, task);
    }

    /**
     * End all operator's tasks.
     *
     * @throws MonitorException
     */
    public final void endAllTasks() throws Exception {
        for (OperatorTask task : this.tasksMap.values()) {
            task.setEnd();
        }
    }
}
