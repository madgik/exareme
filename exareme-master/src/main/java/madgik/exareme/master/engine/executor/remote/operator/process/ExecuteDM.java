/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.remote.operator.process;

import madgik.exareme.common.app.engine.AdpDBDMOperator;
import madgik.exareme.master.engine.executor.remote.operator.ExecuteQueryState;
import madgik.exareme.utils.encoding.Base64Util;
import madgik.exareme.worker.art.concreteOperator.AbstractNiNo;
import org.apache.log4j.Logger;

/**
 * @author herald
 */
public class ExecuteDM extends AbstractNiNo {

    private static Logger log = Logger.getLogger(ExecuteDM.class);

    @Override public void run() throws Exception {
        log.trace("Parse DM Operator ...");
        AdpDBDMOperator dmOp =
            Base64Util.decodeBase64(super.getParameterManager().getQueryString());

        log.debug("Operator Name : " + super.getSessionManager().getOperatorName());

        log.trace("Create state ...");
        ExecuteQueryState state =
            new ExecuteQueryState(dmOp, getDiskManager(), getProcessManager(), false);

        log.debug("Execute build index ...");
        state.executeDM();

        exit(0);
    }
}
