/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.rmi;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.common.optimizer.OperatorCategory;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.*;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorManager;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorManagerInterface;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorType;
import madgik.exareme.worker.art.container.buffer.BufferID;
import madgik.exareme.worker.art.container.buffer.BufferQoS;
import madgik.exareme.worker.art.container.bufferMgr.BufferManager;
import madgik.exareme.worker.art.container.bufferMgr.BufferManagerInterface;
import madgik.exareme.worker.art.container.dataTransfer.DataTransferGateway;
import madgik.exareme.worker.art.container.dataTrasferMgr.DataTransferMgrInterface;
import madgik.exareme.worker.art.container.diskMgr.DiskManager;
import madgik.exareme.worker.art.container.diskMgr.DiskManagerInterface;
import madgik.exareme.worker.art.container.job.*;
import madgik.exareme.worker.art.container.jobQueue.JobQueueInterface;
import madgik.exareme.worker.art.container.netMgr.NetManager;
import madgik.exareme.worker.art.container.netMgr.NetManagerInterface;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManager;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManagerInterface;
import madgik.exareme.worker.art.container.resources.ContainerResources;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManager;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;
import madgik.exareme.worker.art.managementBean.ContainerManagement;
import madgik.exareme.worker.art.managementBean.ManagementUtil;
import madgik.exareme.worker.art.parameter.Parameters;
import madgik.exareme.worker.art.quantumClock.ContainerQuantumClock;
import madgik.exareme.worker.art.registry.updateDeamon.RegistryUpdateDeamon;
import madgik.exareme.worker.art.registry.updateDeamon.RegistryUpdateDeamonFactory;
import madgik.exareme.worker.art.remote.RmiRemoteObject;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import static madgik.exareme.worker.art.parameterConvertor.ParameterConvertor.convert;

//import static madgik.exareme.utils.util.ParatemerConvertor.ParameterConvertor.convert;


/**
 * @author Herald Kllapi
 * @author John Chronis<br>
 * @author Vaggelis Nikolopoulos<br>
 * @since 1.0
 */
public class RmiContainer extends RmiRemoteObject<ContainerProxy> implements Container {

    private final ContainerID containerID = null;
    private String ip = null;
    private ConcreteOperatorManager concreteOperatorManager = null;
    private AdaptorManager adaptorManager = null;
    private BufferManager bufferManager = null;
    private DiskManager diskManager = null;
    private NetManager netManager = null;
    private StatisticsManager statisticsManager = null;
    private DataTransferMgrInterface dataTransferManagerDTP = null;
    private JobQueueInterface jobQueueInterface = null;
    private Executor executor = null;
    private ContainerResources resources = null;
    private ContainerStatus containerStatus = null;
    private ContainerQuantumClock quantumClock = null;
    private RegistryUpdateDeamon registryUpdateDeamon = null;
    private EntityName regEntityName = null;
    private DataTransferGateway dataTransfer = null;
    private static final Logger log = Logger.getLogger(RmiContainer.class);
    private HashMap<ConcreteOperatorID, HashMap<String, Pair<String, BufferID>>>
            operatorIDToBufferID;
    private HashSet<String> dataMatOps;
    private HashMap<String, CreateOperatorJob> idToDtOperator;
    private String containerName;

    public RmiContainer(String containerName, ConcreteOperatorManagerInterface cOpMngrIface,
                        BufferManagerInterface bMngrIface, DiskManagerInterface diskMngrIface,
                        NetManagerInterface netMngrIface, AdaptorManagerInterface adptrMngrIface,
                        StatisticsManagerInterface statsMngrIface, JobQueueInterface jobQueueInterface,
                        Executor executor, ContainerResources resources, EntityName regName,
                        ContainerStatus containerStatus, DataTransferMgrInterface dataTransferManagerDTP,
                        ContainerID containerID) throws RemoteException {
        super(NetUtil.getIPv4() + "_container_" + containerName);

        this.ip = NetUtil.getIPv4();
        this.regEntityName = regName;
        this.containerStatus = containerStatus;
        this.jobQueueInterface = jobQueueInterface;
        this.executor = executor;
        this.resources = resources;
        this.operatorIDToBufferID = new HashMap<>();
        this.dataMatOps = new HashSet<>();
        this.idToDtOperator = new HashMap<>();
        this.dataTransferManagerDTP = dataTransferManagerDTP;
        log.debug("\t RmiAdaptorManager...");
        adaptorManager = new RmiAdaptorManager(adptrMngrIface, cOpMngrIface, bMngrIface, regName);

        log.debug("\t RmiBufferManager...");
        bufferManager = new RmiBufferManager(this, bMngrIface, regName);

        log.debug("\t RmiDiskManager...");
        diskManager = new RmiDiskManager(this, diskMngrIface, regName);

        log.debug("\t RmiNetManager");
        netManager = new RmiNetManager(this, netMngrIface, regName);

        log.debug("\t RmiConcreteOperatorManager...");
        concreteOperatorManager =
                new RmiConcreteOperatorManager(cOpMngrIface, regName, dataTransferManagerDTP);

        log.debug("\t RmiStatisticsManager...");
        statisticsManager = new RmiStatisticsManager(statsMngrIface, regName);

        quantumClock = new ContainerQuantumClock(containerID,
                AdpProperties.getCloudProps().getLong("cloud.warnTime") * 1000,
                AdpProperties.getCloudProps().getLong("cloud.quantum") * 1000);
        quantumClock.startDeamon();

        log.debug("Registering MBean...");
        ContainerManagement containerManager = new ContainerManagement(this);
        ManagementUtil.registerMBean(containerManager, "Container");

        log.debug("Create update deamon ...");
        long lifeTime =
                AdpProperties.getArtProps().getLong("art.container.rmi.RmiContainer.lifetime");
        registryUpdateDeamon =
                RegistryUpdateDeamonFactory.createDeamon(this.createProxy(), (long) (0.75 * lifeTime));
        if(lifeTime != 0) {
            registryUpdateDeamon.startDeamon();
        }

        //TODO(DSH): check
        executor
                .setManagers(statisticsManager, concreteOperatorManager, bufferManager, adaptorManager,
                        dataTransferManagerDTP);
        jobQueueInterface
                .setManagers(statisticsManager, concreteOperatorManager, bufferManager, adaptorManager,
                        dataTransferManagerDTP);

        executor.start();
    }

    @Override
    public ContainerStatus getStatus() throws RemoteException {
        return containerStatus;
    }

    @Override
    public final ContainerProxy createProxy() throws RemoteException {
        super.register();
        return new RmiContainerProxy(ip, super.getRegEntryName(), regEntityName);
    }

    @Override
    public ContainerJobResults execJobs(ContainerJobs jobs) throws RemoteException {
        ContainerJobResults results = new ContainerJobResults();
        ContainerJobResult result = null;
        log.debug("Executing " + jobs.getJobs().size() + " Jobs!");
        for (ContainerJob job : jobs.getJobs()) {
            log.debug("Executing Job: " + job.getType().name() + " " + job.toString());
            if (job.getType()
                    == ContainerJobType.createOperator) { // create buffer and link//////////////////create OP
                if (((CreateOperatorJob) job).type
                        == OperatorType.dataTransfer) {//////////////////////////////////////////DataTransfer
                    log.debug("Data transfer op: " + ((CreateOperatorJob) job).operatorName);
                    log.debug("DataTransferParams: " + ((CreateOperatorJob) job).parameters
                            .listParameterNames());
                    log.debug(
                            "DataTransferParamsList: " + ((CreateOperatorJob) job).linkMapParameters);
                    result = jobQueueInterface
                            .addJob(job, ((CreateOperatorJob) job).contSessionID, jobs.sessionID);
                    results.addJobResult(result);
                    continue;
                }
                result = jobQueueInterface
                        .addJob(job, ((CreateOperatorJob) job).contSessionID, jobs.sessionID);
                log.debug("Create Operator: " + ((CreateOperatorJob) job).operatorName);
                HashMap<String, Pair<String, BufferID>> operatorIDToBufferIDInner = null;
                Boolean isRead = null;
                if (((CreateOperatorJob) job).type == OperatorType.dataMaterialization) {
                    //is read data materialization operator?
                    for (madgik.exareme.worker.art.parameter.Parameter param : ((CreateOperatorJob) job).parameters) {
                        if (param.getName().equals(OperatorEntity.CATEGORY_PARAM)) {
                            if (param.getValue().endsWith("_R_" + OperatorCategory.dt)) {
                                isRead = true;
                            } else if (param.getValue().endsWith("_W_" + OperatorCategory.dt)) {
                                isRead = false;
                            } else {
                                throw new RemoteException(
                                        "DataMaterialization operator is neither Read nor Write!");
                            }
                        }
                    }
                    if (isRead == null) {
                        throw new RemoteException(
                                "DataMaterialization operator is neither Read nor Write!");
                    }
                    if (!isRead) {
                        dataMatOps.add(((CreateOperatorJob) job).operatorName);
                    }

                }
                if (((CreateOperatorJob) job).type != OperatorType.dataMaterialization || isRead) {
                    //for all operators except dataMaterialization writers
                    for (Map.Entry<String, LinkedList<Parameter>> entry : ((CreateOperatorJob) job).linkMapParameters
                            .entrySet()) {
                        //for all output links
                        String part = null;
                        for (Parameter p : entry.getValue()) {//TODO(JV) fix perf
                            if (p.name.equals("part")) {
                                part = p.value;
                            }
                        }
                        if (part == null) {
                            throw new RemoteException("part parameter in Operator is null");
                        }

                        //Create Buffer
                        String bufferName =
                                ((CreateOperatorJob) job).operatorName + "_B_" + entry.getKey() + "_P_"
                                        + part;
                        log.debug("Creating buffer: " + bufferName);
                        BufferQoS qos = new BufferQoS();
                        int qos_num = 1; //TODO(JV): check this
                        qos.setRecordCount(qos_num);
                        qos.setSizeMB(qos_num);

                        ContainerJob createBuffer = new CreateBufferJob(bufferName, qos);
                        ((CreateOperatorJobResult) result).SetBufferJobResult(
                                (CreateBufferJobResult) jobQueueInterface
                                        .addJob(createBuffer, ((CreateOperatorJob) job).contSessionID,
                                                jobs.sessionID));
                        if (operatorIDToBufferIDInner == null) {
                            operatorIDToBufferIDInner = new HashMap<>();
                        }
                        operatorIDToBufferIDInner.put(entry.getKey(), new Pair(bufferName,
                                ((CreateOperatorJobResult) result).bufferJobResult.bufferId));

                        log.debug("Creating WriteAdaptor...");
                        ContainerJob createAdapter =
                                new CreateWriteAdaptorJob(((CreateOperatorJobResult) result).opID,
                                        ((CreateOperatorJobResult) result).bufferJobResult.bufferId,
                                        bufferName, //portName == bufferName (????)
                                        convert(entry.getValue()), AdaptorType.LOCAL_ADAPTOR);
                        jobQueueInterface
                                .addJob(createAdapter, ((CreateOperatorJob) job).contSessionID,
                                        jobs.sessionID);
                    }

                    if (((CreateOperatorJob) job).linkMapParameters.size() > 0) {
                        ((CreateOperatorJobResult) result).bufferJobResult
                                .setOp(((CreateOperatorJob) job).operatorName);
                        operatorIDToBufferID.put(((CreateOperatorJobResult) result).opID,
                                operatorIDToBufferIDInner);
                        ((CreateOperatorJobResult) result).bufferJobResult
                                .setOpToBuffer(operatorIDToBufferIDInner);
                        operatorIDToBufferIDInner = null;
                    }
                }
                results.addJobResult(result);


            } else if (job.getType() == ContainerJobType.distributedJobCreateDataflow) {
                result = new CreateDataflowJobResult();
                results.addJobResult(result);

            } else if (job.getType() == ContainerJobType.createOperatorLink) {
                log.trace("Creating operator link...");
                CreateOperatorLinkJob createLinkJob = (CreateOperatorLinkJob) job;
                if (!dataMatOps.contains(createLinkJob.fromConcreteOperatorID.operatorName)) {
                    //From Operator is not dataMaterialization

                    Parameters params = new Parameters();
                    for (Parameter p : createLinkJob.paramList) {
                        params.addParameter(
                                new madgik.exareme.worker.art.parameter.Parameter(p.name, p.value));
                    }
                    String part = null;
                    for (Parameter p : createLinkJob.paramList) {
                        if (p.name.equals("part")) {
                            part = p.value;
                        }
                    }
                    if (part == null) {
                        throw new RemoteException("part parameter in Link is null");
                    }
                    log.trace("Create read link from buffer: " + createLinkJob.bufferName
                            + " to operator " + createLinkJob.toConcreteOperatorID.operatorName);
                    ContainerJob createReadLink = new CreateReadAdaptorJob(createLinkJob.bufferID,
                            createLinkJob.toConcreteOperatorID, createLinkJob.bufferName, params,
                            createLinkJob.adaptorType);
                    result = jobQueueInterface
                            .addJob(createReadLink, ((CreateOperatorLinkJob) job).contSessionID,
                                    jobs.sessionID);
                    results.addJobResult(result);
                } else {
                    result = new CreateReadAdaptorJobResult(null, null);
                    results.addJobResult(result);
                }

            } else {
                result = jobQueueInterface.addJob(job, jobs.contSessionID, jobs.sessionID);
                results.addJobResult(result);
            }

            if (result.hasException()) {
                break;
            }
        }
        return results;
    }

    @Override
    public void stopContainer() throws RemoteException {
        try {
            quantumClock.stopDeamon();
            registryUpdateDeamon.stopDeamon();
            dataTransferManagerDTP.stopDataTransferServer();
            ManagementUtil.unregisterMBean("Container");
            super.unregister();
            log.info("Container succesfully stopped!");
            // TODO(DSD): stop job queue thread
            executor.terminate();
        } catch (RemoteException e) {
            log.debug("Stop Container", e);
            throw new ServerException("Cannot stop container", e);
        }
    }

    @Override
    public void destroyContainerSession(ContainerSessionID contSessionID, PlanSessionID sessionID)
            throws RemoteException {
        log.debug("Destroy all container sessions ... ");

        bufferManager.destroyContainerSession(contSessionID, sessionID);
        adaptorManager.destroyContainerSession(contSessionID, sessionID);
        concreteOperatorManager.destroyContainerSession(contSessionID, sessionID);
        diskManager.destroyContainerSession(contSessionID, sessionID);
        netManager.destroyContainerSession(contSessionID, sessionID);

        statisticsManager.destroyContainerSession(contSessionID, sessionID);

        // TODO(DSD): clean resources
        jobQueueInterface.destroyContainerSession(contSessionID, sessionID);
    }

    @Override
    public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        log.debug("Destroy all plan sessions ... ");

        concreteOperatorManager.destroySessions(sessionID);
        bufferManager.destroySessions(sessionID);
        adaptorManager.destroySessions(sessionID);

        diskManager.destroySessions(sessionID);
        netManager.destroySessions(sessionID);

        statisticsManager.destroySessions(sessionID);
        // TODO(DSD): clean resources
        jobQueueInterface.destroySessions(sessionID);
    }

    @Override
    public void destroyAllSessions() throws RemoteException {
        log.debug("Destroy all sessions ... ");

        bufferManager.destroyAllSessions();
        adaptorManager.destroyAllSessions();
        concreteOperatorManager.destroyAllSessions();
        diskManager.destroyAllSessions();
        netManager.destroyAllSessions();

        statisticsManager.destroyAllSessions();
        // TODO(DSD): clean resources
        jobQueueInterface.destroyAllSessions();
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getContainerName() {
        return containerName;
    }
}
