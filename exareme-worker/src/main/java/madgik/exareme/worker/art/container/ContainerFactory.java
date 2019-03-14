/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorManagerFactory;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorManagerInterface;
import madgik.exareme.worker.art.container.bufferMgr.BufferManagerInterface;
import madgik.exareme.worker.art.container.bufferMgr.BufferManagerInterfaceFactory;
import madgik.exareme.worker.art.container.dataTransfer.rest.DataTransferRequestHandler;
import madgik.exareme.worker.art.container.dataTrasferMgr.DataTransferMgrInterface;
import madgik.exareme.worker.art.container.dataTrasferMgr.DataTransferMgrLocator;
import madgik.exareme.worker.art.container.diskMgr.DiskManagerInterface;
import madgik.exareme.worker.art.container.diskMgr.DiskManagerInterfaceFactory;
import madgik.exareme.worker.art.container.jobQueue.JobQueue;
import madgik.exareme.worker.art.container.jobQueue.JobQueueInterface;
import madgik.exareme.worker.art.container.netMgr.NetManagerInterface;
import madgik.exareme.worker.art.container.netMgr.NetManagerInterfaceFactory;
import madgik.exareme.worker.art.container.operatorGroupMgr.OperatorGroupManager;
import madgik.exareme.worker.art.container.operatorGroupMgr.OperatorGroupManagerInterface;
import madgik.exareme.worker.art.container.operatorGroupMgr.sync.SynchronizedOperatorGroupManager;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManagerInterface;
import madgik.exareme.worker.art.container.operatorMgr.sync.SynchronizedConcreteOperatorManager;
import madgik.exareme.worker.art.container.operatorMgr.thread.ThreadConcreteOperatorManager;
import madgik.exareme.worker.art.container.resources.ContainerResources;
import madgik.exareme.worker.art.container.rmi.RmiContainer;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerFactory;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import static madgik.exareme.worker.art.container.dataTrasferMgr.DataTransferMgrInterfaceFactory.createDataTransferManagerDTPInterface;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}@di.uoa.gr<br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ContainerFactory {

    private static final Logger log = Logger.getLogger(ContainerFactory.class);

    private ContainerFactory() {
        throw new IllegalStateException("Cannot instantiate this class");
    }

    public static Container createRMIThreadContainer(String containerName, ContainerID containerID,
                                                     EntityName regEntityName, int dtPort) throws RemoteException {

        JobQueueInterface jobQueueInterface = new JobQueue();
        ContainerResources resources =
                new ContainerResources(100);//TODO(DSQ) resources of container
        Executor executor = new Executor(resources);
        jobQueueInterface.setExecutor(executor);
        executor.setJobQueue(jobQueueInterface);

        ContainerStatus containerStatus = new ContainerStatus();

        StatisticsManagerInterface statisticsManagerInterface =
                StatisticsManagerFactory.createSimpleManager(containerName);


        BufferManagerInterface bufferManagerInterface = BufferManagerInterfaceFactory
                .createBufferManagerInterface(containerStatus.bufferStatus, statisticsManagerInterface);

        DiskManagerInterface diskManagerInterface = DiskManagerInterfaceFactory
                .createSimpleDiskManager(containerStatus.diskManagerStatus, statisticsManagerInterface);

        DataTransferRequestHandler.diskManagerInterface = diskManagerInterface;

        NetManagerInterface netManagerInterface = NetManagerInterfaceFactory
                .createSimpleNetManager(containerStatus.netManagerStatus, statisticsManagerInterface);

        AdaptorManagerInterface adaptorManager = AdaptorManagerFactory.createAdaptorManager();

        log.debug("\t DataTranferManager ... ");
        DataTransferMgrInterface dataTransferManagerDTP =
                createDataTransferManagerDTPInterface(dtPort);
        DataTransferMgrLocator.setDataTransferMgr(dataTransferManagerDTP, dtPort);
        OperatorGroupManagerInterface operatorGroupManager =
                new SynchronizedOperatorGroupManager(new OperatorGroupManager());

        ConcreteOperatorManagerInterface concreteOperatorManagerInterface =
                new SynchronizedConcreteOperatorManager(
                        new ThreadConcreteOperatorManager(containerStatus.operatorStatus,
                                diskManagerInterface, statisticsManagerInterface, containerID,
                                jobQueueInterface, dataTransferManagerDTP, operatorGroupManager));


        //DataTransferManagerInterface dataTransferManagerInterface = new DataTransferManager ();
        regEntityName.setDataTransferPort(dtPort);
        log.trace("Just before container creation... ");
        Container container = new RmiContainer(containerName, concreteOperatorManagerInterface,
                bufferManagerInterface, diskManagerInterface, netManagerInterface, adaptorManager,
                statisticsManagerInterface, jobQueueInterface, executor, resources, regEntityName,
                containerStatus, dataTransferManagerDTP, containerID);
        return container;
    }
}
