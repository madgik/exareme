/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer;

import madgik.exareme.master.queryProcessor.optimizer.scheduler.SchedulingResult;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Herald Kllapi <br>
 * @since 1.0
 */
public class SolutionSpace implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<SchedulingResult> results = null;
    private List<Exception> exceptionList = null;
    private long optimizationTime = 0;

    public SolutionSpace() {
        this.results = new LinkedList<>();
        this.exceptionList = new LinkedList<>();
    }

    public void computeStats() {
    }

    public void addResult(SchedulingResult sr) {
        results.add(sr);
    }

    public void addAllResults(Collection<SchedulingResult> sr) {
        results.addAll(sr);
    }

    public void addException(Exception ex) {
        exceptionList.add(ex);
    }

    public List<SchedulingResult> getResults() {
        return results;
    }

    public void clearResults() {
        results.clear();
    }

    public List<Exception> getExceptions() {
        return exceptionList;
    }

    public long getOptimizationTime() {
        return optimizationTime;
    }

    public void setOptimizationTime(long optimizationTime) {
        this.optimizationTime = optimizationTime;
    }

    public SchedulingResult getFastestPlan() {
        if (results.size() == 1) {
            return results.get(0);
        }
        SchedulingResult fastest = null;
        for (SchedulingResult optResult : results) {
            if (optResult.getStatistics().getTimeInQuanta() <= 0.0) {
                continue;
            }
            if (fastest == null) {
                fastest = optResult;
                continue;
            }
            if (fastest.getStatistics().getTimeInQuanta() > optResult.getStatistics()
                    .getTimeInQuanta()) {
                fastest = optResult;
            }
        }
        return fastest;
    }

    public SchedulingResult getSlowestPlan() {
        if (results.size() == 1) {
            return results.get(0);
        }
        SchedulingResult slowest = null;
        for (SchedulingResult optResult : results) {
            if (optResult.getStatistics().getTimeInQuanta() <= 0.0) {
                continue;
            }
            if (slowest == null) {
                slowest = optResult;
                continue;
            }
            if (slowest.getStatistics().getTimeInQuanta() < optResult.getStatistics()
                    .getTimeInQuanta()) {
                slowest = optResult;
            }
        }
        return slowest;
    }

    public SchedulingResult getCheapestPlan() {
        if (results.size() == 1) {
            return results.get(0);
        }
        SchedulingResult cheapest = null;
        for (SchedulingResult optResult : results) {
            if (optResult.getStatistics().getTimeInQuanta() <= 0.0) {
                continue;
            }
            if (cheapest == null) {
                cheapest = optResult;
                continue;
            }
            if (cheapest.getStatistics().getMoneyInQuanta() > optResult.getStatistics()
                    .getMoneyInQuanta()) {
                cheapest = optResult;
            }
        }
        return cheapest;
    }

    public SchedulingResult getMostExpensivePlan() {
        if (results.size() == 1) {
            return results.get(0);
        }
        SchedulingResult mostExpensive = null;
        for (SchedulingResult optResult : results) {
            if (optResult.getStatistics().getTimeInQuanta() <= 0.0) {
                continue;
            }
            if (mostExpensive == null) {
                mostExpensive = optResult;
                continue;
            }
            if (mostExpensive.getStatistics().getMoneyInQuanta() < optResult.getStatistics()
                    .getMoneyInQuanta()) {
                mostExpensive = optResult;
            }
        }
        return mostExpensive;
    }

    public SchedulingResult getFastestPlan(double costBudget) {
        if (results.size() == 1) {
            return results.get(0);
        }
        SchedulingResult fastest = null;
        for (SchedulingResult optResult : results) {
            if (optResult.getStatistics().getTimeInQuanta() <= 0.0) {
                continue;
            }
            if (optResult.getStatistics().getMoneyInQuanta() > costBudget) {
                continue;
            }
            if (fastest == null) {
                fastest = optResult;
                continue;
            }
            if (fastest.getStatistics().getTimeInQuanta() > optResult.getStatistics()
                    .getTimeInQuanta()) {
                fastest = optResult;
            }
        }
        return fastest;
    }

    public SchedulingResult getCheapestPlan(double timeLimit) {
        if (results.size() == 1) {
            return results.get(0);
        }
        SchedulingResult cheapest = null;
        for (SchedulingResult optResult : results) {
            if (optResult.getStatistics().getTimeInQuanta() <= 0.0) {
                continue;
            }
            if (optResult.getStatistics().getTimeInQuanta() > timeLimit) {
                continue;
            }
            if (cheapest == null) {
                cheapest = optResult;
                continue;
            }
            if (cheapest.getStatistics().getMoneyInQuanta() > optResult.getStatistics()
                    .getMoneyInQuanta()) {
                cheapest = optResult;
            }
        }
        return cheapest;
    }

    public List<SchedulingResult> findSkyline() {
        List<SchedulingResult> skyline = new LinkedList<>();
        if (results.size() == 1) {
            skyline.add(results.get(0));
            return skyline;
        }
        for (SchedulingResult optResult : results) {
            boolean dominated = false;
            LinkedList<SchedulingResult> toRemove = new LinkedList<>();
            for (SchedulingResult sk : skyline) {
                if (optResult.getStatistics().getTimeInQuanta() <= sk.getStatistics()
                        .getTimeInQuanta() && optResult.getStatistics().getMoneyInQuanta() <= sk
                        .getStatistics().getMoneyInQuanta()) {
                    /* sk is dominated */
                    toRemove.add(sk);
                } else {
                    if (optResult.getStatistics().getTimeInQuanta() >= sk.getStatistics()
                            .getTimeInQuanta() && optResult.getStatistics().getMoneyInQuanta() >= sk
                            .getStatistics().getMoneyInQuanta()) {
                        /* Event is dominated */
                        dominated = true;
                    }
                }
            }
            for (SchedulingResult remove : toRemove) {
                skyline.remove(remove);
            }
            if (!dominated) {
                skyline.add(optResult);
            }
        }
        return skyline;
    }

    public List<SchedulingResult> findMaxMaxSkyline() {
        List<SchedulingResult> skyline = new LinkedList<>();
        if (results.size() == 1) {
            skyline.add(results.get(0));
            return skyline;
        }
        for (SchedulingResult optResult : results) {
            boolean dominated = false;
            LinkedList<SchedulingResult> toRemove = new LinkedList<>();
            for (SchedulingResult sk : skyline) {
                if (optResult.getStatistics().getTimeInQuanta() >= sk.getStatistics()
                        .getTimeInQuanta() && optResult.getStatistics().getMoneyInQuanta() >= sk
                        .getStatistics().getMoneyInQuanta()) {
                    /* sk is dominated */
                    toRemove.add(sk);
                } else {
                    if (optResult.getStatistics().getTimeInQuanta() <= sk.getStatistics()
                            .getTimeInQuanta() && optResult.getStatistics().getMoneyInQuanta() <= sk
                            .getStatistics().getMoneyInQuanta()) {
                        /* Event is dominated */
                        dominated = true;
                    }
                }
            }
            for (SchedulingResult remove : toRemove) {
                skyline.remove(remove);
            }
            if (!dominated) {
                skyline.add(optResult);
            }
        }
        return skyline;
    }

    @Override
    public String toString() {
        String result = new String();

        result +=
                "containerNum," + "time," + "cost," + "containersUsed," + "containerFragmentation,"
                        + "refinedTime," + "refinedCost," + "refinedContainersUsed,"
                        + "refinedContainerFragmentation," + "\n";

        for (int i = 0; i < results.size(); i++) {
            SchedulingResult op = results.get(i);
            result += op.containerNum + "," + op.getStatistics().getTimeInQuanta() + "," + op
                    .getStatistics().getMoneyInQuanta() + "," + op.getStatistics().getContainersUsed()
                    + "," + "\n";
        }
        return result;
    }
}
