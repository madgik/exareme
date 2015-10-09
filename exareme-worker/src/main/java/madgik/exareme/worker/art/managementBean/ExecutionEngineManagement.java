/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

import madgik.exareme.worker.art.executionEngine.ExecutionEngine;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ExecutionEngineManagement implements ExecutionEngineManagementMBean {
    private static final Logger log = Logger.getLogger(ExecutionEngineManagement.class);
    private ExecutionEngine engine = null;

    public ExecutionEngineManagement(ExecutionEngine engine) {
        this.engine = engine;
    }

    @Override public int getActiveExecutionPlans() throws RemoteException {
        return engine.getStatus().getActiveExecutionPlans();
    }

    @Override
    public String runPerformanceDataflow(int times, int parallelDataflows, String daxFilePath) {
        return null;
    }

    @Override public int getTotalExecutedPlans() throws RemoteException {
        return engine.getStatus().getTotalExecutedPlans();
    }

    @Override public int getSuccessfullyExecutedPlans() throws RemoteException {
        return engine.getStatus().getSuccessfullyExecutedPlans();
    }
}
