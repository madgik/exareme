package madgik.exareme.master.engine.executor.remote.operator;

import madgik.exareme.worker.art.concreteOperator.AbstractMiMo;
import org.apache.log4j.Logger;

/**
 * Created by vagos on 16/4/2015.
 */
public class sleepOperator extends AbstractMiMo {
    private static final Logger log = Logger.getLogger(sleepOperator.class);

    @Override public void run() throws Exception {
        int sleepSeconds = Integer.parseInt(
            super.getParameterManager().getParameters().getParameter("time").get(0).getValue());
        log.info("Going to sleep for: " + sleepSeconds + " secs...");
        Thread.sleep(1000 * sleepSeconds);
        log.info("Exiting...");
        exit(0);
    }
}
