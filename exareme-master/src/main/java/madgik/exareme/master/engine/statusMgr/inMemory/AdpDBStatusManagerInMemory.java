/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.statusMgr.inMemory;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.app.engine.AdpDBStatistics;
import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.master.engine.AdpDBStatusImpl;
import madgik.exareme.master.engine.AdpDBStatusManager;
import madgik.exareme.master.engine.statusMgr.AdpDBJobSession;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the status manager that lies in memory.
 *
 * @author Herald Kllapi <br>
 * herald@di.uoa.gr /
 * University of Athens
 * @since 1.0
 */
public class AdpDBStatusManagerInMemory implements AdpDBStatusManager {

    private final ArrayList<AdpDBJobSession> sessions = new ArrayList<AdpDBJobSession>();

    public AdpDBStatusManagerInMemory() {
    }

    @Override
    public AdpDBStatus createNewStatus(AdpDBQueryID queryID, ExecutionEngineSessionPlan sessionPlan,
                                       Map<String, String> categoryMessageMap) throws RemoteException {
        AdpDBStatus stat = null;
        synchronized (sessions) {
            stat = new AdpDBStatusImpl(sessions.size(), queryID);
            sessions.add(new AdpDBJobSession(queryID, sessionPlan, categoryMessageMap));
        }
        return stat;
    }

    @Override
    public AdpDBStatus createNewStatus(ExecutionEngineSessionPlan sessionPlan,
                                       ConcreteQueryGraph graph, Map<String, String> categoryMessageMap) throws RemoteException {
        AdpDBStatus stat = null;
        synchronized (sessions) {
            stat = new AdpDBStatusImpl(sessions.size());
            sessions.add(new AdpDBJobSession(null, sessionPlan, categoryMessageMap));
        }
        return stat;
    }

    @Override
    public boolean hasFinished(int statusId) {
        AdpDBJobSession session;
        synchronized (sessions) {
            session = sessions.get(statusId);
        }

        synchronized (session) {
            return session.isFinished();
        }
    }

    @Override
    public void setFinished(int statusId) {
        AdpDBJobSession session;
        synchronized (sessions) {
            session = sessions.get(statusId);
        }

        synchronized (session) {
            session.setFinished(true);
        }
    }

    @Override
    public boolean hasError(int statusId) {
        AdpDBJobSession session;
        synchronized (sessions) {
            session = sessions.get(statusId);
        }
        synchronized (session) {
            return session.hasError();
        }
    }

    @Override
    public void setError(int statusId, Exception exception) {
        AdpDBJobSession session;
        synchronized (sessions) {
            session = sessions.get(statusId);
        }
        synchronized (session) {
            session.setError(true, exception);
        }
    }

    @Override
    public Exception getLastException(int statusId) {
        AdpDBJobSession session;
        synchronized (sessions) {
            session = sessions.get(statusId);
        }

        return session.getException();
    }

    @Override
    public List<Exception> getExceptions(int k, int statusId) {
        AdpDBJobSession session;
        synchronized (sessions) {
            session = sessions.get(statusId);
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addException(Exception exception, int statusId) {
        AdpDBJobSession session;
        synchronized (sessions) {
            session = sessions.get(statusId);
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdpDBStatistics getStatistics(int statusId) {
        AdpDBJobSession session;
        synchronized (sessions) {
            session = sessions.get(statusId);
        }

        synchronized (session) {
            return session.getStatistics();
        }
    }

    @Override
    public void updateWith(AdpDBStatistics delta, int statusId) {
        AdpDBJobSession session;
        synchronized (sessions) {
            session = sessions.get(statusId);
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stopExecution(int statusId) throws RemoteException {
        AdpDBJobSession session;
        synchronized (sessions) {
            session = sessions.get(statusId);
        }

        synchronized (session) {
            session.stopExecution();
        }
    }

    @Override
    public void registerListener(AdpDBQueryListener listener, int statusId)
            throws RemoteException {
        AdpDBJobSession session;
        synchronized (sessions) {
            session = sessions.get(statusId);
        }

        synchronized (session) {
            session.registerListener(listener);
        }
    }
}
