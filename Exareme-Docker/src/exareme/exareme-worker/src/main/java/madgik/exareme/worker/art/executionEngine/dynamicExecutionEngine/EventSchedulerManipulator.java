/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;

import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author heraldkllapi
 */
public class EventSchedulerManipulator {
    private static final Logger log = Logger.getLogger(EventSchedulerManipulator.class);
    private PlanEventScheduler globalScheduler = null;
    private Map<PlanSessionID, PlanEventScheduler> activeSchedulers = null;
    private Map<PlanSessionReportID, PlanEventScheduler> activeSchedulersReportIdMap = null;
    private PlanEventSchedulerElasticTree elasticTreeScheduler = null;

    public EventSchedulerManipulator() {
        activeSchedulers =
                Collections.synchronizedMap(new LinkedHashMap<PlanSessionID, PlanEventScheduler>());
        activeSchedulersReportIdMap = Collections
                .synchronizedMap(new LinkedHashMap<PlanSessionReportID, PlanEventScheduler>());
    }

    public PlanEventScheduler getGlobalScheduler() {
        return globalScheduler;
    }

    public void setGlobalScheduler(PlanEventScheduler globalScheduler) {
        this.globalScheduler = globalScheduler;
    }

    public void registerScheduler(PlanSessionID sessionID, PlanSessionReportID sessionReportID,
                                  PlanEventScheduler scheduler) {
        activeSchedulers.put(sessionID, scheduler);
        activeSchedulersReportIdMap.put(sessionReportID, scheduler);
    }

    public PlanEventScheduler getSchedulerWithId(PlanSessionID sessionID) {
        return activeSchedulers.get(sessionID);
    }

    public PlanEventScheduler getSchedulerWiReportId(PlanSessionReportID sessionID) {
        return activeSchedulersReportIdMap.get(sessionID);
    }

    public void removeScheduler(PlanSessionID sessionID, PlanSessionReportID sessionReportID) {
        activeSchedulers.remove(sessionID);
        activeSchedulersReportIdMap.remove(sessionReportID);
    }

    public Collection<PlanEventScheduler> getAllSessions() {
        return activeSchedulers.values();
    }

    public void registerElasticTreeScheduler(PlanEventSchedulerElasticTree scheduler) {
        this.elasticTreeScheduler = scheduler;
    }


    public PlanEventSchedulerElasticTree getElasticTreeScheduler() {
        return elasticTreeScheduler;
    }
}
