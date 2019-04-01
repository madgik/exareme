/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;

import madgik.exareme.worker.art.container.ContainerID;
import madgik.exareme.worker.art.executionEngine.clockTickManager.ClockTickManagerInterface;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class DynamicClockTickManager extends EventSchedulerManipulator
        implements ClockTickManagerInterface {
    private static final Logger log = Logger.getLogger(DynamicClockTickManager.class);

    @Override
    public void containerWarningClockTick(ContainerID id, long timeToTick_ms, long quantumCount)
            throws RemoteException {
        log.info("Container Warning Clock Tick");
        getGlobalScheduler().containerWarningClockTick(id, timeToTick_ms, quantumCount);
        for (PlanEventScheduler scheduler : getAllSessions()) {
            scheduler.containerWarningClockTick(id, timeToTick_ms, quantumCount);
        }
    }

    @Override
    public void containerClockTick(ContainerID id, long quantumCount)
            throws RemoteException {
        log.info("Container Clock Tick");
        getGlobalScheduler().containerClockTick(id, quantumCount);
        for (PlanEventScheduler scheduler : getAllSessions()) {
            scheduler.containerClockTick(id, quantumCount);
        }
    }

    @Override
    public void globalWarningClockTick(long timeToTick_ms, long quantumCount)
            throws RemoteException {
        log.info("Global Warning Clock Tick");
        getGlobalScheduler().globalWarningClockTick(timeToTick_ms, quantumCount);
        for (PlanEventScheduler scheduler : getAllSessions()) {
            scheduler.globalWarningClockTick(timeToTick_ms, quantumCount);
        }
    }

    @Override
    public void globalClockTick(long quantumCount) throws RemoteException {
        log.info("Global Clock Tick");
        getGlobalScheduler().globalClockTick(quantumCount);
        for (PlanEventScheduler scheduler : getAllSessions()) {
            scheduler.globalClockTick(quantumCount);
        }
    }
}
