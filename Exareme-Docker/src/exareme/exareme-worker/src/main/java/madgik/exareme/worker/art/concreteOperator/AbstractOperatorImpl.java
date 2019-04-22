/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.worker.art.concreteOperator.manager.*;
import madgik.exareme.worker.art.container.ContainerID;
import madgik.exareme.worker.art.container.dataTrasferMgr.DataTransferMgrInterface;
import madgik.exareme.worker.art.container.jobQueue.JobQueueInterface;
import madgik.exareme.worker.art.container.monitor.OperatorStatus;
import madgik.exareme.worker.art.container.monitor.StatusVariable;
import madgik.exareme.worker.art.container.operatorGroupMgr.OperatorGroupManagerInterface;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 * This class represents the operator. All operators extends this class.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}@di.uoa.gr<br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public abstract class AbstractOperatorImpl {

    private static final Logger log = Logger.getLogger(AbstractOperatorImpl.class);
    public JobQueueInterface jobQueueInterface;
    public ConcreteOperatorID concreteOperatorID;
    public long start;
    /**
     * True if the exit() method is called else false
     */
    protected boolean hasExit = false;
    protected int exitCode = -1;
    protected Serializable exitMessage = null;
    protected boolean hasError = false;
    protected Exception exception = null;
    protected boolean hasCleanedResources = false;
    private ExceptionManager exceptionManager = null;
    private ParameterManager parameterManager = null;
    private AdaptorManager adaptorManager = null;
    private SessionManager sessionManager = null;
    private TaskManager taskManager = null;
    private VariableManager variableManager = null;
    private DiskManager diskManager = null;
    private NetManager netManager = null;
    private ProcessManager processManager = null;
    private OperatorGroupManagerInterface operatorGroupManager = null;
    private DataTransferMgrInterface dataTransferManagerDTP = null;
    private String opname;

    /* Create the operator session (Logger, Status, Tasks). */
    public void createSession(String operatorName, String category, OperatorType type,
                              ConcreteOperatorID opID, PlanSessionReportID sessionReportID,
                              ContainerSessionID containerSessionID, PlanSessionID sessionID, ContainerID containerID,
                              DataTransferMgrInterface dataTransferManagerDTP,
                              OperatorGroupManagerInterface operatorGroupManager) throws Exception {
        opname = operatorName;
        sessionManager =
                new SessionManager(operatorName, category, type, opID, containerSessionID, sessionID,
                        sessionReportID, new OperatorStatus(), containerID);
        exceptionManager = new ExceptionManager(sessionManager);
        parameterManager = new ParameterManager(sessionManager);
        adaptorManager = createAdaptorManager();
        taskManager = new TaskManager(sessionManager);
        variableManager = new VariableManager(sessionManager);
        diskManager = new DiskManager(sessionManager);
        netManager = new NetManager(sessionManager);
        processManager = new ProcessManager(sessionManager);
        this.dataTransferManagerDTP = dataTransferManagerDTP;
        this.operatorGroupManager = operatorGroupManager;
        /* Create the default tasks */
        taskManager.createTask("Working");
    }

    public void cleanResources() throws Exception {
        if (hasCleanedResources) {
            return;
        }

        log.debug("Close all processes ...");
        processManager.closeAllProcesses();

        log.debug("End all operator's tasks ...");
        taskManager.endAllTasks();

        hasCleanedResources = true;
    }

    private void closeIO() throws Exception {
        log.debug("Closing outputs ... ");
        adaptorManager.closeAllOutputs();

        log.debug("Closing inputs ... ");
        adaptorManager.closeAllInputs();
    }

    public final void freeresources(ConcreteOperatorID opID) {
        jobQueueInterface.freeResources(opID);
    }

    /**
     * Exit the operator.
     *
     * @param code The exit code.
     */
    public final void exit(int code) {
        exit(code, null);
    }

    /**
     * Exit the operator.
     *
     * @param code    The exit code.
     * @param message The exit message.
     */
    public final void exit(int code, Serializable message) {
        if (hasExit == false) {
            hasExit = true;
            exitCode = code;
            exitMessage = message;
            try {
                closeIO();
                cleanResources();

                /* Report the exit code */
                StatusVariable exitCodeSV = new StatusVariable("ExitCode", Integer.class);
                variableManager.register(exitCodeSV);
                exitCodeSV.setStatus(code);
            } catch (Exception e) {
                getExceptionManager().reportException("Exit", e);
            }
        }
    }

    public final int getExitCode() throws Exception {
        if (hasExit) {
            return exitCode;
        }
        throw new Exception("Operator not finished yet!");
    }

    public final Serializable getExitMessage() throws Exception {
        if (hasExit) {
            return exitMessage;
        }
        throw new Exception("Operator not finished yet!");
    }

    public final void error(Exception exception) {
        if (hasError == false && hasExit == false) {
            this.hasExit = true;
            this.exception = exception;
            try {
                closeIO();
                cleanResources();
                getExceptionManager().reportException("", exception);
                log.debug(
                        "exception in abstract op" + exception.getMessage() + " " + this.opname + " ");
                exception.printStackTrace();
            } catch (Exception e) {

                getExceptionManager().reportException("Exit", e);
            }
        }
    }

    public final Exception getException() throws Exception {
        if (hasError) {
            return exception;
        }
        throw new Exception("Operator not has not error!");
    }

    public ExceptionManager getExceptionManager() {
        return exceptionManager;
    }

    public ParameterManager getParameterManager() {
        return parameterManager;
    }

    public DataTransferMgrInterface getDataTransferManagerDTP() {
        return dataTransferManagerDTP;
    }

    protected abstract AdaptorManager createAdaptorManager();

    public DataTransferMgrInterface getDataTransferManager() {
        return dataTransferManagerDTP;
    }

    public AdaptorManager getAdaptorManager() {
        return adaptorManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public VariableManager getVariableManager() {
        return variableManager;
    }

    public DiskManager getDiskManager() {
        return diskManager;
    }

    public NetManager getNetManager() {
        return netManager;
    }

    public ProcessManager getProcessManager() {
        return processManager;
    }

    /**
     * This method is called just before the run method. An operator can overwrite
     * this method to perform actions that has to be done before the operator
     * actually runs. By default this method does nothing.
     *
     * @throws Exception
     */
    public void initializeOperator() throws Exception {
    }

    /**
     * This method is called when the operator has finished. By default this
     * method does nothing.
     *
     * @throws Exception
     */
    public void finalizeOperator() throws Exception {
    }

    /**
     * This method is called when an instance of the operator is ready to start.
     *
     * @throws Exception
     */
    public abstract void run() throws Exception;

    public OperatorGroupManagerInterface getOperatorGroupManager() {
        return operatorGroupManager;
    }
}
