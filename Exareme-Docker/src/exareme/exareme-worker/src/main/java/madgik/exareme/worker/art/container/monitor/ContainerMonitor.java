///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.db.art.container.monitor;
//
//import java.lang.management.ManagementFactory;
//import java.lang.management.ThreadMXBean;
//import java.util.HashMap;
//import madgik.exareme.utils.properties.AdpProperties;
//
///**
// *
// * @author Herald Kllapi <br>
// *      University of Athens /
// *      Department of Informatics and Telecommunications.
// * @since 1.0
// */
//public class ContainerMonitor {
//  private int activeInstances = 0;
//  private int runningInstances = 0;
//  private double memoryUsage = 0;
//  private double cpuLoad = 0;
//  private HashMap<String, Integer> operatorsPerSessionMap = null;
//  private ThreadMXBean bean = null;
//  private HashMap<Long, Long> threadCPUtimeMap = null;
//  private long prevTime = 0;
//  private float reportPeriod = 0;
//
//  public ContainerMonitor() {
//    operatorsPerSessionMap = new HashMap<String, Integer>();
//    threadCPUtimeMap = new HashMap<Long, Long>();
//    prevTime = System.nanoTime();
//
//    bean = ManagementFactory.getThreadMXBean();
//    bean.setThreadCpuTimeEnabled(true);
//
//    reportPeriod = AdpProperties.getArtProps().getFloatProperty(
//            "art.container.maxStatusReportPeriod") * 1000000;
//  }
//
//  public synchronized void addOperator(String session) {
//    activeInstances++;
//
//    if (reportToSessions.containsKey(session)) {
//      Integer count = operatorsPerSessionMap.get(session);
//      operatorsPerSessionMap.put(session, count + 1);
//    } else {
//      
//      log.variable(new StatusVariable(
//              "RunningInstances", Integer.class,
//              activeInstances).toVariableMessage());
//
//      reportToSessions.put(session, log);
//      operatorsPerSessionMap.put(session, 1);
//    }
//  }
//
//  public synchronized void removeOperator(String session) {
//    activeInstances--;
//
//    if (reportToSessions.containsKey(session)) {
//      Integer count = operatorsPerSessionMap.get(session);
//      if (count == 1) {
//        LoggerProxy log = reportToSessions.remove(session);
//        operatorsPerSessionMap.remove(session);
//
//        log.variable(new StatusVariable(
//                "RunningInstances", Integer.class,
//                activeInstances).toVariableMessage());
//      } else {
//        operatorsPerSessionMap.put(session, count - 1);
//      }
//    }
//  }
//
//  public synchronized void report() {
//
//    long currentTime = System.nanoTime();
//
//    if (currentTime - prevTime > reportPeriod) {
//
//      memoryUsage = Runtime.getRuntime().totalMemory();
//      cpuLoad = 0;
//
//      for (long id : bean.getAllThreadIds()) {
//        Long prevThreadTime = threadCPUtimeMap.get(id);
//
//        if (prevThreadTime != null) {
//          long diff = bean.getThreadCpuTime(id) - prevThreadTime;
//          long timeDif = currentTime - prevTime;
//
//          cpuLoad += (double) diff / timeDif;
//        }
//
//        threadCPUtimeMap.put(id, bean.getThreadCpuTime(id));
//      }
//
//      prevTime = currentTime;
//
//      for (LoggerProxy log : reportToSessions.values()) {
//        log.variable(new StatusVariable(
//                "RunningInstances", Integer.class,
//                activeInstances).toVariableMessage());
//
//        log.variable(new StatusVariable(
//                "MemoryUsage", Double.class,
//                memoryUsage).toVariableMessage());
//
//        log.variable(new StatusVariable(
//                "CPUUsage", Double.class,
//                cpuLoad).toVariableMessage());
//      }
//    }
//  }
//
//  public void reportInstances() {
//    for (LoggerProxy log : reportToSessions.values()) {
//      log.variable(new StatusVariable(
//              "RunningInstances", Integer.class,
//              activeInstances).toVariableMessage());
//    }
//  }
//
//  public void reportMemUsage() {
//    for (LoggerProxy log : reportToSessions.values()) {
//      memoryUsage = Runtime.getRuntime().totalMemory();
//      log.variable(new StatusVariable(
//              "MemoryUsage", Double.class,
//              memoryUsage).toVariableMessage());
//    }
//  }
//}
