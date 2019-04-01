/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.session;

import java.io.Serializable;
import java.util.*;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}
 * @di.uoa.gr<br> University of Athens / Department of Informatics and
 * Telecommunications.
 * @since 1.0
 */
public class PlanSessionStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Object lock = new Object();
    public Map<Exception, Date> exceptionDateMap = null;
    private boolean hasStarted = false;
    private Date startDate = null;
    private boolean hasException = false;
    private List<Exception> exceptions = null;
    private boolean hasFinished = false;
    private Date finishDate = null;
    private Map<String, ConcreteOperatorStatus> stateMap = null;
    private int finishedOperators = 0;

    public PlanSessionStatus() {
        this.exceptions = Collections.synchronizedList(new LinkedList<Exception>());

        this.exceptionDateMap = Collections.synchronizedMap(new HashMap<Exception, Date>());

        this.stateMap = Collections.synchronizedMap(new HashMap<String, ConcreteOperatorStatus>());
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public boolean hasFinished() {
        return hasFinished;
    }

    public boolean hasError() {
        return hasException;
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }

    public Map<String, ConcreteOperatorTask> getOperatorTasks(String operatorName) {
        return getStatus(operatorName).taskMap;
    }

    public void planInstantiationException(Exception exception, Date time) {
        synchronized (lock) {
            this.hasException = true;
            this.exceptions.add(exception);
            this.exceptionDateMap.put(exception, time);
            this.hasFinished = true;
            this.finishDate = time;
        }
    }

    public void planStart(Date date) {
        synchronized (lock) {
            this.hasStarted = true;
            this.startDate = date;
        }
    }

    public void operatorFinished(String operatorName, int exidCode, Serializable exitMessage,
                                 Date time) {
        synchronized (lock) {
            ConcreteOperatorStatus state = getStatus(operatorName);
            state.hasFinished = true;
            state.exitCode = exidCode;
            state.exitMessage = exitMessage;
            state.exitDate = time;
            ++finishedOperators;
        }
    }

    public void setFinished(Date time) {
        synchronized (lock) {
            this.hasFinished = true;
            this.finishDate = time;
        }
    }

    public void operatorException(String operatorName, Exception exception, Date time) {
        synchronized (lock) {
            ConcreteOperatorStatus state = getStatus(operatorName);
            state.hasException = true;
            state.exceptions.add(exception);
            state.exceptionDateMap.put(exception, time);
            exceptions.add(exception);
        }
    }

    public void planException() {
        synchronized (lock) {
            this.hasException = true;
        }
    }

    public void taskStart(String operatorName, String taskName, Date time) {
        synchronized (lock) {
            ConcreteOperatorStatus state = getStatus(operatorName);
            ConcreteOperatorTask task = state.taskMap.get(taskName);

            if (task == null) {
                task = new ConcreteOperatorTask(taskName);
                state.taskMap.put(taskName, task);
            }
            task.setStart(time);
        }
    }

    public void taskEnd(String operatorName, String taskName, Date time) {
        synchronized (lock) {
            ConcreteOperatorStatus state = getStatus(operatorName);
            ConcreteOperatorTask task = state.taskMap.get(taskName);

            if (task == null) {
                task = new ConcreteOperatorTask(taskName);
                state.taskMap.put(taskName, task);
            }
            task.setEnd(time);
        }
    }

    public ConcreteOperatorStatus getStatus(String operatorName) {
        ConcreteOperatorStatus state = null;
        synchronized (lock) {
            state = stateMap.get(operatorName);
            if (state == null) {
                state = new ConcreteOperatorStatus(operatorName);
                stateMap.put(operatorName, state);
            }
        }
        return state;
    }
}
