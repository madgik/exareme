package madgik.exareme.master.engine.rmi.tree;

import madgik.exareme.master.queryProcessor.optimizer.SolutionSpace;
import madgik.exareme.master.queryProcessor.optimizer.scheduler.SchedulingResult;

/**
 * Created by panos on 7/5/14.
 */
//todo: review
public class MostProfitableSLA {

    public static double selectMostProfitableSLA(SolutionSpace space, SLA sla) {
        double bestTime = 0.0f, bestProfit = Double.MIN_VALUE;
        for (SchedulingResult schedule : space.findSkyline()) {
            double scheduleTime = schedule.getStatistics().getTimeInQuanta();
            double cost = sla.getCostAtTime(scheduleTime);
            double profit = cost - schedule.getStatistics().getMoneyInQuanta();
            if (profit > bestProfit) {
                bestProfit = profit;
                bestTime = scheduleTime;
            }
        }
        return bestTime;
    }
}
