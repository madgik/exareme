/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.graph;

import madgik.exareme.common.optimizer.OperatorBehavior;
import madgik.exareme.common.optimizer.RunTimeParameters;

import java.util.Random;

/**
 * @author Herald Kllapi
 * @since 1.0
 */
public class GraphGenerator {

    private static int dataCount = 0;

    private GraphGenerator() {
    }

    public static ConcreteQueryGraph parseDax(String url) {
        try {
            GraphParameters params = new GraphParameters();
            return PegasusDaxParser.parseDax(url, params);
        } catch (Exception e) {
            e.printStackTrace();
            //System.err.println(e.getMessage());
            return null;
        }
    }

    public static ConcreteQueryGraph createLatticeGraph(int depth, int breadth, double z,
        double randType, long seed) {
        double[] runTime = {1.0};
        double[] cpuUtil = {1.0};
        int[] memory = {100};
        double[] dataout = {1.0};
        RandomParameters params =
            new RandomParameters(z, randType, runTime, cpuUtil, memory, dataout);
        return LatticeGraphGenerator.createLatticeGraph(depth, breadth, params, seed);
    }

    public static ConcreteQueryGraph createLatticeGraph(int depth, int breadth, long seed) {
        double z = 1.0;
        double randType = 0.0;
        double[] runTime = {1.0};
        double[] cpuUtil = {1.0};
        int[] memory = {30};
        double[] dataout = {1.0};
        RandomParameters params =
            new RandomParameters(z, randType, runTime, cpuUtil, memory, dataout);
        return LatticeGraphGenerator.createLatticeGraph(depth, breadth, params, seed);
    }

    public static ConcreteQueryGraph createLatticeGraph(int depth, int breadth,
        RandomParameters params, long seed) {
        return LatticeGraphGenerator.createLatticeGraph(depth, breadth, params, seed);
    }

    public static ConcreteOperator createOperator(String id, int fanOut, RandomParameters params,
        Random rand) {
        double runTimeValue = params.runTime[params.runTimeDist.next()];
        double cpuUtilizationValue = params.cpuUtil[params.cpuUtilDist.next()];
        int memoryValue = params.memory[params.memoryDist.next()];
        RunTimeParameters runTimeParameters = new RunTimeParameters();
        ConcreteOperator op =
            new ConcreteOperator(id, runTimeValue * runTimeParameters.quantum__SEC,
                cpuUtilizationValue, memoryValue, operatorType(params.operatorType, rand));
        double quantums = params.dataout[params.dataoutDist.next()];
        double bytesPerQuantum =
            runTimeParameters.network_speed__MB_SEC * runTimeParameters.quantum__SEC;
        for (int i = 0; i < fanOut; i++) {
            LinkData data = new LinkData("Data" + dataCount, quantums * bytesPerQuantum);
            op.addOutputData(data);
            dataCount++;
        }
        return op;
    }

    public static ConcreteOperator createOperator(String id, double runTimeValue,
        double cpuUtilizationValue, int memoryValue, OperatorBehavior type, double[] outputData) {
        ConcreteOperator op =
            new ConcreteOperator(id, runTimeValue, cpuUtilizationValue, memoryValue, type);
        for (double out : outputData) {
            LinkData data = new LinkData("Data" + dataCount, out);
            op.addOutputData(data);
            dataCount++;
        }
        return op;
    }

    public static OperatorBehavior operatorType(double randType, Random rand) {
        return (rand.nextDouble() < randType) ?
            OperatorBehavior.pipeline :
            OperatorBehavior.store_and_forward;
    }
}
