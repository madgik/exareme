/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.rmi;

import madgik.exareme.common.app.engine.AdpDBDMOperator;
import madgik.exareme.common.app.engine.AdpDBSelectOperator;
import madgik.exareme.common.schema.Index;
import madgik.exareme.common.schema.PhysicalTable;
import madgik.exareme.master.engine.AdpDBQueryScriptStatistics;
import madgik.exareme.master.engine.rmi.tree.ContainerTopology;
import madgik.exareme.master.engine.rmi.tree.TreeConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.optimizer.ContainerFilter;
import madgik.exareme.master.queryProcessor.optimizer.ContainerResources;
import madgik.exareme.master.queryProcessor.optimizer.SolutionSpace;
import madgik.exareme.master.queryProcessor.optimizer.scheduler.SchedulingResult;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.art.container.ContainerProxy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author heraldkllapi
 */
public class StateData implements Serializable {
    private static final long serialVersionUID = 1L;
    public ArrayList<AdpDBSelectOperator> dbOps = null;
    public ArrayList<AdpDBDMOperator> dmOps = null;
    public ArrayList<Index> indexes = null;
    public ArrayList<PhysicalTable> intermediate = null;
    public ArrayList<PhysicalTable> results = null;
    public List<Pair<ConcreteOperator, Integer>> tableBindings = null;
    public HashMap<String, Integer> inputTableCount = null;
    public ConcreteQueryGraph graph = null;
    public HashMap<String, ConcreteOperator[]> tableProducers = null;
    public String resultTableName = null;
    public AdpDBQueryScriptStatistics statistics = null;
    public ContainerProxy[] proxies;
    public transient ContainerFilter contFilter;
    public ArrayList<ContainerResources> containers = null;
    public SolutionSpace space = null;
    public SchedulingResult result = null;
    public int[] containerTablePartCounts = null;

    // The following are for tree execution
    public TreeConcreteQueryGraph treeGraph = null;
    public ContainerTopology topology = null;

    public StateData() {
        dbOps = new ArrayList<AdpDBSelectOperator>();
        dmOps = new ArrayList<AdpDBDMOperator>();
        indexes = new ArrayList<Index>();
        intermediate = new ArrayList<PhysicalTable>();
        results = new ArrayList<PhysicalTable>();
        tableBindings = new LinkedList<Pair<ConcreteOperator, Integer>>();
        inputTableCount = new HashMap<String, Integer>();
        graph = new ConcreteQueryGraph();
        tableProducers = new HashMap<String, ConcreteOperator[]>();
        statistics = new AdpDBQueryScriptStatistics();
        containers = new ArrayList<ContainerResources>();
    }

    public final void shallowCopyFrom(StateData other) {
        dbOps = other.dbOps;
        dmOps = other.dmOps;
        indexes = other.indexes;
        intermediate = other.intermediate;
        results = other.results;
        tableBindings = other.tableBindings;
        inputTableCount = other.inputTableCount;
        graph = other.graph;
        tableProducers = other.tableProducers;
        resultTableName = other.resultTableName;
        statistics = other.statistics;
        proxies = other.proxies;
        contFilter = other.contFilter;
        containers = other.containers;
        space = other.space;
        result = other.result;
        containerTablePartCounts = other.containerTablePartCounts;
        treeGraph = other.treeGraph;
        topology = other.topology;
    }
}
