/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;

import madgik.exareme.worker.art.executionEngine.ExecutionEngine;
import org.apache.log4j.Logger;

/**
 * @author heraldkllapi
 */
public class LogUtils {
    private static Logger log = Logger.getLogger(ExecutionEngine.class);

    public static void logInfo(String event) {
        log.info(event);
    }

    public static void logException(String event, Exception e) {
        log.error(event, e);
    }
}
