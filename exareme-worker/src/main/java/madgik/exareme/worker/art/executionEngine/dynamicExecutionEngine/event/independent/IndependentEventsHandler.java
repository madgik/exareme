/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.independent;

import madgik.exareme.utils.eventProcessor.ActiveEvent;
import madgik.exareme.utils.eventProcessor.EventHandler;
import madgik.exareme.utils.eventProcessor.EventHandlerRunnable;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.ExecEngineConstants;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author heraldkllapi
 */
public class IndependentEventsHandler implements EventHandler<IndependentEvents> {
    private static final long serialVersionUID = 1L;
    private static final ExecutorService service =
        Executors.newFixedThreadPool(ExecEngineConstants.THREADS_PER_INDEPENDENT_TASKS);
    private static final Logger log = Logger.getLogger(IndependentEventsHandler.class);
    private long startTime = 0;
    private long endTime = 0;

    public IndependentEventsHandler() {

    }

    @Override public void handle(IndependentEvents event, EventProcessor proc)
        throws RemoteException {
        try {
            startTime = System.currentTimeMillis();
            Semaphore wait = new Semaphore(0);
            // Pre-process
            for (ActiveEvent e : event.events) {
                ExecEngineEventHandler handler = (ExecEngineEventHandler) e.getHandler();
                handler.preProcess(e.getEvent(), event.state);
            }
            // Parallel-process
            for (ActiveEvent e : event.events) {
                ((ExecEngineEvent) e.getEvent()).wait = wait;
                service.submit(new EventHandlerRunnable(e, proc));
            }
            //      service.shutdown();
            //      log.debug("Waiting termination ...");
            wait.acquire(event.events.size());
            //      service.awaitTermination(1, TimeUnit.DAYS);
            //      log.debug("ok!");

            // Post-process
            for (ActiveEvent e : event.events) {
                ExecEngineEventHandler handler = (ExecEngineEventHandler) e.getHandler();
                handler.postProcess(e.getEvent(), event.state);
            }
            event.state.getStatistics().incrIndependentMessages();
            endTime = System.currentTimeMillis();
            event.state.getStatistics()
                .addTotalEventProcessTime(endTime - startTime, startTime - event.queueTime);
            event.state.getStatistics().setXaxIndependentMsgCount(event.events.size());
        } catch (Exception e) {
            throw new RemoteException("Cannot handle independent events", e);
        }
    }
}
