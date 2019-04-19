/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine;

import madgik.exareme.common.app.engine.AdpDBDMOperator;
import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBSelectOperator;
import madgik.exareme.common.schema.Index;
import madgik.exareme.common.schema.PhysicalTable;
import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.master.engine.rmi.InputData;
import madgik.exareme.master.engine.rmi.StateData;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.optimizer.scheduler.SchedulingResult;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.worker.art.container.ContainerProxy;

import java.io.Serializable;
import java.util.List;

/**
 * @author herald
 */
public class AdpDBQueryExecutionPlan implements Serializable {
    private InputData input = null;
    private StateData state = null;

    public AdpDBQueryExecutionPlan(InputData input, StateData state) {
        this.input = input;
        this.state = state;
    }

    public QueryScript getScript() {
        return input.script;
    }

    public Registry.Schema getSchema() {
        return input.schema;
    }

    public ConcreteQueryGraph getGraph() {
        return state.graph;
    }

    public SchedulingResult getSchedulingResult() {
        return state.result;
    }

    public ContainerProxy[] getContainerProxies() {
        return state.proxies;
    }

    public List<AdpDBSelectOperator> getQueryOperators() {
        return state.dbOps;
    }

    public List<AdpDBDMOperator> getDataManipulationOperators() {
        return state.dmOps;
    }

    public List<PhysicalTable> getIntermediateTables() {
        return state.intermediate;
    }

    public List<PhysicalTable> getResultTables() {
        return state.results;
    }

    public List<Index> getBuildIndexes() {
        return state.indexes;
    }

    public AdpDBQueryID getQueryID() {
        return input.queryID;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("State:");
        for (ConcreteOperator operator : state.graph.getOperators()) {
            str.append(operator.getName());
            for (AdpDBSelectOperator op : state.dbOps) {
                op.printStatistics(operator.getName().toString());
            }
        }
        str.append("dbOps :");

        str.append("dbmOps :");

        return str.toString();
    }

    //  public void printQueryExecutionPlan(){
    //    StringBuilder out = new StringBuilder();
    //    out.append("Execution plan :");
    //    out.append("\nInput data :");
    //    out.append("\nQuery ID : " + input.queryID.getQueryID());
    //    out.append("\nSchedule : " + input.schedule);
    //    out.append("\nValidate : " + input.validate);
    //    out.append("\nMax Cont : " + input.maxNumCont);
    //
    //    out.append("\nState data :");
    //    out.append("\nDM Ops size : " + state.dmOps.size());
    //    out.append("\nDB Ops size : " + state.dbOps.size());
    //    for (AdpDBSelectOperator dbOp : state.dbOps) {
    //      out.append("\ndbOp type : " + dbOp.getType());
    //    }
    //
    //
    //    System.out.println(out);
    //
    //
    //  }
}
