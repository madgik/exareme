/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.chart;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * @author heraldkllapi
 */
public class BarChart {
    private final String name;
    private final int barSize;
    private final boolean logScale;
    private final ArrayList<String> names = new ArrayList<String>();
    private final ArrayList<Double> values = new ArrayList<Double>();
    private final DecimalFormat df = new DecimalFormat("#.##");
    private int maxNameLength = 3;
    private double minValue = Double.MAX_VALUE;
    private double maxValue = -Double.MAX_VALUE;

    public BarChart(String name) {
        this.name = name;
        this.barSize = 50;
        this.logScale = false;
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        df.setDecimalSeparatorAlwaysShown(true);
    }

    public BarChart(String name, int barSize) {
        this.name = name;
        this.barSize = barSize;
        this.logScale = false;
    }

    public BarChart(String name, int barSize, boolean logScale) {
        this.name = name;
        this.barSize = barSize;
        // Not working properly
        this.logScale = false;
    }

    public void add(double value) {
        add(null, value);
    }

    public void add(String name, double value) {
        values.add(value);
        if (minValue > value) {
            minValue = value;
        }
        if (maxValue < value) {
            maxValue = value;
        }
        names.add(name);
        if (name != null) {
            if (maxNameLength < name.length()) {
                maxNameLength = name.length();
            }
        }
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        DecimalFormat nf = new DecimalFormat("##");
        nf.setMinimumIntegerDigits(3);
        sb.append("  == " + name + " Log(" + logScale + ") == \n");
        sb.append("Values in [" + df.format(minValue) + ", " + df.format(maxValue) + "] \n");

        // Find the ...
        int maxValueStringSize = 0;
        for (int i = 0; i < values.size(); ++i) {
            double v = values.get(i);
            int size = getStringValue(v).length();
            if (maxValueStringSize < size) {
                maxValueStringSize = size;
            }
        }
        // Print bar chart
        for (int i = 0; i < values.size(); ++i) {
            double v = values.get(i);
            String name = names.get(i);
            if (name == null) {
                name = nf.format(i);
            }
            name += getEmpty(maxNameLength - name.length());
            int size = getSize(v);
            String valueStr = getStringValue(v);
            sb.append(name + " |" + getBar(size) + "|" + getEmpty(barSize - size) + " : " +
                    getEmpty(maxValueStringSize - valueStr.length()) + valueStr + "\n");
        }
        return sb.toString();
    }

    private int getSize(double v) {
        double min = minValue;
        double max = maxValue;
        double value = v;

        if (logScale) {
            if (minValue > 0) {
                min = Math.log10(minValue);
            }
            max = Math.log10(maxValue);
            if (v > 0) {
                value = Math.log10(v);
            }
        }
        int size = (int) ((double) barSize * value / (max - min));
        return (size > barSize) ? barSize : size;
    }

    private String getStringValue(double v) {
        String valStr = df.format(v);
        int idx = valStr.length() - valStr.lastIndexOf(".");
        for (int i = idx; i <= 2; ++i) {
            valStr += "0";
        }
        if (valStr.equals("00")) {
            valStr = "0.00";
        }
        return valStr;
    }

    private String getBar(int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; ++i) {
            sb.append("=");
        }
        return sb.toString();
    }

    private String getEmpty(int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; ++i) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
