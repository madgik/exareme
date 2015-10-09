//package madgik.exareme.core.app.db.engine.rmi;
//
//import SemanticException;
//import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
//import madgik.exareme.master.queryProcessor.monitor.scheduler.IgnoreScheduleMonitor;
//import madgik.exareme.master.queryProcessor.optimizer.*;
//import madgik.exareme.master.queryProcessor.optimizer.constraint.Constraints;
//import madgik.exareme.master.queryProcessor.optimizer.ignoreOperatorFilter.NoIgnoreOperatorFilter;
//import madgik.exareme.db.visualizers.console.ConsoleUtils;
//import org.apache.log4j.Level;
//
//import java.rmi.RemoteException;
//import java.util.ArrayList;
//import TopologyGenerator;
//
///**
// * Created by panos on 5/30/14.
// */
//public class RmiAdpDBTreeSelectScheduler extends RmiAdpDBSelectScheduler {
//
//  public static SolutionSpace schedule(ConcreteQueryGraph graph,
//                                       AssignedOperatorFilter subgraphFilter,
//                                       ArrayList<ContainerResources> containers,
//                                       ContainerFilter contFilter) throws RemoteException {
//    if (true) {
//      return RmiAdpDBSelectScheduler.schedule(graph, subgraphFilter, containers, contFilter);
//    }
//
//    // TODO(panos): fix the following
//    printTotalSchedulesToLog(graph.getNumOfOperators(), containers.size());
//    SolutionSpace space;
//    try {
//      QuerySchedulerProperties props = new QuerySchedulerProperties();
//      MultiObjectiveTreeQueryScheduler scheduler = new MultiObjectiveTreeQueryScheduler(
//          TopologyGenerator.generateDemoContainerTopology());
//      space = scheduler.callOptimizer(graph,
//                                      subgraphFilter,
//                                      NoIgnoreOperatorFilter.getInstance(),
//                                      containers,
//                                      contFilter,
//                                      props,
//                                      Constraints.NO_CONSTRAINTS,
//                                      runTimeParams,
//                                      finProps,
//                                      IgnoreScheduleMonitor.instance);
//    } catch (Exception e) {
//      log.error("Cannot schedule graph", e);
//      throw new SemanticException("Cannot schedule graph", e);
//    }
//    ConsoleUtils.printSkylineToLog(graph, runTimeParams, space, log, Level.DEBUG);
//    return space;
//  }
//}
