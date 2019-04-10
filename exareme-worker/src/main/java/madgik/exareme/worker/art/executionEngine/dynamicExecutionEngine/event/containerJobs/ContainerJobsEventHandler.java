/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containerJobs;

import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.container.ContainerJobResult;
import madgik.exareme.worker.art.container.ContainerJobResults;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createOperator.CreateOperatorEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createOperator.CreateOperatorEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createOperatorConnect.CreateOperatorConnectEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createOperatorConnect.CreateOperatorConnectEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.destroy.DestroyEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.destroy.DestroyEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.independent.IndependentEvents;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.start.StartEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.start.StartEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.stop.StopEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.stop.StopEventHandler;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;


/**
 * @author heraldkllapi
 */
public class ContainerJobsEventHandler implements ExecEngineEventHandler<ContainerJobsEvent> {

    public static final ContainerJobsEventHandler instance = new ContainerJobsEventHandler();

    public static ContainerJobsEvent getEvent(String container, IndependentEvents events,
                                              HashMap<String, ContainerJobsEvent> jobsMap, PlanEventSchedulerState state) {
        ContainerJobsEvent jobs = jobsMap.get(container);
        if (jobs == null) {
            jobs = new ContainerJobsEvent(state);
            events.addEvent(jobs, ContainerJobsEventHandler.instance,
                    ContainerJobsEventListener.instance);
            jobsMap.put(container, jobs);
        }
        return jobs;
    }

    @Override
    public void preProcess(ContainerJobsEvent event, PlanEventSchedulerState state)
            throws RemoteException {
        for (CreateOperatorEvent op : event.operators) {
            CreateOperatorEventHandler.instance.preProcess(op, state);
            event.session = op.session;
        }

        for (CreateOperatorConnectEvent con : event.connects) {
            CreateOperatorConnectEventHandler.instance.preProcess(con, state);
            event.session = con.session;
        }

        for (DestroyEvent destroy : event.destroys) {
            DestroyEventHandler.instance.preProcess(destroy, state);
            event.session = destroy.session;
        }
        for (StartEvent start : event.starts) {
            StartEventHandler.instance.preProcess(start, state);
            event.session = start.session;
        }
        for (StopEvent stop : event.stops) {
            StopEventHandler.instance.preProcess(stop, state);
            event.session = stop.session;
        }
    }

    @Override
    public void handle(ContainerJobsEvent event, EventProcessor proc)
            throws RemoteException {
        ContainerJobs jobs = new ContainerJobs();
        for (CreateOperatorEvent op : event.operators) {
            jobs.addJobs(op.jobs);
        }
        for (CreateOperatorConnectEvent con : event.connects) {
            jobs.addJobs(con.jobs);
        }
        for (DestroyEvent destroy : event.destroys) {
            jobs.addJobs(destroy.jobs);
        }
        for (StartEvent start : event.starts) {
            jobs.addJobs(start.jobs);
        }
        for (StopEvent stop : event.stops) {
            jobs.addJobs(stop.jobs);
        }
        event.results = event.session.execJobs(jobs);
        event.messageCount = 1;
    }

    @Override
    public void postProcess(ContainerJobsEvent event, PlanEventSchedulerState state)
            throws RemoteException {
        List<ContainerJobResult> results = event.results.getJobResults();
        int index = 0;
        for (CreateOperatorEvent op : event.operators) {
            op.results = new ContainerJobResults();
            op.results.addJobResult(results.get(index));
            index++;
            CreateOperatorEventHandler.instance.postProcess(op, state);
        }
        for (CreateOperatorConnectEvent con : event.connects) {
            con.results = new ContainerJobResults();
            con.results.addJobResult(results.get(index));
            index++;
            CreateOperatorConnectEventHandler.instance.postProcess(con, state);
        }

        for (DestroyEvent destroy : event.destroys) {
            destroy.results = new ContainerJobResults();
            destroy.results.addJobResult(results.get(index));
            index++;
            DestroyEventHandler.instance.postProcess(destroy, state);
        }
        for (StartEvent start : event.starts) {
            start.results = new ContainerJobResults();
            start.results.addJobResult(results.get(index));
            index++;
            StartEventHandler.instance.postProcess(start, state);
        }
        for (StopEvent stop : event.stops) {
            stop.results = new ContainerJobResults();
            stop.results.addJobResult(results.get(index));
            index++;
            StopEventHandler.instance.postProcess(stop, state);
        }
        state.getStatistics().incrControlMessagesCountBy(event.messageCount);
    }
}
