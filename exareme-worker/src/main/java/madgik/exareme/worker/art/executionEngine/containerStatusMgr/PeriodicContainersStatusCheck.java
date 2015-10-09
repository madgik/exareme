package madgik.exareme.worker.art.executionEngine.containerStatusMgr;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Vaggelis  on 3/6/2015.
 */
public class PeriodicContainersStatusCheck {
    private static int sleepTime_s = 10;
    private static Logger log = Logger.getLogger(PeriodicContainersStatusCheck.class);
    Set<EntityName> containersToCheck;
    PlanEventScheduler planEventScheduler;


    public PeriodicContainersStatusCheck(PlanEventScheduler planEventScheduler,
        Set<EntityName> containersToCheck) {
        this.containersToCheck = containersToCheck;
        this.planEventScheduler = planEventScheduler;
        Thread periodic = new PeriodicCheck();
        periodic.start();
    }

    public PeriodicContainersStatusCheck(PlanEventScheduler planEventScheduler) {
        this(planEventScheduler, new HashSet<EntityName>());

    }

    public void addConainerToCheck(EntityName container) {
        log.trace("Adding container to check: " + container);
        containersToCheck.add(container);
    }

    private class PeriodicCheck extends Thread {
        public void run() {
            int i = 10;
            while (i > 0) {
                i--;
                Set<EntityName> faultyContainers = new HashSet<>();
                for (EntityName containerName : containersToCheck) {
                    log.trace("Checking container: " + containerName);
                    try {
                        ContainerProxy containerProxy = planEventScheduler.getState().registryProxy
                            .lookupContainer(containerName);

                    } catch (Exception e) {
                        log.error("Container connection error: " + e);
                        faultyContainers.add(containerName);
                    }

                }
                if (!faultyContainers.isEmpty()) {
                    if (planEventScheduler != null) {
                        planEventScheduler.containersError(faultyContainers);
                    } else {
                        log.error("PlanEventScheduler should not be null!");
                    }
                }

                log.trace("Going to sleep for " + sleepTime_s + " seconds.");
                try {
                    Thread.sleep(sleepTime_s * 1000);
                } catch (InterruptedException e) {
                }

            }
        }
    }
}
