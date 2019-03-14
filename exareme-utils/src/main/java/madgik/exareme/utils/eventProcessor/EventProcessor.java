/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.eventProcessor;

import madgik.exareme.utils.eventProcessor.event.stop.StopEvent;
import madgik.exareme.utils.eventProcessor.event.stop.StopEventHandler;
import madgik.exareme.utils.eventProcessor.event.stop.StopEventListener;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author herald
 */
public class EventProcessor {

    private static final Logger log = Logger.getLogger(EventProcessor.class);
    private int concurrentEvents = 0;
    private EventQueue eventQueue = null;
    private ExecutorService executor = null;
    private EventProcessorThread processorThread = null;

    public EventProcessor(int concurrentEvents) {
        this.concurrentEvents = concurrentEvents;
        this.eventQueue = new EventQueue();
        this.executor = Executors.newFixedThreadPool(this.concurrentEvents);
    }

    public EventFuture queue(Event event, EventHandler handler, EventListener listener) {
        //	logger.info("Queued event: " + event.getClass().getName());
        EventFuture future = new EventFuture(event);
        ActiveEvent activeEvent = new ActiveEvent(event, handler, listener, future);
        eventQueue.queue(activeEvent);
        return future;
    }

    public void start() {
        processorThread = new EventProcessorThread(eventQueue, executor, this);
        processorThread.start();
    }

    public void stop() {
        this.stop(false);
    }

    public void stop(boolean now) {
        if (now) {
            // TODO(herald): Take buckup!
            processorThread.stop();
        } else {
            Semaphore semaphore = new Semaphore(0);
            StopEventHandler eventHandler = new StopEventHandler(semaphore);
            StopEventListener eventListener = new StopEventListener();
            queue(new StopEvent(), eventHandler, eventListener);
            /* Wait for termination */
            try {
                semaphore.acquire();
            } catch (Exception e) {
            }
            processorThread.stop();
        }
        executor.shutdownNow();
        log.info("Event Processor Stopped!");
    }
}
