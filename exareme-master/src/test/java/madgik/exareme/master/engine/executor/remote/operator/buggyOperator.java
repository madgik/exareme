package madgik.exareme.master.engine.executor.remote.operator;

import madgik.exareme.worker.art.concreteOperator.AbstractMiMo;
import org.apache.log4j.Logger;

/**
 * Created by vagos on 16/4/2015.
 */
public class buggyOperator extends AbstractMiMo {
    private static final Logger log = Logger.getLogger(buggyOperator.class);

    @Override
    public void run() throws Exception {
        int sleepSeconds = Integer.parseInt(
                super.getParameterManager().getParameters().getParameter("time").get(0).getValue());
        log.info("Going to sleep for: " + sleepSeconds + " secs and then throw bug exception...");
        Thread.sleep(1000 * sleepSeconds);
        if (true) {
            log.info("Throwing exception...");
            throw new Exception("bug..");
        } else {
            log.info("Exiting...");
            exit(0);
        }
    }
}
