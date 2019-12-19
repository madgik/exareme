/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.remote.operator.process;

import madgik.exareme.common.app.engine.AdpDBSelectOperator;
import madgik.exareme.common.schema.Table;
import madgik.exareme.master.engine.executor.remote.operator.ExecuteQueryState;
import madgik.exareme.utils.encoding.Base64Util;
import madgik.exareme.worker.art.concreteOperator.AbstractMiMo;
import org.apache.log4j.Logger;

/**
 * @author herald
 */
public class ExecuteSelect extends AbstractMiMo {

    private static Logger log = Logger.getLogger(ExecuteSelect.class);

    @Override
    public void run() throws Exception {
        log.trace("Parse DB Operator ...");
        AdpDBSelectOperator dbOp =
                Base64Util.decodeBase64(super.getParameterManager().getQueryString());

        String operatorName = super.getSessionManager().getOperatorName();
        log.debug("Operator Name : " + operatorName);
        dbOp.printStatistics(operatorName);

        log.trace("Create state ...");
        ExecuteQueryState state =
                new ExecuteQueryState(dbOp, getDiskManager(), getProcessManager(), false);

        log.debug("Read inputs ...");
        state.readInputs(super.getAdaptorManager());

        super.getAdaptorManager().closeAllInputs();

        log.debug("Execute query ...");
        state.executeSelect();

        log.debug("Write output ...");
        if (super.getAdaptorManager().getOutputCount() > 0) {
            state.writeOutputs(super.getAdaptorManager());
        }

        super.getAdaptorManager().closeAllOutputs();

        if (super.getAdaptorManager().getOutputCount() == 0) {
            log.debug("Save the non-temporary tables ...");
            Table outputTable = dbOp.getQuery().getOutputTable().getTable();
            if (outputTable.isTemp() == false) {
                log.debug("Saving output table (" + outputTable.getName() + ") ...");
                state.saveOutputTable();
            }
        } else {
            log.debug("Skip saving tables (" + dbOp.getQuery().getOutputTable().getTable().getName()
                    + ") ... ");
        }
        log.info("Currently executing: \n " + state.toString());
        exit(0, state.getExitMessage());
    }
}
