/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduler;

import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.optimizer.FinancialProperties;
import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.master.queryProcessor.graph.ConcreteGraphStatistics;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.graph.ExportToArtQL;
import madgik.exareme.master.queryProcessor.optimizer.ContainerResources;
import madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.ScheduleEstimator;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.*;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperator;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperatorGroup;
import madgik.exareme.worker.art.executionEngine.resourceMgr.PlanSessionResourceManager;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlanParser;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.registry.ArtRegistryProxy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author heraldkllapi
 */
public class RepartitionTest {
    private static final RunTimeParameters runTime = new RunTimeParameters();
    private static final FinancialProperties finProp = new FinancialProperties();
    private static PlanEventSchedulerState state = null;

    public static void main(String[] args) throws Exception {
        int parts = 4;
        ConcreteQueryGraph graph = null; // GraphGenerator.createDataRepartition(parts, parts);
        //    System.out.println(ExportToDotty.exportToDotty(graph));

        ConcreteGraphStatistics stats = new ConcreteGraphStatistics(graph, runTime);
        System.out.println(stats);

        schedule(graph);
    }

    private static void schedule(ConcreteQueryGraph graph) throws Exception {
        OperatorGroupDependencySolver solver = createSolver(graph);
        LinkedHashMap<Long, OperatorGroup> groups = solver.getActivatedGroups();
        while (groups.isEmpty() == false) {
            System.out.println(" :: " + groups + "\n\n");
            for (OperatorGroup g : groups.values()) {
                terminateGroup(g, solver);
            }
            groups = solver.getActivatedGroups();
        }
    }

    private static void terminateGroup(OperatorGroup g, OperatorGroupDependencySolver solver)
            throws Exception {
        g.createPartialPlan();
        ActiveOperatorGroup ag = g.createNewActiveGroup(null);
        for (OperatorEntity op : ag.operatorMap.values()) {
            String originalName = op.operatorName.substring(0, op.operatorName.indexOf("."));

            // Set terminated to this container
            ActiveOperator activeOperator = state.getActiveOperator(originalName);
            activeOperator.operatorGroup.setTerminated(activeOperator.operatorEntity, true);
        }
        solver.setTerminated(g);
    }

    private static OperatorGroupDependencySolver createSolver(ConcreteQueryGraph graph)
            throws Exception {
        // Create session
        PlanSessionID sessionID = new PlanSessionID(0);
        PlanSessionReportID reportID = new PlanSessionReportID(0);
        // Event processor
        EventProcessor eventProcessor = new EventProcessor(1);
        // NOT NEEDED
        DynamicPlanManager planManager = null;
        ArtRegistryProxy registryProxy = null;

        PlanSessionResourceManager resourceManager = new PlanSessionResourceManager();
        PlanEventScheduler eventScheduler =
                new PlanEventScheduler(sessionID, reportID, eventProcessor, planManager,
                        resourceManager, registryProxy);

        List<String> containers = new ArrayList<String>();
        for (int i = 0; i < graph.getNumOfOperators() / 4; ++i) {
            String c = "c" + i;
            containers.add(c);
        }
        SchedulingResult schedule = scheduleRepartition(graph, containers);
        ExportToArtQL.exportToArtQL(graph, schedule, (LinkedList<String>) containers);

        // Create and execute plan
        String art = ExportToArtQL.exportToArtQL(graph, schedule, (LinkedList<String>) containers);
        System.out.println(art);
        ExecutionPlanParser planParser = new ExecutionPlanParser();
        ExecutionPlan executionPlan = planParser.parse(art);
        eventScheduler.execute(executionPlan);

        state = eventScheduler.getState();

        return new OperatorGroupDependencySolver(eventScheduler.getState());
    }

    private static SchedulingResult scheduleRepartition(ConcreteQueryGraph graph,
                                                        List<String> containerNames) {
        ArrayList<ContainerResources> containers = new ArrayList<>();
        for (int i = 0; i < containerNames.size(); ++i) {
            containers.add(new ContainerResources());
        }
        ScheduleEstimator bounds = new ScheduleEstimator(graph, containers, runTime);
        for (int opId = 0; opId < graph.getNumOfOperators(); ++opId) {
            //bounds.addOperatorAssignment(opId, opId % containers.size(), graph);
        }
        SchedulingResult result =
                new SchedulingResult(containerNames.size(), runTime, finProp, null, bounds);
        return result;
    }
}
