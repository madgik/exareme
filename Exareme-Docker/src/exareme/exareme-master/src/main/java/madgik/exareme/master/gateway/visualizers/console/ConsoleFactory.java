/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.gateway.visualizers.console;

import madgik.exareme.master.gateway.visualizers.html.HTMLPlanSessionStatisticsFormat;

/**
 * @author herald
 */
public class ConsoleFactory {

    private static ConsolePlanSessionStatisticsFormat statsFormat =
            new ConsolePlanSessionStatisticsFormat();

    private static HTMLPlanSessionStatisticsFormat htmlStatsFormat =
            new HTMLPlanSessionStatisticsFormat();

    private ConsoleFactory() {
    }

    public static ConsolePlanSessionStatisticsFormat getDefaultStatisticsFormat() {
        return statsFormat;
    }

    public static HTMLPlanSessionStatisticsFormat getHTMLStatisticsFormat() {
        return htmlStatsFormat;
    }
}
