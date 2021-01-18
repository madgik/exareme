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
        try {
            sessionManager = sessionPlan.getPlanSessionStatusManagerProxy();
            statsManager = sessionPlan.getPlanSessionStatisticsManagerProxy();

            PlanSessionProgressStats stats = statsManager.getProgress();

            statusManager.getStatistics(status.getId()).setTotalOperators(stats.getTotalProc());
            statusManager.getStatistics(status.getId()).setTotalDataTransfers(stats.getTotalData());

            while (!sessionManager.hasFinished() && !sessionManager.hasError()) {

                boolean updateProgressStatistics = updateProgressStatistics();
                if (updateProgressStatistics) {
                    log.info("Session is running... ID: " + sessionPlan.getSessionID().getLongId()
                            + " , QueryID: " + queryID.getQueryID());
                    log.debug("Update listeners ...");
                    synchronized (listeners) {
                        for (AdpDBQueryListener l : listeners) {
                            log.debug(status.toString());
                            l.statusChanged(queryID, status);
                        }
                    }
                }

                Thread.sleep(100 * statsUpdateSecs);

                // Reload the managers
                sessionManager = sessionPlan.getPlanSessionStatusManagerProxy();
                statsManager = sessionPlan.getPlanSessionStatisticsManagerProxy();
                if (sessionManager == null || statsManager == null) {
                    log.error("Session Manager or stats Manager null! " + sessionManager + ", " + statsManager);
                }
            }

            updateProgressStatistics();
            statusManager.getStatistics(status.getId())
                    .setAdpEngineStatistics(statsManager.getStatistics());

            if (sessionManager != null && !sessionManager.hasError()) {
                log.debug("Session finished, closing! ID: " + sessionPlan.getSessionID().getLongId()
                        + " , QueryID: " + queryID.getQueryID());
                statusManager.setFinished(status.getId());
            } else {
                statusManager.setError(status.getId(), sessionManager.getErrorList().get(0));
            }
            log.debug("Session closing! ID: "+ sessionPlan.getSessionID().getLongId()
                    + " , QueryID: " + queryID.getQueryID());
            sessionPlan.close();

        } catch (Exception e) {
            statusManager.setError(status.getId(), e);
            log.error("Cannot monitor job, sessionID: " + sessionPlan.getSessionID().getLongId());
            log.error("Cannot monitor job, queryID: " + status.getQueryID().getQueryID(), e);
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
