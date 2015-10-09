/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containerJobs;

import madgik.exareme.worker.art.container.ContainerJobResults;
import madgik.exareme.worker.art.container.ContainerSession;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createOperator.CreateOperatorEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createOperatorConnect.CreateOperatorConnectEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.destroy.DestroyEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.start.StartEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.stop.StopEvent;

import java.util.ArrayList;

//import madgik.exareme.db.art.executionEngine.dynamicExecutionEngine.event.createBuffer.CreateBufferEvent;
//import madgik.exareme.db.art.executionEngine.dynamicExecutionEngine.event.createBufferConnect.CreateBufferConnectEvent;


/**
 * @author heraldkllapi
 */
public class ContainerJobsEvent extends ExecEngineEvent {
    public ArrayList<CreateOperatorEvent> operators = null;
    //public ArrayList<CreateBufferEvent> buffers = null;
    //public ArrayList<CreateBufferConnectEvent> connects = null;
    public ArrayList<CreateOperatorConnectEvent> connects = null;
    public ArrayList<DestroyEvent> destroys = null;
    public ArrayList<StartEvent> starts = null;
    public ArrayList<StopEvent> stops = null;
    // Process
    public ContainerSession session = null;
    public ContainerJobResults results = null;
    public int messageCount = 0;

    public ContainerJobsEvent(PlanEventSchedulerState state) {
        super(state);
        operators = new ArrayList<>();
        //buffers = new ArrayList<>();
        //connects = new ArrayList<>();
        connects = new ArrayList<>();
        destroys = new ArrayList<>();
        starts = new ArrayList<>();
        stops = new ArrayList<>();
    }
}
