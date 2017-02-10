package madgik.exareme.worker.art.quantumClock;//package madgik.exareme.worker.art.quantumClock;
//
//import junit.framework.TestCase;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//
//import java.util.Random;
//
///**
// * @author heraldkllapi
// */
//public class QuantumClockTest extends TestCase {
//    public QuantumClockTest(String testName) {
//        super(testName);
//    }
//
//    @Override protected void setUp() throws Exception {
//        super.setUp();
//    }
//
//    @Override protected void tearDown() throws Exception {
//        super.tearDown();
//    }
//
//    public void testClockTick() throws Exception {
//        Logger.getRootLogger().setLevel(Level.ALL);
//
//        // Start the clock deamon.
//        QuantumClockImpl clock = new QuantumClockImpl(200, 500);
//        clock.startDeamon();
//        Thread.sleep(5000);
//        clock.stopDeamon();
//
//        // The max delay should be limited
//        assertTrue(clock.maxDelay < 10  /* ms */);
//    }
//
//    public class QuantumClockImpl extends QuantumClock {
//        final Logger log = Logger.getLogger(QuantumClockImpl.class);
//        public long maxDelay = 0;
//        long begin = 0;
//        long quantumSize;
//        long warnTimeBeforeQuantum;
//        Random rand = new Random();
//
//        public QuantumClockImpl(long warnTimeBeforeQuantum, long quantumSize) {
//            super(warnTimeBeforeQuantum, quantumSize);
//            this.warnTimeBeforeQuantum = warnTimeBeforeQuantum;
//            this.quantumSize = quantumSize;
//            this.begin = System.currentTimeMillis();
//        }
//
//        @Override public void clockTick(long quantumCount) {
//            try {
//                log.info(System.currentTimeMillis() - begin);
//                double delay =
//                    (System.currentTimeMillis() - begin) - (quantumCount + 1) * quantumSize;
//                maxDelay = (long) Math.max(maxDelay, delay);
//                log.info(delay);
//
//                long sleepTime = 10 + Math.abs(rand.nextInt() % 100);
//                Thread.sleep(sleepTime);
//                log.debug("x: " + sleepTime);
//            } catch (Exception e) {
//                log.error("Error", e);
//            }
//        }
//
//        @Override protected void clockWarningTick(long timeToNextMS, long quantumCount) {
//            log.info(System.currentTimeMillis() - begin);
//            long delay = (System.currentTimeMillis() - begin) - (quantumCount + 1) * quantumSize
//                + warnTimeBeforeQuantum;
//            maxDelay = Math.max(maxDelay, delay);
//            log.info(delay);
//            log.debug("Time to next: " + timeToNextMS);
//        }
//    }
//}
