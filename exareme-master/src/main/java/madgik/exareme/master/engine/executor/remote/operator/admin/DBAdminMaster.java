package madgik.exareme.master.engine.executor.remote.operator.admin;

import madgik.exareme.worker.art.concreteOperator.AbstractMiMo;
import madgik.exareme.worker.art.concreteOperator.manager.AdaptorManager;
import madgik.exareme.worker.art.concreteOperator.manager.ParameterManager;
import madgik.exareme.worker.art.parameter.Parameter;
import org.apache.log4j.Logger;

/**
 * @author alex
 */
public class DBAdminMaster extends AbstractMiMo {
    private static final Logger log = Logger.getLogger(DBAdminMaster.class);

    @Override public void run() throws Exception {
        log.info(DBAdminMaster.class.toString() + " ...");

        ParameterManager parameterManager = super.getParameterManager();
        String query = parameterManager.getQueryString();
        log.info("Query : " + query);

        for (Parameter parameter : parameterManager.getParameters()) {
            log.info(parameter.getName() + " : " + parameter.getValue());
        }

        AdaptorManager adaptorManager = super.getAdaptorManager();
        adaptorManager.closeAllInputs();
        log.info("Inputs closed.");

        int outputCount = adaptorManager.getOutputCount();
        log.info("Output : " + outputCount);

        exit(0, "exit");
    }

}
