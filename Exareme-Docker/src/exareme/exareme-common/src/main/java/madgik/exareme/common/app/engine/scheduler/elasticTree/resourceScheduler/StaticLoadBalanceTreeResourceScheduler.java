/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.resourceScheduler;


import madgik.exareme.common.app.engine.scheduler.elasticTree.ContainerTopology;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.ResourceSchedulingAlgorithm;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud.ComputeCloud;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.dataflow.Dataflow;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.dataflow.RunningDataflow;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.runtime.GlobalSystemState;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class StaticLoadBalanceTreeResourceScheduler implements ResourceSchedulingAlgorithm {
    private final ContainerTopology topology;
    private final int containersPerLevel[];
    protected int concurentQueries = 0;

    public StaticLoadBalanceTreeResourceScheduler(int containersPerLevel[],
                                                  ContainerTopology topology) {
        this.containersPerLevel = containersPerLevel;
        this.topology = topology;
    }

    @Override
    public void initializeResources(ComputeCloud cloud, GlobalSystemState state) {
        topology.allocateStaticContainersPerLevel(containersPerLevel);
    }

    @Override
    public void finalizeResources(ComputeCloud cloud, GlobalSystemState state) {
        topology.deleteAllContainers();
    }

    @Override
    public void reserveResources(Dataflow dataflow, RunningDataflow runningDataflow,
                                 ComputeCloud cloud, GlobalSystemState state) {
        concurentQueries++;
    }

    @Override
    public void dataflowFinished(RunningDataflow runningDataflow, ComputeCloud cloud,
                                 GlobalSystemState state) {
        concurentQueries--;
    }

    @Override
    public void dataflowError(RunningDataflow runningDataflow, ComputeCloud cloud,
                              GlobalSystemState state) {
        concurentQueries--;
    }

    @Override
    public void reorganizeResources(GlobalSystemState state) throws RemoteException {
        // Do nothing!
    }
}
