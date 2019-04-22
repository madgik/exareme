/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.arm.compute.ArmComputeProxy;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.remote.ObjectProxy;

import java.rmi.RemoteException;

/**
 * This is the ArtRegistryProxy interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}@di.uoa.gr<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ArtRegistryProxy extends ObjectProxy<ArtRegistry> {

    void registerContainer(ContainerProxy containerProxy) throws RemoteException;

    ContainerProxy lookupContainer(EntityName epr) throws RemoteException;

    ContainerProxy[] getContainers() throws RemoteException;

    void removeContainer(EntityName epr) throws RemoteException;

    void removeLogger(EntityName epr) throws RemoteException;

    void registerExecutionEngine(ExecutionEngineProxy executionEngineProxy) throws RemoteException;

    ExecutionEngineProxy lookupExecutionEngine(EntityName epr) throws RemoteException;

    ExecutionEngineProxy[] getExecutionEngines() throws RemoteException;

    void removeExecutionEngine(EntityName epr) throws RemoteException;

    void registerComputeMediator(ArmComputeProxy armComputeProxy) throws RemoteException;

    ArmComputeProxy lookupComputeMediator(EntityName name) throws RemoteException;

    ArmComputeProxy[] getComputeMediators() throws RemoteException;

    void removeComputeMediator(EntityName name) throws RemoteException;
}
