/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.chart;

import java.text.DecimalFormat;

/**
 * @author herald
 */
public class DataFormat {
    private DataUnit unit = null;
    private DecimalFormat format = null;

    public DataFormat(DataUnit unit, DecimalFormat format) {
        this.unit = unit;
        this.format = format;
    }

    public String format(long bytes) {
        double data = 0.0;
        switch (unit) {
            case B:
                data = (double) bytes;
                break;
            case KB:
                data = (double) bytes / 1024.0;
                break;
            case MB:
                data = (double) bytes / (1024.0 * 1024.0);
                break;
            case GB:
                data = (double) bytes / (1024.0 * 1024.0 * 1024.0);
                break;
            case TB:
                data = (double) bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
                break;
            case PB:
                data = (double) bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0);
                break;
        }
        return format.format(data);
    }
}
