/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.rmi;

import madgik.exareme.common.optimizer.FinancialProperties;
import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.master.engine.parser.SemanticException;
import madgik.exareme.master.queryProcessor.ConsoleUtils;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.optimizer.*;
import madgik.exareme.utils.properties.AdpDBProperties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author heraldkllapi
 */
public class RmiAdpDBSelectScheduler {

    public static final RunTimeParameters runTimeParams = new RunTimeParameters();
    public static final FinancialProperties finProps = new FinancialProperties();
    public static final String PROPS = "db.optimizer.scheduler.";
    protected static final Logger log = Logger.getLogger(RmiAdpDBSelectScheduler.class);
    public static BigInteger exhaustiveThreshold = null;

    static {
        runTimeParams.disk_throughput__MB_SEC =
            AdpDBProperties.getAdpDBProps().getFloat(PROPS + "runtime.diskThroughput");
        runTimeParams.network_speed__MB_SEC =
            AdpDBProperties.getAdpDBProps().getFloat(PROPS + "runtime.networkSpeed");
        runTimeParams.quantum__SEC =
            AdpDBProperties.getAdpDBProps().getFloat(PROPS + "runtime.quantum");
        exhaustiveThreshold = new BigInteger(
            AdpDBProperties.getAdpDBProps().getString(PROPS + "whatif.exhaustiveThreshold"));
    }

    //todo like our demo main for plan generation :)
    public static SolutionSpace schedule(ConcreteQueryGraph graph,
        AssignedOperatorFilter subgraphFilter, ArrayList<ContainerResources> containers,
        ContainerFilter contFilter) throws RemoteException {
        int algIndex = AdpDBProperties.getAdpDBProps().getInt(PROPS + "algorithm.index");

        String schedulerClassName =
            "madgik.exareme.master.queryProcessor.optimizer.scheduler." + AdpDBProperties
                .getAdpDBProps().getString(PROPS + "algorithm." + algIndex + ".name");
        log.debug("Using search scheduler '" + schedulerClassName + "' ...");

        SolutionSpace space = new SolutionSpace();
        try {
            MultiObjectiveQueryScheduler scheduler =
                (MultiObjectiveQueryScheduler) Class.forName(schedulerClassName).newInstance();
            space = scheduler
                .callOptimizer(graph, subgraphFilter, containers, contFilter, runTimeParams,
                    finProps);
        } catch (Exception e) {
            log.error("Cannot schedule graph", e);
            throw new SemanticException("Cannot schedule graph", e);
        }
        ConsoleUtils.printSkylineToLog(graph, runTimeParams, space, log, Level.DEBUG);
        return space;
    }
}
