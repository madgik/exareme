/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine;

import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.executionEngine.clockTickManager.ClockTickManagerProxy;
import madgik.exareme.worker.art.executionEngine.reportMgr.PlanSessionReportManagerProxy;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.executionEngine.sessionMgr.PlanSessionManagerProxy;
import madgik.exareme.worker.art.executionEngine.statisticsMgr.PlanSessionStatisticsManagerProxy;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManagerProxy;
import madgik.exareme.worker.art.remote.RemoteObject;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ExecutionEngine extends RemoteObject<ExecutionEngineProxy> {

    PlanSessionID createNewSession() throws RemoteException;

    PlanSessionID createNewSessionElasticTree() throws RemoteException;

    PlanSessionStatusManagerProxy getPlanSessionStatusManagerProxy(PlanSessionID sessionID)
        throws RemoteException;

    PlanSessionReportManagerProxy getPlanSessionReportManagerProxy(
        PlanSessionReportID sessionPrivateID) throws RemoteException;

    PlanSessionManagerProxy getPlanSessionManagerProxy(PlanSessionID sessionID)
        throws RemoteException;

    PlanSessionStatisticsManagerProxy getPlanSessionStatisticsManagerProxy(PlanSessionID sessionID)
        throws RemoteException;

    ClockTickManagerProxy getClockTickManagerProxy() throws RemoteException;

    void destroySession(PlanSessionID sessionID) throws RemoteException;

    void destroyAllSessions() throws RemoteException;

    ExecutionEngineStatus getStatus() throws RemoteException;

    void stopExecutionEngine() throws RemoteException;

    void stopExecutionEngine(boolean force) throws RemoteException;
}
