/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.logger;


import madgik.exareme.utils.units.Metrics;

/**
 * @author heraldkllapi
 */
public class LogEvents {

    public enum EventType {
        init,
        queries,
        queryStart,
        querySuccess,
        queryError,
        elasticTreeLevel,
        money,
        unknown
    }


    public static class Event {
        public final long time;
        public final EventType type;

        public Event(long time, EventType type) {
            this.time = time / Metrics.MiliSec;
            this.type = type;
        }

        public Init toInit() {
            return (Init) this;
        }

        public QueryStart toQueryStart() {
            return (QueryStart) this;
        }

        public QuerySuccess toQuerySuccess() {
            return (QuerySuccess) this;
        }

        public QueryError toQueryError() {
            return (QueryError) this;
        }

        public ElasticTreeLevel toElasticLevel() {
            return (ElasticTreeLevel) this;
        }

        public Money toMoney() {
            return (Money) this;
        }

        public Unknown toUnknown() {
            return (Unknown) this;
        }
    }


    public static class Init extends Event {
        public final long globalTime;

        public Init(long time, long globalTime) {
            super(time, EventType.init);
            this.globalTime = globalTime;
        }
    }


    public static class Queries extends Event {
        public final int totalQueries;
        public final int errorQueries;
        public final int runningQueries;
        public final int queuedQueries;

        public Queries(long time, int totalQueries, int errorQueries, int runningQueries,
                       int queuedQueries) {
            super(time, EventType.queries);
            this.totalQueries = totalQueries;
            this.errorQueries = errorQueries;
            this.runningQueries = runningQueries;
            this.queuedQueries = queuedQueries;
        }
    }


    public static class QueryStart extends Event {
        public final long queryId;

        public QueryStart(long time, long queryId) {
            super(time, EventType.queryStart);
            this.queryId = queryId;
        }
    }


    public static class QuerySuccess extends Event {
        public final long queryId;
        public final double execTime;
        public final double money;

        public QuerySuccess(long time, long queryId, double execTime, double money) {
            super(time, EventType.querySuccess);
            this.queryId = queryId;
            this.execTime = execTime;
            this.money = money;
        }
    }


    public static class QueryError extends Event {
        public final long queryId;

        public QueryError(long time, long queryId) {
            super(time, EventType.queryError);
            this.queryId = queryId;
        }
    }


    public static class ElasticTreeLevel extends Event {
        public final int level;
        public final int containers;
        public final long numOps;
        public final double cpuLoad;
        public final double cpuVar;
        public final double dataLoad;
        public final double dataVar;

        public ElasticTreeLevel(long time, int level, int containers, long numOps, double cpuLoad,
                                double cpuVar, double dataLoad, double dataVar) {
            super(time, EventType.elasticTreeLevel);
            this.level = level;
            this.containers = containers;
            this.numOps = numOps;
            this.cpuLoad = cpuLoad;
            this.cpuVar = cpuVar;
            this.dataLoad = dataLoad;
            this.dataVar = dataVar;
        }
    }


    public static class Money extends Event {
        public final double totalCost;
        public final double totalRevenue;
        public final double profit;
        public final double windowCost;
        public final double windowRevenue;

        public Money(long time, double totalCost, double totalRevenue, double profit,
                     double windowCost, double windowRevenue) {
            super(time, EventType.money);
            this.totalCost = totalCost;
            this.totalRevenue = totalRevenue;
            this.profit = profit;
            this.windowCost = windowCost;
            this.windowRevenue = windowRevenue;
        }
    }


    public static class Unknown extends Event {
        public final String[] parts;

        public Unknown(long time, String[] parts) {
            super(time, EventType.unknown);
            this.parts = parts;
        }
    }
}
