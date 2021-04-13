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
    private static int sleepTime_s = 2;
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

    public void addContainerToCheck(EntityName container) {
        log.debug("Adding container to check: " + container);
        containersToCheck.add(container);
    }

    private class PeriodicCheck extends Thread {
        public void run() {
            while (!planEventScheduler.getState().isTerminated()) {
                Set<EntityName> faultyContainers = new HashSet<>();
                for (EntityName containerName : containersToCheck) {
                    log.debug("Checking container: " + containerName);
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
                        log.error("Reported container error and exiting!");
                        return;
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
            log.trace("Plan terminated");
        }
    }
}
