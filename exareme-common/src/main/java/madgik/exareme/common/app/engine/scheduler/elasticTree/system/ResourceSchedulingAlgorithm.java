/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system;


import madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud.ComputeCloud;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.dataflow.Dataflow;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.dataflow.RunningDataflow;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.runtime.GlobalSystemState;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public interface ResourceSchedulingAlgorithm {

    void initializeResources(ComputeCloud cloud, GlobalSystemState state);

    void finalizeResources(ComputeCloud cloud, GlobalSystemState state);

    void reserveResources(Dataflow dataflow, RunningDataflow runDataflow, ComputeCloud cloud,
                          GlobalSystemState state);

    void dataflowFinished(RunningDataflow runningDataflow, ComputeCloud cloud,
                          GlobalSystemState state);

    void dataflowError(RunningDataflow runningDataflow, ComputeCloud cloud,
                       GlobalSystemState state);

    // SYSTEM INTEGRATION
    void reorganizeResources(GlobalSystemState state) throws RemoteException;
}
