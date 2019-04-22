/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.common.schema.Statistics;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.master.engine.historicalData.AdpDBHistoricalDataManager;
import madgik.exareme.master.engine.historicalData.AdpDBHistoricalQueryData;
import madgik.exareme.master.engine.scheduler.event.optimize.OptimizeEvent;
import madgik.exareme.master.engine.scheduler.event.optimize.OptimizeEventHandler;
import madgik.exareme.master.engine.scheduler.event.optimize.OptimizeEventListener;
import madgik.exareme.master.engine.scheduler.event.queryError.QueryErrorEvent;
import madgik.exareme.master.engine.scheduler.event.queryError.QueryErrorEventHandler;
import madgik.exareme.master.engine.scheduler.event.queryError.QueryErrorEventListener;
import madgik.exareme.master.engine.scheduler.event.querySuccess.QuerySuccessEvent;
import madgik.exareme.master.engine.scheduler.event.querySuccess.QuerySuccessEventHandler;
import madgik.exareme.master.engine.scheduler.event.querySuccess.QuerySuccessEventListener;
import madgik.exareme.master.engine.scheduler.event.scheduleQuery.ScheduleEvent;
import madgik.exareme.master.engine.scheduler.event.scheduleQuery.ScheduleEventHandler;
import madgik.exareme.master.engine.scheduler.event.scheduleQuery.ScheduleEventListener;
import madgik.exareme.master.engine.stats.AdpDBStats;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.utils.eventProcessor.EventProcessor;

import java.rmi.RemoteException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author herald
 */
public class AdpDBQueryScheduler {

    public final EventProcessor eventProcessor;
    private final AdpDBStats statistics;
    private final QuerySchedulerState state;
    private final AdpDBManager manager;
    private final AdpDBHistoricalDataManager histManager;
    private final AdpDBQueryID queryID;
    private final Registry.Schema schema;
    private ReentrantLock lock = null;

    // Optimize
    private OptimizeEventHandler optimizeEventHandler = null;
    private OptimizeEventListener optimizeEventListener = null;

    // QueryError
    private QueryErrorEventHandler queryErrorEventHandler = null;
    private QueryErrorEventListener queryErrorEventListener = null;

    // QuerySuccess
    private QuerySuccessEventHandler querySuccessEventHandler = null;
    private QuerySuccessEventListener querySuccessEventListener = null;

    // Schedule
    private ScheduleEventHandler scheduleEventHandler = null;
    private ScheduleEventListener scheduleEventListener = null;

    public AdpDBQueryScheduler(Registry.Schema schema, AdpDBStats statistics,
                               EventProcessor eventProcessor, AdpDBManager manager, AdpDBHistoricalDataManager histManager,
                               AdpDBQueryID queryID) throws RemoteException {
        this.statistics = statistics;
        this.eventProcessor = eventProcessor;
        this.manager = manager;
        this.histManager = histManager;
        this.queryID = queryID;
        this.schema = schema;
        this.state = new QuerySchedulerState(eventProcessor, this, manager, queryID);
        this.lock = new ReentrantLock();

        createHandlers();
        createListeners();
    }

    private void createHandlers() {
        optimizeEventHandler = new OptimizeEventHandler(state);
        queryErrorEventHandler = new QueryErrorEventHandler(state);
        querySuccessEventHandler = new QuerySuccessEventHandler(state);
        scheduleEventHandler = new ScheduleEventHandler(state);
    }

    private void createListeners() {
        optimizeEventListener = new OptimizeEventListener(state);
        queryErrorEventListener = new QueryErrorEventListener(state);
        querySuccessEventListener = new QuerySuccessEventListener(state);
        scheduleEventListener = new ScheduleEventListener(state);
    }

    public QuerySchedulerState getState() {
        return state;
    }

    public void registerListener(AdpDBQueryListener listener) {
        state.registerListener(listener);
    }

    public void schedule(String queryScript, AdpDBHistoricalQueryData queryData, String database)
            throws RemoteException {
        lock.lock();
        try {
            optimize(queryScript, schema, statistics.getStatistics(), queryData);
        } finally {
            lock.unlock();
        }
    }

    public void schedule(QueryScript queryScript, AdpDBHistoricalQueryData queryData)
            throws RemoteException {
        lock.lock();
        try {
            optimize(queryScript, schema, statistics.getStatistics(), queryData);
        } finally {
            lock.unlock();
        }
    }

    public void optimize(String queryScript, Registry.Schema schema, Statistics stats,
                         AdpDBHistoricalQueryData queryData) throws RemoteException {
        lock.lock();
        try {
            OptimizeEvent event = new OptimizeEvent(queryScript, schema, stats, queryData, queryID);
            state.eventProcessor.queue(event, optimizeEventHandler, optimizeEventListener);
        } catch (Exception e) {
            throw new RemoteException("Cannot queue optimize query", e);
        } finally {
            lock.unlock();
        }
    }

    public void optimize(QueryScript queryScript, Registry.Schema schema, Statistics stats,
                         AdpDBHistoricalQueryData queryData) throws RemoteException {
        lock.lock();
        try {
            OptimizeEvent event = new OptimizeEvent(queryScript, schema, stats, queryData, queryID);
            state.eventProcessor.queue(event, optimizeEventHandler, optimizeEventListener);
        } catch (Exception e) {
            throw new RemoteException("Cannot queue optimize query", e);
        } finally {
            lock.unlock();
        }
    }

    public void schedule(AdpDBQueryExecutionPlan execPlan) throws RemoteException {
        lock.lock();
        try {
            ScheduleEvent event = new ScheduleEvent(execPlan, queryID);
            state.eventProcessor.queue(event, scheduleEventHandler, scheduleEventListener);
        } catch (Exception e) {
            throw new RemoteException("Cannot queue schedule plan", e);
        } finally {
            lock.unlock();
        }
    }

    public void success(AdpDBQueryExecutionPlan execPlan, AdpDBStatus status)
            throws RemoteException {
        lock.lock();
        try {
            QuerySuccessEvent event = new QuerySuccessEvent(execPlan, schema, status, queryID);
            state.eventProcessor.queue(event, querySuccessEventHandler, querySuccessEventListener);
        } catch (Exception e) {
            throw new RemoteException("Cannot queue success", e);
        } finally {
            lock.unlock();
        }
    }

    public void error(Exception exception) throws RemoteException {
        lock.lock();
        try {
            QueryErrorEvent event = new QueryErrorEvent(exception, queryID);
            state.eventProcessor.queue(event, queryErrorEventHandler, queryErrorEventListener);
        } catch (Exception e) {
            throw new RemoteException("Cannot queue error", e);
        } finally {
            lock.unlock();
        }
    }
}
