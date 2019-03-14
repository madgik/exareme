/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduler;

import madgik.exareme.common.optimizer.FinancialProperties;
import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.graph.GraphGenerator;
import madgik.exareme.master.queryProcessor.optimizer.ContainerResources;
import madgik.exareme.master.queryProcessor.optimizer.MultiObjectiveQueryScheduler;
import madgik.exareme.master.queryProcessor.optimizer.SolutionSpace;
import madgik.exareme.master.queryProcessor.optimizer.assignedOperatorFilter.FastSubgraphFilter;
import madgik.exareme.master.queryProcessor.optimizer.containerFilter.NoContainerFilter;
import madgik.exareme.utils.check.Check;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

/**
 * @author heraldkllapi
 */
public class SkyTest {
    public SkyTest() {

    }

    @Test
    public void testSky() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        //    ConcreteQueryGraph graph = GraphGenerator.createLatticeGraph(5, 10, 0);
        File loadFile = new File(SkyTest.class.getResource("MONTAGE.n.100.0.dax").getFile());
        ConcreteQueryGraph graph = GraphGenerator.parseDax(loadFile.toURL().toString());
        Check.NotNull(graph);

        RunTimeParameters runTimeParams = new RunTimeParameters();
        FinancialProperties financialProps = new FinancialProperties();

        int containerNum = 100;
        ArrayList<ContainerResources> containers = new ArrayList<>(containerNum);
        for (int i = 0; i < containerNum; ++i) {
            containers.add(new ContainerResources());
        }

        FastSubgraphFilter filter = new FastSubgraphFilter(graph.getNumOfOperators());

        MultiObjectiveQueryScheduler sa = new SkyScheduler();
        SolutionSpace space =
                sa.callOptimizer(graph, filter, containers, NoContainerFilter.getInstance(),
                        runTimeParams, financialProps);

        for (SchedulingResult sr : space.getResults()) {
            System.out.println("   " +
                    sr.getStatistics().getTimeInQuanta() + " " +
                    sr.getStatistics().getMoneyInQuanta());
        }
        space.computeStats();
        System.out.println("TIME: " + space.getOptimizationTime());
    }
}
