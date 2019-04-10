/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.graph;

import madgik.exareme.utils.statistics.Zipf;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Properties;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RandomParameters implements Serializable {
    public double z;
    public double operatorType;
    public double[] runTime = null;
    public double[] cpuUtil = null;
    public int[] memory = null;
    public double[] dataout = null;
    public Zipf runTimeDist = null;
    public Zipf cpuUtilDist = null;
    public Zipf memoryDist = null;
    public Zipf dataoutDist = null;

    public RandomParameters(double z, double operatorType, double[] runTime, double[] cpuUtil,
                            int[] memory, double[] dataout) {
        this.z = z;
        this.operatorType = operatorType;

        this.runTime = runTime;
        this.cpuUtil = cpuUtil;
        this.memory = memory;
        this.dataout = dataout;

        this.runTimeDist = new Zipf(this.runTime.length, z);
        this.cpuUtilDist = new Zipf(this.cpuUtil.length, z);
        this.memoryDist = new Zipf(this.memory.length, z);
        this.dataoutDist = new Zipf(this.dataout.length, z);
    }

    @SuppressWarnings("static-access")
    public static RandomParameters parseFile(String url)
            throws IOException {
        Properties properties = new Properties();
        properties.load(new URL(url).openStream());

        double z = Double.parseDouble(properties.getProperty("z"));
        double operatorType = Double.parseDouble(properties.getProperty("type"));

        String[] runtimeStr = properties.getProperty("time").split(",");
        double[] runTime = new double[runtimeStr.length];
        for (int i = 0; i < runTime.length; i++) {
            runTime[i] = Double.parseDouble(runtimeStr[i]);
        }

        String[] cpuUtilStr = properties.getProperty("cpu").split(",");
        double[] cpuUtil = new double[cpuUtilStr.length];
        for (int i = 0; i < cpuUtil.length; i++) {
            cpuUtil[i] = Double.parseDouble(cpuUtilStr[i]);
        }

        String[] memoryStr = properties.getProperty("mem").split(",");
        int[] memory = new int[memoryStr.length];
        for (int i = 0; i < memory.length; i++) {
            memory[i] = Integer.parseInt(memoryStr[i]);
        }

        String[] dataoutStr = properties.getProperty("data").split(",");
        double[] dataout = new double[dataoutStr.length];
        for (int i = 0; i < dataout.length; i++) {
            dataout[i] = Double.parseDouble(dataoutStr[i]);
        }

        RandomParameters parameters =
                new RandomParameters(z, operatorType, runTime, cpuUtil, memory, dataout);

        return parameters;
    }

    public void resetRandom(long seed) {
        this.runTimeDist = new Zipf(this.runTime.length, z, seed);
        this.cpuUtilDist = new Zipf(this.cpuUtil.length, z, seed);
        this.memoryDist = new Zipf(this.memory.length, z, seed);
        this.dataoutDist = new Zipf(this.dataout.length, z, seed);
    }
}
