/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.rmi;

import madgik.exareme.common.app.engine.AdpDBDMOperator;
import madgik.exareme.common.app.engine.AdpDBOperatorType;
import madgik.exareme.common.app.engine.DMQuery;
import madgik.exareme.common.optimizer.FinancialProperties;
import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.common.schema.*;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.master.engine.AdpDBStatusManager;
import madgik.exareme.master.engine.parser.SemanticException;
import madgik.exareme.master.engine.util.SchemaUtil;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;
import madgik.exareme.master.queryProcessor.optimizer.ContainerResources;
import madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.ScheduleEstimator;
import madgik.exareme.master.queryProcessor.optimizer.scheduler.SchedulingResult;
import madgik.exareme.utils.collections.ListUtil;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.*;

/**
 * @author herald
 */
public class RmiAdpDBDMOptimizer {

    private static final Logger log = Logger.getLogger(RmiAdpDBDMOptimizer.class);
    private AdpDBStatusManager statusManager = null;
    private ArtManager manager = null;

    public RmiAdpDBDMOptimizer(AdpDBStatusManager statusManager, ArtManager manager) {
        this.statusManager = statusManager;
        this.manager = manager;
    }

    public AdpDBQueryExecutionPlan optimize(InputData input, AdpDBClientProperties props)
        throws RemoteException {
        log.debug("Creating state ...");
        StateData state = new StateData();
        log.debug("Getting containers ... ");
        state.proxies = ArtRegistryLocator.getArtRegistryProxy().getContainers();
        ArrayList<ContainerResources> containers =
            new ArrayList<ContainerResources>(state.proxies.length);
        for (int i = 0; i < state.proxies.length; ++i) {
            containers.add(new ContainerResources());
        }
        log.debug("Using " + state.proxies.length + " containers.");
        ArrayList<Integer> operatorLocations = new ArrayList<Integer>();
        log.debug("Generate dataflow graph ...");
        BitSet contFilter = new BitSet(state.proxies.length);
        for (DMQuery dm : input.script.getDMQueries()) {
            PhysicalTable pTable = input.schema.getPhysicalTable(dm.getTable());
            Index index = null;
            AdpDBOperatorType type = getType(dm);
            if (type == AdpDBOperatorType.buildIndex) {
                BuildIndex bi = (BuildIndex) dm;
                List<String> columns = bi.getColumns();
                //        if (columns.size() != 1) {
                //          throw new SemanticException("Cannot handle more than columns.");
                //        }
                index = new Index(pTable.getName(), columns.get(0), bi.getIndexName());
                state.indexes.add(index);
            }
            log.debug("Generate db operation for table: " + type + "@'" + pTable.getName() + "'");
            List<Integer> runOnParts = dm.getRunOnParts();
            int numOfParitions = pTable.getNumberOfPartitions();
            if (runOnParts.size() > 0 && runOnParts.get(runOnParts.size() - 1) >= numOfParitions) {
                throw new SemanticException("Table partition limit exceeded: " + numOfParitions);
            }
            for (int p = 0; p < numOfParitions; ++p) {
                if (runOnParts.size() > 0) {
                    int foundAt = Collections.binarySearch(runOnParts, p);
                    if (foundAt < 0) {
                        continue;
                    }
                }
                SchemaUtil.getLocationsOfPartitions(state.proxies, pTable, p, contFilter);
                int[] locations = new int[contFilter.cardinality()];
                if (locations.length == 0) {
                    throw new ServerException(
                        "Location of table not found: " + pTable.getName() + "/" + p);
                }
                log.debug("Table '" + pTable.getName() + "/" + p + "' located at " + Arrays
                    .toString(locations));
                int loc = 0;
                for (int i = contFilter.nextSetBit(0); i >= 0; i = contFilter.nextSetBit(i + 1)) {
                    locations[loc] = i;
                    loc++;
                }
                for (int l : locations) {
                    String name = null;
                    switch (type) {
                        case buildIndex:
                            name = "buildI_" + ((BuildIndex) dm).getIndexName();
                            break;
                        case dropIndex:
                            name = "dropI_" + ((DropIndex) dm).getIndexName();
                            break;
                        case dropTable:
                            name = "dropT_" + dm.getTable();
                            break;
                    }
                    ConcreteOperator indexOperator = ConcreteGraphFactory
                        .createBuildIndex(pTable.getName() + "_" + p + "_" + name + "_" + l);
                    // Add index partition
                    if (index != null) {
                        index.addPartition(p);
                    }
                    log.debug("add_operator_d " + indexOperator.getName() + " "
                        + indexOperator.operatorName);
                    state.graph.addOperator(indexOperator);
                    AdpDBDMOperator dmOp = new AdpDBDMOperator(type, dm);
                    dmOp.setPart(p);
                    ListUtil.setItem(state.dmOps, indexOperator.opID, dmOp);
                    // TODO(herald): Round robin! this is temporary!
                    ListUtil.setItem(operatorLocations, indexOperator.opID,
                        locations[p % locations.length]);
                    break;
                }
            }
        }

        RunTimeParameters rtp = new RunTimeParameters();
        FinancialProperties fp = new FinancialProperties();
        ScheduleEstimator planAssigment = new ScheduleEstimator(state.graph, containers, rtp);
        for (ConcreteOperator cop : state.graph.getOperators()) {
            planAssigment
                .addOperatorAssignment(cop.opID, operatorLocations.get(cop.opID), state.graph);
        }
        state.result =
            new SchedulingResult(state.graph.getNumOfOperators(), rtp, fp, null, planAssigment);

        return new AdpDBQueryExecutionPlan(input, state);
    }

    private AdpDBOperatorType getType(DMQuery dm) {
        if (dm instanceof BuildIndex) {
            return AdpDBOperatorType.buildIndex;
        }
        if (dm instanceof DropIndex) {
            return AdpDBOperatorType.dropIndex;
        }
        if (dm instanceof DropTable) {
            return AdpDBOperatorType.dropTable;
        }
        return null;
    }
}
