/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.statusMgr;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.app.engine.AdpDBStatistics;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * @author herald
 */
public class AdpDBJobSession {
    private boolean finished = false;
    private boolean error = false;
    private Exception exception = null;
    private AdpDBStatistics statistics = null;
    private ExecutionEngineSessionPlan sessionPlan = null;
    private AdpDBQueryID queryID;

    public AdpDBJobSession(AdpDBQueryID queryID, ExecutionEngineSessionPlan sessionPlan,
                           Map<String, String> categoryMessageMap) {
        statistics = new AdpDBStatistics(categoryMessageMap);
        this.sessionPlan = sessionPlan;
        this.queryID = queryID;
    }


    public boolean hasError() {
        return error;
    }

    public Exception getException() {
        return exception;
    }

    public void setError(boolean error, Exception exception) {
        this.error = error;
        this.exception = exception;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void registerListener(AdpDBQueryListener listener) throws RemoteException {
        AdpDBManagerLocator.getDBManager().getAdpDBExecutor().registerListener(listener, queryID);
    }

    public AdpDBStatistics getStatistics() {
        return statistics;
    }

    public void addStatistics(AdpDBStatistics delta) {
    }

    public void stopExecution() throws RemoteException {
        sessionPlan.close();
    }
}
