/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.experiments;


import madgik.exareme.common.app.engine.scheduler.elasticTree.logger.ElasticTreeLoggerUtils;
import madgik.exareme.common.app.engine.scheduler.elasticTree.logger.LogEvents;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author heraldkllapi
 */
public class ExpPostProcessing {

    private final static String path = "";

    private static final double SMOOTH_WINDOW = 300;
    private static final double REPORT_EVERY = 300;
    private static final double REPORT_EVERY_QUERY = 0;

    private static final double SMOOTH_WINDOW_TREE = 300;
    private static final double REPORT_EVERY_TREE = 300;

    public static void main(String[] args) throws Exception {
        String exp = "3hoursEachTry/";
        //    String expFile = path + exp + "/Tree-phase-0.log";
        //    String expFile = path + exp + "/Tree-phase-1.log";
        String expFile = path + exp + "/Tree-phase-2.log";

        //    String exp = "static/";
        //    String expFile = path + exp + "/Tree-dynamic.log";
        //    String expFile = path + exp + "/Tree-small.log";
        //    String expFile = path + exp + "/Tree-medium.log";
        //    String expFile = path + exp + "/Tree-large.log";

        //    parseExecTime(expFile);
        //    parseMoney(expFile);
        parseTreeLevels(expFile);
    }

    private static void parseExecTime(String expFile) throws Exception {
        Scanner scanner = new Scanner(new File(expFile));
        System.out.println("time\t execTime\t smoothed");
        FunctionSmoothing execSmooth = new FunctionSmoothing(SMOOTH_WINDOW);
        double prevReportTime = 0;
        while (scanner.hasNextLine()) {
            String next = scanner.nextLine();
            LogEvents.Event event = ElasticTreeLoggerUtils.parseEvent(next);
            if (event.type == LogEvents.EventType.querySuccess) {
                execSmooth.add(event.time, event.toQuerySuccess().execTime);
                if (prevReportTime < event.time - REPORT_EVERY_QUERY) {
                    System.out.println(event.time + "\t" +
                            event.toQuerySuccess().execTime + "\t" +
                            execSmooth.getValue());
                    prevReportTime = event.time;
                }
            } else if (event.type == LogEvents.EventType.queryError) {
                //        System.out.println(event.time + "\t" + -1);
            }
        }
    }

    private static void parseMoney(String expFile) throws Exception {
        Scanner scanner = new Scanner(new File(expFile));
        System.out.println("time\t cost\t rev\t profit\t margCost\t margRev\t margProfit");
        ArrayList<LogEvents.Money> events = new ArrayList<>();

        FunctionSmoothing costSmooth = new FunctionSmoothing(SMOOTH_WINDOW, true);
        FunctionSmoothing costMargSmooth = new FunctionSmoothing(SMOOTH_WINDOW, true);

        FunctionSmoothing revenueSmooth = new FunctionSmoothing(SMOOTH_WINDOW, true);
        FunctionSmoothing revenueMargSmooth = new FunctionSmoothing(SMOOTH_WINDOW, true);

        FunctionSmoothing profitSmooth = new FunctionSmoothing(SMOOTH_WINDOW, true);
        FunctionSmoothing profitMargSmooth = new FunctionSmoothing(SMOOTH_WINDOW, true);

        double prevReportTime = Double.MIN_VALUE;
        while (scanner.hasNextLine()) {
            String next = scanner.nextLine();
            LogEvents.Event event = ElasticTreeLoggerUtils.parseEvent(next);
            if (event.type == LogEvents.EventType.money) {
                events.add(event.toMoney());
                double windowCost = event.toMoney().totalCost;
                double windowRev = event.toMoney().totalRevenue;
                double windowProfit = event.toMoney().profit;
                for (int i = events.size() - 1; i >= 0; i--) {
                    LogEvents.Money e = events.get(i);
                    if (e.time < event.time - SMOOTH_WINDOW) {
                        windowCost -= e.totalCost;
                        windowRev -= e.totalRevenue;
                        windowProfit -= e.profit;
                        break;
                    }
                }
                if (prevReportTime < event.time - REPORT_EVERY) {
                    costSmooth.add(event.time, event.toMoney().totalCost);
                    costMargSmooth.add(event.time, windowCost);

                    revenueSmooth.add(event.time, event.toMoney().totalRevenue);
                    revenueMargSmooth.add(event.time, windowRev);

                    profitSmooth.add(event.time, event.toMoney().profit);
                    profitMargSmooth.add(event.time, windowProfit);
                    System.out.println(event.time + "\t" +
                            event.toMoney().totalCost + "\t" +
                            event.toMoney().totalRevenue + "\t" +
                            event.toMoney().profit + "\t" +
                            (costMargSmooth.getValue() * REPORT_EVERY) + "\t" +
                            (revenueMargSmooth.getValue() * REPORT_EVERY) + "\t" +
                            (profitMargSmooth.getValue() * REPORT_EVERY));
                    prevReportTime = event.time;
                }
            }
        }
    }

    private static void parseTreeLevels(String expFile) throws Exception {
        Scanner scanner = new Scanner(new File(expFile));
        System.out.println("time\t level0\t level1\t level2");
        FunctionSmoothing[] levels = new FunctionSmoothing[3];
        levels[0] = new FunctionSmoothing(SMOOTH_WINDOW_TREE);
        levels[1] = new FunctionSmoothing(SMOOTH_WINDOW_TREE);
        levels[2] = new FunctionSmoothing(SMOOTH_WINDOW_TREE);
        double prevReportTime = Double.MIN_VALUE;

        while (scanner.hasNextLine()) {
            String next = scanner.nextLine();
            LogEvents.Event event = ElasticTreeLoggerUtils.parseEvent(next);
            if (event.type == LogEvents.EventType.elasticTreeLevel) {
                LogEvents.ElasticTreeLevel e = event.toElasticLevel();
                if (prevReportTime < event.time - REPORT_EVERY_TREE) {
                    levels[e.level].add(event.time, event.toElasticLevel().containers);
                    if (e.level == 2) {
                        System.out.println(event.time + "\t" +
                                levels[0].getValue() + "\t" +
                                levels[1].getValue() + "\t" +
                                levels[2].getValue());
                        prevReportTime = event.time;
                    }
                }
            }
        }
    }
}
