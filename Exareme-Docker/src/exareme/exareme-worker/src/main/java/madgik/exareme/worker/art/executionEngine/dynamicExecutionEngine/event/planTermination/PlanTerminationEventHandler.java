/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.planTermination;

import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanTerminationListener;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.Date;

/**
 * @author herald
 */
public class PlanTerminationEventHandler implements ExecEngineEventHandler<PlanTerminationEvent> {
    public static final PlanTerminationEventHandler instance = new PlanTerminationEventHandler();
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(PlanTerminationEventHandler.class);

    public PlanTerminationEventHandler() {
    }

    @Override
    public void preProcess(PlanTerminationEvent event, PlanEventSchedulerState state)
            throws RemoteException {
        if (!state.isTerminated()) {
            for (ContainerProxy proxy : state.getContainerProxies()) {
                try {
                    log.debug("closing session of container : " + proxy.getEntityName().getName());
                    proxy.destroySessions(state.getPlanSessionID());
                } catch (Exception e) {
                    log.error("Cannot close the sessions for proxy: " + proxy, e);
                    // throw new ServerException("Cannot close all sessions", e);
                }
            }
            state.getStatistics().setEndTime(System.currentTimeMillis());
        } else {
            log.debug("Plan already terminated!");
        }
        synchronized (state.terminationListeners) {
            log.debug("Trigger the termination listeners ... ");
            int listenerCount = 0;
            for (PlanTerminationListener listener : state.terminationListeners) {
                listener.terminated(state.getPlanSessionID());
                listenerCount++;
            }
            state.terminationListeners.clear();
            log.debug("Triggered " + listenerCount + " listeners!");
        }
        if (!state.isTerminated()) {
            state.setTerminated(true);
            state.getPlanSession().getPlanSessionStatus().setFinished(new Date());
        }
        /* Clean */
        for (OperatorEntity coe : state.groupDependencySolver().getOperatorsWithActiveResources()) {
            state.resourceManager.getAvailableResources(coe.container).releaseMemory(coe);
        }
        state.resourceManager.printUsage("Plan Terminated, Active: ");

    }

    @Override
    public void handle(PlanTerminationEvent event, EventProcessor proc)
            throws RemoteException {
    }

    @Override
    public void postProcess(PlanTerminationEvent event, PlanEventSchedulerState state)
            throws RemoteException {
        state.getStatistics().incrControlMessagesCountBy(event.messageCount);
    }
}
