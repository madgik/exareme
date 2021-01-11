/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.remote;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.app.engine.AdpDBStatistics;
import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.master.engine.AdpDBStatusManager;
import madgik.exareme.utils.properties.AdpDBProperties;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionProgressStats;
import madgik.exareme.worker.art.executionEngine.statisticsMgr.PlanSessionStatisticsManagerProxy;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManagerProxy;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

/**
 * @author herald
 */
public class AdpDBArtJobMonitor implements Runnable {

    private static final Logger log = Logger.getLogger(AdpDBArtJobMonitor.class);
    private static final int statsUpdateSecs =
            AdpDBProperties.getAdpDBProps().getInt("db.client.statisticsUpdate_sec");
    private int statsOldOP = 0;
    private int statsOldDT = 0;
    private int statsOldER = 0;

    private final ExecutionEngineSessionPlan sessionPlan;
    private final AdpDBStatus status;
    private final AdpDBStatusManager statusManager;
    private final AdpDBQueryID queryID;
    private final ArrayList<AdpDBQueryListener> listeners;
    private PlanSessionStatusManagerProxy sessionManager = null;
    private PlanSessionStatisticsManagerProxy statsManager = null;

    public AdpDBArtJobMonitor(ExecutionEngineSessionPlan sessionPlan, AdpDBStatus status,
                              AdpDBStatusManager statusManager, AdpDBQueryID queryID) {
        this.sessionPlan = sessionPlan;
        this.status = status;
        this.statusManager = statusManager;
        this.queryID = queryID;
        this.listeners = new ArrayList<AdpDBQueryListener>(1);
    }

    @Override
    public void run() {
        //int tries_remaining = 300; // Restart after 5 minutes
        try {
            sessionManager = sessionPlan.getPlanSessionStatusManagerProxy();
            statsManager = sessionPlan.getPlanSessionStatisticsManagerProxy();

            PlanSessionProgressStats stats = statsManager.getProgress();

            statusManager.getStatistics(status.getId()).setTotalOperators(stats.getTotalProc());
            statusManager.getStatistics(status.getId()).setTotalDataTransfers(stats.getTotalData());

            log.info("Monitor: " + status.getId() + " Line 62 status: " + status.getId());

            while (sessionManager.hasFinished() == false && sessionManager.hasError() == false) {

                Thread.sleep(1000 * statsUpdateSecs);
                //tries_remaining--;
                //if (tries_remaining == 0) {
                //    throw new TimeoutException("Session stuck and stopped after 5 minutes.");
                //}
                boolean updateProgressStatistics = updateProgressStatistics();
                //log.info("Monitor: " + status.getId() + " Line 68 update: " + updateProgressStatistics);
                //log.info("Monitor: " + status.getId() + " Line 69 update: " + sessionPlan);
                sessionManager = sessionPlan.getPlanSessionStatusManagerProxy();
                //log.info("Monitor: " + status.getId() + " Line 69.5 update: " + sessionManager);
                //log.info("Monitor: " + status.getId() + " Line 70 update: " + sessionManager.hasFinished() + " " + sessionManager.hasError());
                statsManager = sessionPlan.getPlanSessionStatisticsManagerProxy();
                //log.info("Monitor: " + status.getId() + " Line 71.5 update: " + statsManager);
                //log.info("Monitor: " + status.getId() + " Line 72 update: " + statsManager);
                if (sessionManager == null || statsManager == null) {
                    log.info("--+ error");
                }
                if (updateProgressStatistics) {
                    log.info("Session is running...");
                    log.debug("Update listeners ...");
                    synchronized (listeners) {
                        for (AdpDBQueryListener l : listeners) {
                            log.debug(status.toString());

                            //log.info("Monitor: " + status.getId() + " Line 81 statusToBeChanged");
                            l.statusChanged(queryID, status);

                            //log.info("Monitor: " + status.getId() + " Line 84 statusChanged");
                        }
                    }
                }
            }

            //log.info("Monitor: " + status.getId() + " Line 90 update");
            updateProgressStatistics();
            //log.info("Monitor: " + status.getId() + " Line 92 update");
            statusManager.getStatistics(status.getId())
                    .setAdpEngineStatistics(statsManager.getStatistics());

            //log.info("Monitor: " + status.getId() + " Line 96 update");
            if (sessionManager != null && sessionManager.hasError() == false) {
                statusManager.setFinished(status.getId());

            //    log.info("Monitor: " + status.getId() + " Line 100 update");
            } else {

            //    log.info("Monitor: " + status.getId() + " Line 103 update");
                statusManager.setError(status.getId(), sessionManager.getErrorList().get(0));
            }
            //log.info("Monitor: " + status.getId() + " Line 106 update");
            sessionPlan.close();
        } catch (Exception e) {
            statusManager.setError(status.getId(), e);
            log.error("Cannot monitor job, queryId: " + queryID.getLongId(), e);
            log.error("Cannot monitor job, status: " + status.getId(), e);
        } finally {
            log.debug("Terminate listeners ( " + listeners.size() + ")...");
            synchronized (listeners) {
                for (AdpDBQueryListener l : listeners) {
                    l.terminated(queryID, status);
                }
            }
        }
    }

    public void registerListener(AdpDBQueryListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    private boolean updateProgressStatistics() throws RemoteException {

        try {
            PlanSessionProgressStats stats = statsManager.getProgress();
            AdpDBStatistics adpStats = statusManager.getStatistics(status.getId());

            int operatorsCompleted = stats.processingOperatorsCompleted();
            adpStats.setOperatorsCompleted(operatorsCompleted);

            int transferCompleted = stats.getDataTransferCompleted();
            adpStats.setDataTransferCompleted(transferCompleted);

            int errors = stats.getErrors();
            adpStats.setErrors(errors);

            if (statsOldOP != operatorsCompleted || statsOldER != errors
                    || statsOldDT != transferCompleted) {
                statsOldDT = transferCompleted;
                statsOldER = errors;
                statsOldOP = operatorsCompleted;
                return true;
            }
        } catch (UnmarshalException e) {
            log.error("Cannot decode information ...");
        }
        return false;
    }
}
