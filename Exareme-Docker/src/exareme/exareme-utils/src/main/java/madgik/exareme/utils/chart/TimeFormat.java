/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.chart;

import java.text.DecimalFormat;

/**
 * @author herald
 */
public class TimeFormat {

    private TimeUnit unit = null;
    private DecimalFormat format = null;

    public TimeFormat(TimeUnit unit) {
        this.unit = unit;
        this.format = new DecimalFormat("#####.##");
        this.format.setMinimumFractionDigits(2);
        this.format.setMaximumFractionDigits(2);
    }

    public String format(long ms) {

        double time = 0.0;

        switch (unit) {
            case miliSec:
                time = (double) ms;
                break;
            case sec:
                time = (double) ms / 1000.0;
                break;
            case min:
                time = (double) ms / (1000.0 * 60.0);
                break;
            case hour:
                time = (double) ms / (1000.0 * 60.0 * 60.0);
                break;
            case day:
                time = (double) ms / (1000.0 * 60.0 * 60.0 * 24.0);
                break;
            case week:
                time = (double) ms / (1000.0 * 60.0 * 60.0 * 24.0 * 7.0);
                break;
        }

        String decTime = format.format(time);
        String[] dotParts = decTime.split("\\.");
        String[] commaParts = decTime.split("\\,");

        String min = "0";
        String sec = "0";

        if (dotParts.length > 1) {
            min = dotParts[0];
            sec = dotParts[1];
        }

        if (commaParts.length > 1) {
            min = commaParts[0];
            sec = commaParts[1];
        }

        int intSec = Integer.parseInt(sec);
        int dSec = (intSec * 60) / 100;

        sec = "" + dSec;
        if (sec.length() == 1) {
            sec = "0" + sec;
        }

        return min + ":" + sec;
    }
}
