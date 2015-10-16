/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.eventProcessor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author herald
 */
public class EventQueue {
    private Semaphore count = null;
    private List<ActiveEvent> queue = null;

    public EventQueue() {
        this.count = new Semaphore(0);
        this.queue = Collections.synchronizedList(new LinkedList<ActiveEvent>());
    }

    public void queue(ActiveEvent event) {
        queue.add(event);
        count.release();
    }

    public ActiveEvent getNext() throws InterruptedException {
        count.acquire();
        return queue.remove(0);
    }
}
