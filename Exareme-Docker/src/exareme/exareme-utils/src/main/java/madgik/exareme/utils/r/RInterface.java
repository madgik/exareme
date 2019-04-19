/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.r;


import madgik.exareme.utils.proc.ProcessDefn;
import madgik.exareme.utils.proc.ProcessResult;
import madgik.exareme.utils.proc.ProcessUtil;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.Scanner;

/**
 * @author heraldkllapi
 */
public class RInterface {
    private static final Logger log = Logger.getLogger(RInterface.class);

    public static double optimize(String function, double[] initialVals, double[] minValues,
                                  double[] maxValues, double[] variables) throws RemoteException {
        StringBuilder program = new StringBuilder();
        program.append(function);
        if (initialVals == null) {
            initialVals = new double[variables.length];
        }

        program.append("result <- optim(" + vec(initialVals) + ", " +
                "f, method=\"L-BFGS-B\", control=list(fnscale=-1)");
        if (minValues != null) {
            program.append(", lower=" + vec(minValues));
        }
        if (maxValues != null) {
            program.append(", upper=" + vec(maxValues));
        }
        program.append(");\n\n\n");
        program.append("cat(\"PAR:\\t\");cat(result$par);cat(\"\\n\");\n");
        program.append("cat(\"VALUE:\\t\");cat(result$value);cat(\"\\n\");\n\n");
        log.debug("OPTIMIZING: \n" + program.toString());
        return optimize(program.toString(), variables);
    }

    private static String vec(double[] vec) {
        StringBuilder vector = new StringBuilder("c(");
        vector.append(vec[0]);
        for (int i = 1; i < vec.length; ++i) {
            vector.append(", " + vec[i]);
        }
        vector.append(")");
        return vector.toString();
    }

    public static double optimize(String program, double[] variables) throws RemoteException {
        try {
            ProcessDefn proc = new ProcessDefn();
            //      proc.addArgs("--no-save");
            proc.setProcess("/usr/bin/r");
            proc.setStdin(program);
            ProcessResult result = ProcessUtil.Exec(proc);

            log.info(result.stdout);
            log.error(result.stderr);

            double objectiveFunction = Double.NaN;

            Scanner scan = new Scanner(result.stdout);
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.startsWith("PAR:")) {
                    String[] par = line.split("\t");
                    String[] values = par[1].split(" ");
                    for (int i = 0; i < values.length; ++i) {
                        variables[i] = Double.parseDouble(values[i]);
                    }
                    continue;
                }
                if (line.startsWith("VALUE:")) {
                    String[] value = line.split("\t");
                    objectiveFunction = Double.parseDouble(value[1]);
                    break;
                }
            }

            return objectiveFunction;
        } catch (Exception e) {
            throw new RemoteException("Cannot run optimize", e);
        }
    }
}
