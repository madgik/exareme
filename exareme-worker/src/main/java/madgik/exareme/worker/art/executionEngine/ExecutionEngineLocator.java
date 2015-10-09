/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ExecutionEngineLocator {
    private static ExecutionEngineProxy executionEngineProxy = null;

    public static void setExecutionEngine(ExecutionEngineProxy engine) {
        ExecutionEngineLocator.executionEngineProxy = engine;
    }

    public static ExecutionEngineProxy getExecutionEngineProxy() {
        return executionEngineProxy;
    }
}
