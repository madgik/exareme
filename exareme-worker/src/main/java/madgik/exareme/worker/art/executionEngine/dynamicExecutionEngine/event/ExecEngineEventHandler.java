/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event;

import madgik.exareme.utils.eventProcessor.Event;
import madgik.exareme.utils.eventProcessor.EventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public interface ExecEngineEventHandler<T extends Event> extends EventHandler<T> {

    void preProcess(T event, PlanEventSchedulerState state) throws RemoteException;

    void postProcess(T event, PlanEventSchedulerState state) throws RemoteException;
}
