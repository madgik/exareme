/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer;

import madgik.exareme.common.optimizer.FinancialProperties;
import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author Herald Kllapi
 * @since 1.0
 */
public interface MultiObjectiveQueryScheduler extends Serializable {

    SolutionSpace callOptimizer(final ConcreteQueryGraph queryGraph,
                                final AssignedOperatorFilter subgraphFilter, final ArrayList<ContainerResources> containers,
                                final ContainerFilter containerFilter, final RunTimeParameters runTimeParameters,
                                final FinancialProperties financialProperties) throws RemoteException;
}
