/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.eventProcessor;

import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;

public class EventProcessorThread extends Thread {

    private static Logger log = Logger.getLogger(EventProcessorThread.class);
    private EventQueue eventQueue = null;
    private ExecutorService executor = null;
    private EventProcessor processor = null;

    public EventProcessorThread(EventQueue eventQueue, ExecutorService executor,
        EventProcessor processor) {
        this.eventQueue = eventQueue;
        this.executor = executor;
        this.processor = processor;
    }

    @Override public void run() {
        while (true) {
            try {
                ActiveEvent next = eventQueue.getNext();
                EventHandlerRunnable job = new EventHandlerRunnable(next, processor);
                executor.submit(job);
            } catch (Exception e) {
                log.error("Cannot run event", e);
            }
        }
    }
}
