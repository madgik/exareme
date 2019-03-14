/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator;

import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.graph.GraphGenerator;
import madgik.exareme.master.queryProcessor.optimizer.ContainerResources;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.BitSet;

import static org.junit.Assert.assertEquals;

/**
 * @author herald
 */
public class ScheduleEstimatorTest {

    public ScheduleEstimatorTest() {
    }

    @Test
    public void testOneContainer() {
        ConcreteQueryGraph graph = GraphGenerator.createLatticeGraph(3, 3, 0);
        Assert.assertNotNull(graph);
        assertEquals(5, graph.getNumOfOperators());

        RunTimeParameters runTime = new RunTimeParameters();

        ArrayList<ContainerResources> containers = new ArrayList<>();
        containers.add(new ContainerResources());

        ScheduleEstimator estimator = new ScheduleEstimator(graph, containers, runTime);

        SnFOperatorDependencySolver solver = new SnFOperatorDependencySolver(graph);
        int opsAssigned = 0;
        BitSet activated = solver.getInitial();
        while (opsAssigned < graph.getNumOfOperators()) {
            opsAssigned += activated.cardinality();
            for (int id = activated.nextSetBit(0); id >= 0; id = activated.nextSetBit(id + 1)) {
                estimator.addOperatorAssignment(id, 0, graph);
            }
            activated = solver.addTerminated(activated);
        }
        ScheduleStatistics stats = estimator.getScheduleStatistics();
        double time = 5.0 + // CPU time
                6.0;  // Disk time
        assertEquals(time, stats.getTimeInQuanta(), 0.01);
        // TIme and money should be the same in one container.
        assertEquals(time, stats.getMoneyInQuanta(), 0.01);
    }

    @Test
    public void testAllDiffContainers() {
        ConcreteQueryGraph graph = GraphGenerator.createLatticeGraph(3, 3, 0);
        Assert.assertNotNull(graph);
        assertEquals(5, graph.getNumOfOperators());

        RunTimeParameters runTime = new RunTimeParameters();

        int containerNum = graph.getNumOfOperators();
        ArrayList<ContainerResources> containers = new ArrayList<>(containerNum);
        for (int i = 0; i < containerNum; ++i) {
            containers.add(new ContainerResources());
        }

        ScheduleEstimator estimator = new ScheduleEstimator(graph, containers, runTime);

        SnFOperatorDependencySolver solver = new SnFOperatorDependencySolver(graph);
        int opsAssigned = 0;
        BitSet activated = solver.getInitial();
        while (opsAssigned < graph.getNumOfOperators()) {
            for (int id = activated.nextSetBit(0); id >= 0; id = activated.nextSetBit(id + 1)) {
                estimator.addOperatorAssignment(id, opsAssigned, graph);
                opsAssigned++;
            }
            activated = solver.addTerminated(activated);
        }
        ScheduleStatistics stats = estimator.getScheduleStatistics();
        double time = 5 + // The first operator has 1 CPU, 1 disk, and 3 network
                3 + // Each of the next (3) has 1 CPU, 1 disk and 1 network
                3; // The last one has 1 cpu, 1 disk, and 1 network
        double money = 3 + 2 * 3 + // 1 cpu + 2 disk + 2 * 3 network
                (3 + 2) * 3 + // 1 cpu + 2 disk (for 3 ops)
                3; // same as op 1 but no network
        assertEquals(time, stats.getTimeInQuanta(), 0.01);
        assertEquals(money, stats.getMoneyInQuanta(), 0.01);
    }
}
