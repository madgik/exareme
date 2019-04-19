/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.chart;


/**
 * @author heraldkllapi
 */
public class BarChartDemo {

    public static void main(String[] args) {
        BarChart chart = new BarChart("Demo", 50, true);
        for (int i = 0; i < 15; i++) {
            chart.add(i + " - " + (i + 1), 1 + i + 0.1);
        }
        System.out.println(chart.format());
    }
}
