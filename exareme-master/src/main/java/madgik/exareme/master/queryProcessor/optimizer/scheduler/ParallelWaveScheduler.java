/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduler;

import madgik.exareme.common.optimizer.FinancialProperties;
import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.graph.Link;
import madgik.exareme.master.queryProcessor.optimizer.*;
import madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.ScheduleEstimator;
import org.apache.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.rmi.RemoteException;
import java.util.*;

/**
 * @author heraldkllapi
 */
public class ParallelWaveScheduler implements MultiObjectiveQueryScheduler {

    public static final String algName = "MapReduce";
    private static final long serialVersionUID = 1L;
    private final static int FIND_SKYLINE_EVERY = 100;
    private static final Logger log = Logger.getLogger(ParallelWaveScheduler.class);
    private static ThreadMXBean bean = null;
    private final String thisID = this.toString().substring(this.toString().lastIndexOf('@'));

    public ParallelWaveScheduler() {
        bean = ManagementFactory.getThreadMXBean();
        bean.setThreadCpuTimeEnabled(true);
    }

    @Override public SolutionSpace callOptimizer(final ConcreteQueryGraph queryGraph,
        final AssignedOperatorFilter subgraphFilter, final ArrayList<ContainerResources> containers,
        final ContainerFilter containerFilter, final RunTimeParameters runTimeParams,
        final FinancialProperties finProps) throws RemoteException {
        long start = bean.getCurrentThreadCpuTime() / 1000000;
        SolutionSpace skyline = new SolutionSpace();
        GraphParallelOperatorWaves2 waves = new GraphParallelOperatorWaves2();
        waves.findWaves(queryGraph);
        int waveLimits[] = new int[waves.getNumOfWaves()];
        for (int w = 0; w < waves.getNumOfWaves(); ++w) {
            int maxPar = Math.min(waves.getOpsInWave(w), containers.size());
            waveLimits[w] = maxPar - 1;
        }
        SchedulingResult sch =
            run(waveLimits, waves, queryGraph, subgraphFilter, containers, containerFilter,
                runTimeParams, finProps);
        skyline.addResult(sch);
        if (skyline.getResults().size() > FIND_SKYLINE_EVERY) {
            List<SchedulingResult> sky = skyline.findSkyline();
            skyline.clearResults();
            skyline.addAllResults(sky);
        }
        long end = bean.getCurrentThreadCpuTime() / 1000000;
        skyline.setOptimizationTime(end - start);
        skyline.computeStats();
        return skyline;
    }

    private SchedulingResult run(int waveLimits[], GraphParallelOperatorWaves2 waves,
        final ConcreteQueryGraph queryGraph, final AssignedOperatorFilter subgraphFilter,
        final ArrayList<ContainerResources> containers, final ContainerFilter containerFilter,
        final RunTimeParameters runTimeParams, final FinancialProperties finProps)
        throws RemoteException {
        ArrayList<SchedulingResult> graphSchedules = new ArrayList<>();
        ScheduleEstimator planAssigment =
            new ScheduleEstimator(queryGraph, containers, runTimeParams);
        int[] currentContainer = new int[waveLimits.length];
        // Run all waves individually
        for (ConcreteOperator cOp : queryGraph.getOperators()) {
            int c = subgraphFilter.getOperatorAssignment(cOp.opID);
            if (c >= 0) {
                planAssigment.addOperatorAssignment(cOp.opID, c, queryGraph);
                continue;
            }
            int w = waves.getWave(cOp.opID);
            c = currentContainer[w];
            currentContainer[w] = (currentContainer[w] + 1) % (waveLimits[w] + 1);
            planAssigment.addOperatorAssignment(cOp.opID, c, queryGraph);
        }
        SchedulingResult combinedResult =
            new SchedulingResult(containers.size(), runTimeParams, finProps, null, planAssigment);
        // Combine all into one graph
        return combinedResult;
    }


    private static class GraphParallelOperatorWaves2 {
        private ConcreteQueryGraph graph = null;
        private ArrayList<ArrayList<ConcreteOperator>> waves = new ArrayList<>();
        private HashMap<Integer, Integer> opIdWaveMap = new HashMap<>();
        private LinkedList<ConcreteOperator> readyOperatorList = new LinkedList<>();
        private LinkedHashMap<Integer, ConcreteOperator> readyOperators = new LinkedHashMap<>();

        public GraphParallelOperatorWaves2() {

        }

        public void findWaves(ConcreteQueryGraph graph) {
            // Initialize
            this.graph = graph;
            this.waves.clear();
            this.opIdWaveMap.clear();
            this.readyOperatorList.clear();
            this.readyOperators.clear();
            // Find the operators with no input
            for (ConcreteOperator start : graph.getOperators()) {
                if (graph.getInputLinks(start.opID).isEmpty()) {
                    this.readyOperators.put(start.opID, start);
                    this.readyOperatorList.add(start);
                }
            }
            // Add the rest
            while (readyOperatorList.size() > 0) {
                ConcreteOperator op = readyOperatorList.remove(0);
                int wave = getWaveOfOperator(op);
                addOperator(op, wave);
                for (Link fromLink : graph.getOutputLinks(op.opID)) {
                    if (readyOperators.containsKey(fromLink.to.opID) || opIdWaveMap
                        .containsKey(fromLink.to.opID)) {
                        continue;
                    }
                    boolean allAssigned = true;
                    for (Link toLink : graph.getInputLinks(fromLink.to.opID)) {
                        if (opIdWaveMap.containsKey(toLink.from.opID) == false) {
                            allAssigned = false;
                            break;
                        }
                    }
                    if (allAssigned) {
                        readyOperators.put(fromLink.to.opID, fromLink.to);
                        readyOperatorList.add(fromLink.to);
                    }
                }
            }
        }

        private int getWaveOfOperator(ConcreteOperator cOp) {
            if (graph.getInputLinks(cOp.opID).isEmpty()) {
                return 0;
            }
            int maxInputWave = -1;
            for (Link in : graph.getInputLinks(cOp.opID)) {
                int inWave = getWave(in.from.opID);
                maxInputWave = Math.max(maxInputWave, inWave);
            }
            return maxInputWave + 1;
        }

        private void addOperator(ConcreteOperator cOp, int wave) {
            opIdWaveMap.put(cOp.opID, wave);
            ArrayList<ConcreteOperator> waveOps = null;
            while (waves.size() <= wave) {
                waveOps = new ArrayList<>();
                waves.add(waveOps);
            }
            if (waveOps == null) {
                waveOps = waves.get(wave);
            }
            waveOps.add(cOp);
        }

        public int getNumOfWaves() {
            return waves.size();
        }

        public int getOpsInWave(int waveNum) {
            return waves.get(waveNum).size();
        }

        public int getWave(int opId) {
            return opIdWaveMap.get(opId);
        }
    }
}
