package madgik.exareme.worker.art.client.armExps;///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.db.art.client.armExps;
//
//import madgik.exareme.db.arm.compute.cluster.PatternElement;
//import madgik.exareme.db.arm.compute.containerMgr.ContainerManagerInterface;
//import madgik.exareme.db.arm.compute.session.ActiveContainer;
//import madgik.exareme.db.arm.compute.session.ArmComputeSessionID;
//import madgik.exareme.common.art.entity.EntityName;
//import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
//import madgik.exareme.master.queryProcessor.optimizer.assignedOperatorFilter.NoSubgraphFilter;
//import madgik.exareme.master.queryProcessor.optimizer.containerFilter.NoContainerFilter;
//import madgik.exareme.master.queryProcessor.optimizer.scheduler.OperatorAssignment;
//import madgik.exareme.master.queryProcessor.optimizer.scheduler.ParallelWaveScheduler;
//import madgik.exareme.master.queryProcessor.optimizer.scheduler.SchedulingResult;
//import madgik.exareme.utils.association.Pair;
//
//import java.rmi.RemoteException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// * @author heraldkllapi
// */
//public class ClientDataflowExecutor extends Thread {
//
//    private long sum = 0;
//    private int id;
//    private MultiObjectiveQueryScheduler scheduler = null;
//    private ConcreteQueryGraph graph = null;
//    private List<EntityName> entities = null;
//    private ContainerManagerInterface manager = null;
//    private double actualTime = 0;
//    private double predictedTime = 0;
//
//    public ClientDataflowExecutor(int id, ConcreteQueryGraph graph, List<EntityName> entities,
//        ContainerManagerInterface manager) {
//        this.id = id;
//        scheduler = new ParallelWaveScheduler();
//        this.graph = graph;
//        this.entities = entities;
//        this.manager = manager;
//    }
//
//    @Override public void run() {
//
//        try {
//            long start = System.currentTimeMillis();
//            ArrayList<ContainerResources> containers = new ArrayList<ContainerResources>(entities.
//                size());
//            for (int i = 0; i < entities.size(); ++i) {
//                containers.add(new ContainerResources());
//            }
//            RunTimeParameters params = new RunTimeParameters();
//            //params.quantum__SEC=2000;
//            SolutionSpace space = scheduler
//                .callOptimizer(graph, NoSubgraphFilter.getInstance(), containers,
//                    NoContainerFilter.getInstance(), params, new FinancialProperties());
//
//            SchedulingResult schedule = space.getFastestPlan();
//            predictedTime = schedule.getStatistics().getTimeInQuanta();
//
//            ArrayList<PatternElement> patternElements = new ArrayList<PatternElement>();
//
//            for (OperatorAssignment oa : schedule.operatorAssigments) {
//                if (oa == null || oa.dataTransfer) {
//                    continue;
//                }
//                PatternElement p = new PatternElement();
//                p.relative_start_time = oa.start / params.quantum__SEC;
//                p.duration = oa.processTime / params.quantum__SEC;
//                p.relative_name = oa.container;
//                patternElements.add(p);
//                sum += p.duration;
//            }
///*
//      PatternElement element;
//
//      element = new PatternElement();
//      element.setParameters(1,5,0);
//      patternElements.add(element);
//
//      element = new PatternElement();
//      element.setParameters(2,5,0);
//      patternElements.add(element);
//
//      element = new PatternElement();
//      element.setParameters(3,5,0);
//      patternElements.add(element);
//
//      element = new PatternElement();
//      element.setParameters(4,5,0);
//      patternElements.add(element);
//
//      element = new PatternElement();
//      element.setParameters(5,5,0);
//      patternElements.add(element);
//
//
//      element = new PatternElement();
//      element.setParameters(6,5,0);
//      patternElements.add(element);
//
//      element = new PatternElement();
//      element.setParameters(1,5,2);
//      patternElements.add(element);
//
//      element = new PatternElement();
//      element.setParameters(2,5,2);
//      patternElements.add(element);
//
//      element = new PatternElement();
//      element.setParameters(3,5,2);
//      patternElements.add(element);
//
//      element = new PatternElement();
//      element.setParameters(4,5,2);
//      patternElements.add(element);
//
//      element = new PatternElement();
//      element.setParameters(5,5,2);
//      patternElements.add(element);
//
//      element = new PatternElement();
//      element.setParameters(6,5,2);
//      patternElements.add(element);
//
//      element = new PatternElement();
//      element.setParameters(7,5,2);
//
//      element = new PatternElement();
//      element.setParameters(8,5,2);
//
//      element = new PatternElement();
//      element.setParameters(9,5,2);
//
//      element = new PatternElement();
//      element.setParameters(10,5,2);
//      patternElements.add(element);
//  */
//            simulate(patternElements);
//            long end = System.currentTimeMillis();
//            actualTime = (end - start) / 1000.0;
//            System.out
//                .println("At the end in Quanta " + schedule.getStatistics().getMoneyInQuanta());
//        } catch (Exception e) {
//
//        }
//    }
//
//    private void simulate(ArrayList<PatternElement> patternElements) {
//
//        ArrayList<Thread> threads = new ArrayList<Thread>();
//        ArrayList<Pair<PatternElement, ActiveContainer>> result = null;
//
//        System.out.println("Predicted : " + predictedTime);
//
//        ArmComputeSessionID sID = new ArmComputeSessionID(id);
//        try {
//            manager.setPattern(patternElements, sID);
//        } catch (RemoteException ex) {
//            System.out.println("1 " + ex.getMessage());
//            Logger.getLogger(ClientDataflowExecutor.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        //System.out.println("Arxi");
//        ArrayList<Pair<PatternElement, ActiveContainer>> totalResults =
//            new ArrayList<Pair<PatternElement, ActiveContainer>>();
//        while (true) {
//            //System.out.println("size "+totalResults.size()+" a "+patternElements.size());
//            if (totalResults.size() == patternElements.size()) {
//                System.out.println("pira " + totalResults.size());
//                break;
//            }
//            try {
//                //System.out.println("Zhtaw");
//                result = manager.getAtMostContainers(sID);
//                //System.out.println("I took "+result.size());
//            } catch (RemoteException ex) {
//                Logger.getLogger(ClientDataflowExecutor.class.getName())
//                    .log(Level.SEVERE, null, ex);
//            }
//
//            totalResults.addAll(result);
//
//            for (Pair<PatternElement, ActiveContainer> pair : result) {
//
//                Thread t = new Thread() {
//                    double duration;
//                    ContainerManagerInterface manager;
//                    ActiveContainer container;
//                    ArmComputeSessionID sID;
//
//                    public Thread setDuration(ContainerManagerInterface manager, double duration,
//                        ActiveContainer cont, ArmComputeSessionID sID) {
//                        this.manager = manager;
//                        this.duration = duration;
//                        this.container = cont;
//                        this.sID = sID;
//                        return this;
//                    }
//
//                    @Override public void run() {
//                        try {
//                            Thread.sleep((int) (duration * 1000.0));
//                            manager.stopContainer(container, sID);
//                            return;
//                        } catch (Exception ex) {
//                            System.out.println("3 " + ex.getMessage());
//                            Logger.getLogger(ClientDataflowExecutor.class.getName()).
//                                log(Level.SEVERE, null, ex);
//                        }
//                    }
//                }.setDuration(manager, pair.a.duration, pair.b, sID);
//                threads.add(t);
//                t.start();
//            }
//        }
//
//        for (Thread thread : threads) {
//            try {
//                thread.join();
//            } catch (InterruptedException ex) {
//                System.out.println("4 " + ex.getMessage());
//                Logger.getLogger(ClientDataflowExecutor.class.getName())
//                    .log(Level.SEVERE, null, ex);
//            }
//        }
//
//
//        try {
//            manager.closeSession(sID);
//        } catch (RemoteException ex) {
//            System.out.println("5 " + ex.getMessage());
//            Logger.getLogger(ClientDataflowExecutor.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    public void printStats() {
//        System.out.println("ID: " + id + " Predicted : " + predictedTime);
//        System.out.println("ID: " + id + " Actual    : " + actualTime);
//    }
//}
//
