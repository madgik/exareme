/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.*;
import madgik.exareme.worker.art.executionEngine.resourceMgr.PlanSessionResourceManager;
import madgik.exareme.worker.art.executionEngine.rmi.RmiExecutionEngine;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import madgik.exareme.worker.art.registry.ArtRegistryProxy;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ExecutionEngineFactory {
    private ExecutionEngineFactory() {
    }

    public static ExecutionEngine createRmiDynamicExecutionEngine(EntityName regEntityName)
        throws RemoteException {
        ArtRegistryProxy registryProxy = ArtRegistryLocator.getArtRegistryProxy();
        ExecutionEngineStatus executionEngineStatus = new ExecutionEngineStatus();
        DynamicReportManager reportManager = new DynamicReportManager();
        DynamicStatusManager statusManager = new DynamicStatusManager();
        DynamicStatisticsManager statisticsManager = new DynamicStatisticsManager();
        DynamicClockTickManager clockTickManager = new DynamicClockTickManager();
        PlanSessionResourceManager resourceManager = new PlanSessionResourceManager();

        DynamicPlanManager sessionManager =
            new DynamicPlanManager(registryProxy, reportManager, statusManager, resourceManager,
                clockTickManager, statisticsManager);

        RmiExecutionEngine execEngine =
            new RmiExecutionEngine(sessionManager, statusManager, reportManager, statisticsManager,
                clockTickManager, regEntityName, executionEngineStatus);

        sessionManager.setExecutionEngine(execEngine);
        sessionManager.createGlobalScheduler();
        return execEngine;
    }
}
