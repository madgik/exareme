/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.quantumClock;

import org.apache.log4j.Logger;

import java.util.concurrent.Semaphore;

/**
 * This class provide generic clock functionality. Use robust pulse generation algorithms in the
 * future.
 *
 * @author herald kllapi
 */
public abstract class QuantumClock extends Thread {
    private static final Logger log = Logger.getLogger(QuantumClock.class);
    private final long quantumSize;
    private final long warningSize;
    private final long warnTimeBeforeQuantum;
    private final Semaphore lock = new Semaphore(1);
    private boolean stop;
    private long bigBang;
    private long quantumCount = 0;
    // This is 1 when the net tick should the the warning tick else is 0
    private int warningPhase = 1;

    public QuantumClock(long warnTimeBeforeQuantum, long quantumSize) {
        // Maybe use a database with transactions for all these things in order to be persistent.
        this.quantumSize = quantumSize;
        this.warnTimeBeforeQuantum = warnTimeBeforeQuantum;
        this.warningSize = quantumSize - warnTimeBeforeQuantum;
        log.trace("Quantum Size = " + quantumSize);
        log.trace("Warn Size = " + warningSize);
        log.trace("Warn Time Before = " + warnTimeBeforeQuantum);
        this.setName("Global Quantum Clock");
    }

    public final void startDeamon() {
        stop = false;
        this.start();
    }

    public final void stopDeamon() {
        while (this.isAlive()) {
            try {
                lock.acquire();
                try {
                    stop = true;
                    this.interrupt();
                } finally {
                    lock.release();
                }
            } catch (InterruptedException _) {
                // Ignore interrupts
            }
        }
    }

    @Override public final void run() {
        // Start of time
        bigBang = System.currentTimeMillis();
        long lastClockTick = bigBang;

        // The time to sleep in the beggining is the same as the almost tick size
        long sleepTime = warningSize;
        while (!stop) {
            try {
                long now = System.currentTimeMillis();
                long elapsedTime = now - lastClockTick;
                if (elapsedTime >= sleepTime) {
                    // Call user defined handler
                    long offset = computeOffset(now);
                    try {
                        callTickFunction(offset);
                    } catch (Exception e) {
                        log.error("Cannot call tick report function", e);
                    }

                    // Re-compute error after function call bacause it may be expensive
                    now = System.currentTimeMillis();
                    offset = computeOffset(now);

                    // Clock tick
                    if (warningPhase == 0) {
                        quantumCount++;
                        lastClockTick = now;
                    }

                    // Change phase
                    warningPhase = 1 - warningPhase;
                    sleepTime = computeSleepTime(offset);
                } else {
                    // This can happen if the thread is interrupted!
                    sleepTime = computeSleepTime(0) - elapsedTime;
                }
                log.trace("Sleep for " + sleepTime);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                // Do nothing on interrupt!
            } catch (Exception ex) {
                log.error("Cannot report quantum event", ex);
            }
        }
        log.trace("Stopping ...");
    }

    private long computeOffset(long now) {
        // Remove previous quanta
        long elapsedTime = (now - bigBang) - quantumSize * quantumCount;
        long offset = elapsedTime - (warningPhase * warningSize + (1 - warningPhase) * quantumSize);
        log.trace(offset + " ms offset @ " + quantumCount);
        return offset;
    }

    private long computeSleepTime(long error) {
        return warningPhase * warningSize + (1 - warningPhase) * warnTimeBeforeQuantum - error;
    }

    private void callTickFunction(long error) {
        try {
            lock.acquire();
            try {
                if (warningPhase == 1) {
                    log.trace("Call tick warning function ...");
                    clockWarningTick(warnTimeBeforeQuantum - error, quantumCount);
                } else {
                    log.trace("Call tick function ...");
                    clockTick(quantumCount);
                }
            } finally {
                lock.release();
            }
        } catch (InterruptedException e) {
            // Ignore interrupts
        }
    }

    // This method is called when the clock is about to click
    protected abstract void clockWarningTick(long timeToTick_ms, long quantumCount);

    // This method is called at the time that the clock quantum expires
    protected abstract void clockTick(long quantumCount);
}
