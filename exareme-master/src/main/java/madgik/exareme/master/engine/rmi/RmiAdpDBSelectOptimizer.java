/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.rmi;

import madgik.exareme.common.app.engine.AdpDBOperatorType;
import madgik.exareme.common.app.engine.AdpDBSelectOperator;
import madgik.exareme.common.schema.Partition;
import madgik.exareme.common.schema.PhysicalTable;
import madgik.exareme.common.schema.Select;
import madgik.exareme.common.schema.TableView;
import madgik.exareme.common.schema.expression.DataPattern;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.master.engine.AdpDBStatusManager;
import madgik.exareme.master.engine.parser.SemanticException;
import madgik.exareme.master.engine.rmi.tree.ContainerTopologyFactory;
import madgik.exareme.master.engine.rmi.tree.TreeGraphFactory;
import madgik.exareme.master.engine.rmi.tree.TreeUtils;
import madgik.exareme.master.engine.util.SchemaUtil;
import madgik.exareme.master.queryProcessor.ConsoleUtils;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;
import madgik.exareme.master.queryProcessor.graph.Link;
import madgik.exareme.master.queryProcessor.graph.VerifyDAG;
import madgik.exareme.master.queryProcessor.optimizer.ContainerResources;
import madgik.exareme.master.queryProcessor.optimizer.SolutionSpace;
import madgik.exareme.master.queryProcessor.optimizer.assignedOperatorFilter.FastSubgraphFilter;
import madgik.exareme.master.queryProcessor.optimizer.containerFilter.FastContainerFilter;
import madgik.exareme.master.queryProcessor.optimizer.containerFilter.NoContainerFilter;
import madgik.exareme.master.queryProcessor.optimizer.scheduler.SchedulingResult;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.check.Check;
import madgik.exareme.utils.collections.ListUtil;
import madgik.exareme.utils.combinatorics.CartesianProduct;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.utils.properties.AdpDBProperties;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.utils.serialization.SerializationUtil;
import madgik.exareme.worker.art.container.ContainerLocator;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.container.rmi.RmiContainerProxy;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.*;

/**
 * @author herald
 */
public class RmiAdpDBSelectOptimizer {
    private static final Logger log = Logger.getLogger(RmiAdpDBSelectOptimizer.class);
    private static final boolean filterColumns =
        AdpDBProperties.getAdpDBProps().getBoolean("db.execution.filterUnusedColumns");
    // TODO(herald): find a better way to do this ... also use topology ...
    //  private final int TREE_REDUCTION_PER_LEVEL = 2;
    private static final int VIRTUAL_CPUS_PER_CONTAINER = 1;
    private static int containerCounter = 0;

    public RmiAdpDBSelectOptimizer(AdpDBStatusManager statusManager, ArtManager manager) {
    }

    public AdpDBQueryExecutionPlan optimize(InputData input, AdpDBClientProperties props)
        throws RemoteException {
        log.debug("Use sketching: " + OptimizerConstants.USE_SKETCH);
        InputData sketchInput = new InputData();
        log.debug("Creating sketch input ...");
        createSketchInput(input, sketchInput, props);
        log.debug("Creating states ...");
        StateData sketchState = new StateData();
        log.debug("Getting containers ... ");
        sketchState.proxies = ArtRegistryLocator.getArtRegistryProxy().getContainers();
        log.debug("Creating proxies and virtual containers (if needed) ...");
        createProxies(sketchInput, sketchState, props);
        if (props.isTreeEnabled()) {
            log.debug("Generating tree graph ...");
            sketchState.topology = ContainerTopologyFactory.getTopology();
            sketchState.treeGraph = TreeGraphFactory.createQueryGraph(sketchState.topology);
            sketchState.treeGraph = null;
        }
        log.debug("Generate dataflow graph ...");
        createQueryGraph(sketchInput, sketchState, props);
        log.debug("Checking if the graph is DAG ...");
        Check.True(VerifyDAG.isDAG(sketchState.graph), "Graph not DAG!");
        log.debug("Bind operators to location of table: " + sketchState.tableBindings.size());
        FastSubgraphFilter subgraphFilter =
            new FastSubgraphFilter(sketchState.graph.getNumOfOperators());
        for (Pair<ConcreteOperator, Integer> co : sketchState.tableBindings) {
            subgraphFilter.assignOperator(co.a.opID, co.b);
        }
        log.debug("Create container filter ...");
        createContainerFilter(sketchInput, sketchState, props);
        if (sketchInput.validate) {
            log.debug("Validating graph ...");
            RmiAdpDBOptimizer.validateGraph(sketchInput, sketchState, props);
        }
        log.debug("Post process graph ...");
        RmiAdpDBOptimizer.postProcessGraph(sketchInput, sketchState, props);
        log.debug("Estimating running time and data for each operator ... ");
        RmiAdpDBOptimizer.estimateTimeAndData(sketchInput, sketchState, props);
        log.debug("Annotating graph with historical data ...");
        RmiAdpDBOptimizer
            .annotateWithHistoricalData(sketchState.graph, sketchInput.queryData, sketchState.dbOps,
                props);
        log.debug("Adding local file data ...");
        RmiAdpDBOptimizer.addLocalFileData(sketchState.graph);
        log.debug("Create the solution space ...");
        if (props.isTreeEnabled()) {
            //      sketchState.space = RmiAdpDBTreeSelectScheduler.schedule(
            //          sketchState.graph, subgraphFilter, sketchState.containers, sketchState.contFilter);
        } else {
            sketchState.space = RmiAdpDBSelectScheduler
                .schedule(sketchState.graph, subgraphFilter, sketchState.containers,
                    sketchState.contFilter);
        }
        log.debug("Creating final graph from sketch ...");
        StateData state = new StateData();
        createFinalGraphFromSketch(sketchInput, input, sketchState, state, props);
        String skyChoice =
            AdpDBProperties.getAdpDBProps().getString("db.optimizer.scheduler.plan.choice");
        Check.NotNull(skyChoice, "No valid schedule selection method");
        state.result = getSchedule(skyChoice, state.space);
        log.debug("Choosing the schedule to execute: " + skyChoice);
        ConsoleUtils.printStatisticsToLog(
            skyChoice + " " + state.result.getStatistics().getContainersUsed(), state.graph,
            RmiAdpDBSelectScheduler.runTimeParams, state.result.getStatistics(), log, Level.DEBUG);
        log.debug("Remove table input gravity operators ...");
        RmiAdpDBOptimizer.removeInputTableGravity(sketchInput, state, props);
        //        for (AdpDBSelectOperator op : state.dbOps) {
        //            op.printStatistics(op.getOutputTables().iterator().next());
        //        }
        return new AdpDBQueryExecutionPlan(sketchInput, state);
    }

    private void createSketchInput(InputData input, InputData sketchInput,
        AdpDBClientProperties props) throws SemanticException {
        sketchInput.copyFrom(input);
        if (OptimizerConstants.USE_SKETCH == false) {
            return;
        }
        log.info("Using sketch ...");
        Registry.Schema schema = input.schema;
        Registry.Schema sketchSchema = sketchInput.schema;
        for (PhysicalTable table : schema.getPhysicalTables()) {
            PhysicalTable sketchTable = sketchSchema.getPhysicalTable(table.getName());
            int parts = sketchTable.getNumberOfPartitions();
            for (int i = parts - 1; i >= OptimizerConstants.MAX_TABLE_PARTS; --i) {
                sketchTable.removePartition(i);
            }
            Check.True(sketchTable.getNumberOfPartitions() <= OptimizerConstants.MAX_TABLE_PARTS,
                "Sketch not updated correctly!");
        }
    }

    private void createProxies(InputData input, StateData state, AdpDBClientProperties props)
        throws RemoteException {
        addVirtualContainers(input, state);
        // Sort the proxies based on name
        Arrays.sort(state.proxies, new Comparator<ContainerProxy>() {
            @Override public int compare(ContainerProxy o1, ContainerProxy o2) {
                return o1.getEntityName().compareTo(o2.getEntityName());
            }
        });
        for (int i = 0; i < state.proxies.length; ++i) {
            state.containers.add(new ContainerResources());
        }
        log.debug("Using " + state.proxies.length + " containers.");
        state.containerTablePartCounts = new int[state.proxies.length];
    }

    private void addVirtualContainers(InputData input, StateData state) throws RemoteException {
        // In the development machine, add all containers as localhost
        if (AdpProperties.getEnvProps().getString("run_level").equals("develop")) {
            Check.Equals(state.proxies.length, 1);
            log.debug("Adding virtual containers ...");
            HashMap<String, ContainerProxy> virtualContainers =
                new HashMap<String, ContainerProxy>();
            for (PhysicalTable pt : input.schema.getPhysicalTables()) {
                log.debug("Checking " + pt.getName() + " ...");
                for (int p = 0; p < pt.getNumberOfPartitions(); ++p) {
                    Partition part = pt.getPartition(p);
                    for (String location : part.getLocations()) {
                        if (virtualContainers.containsKey(location) || state.proxies[0].
                            getEntityName().getName().startsWith(location)) {
                            continue;
                        }
                        for (int cpu = 0; cpu < VIRTUAL_CPUS_PER_CONTAINER; ++cpu) {
                            ContainerLocator.getLocalContainer().connect().createProxy();
                            ContainerProxy vProxy = new RmiContainerProxy(NetUtil.getIPv4(),
                                location + "_container_" + cpu,
                                ArtRegistryLocator.getLocalRmiRegistryEntityName());
                            ArtRegistryLocator.getArtRegistryProxy().registerContainer(vProxy);
                            virtualContainers.put(location, vProxy);
                        }
                    }
                }
            }
            log.debug("Added " + virtualContainers.size() + " virtual containers.");
            if (virtualContainers.isEmpty() == false) {
                state.proxies = ArtRegistryLocator.getArtRegistryProxy().getContainers();
            }
        }
    }

    private void createContainerFilter(InputData input, StateData state,
        AdpDBClientProperties props) {
        if (input.maxNumCont < 0) {
            log.debug("No container filter used!");
            state.contFilter = NoContainerFilter.getInstance();
        } else {
            log.debug("Creating container filter "
                + "(Greedy algorithm that select those with most tables) ...");
            ArrayList<Pair<Integer, Integer>> contTablesCntPairs =
                new ArrayList<Pair<Integer, Integer>>();
            for (int i = 0; i < state.proxies.length; ++i) {
                contTablesCntPairs.add(new Pair<Integer, Integer>(i, 0));
            }
            for (Pair<ConcreteOperator, Integer> co : state.tableBindings) {
                contTablesCntPairs.get(co.b).b += 1;
            }
            Comparator<Pair<Integer, Integer>> comparator = Pair.getBComparatorDesc();
            Collections.sort(contTablesCntPairs, comparator);
            for (Pair<Integer, Integer> co : contTablesCntPairs) {
                log.debug("Table [" + co.a + "] : " + co.b);
            }
            state.contFilter = new FastContainerFilter(state.proxies.length);
            FastContainerFilter fastCFilter = (FastContainerFilter) state.contFilter;
            for (int i = 0; i < input.maxNumCont; ++i) {
                log.debug("Selected container " + contTablesCntPairs.get(i).a + " with "
                    + contTablesCntPairs.get(i).b + " table partitions.");
                fastCFilter.useContainer(contTablesCntPairs.get(i).a);
            }
        }
    }

    private SchedulingResult getSchedule(String scheduleChoice, SolutionSpace space)
        throws RemoteException {
        SchedulingResult sch = null;
        if (scheduleChoice.equals("fast")) {
            sch = space.getFastestPlan();
        } else if (scheduleChoice.equals("balanced")) {
            double minM = space.getCheapestPlan().getStatistics().getMoneyInQuanta();
            double maxM = space.getMostExpensivePlan().getStatistics().getMoneyInQuanta();
            double budget = (minM + maxM) / 2.0;
            log.debug("Balanced budget : " + budget);
            sch = space.getFastestPlan(budget);
        } else if (scheduleChoice.equals("cheap")) {
            sch = space.getCheapestPlan();
        } else {
            throw new RemoteException("No valid schedule selection method");
        }
        return sch;
    }

    protected void createQueryGraph(InputData inData, StateData state, AdpDBClientProperties props)
        throws RemoteException {
        if (props.isTreeEnabled()) {
            log.debug("Creating tree schedule ...");
            processLeafQuery(inData.script.getTreeLeafQuery(), inData, state);
            processInternalQuery(inData.script.getTreeInternalQuery(), inData, state);
            processRootQuery(inData.script.getTreeRootQuery(), inData, state);
        } else {
            log.debug("Creating graph schedule ...");
            for (Select q : inData.script.getSelectQueries()) {
                processQuery(q, inData, state);
            }
        }
        bindLocationOFResultTables(inData, state);

    }

    private void processQuery(Select query, InputData input, StateData state)
        throws RemoteException {
        ProcessQueryState queryState = new ProcessQueryState();
        prepareInput(query, queryState, input, state);
        applyInputPattern(query, queryState, input, state);
        applyOutputPattern(query, queryState, input, state);
    }

    private void processLeafQuery(Select query, InputData input, StateData state)
        throws RemoteException {
        // Direct is mandatory here!
        // TODO(herald): this might be OK with cartesian as well.
        Check.True(query.getParsedSqlQuery().getInputDataPattern() == DataPattern.direct_product,
            "The leaf pattern should be direct product");
        processQuery(query, input, state);
    }

    private void processInternalQuery(Select query, InputData input, StateData state)
        throws RemoteException {
        // This might have more than one levels! For noe assume is only 1.
        processQuery(query, input, state);
    }

    private void processRootQuery(Select query, InputData input, StateData state)
        throws RemoteException {
        processQuery(query, input, state);
    }

    private void createDataTransferQueries(Select transferQ, TableView inTableView,
        PhysicalTable pTable) throws RemoteException {
        if (filterColumns) {
            // Transfer only the used columns!
            StringBuilder queryString = new StringBuilder();
            queryString.append("select ");
            List<String> usedColumns = inTableView.getUsedColumnNames();
            for (int c = 0; c < usedColumns.size(); ++c) {
                String cName = usedColumns.get(c);
                if (c == 0) {
                    queryString.append(cName);
                    continue;
                }
                queryString.append(", ").append(cName);
            }
            queryString.append(" from ").append(pTable.getName());
            transferQ.setQuery(queryString.toString());
        } else {
            // Transfer the entire table partition.
            transferQ.setQuery("select * from " + pTable.getName());
        }
        transferQ.clearInputTables();
        transferQ.addInput(inTableView);
        transferQ.setOutputTable(inTableView);
    }

    private void bindTablePartition(PhysicalTable pTable, int p, ConcreteOperator reader,
        InputData input, StateData state) throws RemoteException {
        BitSet contFilter = new BitSet(state.proxies.length);
        SchemaUtil.getLocationsOfPartitions(state.proxies, pTable, p, contFilter);
        int[] locations = new int[contFilter.cardinality()];
        Check.True(locations.length > 0, "Partition not found: " + pTable.getName() + "/" + p);
        for (int i = contFilter.nextSetBit(0), l = 0;
             i >= 0; i = contFilter.nextSetBit(i + 1), l++) {
            locations[l] = i;
        }
        log.debug("Table '" + pTable.getName() + "/" + p + "' is at " + Arrays.toString(locations));
        // Bind the input partition.
        // Use the container that has the smallest number of partitions.
        int minContainer = -1;
        for (int loc : locations) {
            if (minContainer < 0) {
                minContainer = loc;
                continue;
            }
            if (state.containerTablePartCounts[minContainer]
                > state.containerTablePartCounts[loc]) {
                minContainer = loc;
            }
        }
        state.tableBindings.add(new Pair<ConcreteOperator, Integer>(reader, minContainer));
        state.containerTablePartCounts[minContainer]++;
    }

    private void prepareInput(Select query, ProcessQueryState queryState, InputData input,
        StateData state) throws RemoteException {
        DataPattern inputPattern = query.getParsedSqlQuery().getInputDataPattern();
        TableView outTableView = query.getOutputTable();
        log.debug("Creating query " + outTableView.getName());
        log.debug("Access the input tables ... ");
        List<Integer> runOnParts = query.getRunOnParts();
        log.debug("Run on parts: " + runOnParts);
        for (TableView inTableView : query.getInputTables()) {
            log.debug("Get physical table of view '" + inTableView.getName() + "'");
            PhysicalTable pTable = input.schema.getPhysicalTable(inTableView.getName());
            boolean intermediateTable = false;
            if (pTable == null) {
                log.debug("The view is on an intermediate result.");
                pTable = input.script.getTable(inTableView.getName());
                intermediateTable = true;
                Check.True(pTable.getTable().getLevel() >= 0,
                    "Intermediate table level not set: " + pTable.getName());
            } else {
                // Input tables have level 0.
                pTable.getTable().setLevel(0);
            }
            queryState.inputPhyTables.add(pTable);
            int partitions = pTable.getNumberOfPartitions();
            ConcreteOperator[] outputs = new ConcreteOperator[partitions];
            if (intermediateTable == false) {
                Integer inputCount = state.inputTableCount.get(pTable.getName());
                if (inputCount == null) {
                    inputCount = 0;
                }
                state.inputTableCount.put(pTable.getName(), inputCount + 1);
                log.debug("Input is an existing table. Adding table readers ... ");
                if (runOnParts.size() > 0 && runOnParts.get(runOnParts.size() - 1) >= partitions) {
                    throw new SemanticException("Table partition limit exceeded: " + partitions);
                }
                for (int p = 0; p < partitions; ++p) {
                    if (runOnParts.size() > 0) {
                        int found = Collections.binarySearch(runOnParts, p);
                        if (found < 0) {
                            outputs[p] = null;
                            continue;
                        }
                    }
                    ConcreteOperator reader = ConcreteGraphFactory
                        .createTableReader(pTable.getName() + "_" + p + "_" + inputCount);
                    state.graph.addOperator(reader);
                    outputs[p] = reader;
                    Select transferQ = SerializationUtil.deepCopy(query);
                    createDataTransferQueries(transferQ, inTableView, pTable);
                    AdpDBSelectOperator dbOp =
                        new AdpDBSelectOperator(AdpDBOperatorType.tableInput, transferQ, p);
                    log.debug("--DEBUG : " + reader.opID + " / " + reader.operatorName);
                    ListUtil.setItem(state.dbOps, reader.opID, dbOp);
                    dbOp.addOutput(pTable.getName(), p);
                    dbOp.addInput(pTable.getName(), p);
                    // Bind the table reader to its location.
                    // Balance the number of partitions.
                    bindTablePartition(pTable, p, reader, input, state);
                }
                state.tableProducers.put(inTableView.getName(), outputs);
            }
            if (inTableView.getPattern() != inputPattern) {
                throw new SemanticException(
                    "All the patterns of the input must be the same: " + inTableView.getPattern()
                        + " != " + inputPattern);
            }
        }
    }

    private void applyInputPattern(
        Select query,
        ProcessQueryState queryState,
        InputData input,
        StateData state) throws RemoteException {

        TableView outTableView = query.getOutputTable();
        List<Integer> runOnParts = query.getRunOnParts();
        DataPattern inputPattern = query.getParsedSqlQuery().getInputDataPattern();
        log.debug("Input pattern  : " + inputPattern);

        ArrayList<ConcreteOperator[]> inputs = new ArrayList<ConcreteOperator[]>();
        int outputPartitions = 1;
        log.debug("Apply input pattern ... ");

        switch (inputPattern) {
            case direct_product: {
                int numOfPartitions = 1;
                log.debug("Input tables must have only one part or the same number of parts ...");
                for (TableView inTableView : query.getInputTables()) {
                    ConcreteOperator[] producers = state.tableProducers.get(inTableView.getName());
                    inputs.add(producers);
                    // One partition is fine!
                    if (producers.length == 1) {
                        continue;
                    }
                    if (numOfPartitions == 1) {
                        numOfPartitions = producers.length;
                    } else {
                        if (numOfPartitions != producers.length) {
                            throw new SemanticException(
                                "Direct format exception: " + query.toString());
                        }
                    }
                }
                outputPartitions = numOfPartitions;
                break;
            }
            case tree: {
                // This works only with one input table
                Check.True(query.getInputTables().size() == 1,
                    "Only one input table can be used with tree reduction");
                TableView inTableView = query.getInputTables().get(0);
                ConcreteOperator[] producers = state.tableProducers.get(inTableView.getName());
                inputs.add(producers);
                // TODO(herald): Change this to more than one levels
                int treeReduction = TreeUtils.getReductionPerLevel(producers.length, 3);
                outputPartitions = (int) Math.ceil((double) producers.length / treeReduction);
                break;
            }
            case cartesian_product:
            case external:
            case remote:
            case virtual: {
                for (TableView inTableView : query.getInputTables()) {
                    ConcreteOperator[] producers = state.tableProducers.get(inTableView.getName());
                    inputs.add(producers);
                    outputPartitions *= producers.length;
                }
                // From an external input, only one partition is produced.
                if (inputPattern == DataPattern.external) {
                    outputPartitions = 1;
                }
                // From an remote input, only one partition is produced.
                if (inputPattern == DataPattern.remote) {
                    outputPartitions = 1;
                }
                // Virtual inputs run on all machines.
                if (inputPattern == DataPattern.virtual) {
                    outputPartitions = state.containers.size();
                }
                break;
            }
        }
        log.debug("Inputs : " + inputs.size());
        log.debug("Output partitions: " + outputPartitions);

        queryState.outputTable = new PhysicalTable(outTableView.getTable());
        queryState.outputs = new ConcreteOperator[outputPartitions];
        switch (inputPattern) {
            case direct_product:
            case external:
            case remote:
            case virtual: {
                for (int oPart = 0; oPart < outputPartitions; ++oPart) {
                    if (runOnParts.size() > 0) {
                        int found = Collections.binarySearch(runOnParts, oPart);
                        if (found < 0) {
                            queryState.outputs[oPart] = null;
                            log.debug("Part of table '" + outTableView.getName() + "' not used: "
                                + oPart);
                            continue;
                        }
                    }
                    log.trace("Adding operator: " + outTableView.getName() + "_" + oPart);
                    ConcreteOperator runQuery =
                        ConcreteGraphFactory.createRunQuery(outTableView.getName() + "_" + oPart);
                    state.graph.addOperator(runQuery);
                    queryState.outputTable
                        .addPartition(new Partition(queryState.outputTable.getName(), oPart));
                    AdpDBSelectOperator dbOp =
                        new AdpDBSelectOperator(AdpDBOperatorType.runQuery, query, oPart);
                    ListUtil.setItem(state.dbOps, runQuery.opID, dbOp);
                    dbOp.addOutput(queryState.outputTable.getName(), oPart);
                    if (inputPattern == DataPattern.external) {
                        log.debug("Bind external operator to localhost.");
                        String localIP = NetUtil.getIPv4() + "_";
                        ContainerProxy localHost = null;
                        int localContainer = 0;
                        for (ContainerProxy cp : state.proxies) {
                            if (cp.getEntityName().getName().contains(localIP)) {
                                localHost = cp;
                                break;
                            }
                            ++localContainer;
                        }
                        Check.NotNull(localHost, "Localhost not found: " + localIP);
                        log.debug("Localhost container is: " + localHost.getEntityName().getName());
                        state.tableBindings
                            .add(new Pair<ConcreteOperator, Integer>(runQuery, localContainer));
                        state.containerTablePartCounts[localContainer]++;
                    }
                    if (inputPattern == DataPattern.remote) {
                        log.debug("Bind virtual operators in simple round robin fashion.");
                        if ( containerCounter == state.proxies.length){
                            containerCounter = 0;
                        }
                        ContainerProxy containerProxy = state.proxies[containerCounter];
                        Check.NotNull(
                            containerProxy,
                            "proxy not found: " + containerProxy.getEntityName().getIP());
                        log.debug("proxy container is: "
                            + containerProxy.getEntityName().getName());
                        state.tableBindings
                            .add(new Pair<ConcreteOperator, Integer>(runQuery, containerCounter));
                        state.containerTablePartCounts[containerCounter]++;
                        containerCounter++;
                    }
                    if (inputPattern == DataPattern.virtual) {
                        log.debug("Bind virtual operators to all machines.");
                        state.tableBindings
                            .add(new Pair<ConcreteOperator, Integer>(runQuery, oPart));
                        state.containerTablePartCounts[oPart]++;
                    }
                    // Filter only the specified partitions
                    for (int i = 0; i < inputs.size(); ++i) {
                        ConcreteOperator fromOp =
                            inputs.get(i)[(inputs.get(i).length == 1) ? 0 : oPart];
                        if (fromOp == null) {
                            continue;
                        }
                        Link link =
                            ConcreteGraphFactory.createLink(oPart + "." + i, fromOp, runQuery);
                        state.graph.addLink(link);
                        // Add all the outputs of from
                        AdpDBSelectOperator fromDbOp = state.dbOps.get(fromOp.opID);
                        if (dbOp.addToInputsAllOutputsOf(fromDbOp) != 1) {
                            throw new SemanticException(
                                "The input of a db operator from another operator must be one!");
                        }
                    }
                    queryState.outputs[oPart] = runQuery;
                }
                break;
            }
            case cartesian_product: {
                int[] idxs = new int[inputs.size()];
                CartesianProduct product = new CartesianProduct(idxs);
                for (int i = 0; i < inputs.size(); i++) {
                    product.setLimit(i, inputs.get(i).length);
                }
                int oPart = 0;
                do {
                    if (runOnParts.size() > 0) {
                        int found = Collections.binarySearch(runOnParts, oPart);
                        if (found < 0) {
                            queryState.outputs[oPart] = null;
                            log.debug("Part of table '" + outTableView.getName() + "' not used: "
                                + oPart);
                            oPart++;
                            continue;
                        }
                    }
                    ConcreteOperator runQuery =
                        ConcreteGraphFactory.createRunQuery(outTableView.getName() + "_" + oPart);
                    state.graph.addOperator(runQuery);
                    queryState.outputTable
                        .addPartition(new Partition(queryState.outputTable.getName(), oPart));
                    AdpDBSelectOperator dbOp =
                        new AdpDBSelectOperator(AdpDBOperatorType.runQuery, query, oPart);
                    ListUtil.setItem(state.dbOps, runQuery.opID, dbOp);
                    dbOp.addOutput(queryState.outputTable.getName(), oPart);

                    // Filter only the specified partitions
                    for (int i = 0; i < inputs.size(); i++) {
                        ConcreteOperator fromOp = inputs.get(i)[idxs[i]];
                        if (fromOp == null) {
                            continue;
                        }
                        Link link =
                            ConcreteGraphFactory.createLink(oPart + "." + i, fromOp, runQuery);
                        state.graph.addLink(link);
                        // Add all the outputs of from
                        AdpDBSelectOperator fromDbOp = state.dbOps.get(fromOp.opID);
                        if (dbOp.addToInputsAllOutputsOf(fromDbOp) != 1) {
                            throw new SemanticException(
                                "The input of a db operator from another operator must be one!");
                        }
                    }
                    queryState.outputs[oPart] = runQuery;
                    oPart++;
                } while (product.next());
                break;
            }
            case tree: {
                Check.True(runOnParts.isEmpty(),
                    "Tree reduction cannot be combined with partition selection!");
                Check.True(inputs.size() == 1, "Tree reduction is compatible with one input only!");
                // Compute tree redution
                int treeReduction = TreeUtils.getReductionPerLevel(inputs.get(0).length, 3);
                for (int oPart = 0; oPart < outputPartitions; ++oPart) {
                    log.trace("Adding operator: " + outTableView.getName() + "_" + oPart);
                    ConcreteOperator runQuery =
                        ConcreteGraphFactory.createRunQuery(outTableView.getName() + "_" + oPart);
                    state.graph.addOperator(runQuery);
                    queryState.outputTable
                        .addPartition(new Partition(queryState.outputTable.getName(), oPart));
                    AdpDBSelectOperator dbOp =
                        new AdpDBSelectOperator(AdpDBOperatorType.runQuery, query, oPart);
                    ListUtil.setItem(state.dbOps, runQuery.opID, dbOp);
                    dbOp.addOutput(queryState.outputTable.getName(), oPart);

                    int beginIndex = treeReduction * oPart;
                    int endIndex = beginIndex + treeReduction;
                    if (oPart == outputPartitions - 1) {
                        endIndex = inputs.get(0).length;
                    }
                    for (int i = beginIndex; i < endIndex; ++i) {
                        ConcreteOperator fromOp = inputs.get(0)[i];
                        if (fromOp == null) {
                            continue;
                        }
                        Link link =
                            ConcreteGraphFactory.createLink(oPart + "." + i, fromOp, runQuery);
                        state.graph.addLink(link);
                        // Add all the outputs of from
                        AdpDBSelectOperator fromDbOp = state.dbOps.get(fromOp.opID);
                        if (dbOp.addToInputsAllOutputsOf(fromDbOp) != 1) {
                            throw new SemanticException(
                                "The input of a db operator from another operator must be one!");
                        }
                    }
                    queryState.outputs[oPart] = runQuery;
                }
                break;
            }
        }
    }

    private void applyOutputPattern(
        Select query,
        ProcessQueryState queryState,
        InputData input,
        StateData state) throws RemoteException {

        TableView outTableView = query.getOutputTable();
        DataPattern outputPattern = query.getParsedSqlQuery().getOutputDataPattern();
        log.debug("Output pattern : " + outputPattern);
        log.debug("Apply output pattern ... ");

        int numOfOutPartitions = -1;
        switch (outputPattern) {
            case same: {
                log.debug("Apply same output pattern ... ");
                numOfOutPartitions = queryState.outputs.length;
                break;
            }
            case many: {
                log.debug("Apply many output pattern ... ");
                // TODO: This is the problem with tree output ...
                numOfOutPartitions = outTableView.getNumOfPartitions();
                if (numOfOutPartitions != 1) {
                    if (outTableView.getPatternColumnNames().isEmpty()) {
                        throw new SemanticException(
                            "Output pattern 'many' with more than one outputs, "
                                + "must specify at least one column.");
                    }
                }
                break;
            }
        }
        log.debug("Creating '" + numOfOutPartitions + "' outputs ... ");
        PhysicalTable postOutputTable = new PhysicalTable(outTableView.getTable());
        log.debug("Find level of table ...");
        int level = -1;
        for (PhysicalTable in : queryState.inputPhyTables) {
            if (level < in.getTable().getLevel()) {
                level = in.getTable().getLevel();
            }
        }
        // The level of the table is one more than the highest level of the input tables.
        level++;
        log.debug("Level of table '" + postOutputTable.getName() + "' is " + level);
        postOutputTable.getTable().setLevel(level);

        log.debug("Clear output tables from operators ... ");
        for (ConcreteOperator out : queryState.outputs) {
            if (out == null) {
                continue;
            }
            AdpDBSelectOperator outDbOp = state.dbOps.get(out.opID);
            outDbOp.clearOutputs(queryState.outputTable.getName());
        }
        ConcreteOperator[] postOutputs = new ConcreteOperator[numOfOutPartitions];
        for (int oPart = 0; oPart < numOfOutPartitions; ++oPart) {
            ConcreteOperator gatherData = ConcreteGraphFactory
                .createTableTransferReplicator(outTableView.getName() + "_P_" + oPart);
            state.graph.addOperator(gatherData);
            postOutputTable.addPartition(new Partition(queryState.outputTable.getName(), oPart));

            Select unionQ = SerializationUtil.deepCopy(query);
            unionQ.setQuery("select * from " + queryState.outputTable.getName());
            unionQ.getOutputTable().setNumOfPartitions(1);

            AdpDBSelectOperator dbOp =
                new AdpDBSelectOperator(AdpDBOperatorType.tableUnionReplicator, unionQ, oPart);

            ListUtil.setItem(state.dbOps, gatherData.opID, dbOp);
            dbOp.addOutput(queryState.outputTable.getName(), oPart);

            int min = 0;
            int max = queryState.outputs.length;
            // If the output is the same, add only one post output to each output.
            if (outputPattern == DataPattern.same) {
                min = oPart;
                max = oPart + 1;
            }
            for (int i = min; i < max; i++) {
                ConcreteOperator fromOp = queryState.outputs[i];
                if (fromOp == null) {
                    continue;
                }
                Link link = ConcreteGraphFactory.createLink(oPart + "." + i, fromOp, gatherData);
                state.graph.addLink(link);
                // Add all the outputs of from
                AdpDBSelectOperator fromDbOp = state.dbOps.get(fromOp.opID);
                fromDbOp.addOutput(queryState.outputTable.getName(), oPart);
                dbOp.addInput(queryState.outputTable.getName(), oPart);
            }
            postOutputs[oPart] = gatherData;
        }
        log.debug("Add post output tables ... ");
        input.script.addTable(postOutputTable);
        state.tableProducers.put(query.getOutputTable().getName(), postOutputs);
        if (query.getOutputTable().getTable().isTemp() == false) {
            if (state.resultTableName != null) {
                throw new SemanticException("Not supported > 1 results.");
            }
            state.resultTableName = query.getOutputTable().getName();
            //            query.getOutputTable().getTable().getSqlDefinition();
        }
    }

    private void bindLocationOFResultTables(InputData input, StateData state) {
        // Bind the output table producers to the location of the result. For now, use round robin!
        if (state.resultTableName != null) {
            log.debug("Bind output table '" + state.resultTableName + "' to location ... ");
            state.results.add(input.script.getTable(state.resultTableName));
            PhysicalTable resultTable = input.script.getTable(state.resultTableName);
            ConcreteOperator[] resultTableOps = state.tableProducers.get(state.resultTableName);
            // TODO(herald): add here the partition placement algorithm.
            int container = 0;
            for (int p = 0; p < resultTableOps.length; ++p) {
                ConcreteOperator co = resultTableOps[p];
                state.tableBindings.add(new Pair<ConcreteOperator, Integer>(co, container));
                resultTable.getPartition(p)
                    .addLocation(state.proxies[container].getEntityName().getIP());
                container = (container + 1) % state.proxies.length;
            }
        }
    }

    private void createFinalGraphFromSketch(InputData sketchInput, InputData input,
        StateData sketchState, StateData state, AdpDBClientProperties props)
        throws RemoteException {
        // The work is alrady done if sketching is not used!
        if (OptimizerConstants.USE_SKETCH == false) {
            input.copyFrom(sketchInput);
            state.shallowCopyFrom(sketchState);
            return;
        }

        state.proxies = sketchState.proxies;
        state.containers = sketchState.containers;
        state.containerTablePartCounts = new int[state.proxies.length];

        log.debug("Generate dataflow graph ...");
        createQueryGraph(input, state, props);
        log.debug("Checking if the graph is DAG ...");
        Check.True(VerifyDAG.isDAG(state.graph), "The graph is not a DAG");
        log.debug("Bind " + state.tableBindings.size() +
            " operators to the location of tables ... ");
        FastSubgraphFilter subgraphFilter = new FastSubgraphFilter(state.graph.getNumOfOperators());
        for (Pair<ConcreteOperator, Integer> co : state.tableBindings) {
            subgraphFilter.assignOperator(co.a.opID, co.b);
        }
        log.debug("Create container filter ...");
        createContainerFilter(input, state, props);
        if (input.validate) {
            log.debug("Validating graph ...");
            RmiAdpDBOptimizer.validateGraph(input, state, props);
        }
        log.debug("Post process graph ...");
        RmiAdpDBOptimizer.postProcessGraph(input, state, props);
        log.debug("Estimating running time and data for each operator ... ");
        RmiAdpDBOptimizer.estimateTimeAndData(input, state, props);
        log.debug("Annotating graph with historical data ...");
        RmiAdpDBOptimizer
            .annotateWithHistoricalData(state.graph, input.queryData, state.dbOps, props);
        log.debug("Adding local file data ...");
        RmiAdpDBOptimizer.addLocalFileData(state.graph);
        state.space = sketchState.space;
    }
}
