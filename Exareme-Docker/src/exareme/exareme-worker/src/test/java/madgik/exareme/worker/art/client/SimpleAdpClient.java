/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.client;

import madgik.exareme.utils.check.Check;
import madgik.exareme.worker.arm.compute.ArmCompute;
import madgik.exareme.worker.arm.manager.ArmManager;
import madgik.exareme.worker.arm.manager.ArmManagerFactory;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.manager.ArtManagerFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A simple client that uses ARM and ART.
 *
 * @author heraldkllapi
 */
public class SimpleAdpClient {

    private static Logger log = Logger.getLogger(SimpleAdpClient.class);

    public static void main(String[] args) throws Exception {
        Logger.getRootLogger().setLevel(Level.ALL);

        log.info("Create and start art manager");
        ArtManager art = ArtManagerFactory.createRmiArtManager();
        art.getRegistryManager().startArtRegistry();
        art.getExecutionEngineManager().startExecutionEngine();
        art.getContainerManager().startContainer();

        log.info("Create and start arm manager");
        ArmManager arm = ArmManagerFactory.createRmiArtManager();
        ArmCompute compute = arm.getComputeManager().getCompute();
        Check.NotNull(compute);

        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        log.info("Create a compute session");
        for (int i = 0; i < 200; ++i) {
            Thread.sleep(10);
            threadPool.submit(
                    new SimpleClientJob(compute, 7  /* numContainers */, 1  /* keepFor_ms */, 1  /* release_ms */));
        }
        threadPool.shutdown();
        threadPool.awaitTermination(100000, TimeUnit.DAYS);
        System.exit(0);
    }
}
