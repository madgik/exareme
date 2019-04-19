/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.rmi;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.association.Triple;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobResult;
import madgik.exareme.worker.art.container.ContainerJobResults;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.adaptor.*;
import madgik.exareme.worker.art.container.adaptor.rmi.RmiReadSocketStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.rmi.RmiReadStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.rmi.RmiWriteSocketStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.rmi.RmiWriteStreamAdaptor;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorManager;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorManagerInterface;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorType;
import madgik.exareme.worker.art.container.buffer.BufferID;
import madgik.exareme.worker.art.container.buffer.CombinedBuffer;
import madgik.exareme.worker.art.container.bufferMgr.BufferManagerInterface;
import madgik.exareme.worker.art.container.job.CreateReadAdaptorJob;
import madgik.exareme.worker.art.container.job.CreateReadAdaptorJobResult;
import madgik.exareme.worker.art.container.job.CreateWriteAdaptorJob;
import madgik.exareme.worker.art.container.job.CreateWriteAdaptorJobResult;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManagerInterface;
import madgik.exareme.worker.art.parameter.Parameters;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

/**
 * University of Athens / Department of Informatics and Telecommunications.
 *
 * @author Herald Kllapi <br>
 * @author John Chronis<br>
 * @author Vaggelis Nikolopoulos<br>
 * @since 1.0
 */
public class RmiAdaptorManager implements AdaptorManager {

    private static final Logger log = Logger.getLogger(RmiAdaptorManager.class);
    private AdaptorManagerInterface adaptorManagerInterface = null;
    private ConcreteOperatorManagerInterface concreteOperatorManager = null;
    private BufferManagerInterface bufferManager = null;
    private EntityName regEntityName = null;

    public RmiAdaptorManager(AdaptorManagerInterface adaptorManagerInterface,
                             ConcreteOperatorManagerInterface concreteOperatorManager,
                             BufferManagerInterface bufferManager, EntityName regEntityName) throws RemoteException {
        this.adaptorManagerInterface = adaptorManagerInterface;
        this.concreteOperatorManager = concreteOperatorManager;
        this.bufferManager = bufferManager;
        this.regEntityName = regEntityName;
    }

    @Override
    public ContainerJobResult prepareJob(ContainerJob job, ContainerSessionID containerSessionID,
                                         PlanSessionID sessionID) throws RemoteException {
        switch (job.getType()) {
            case createReadAdaptor:
            case createOperatorLink: {
                return createReadAdaptor((CreateReadAdaptorJob) job, containerSessionID, sessionID);
            }
            case createWriteAdaptor: {
                return createWriteAdaptor((CreateWriteAdaptorJob) job, containerSessionID,
                        sessionID);
            }
        }
        throw new RemoteException("Job type not found: " + job.getType());
    }

    @Override
    public boolean hasExec(ContainerJob job) {
        return false;
    }

    @Override
    public void execJob(ContainerJob job, ContainerSessionID containerSessionID,
                        PlanSessionID sessionID) throws RemoteException {

    }

    private CreateReadAdaptorJobResult createReadAdaptor(CreateReadAdaptorJob job,
                                                         ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        AdaptorID adaptorID = null;
        CombinedReadAdaptorProxy proxy = null;
        log.debug("Create Read Adaptor: " + job.adaptorName + " port: " + job.portName);
        if (job.concreteOperatorId == null) {
            proxy = createReadAdaptorProxy(job.bufferID, job.type, containerSessionID, sessionID);
        } else {
            adaptorID = createReadAdaptor(job.bufferID, job.concreteOperatorId, job.adaptorName,
                    job.portName, job.parameters, job.type, containerSessionID, sessionID);
        }

        return new CreateReadAdaptorJobResult(adaptorID, proxy);
    }

    private CreateWriteAdaptorJobResult createWriteAdaptor(CreateWriteAdaptorJob job,
                                                           ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        AdaptorID adaptorID = null;
        CombinedWriteAdaptorProxy proxy = null;
        log.debug("Create Write Adaptor: " + job.portName);
        if (job.concreteOperatorId == null) {
            proxy = createWriteAdaptorProxy(job.bufferID, job.type, containerSessionID, sessionID);
        } else {
            adaptorID = createWriteAdaptor(job.concreteOperatorId, job.bufferID, job.adaptorName,
                    job.portName, job.parameters, job.type, containerSessionID, sessionID);
        }
        return new CreateWriteAdaptorJobResult(adaptorID, proxy);
    }

    private AdaptorID createReadAdaptor(BufferID bufferID, ConcreteOperatorID concreteOperatorId,
                                        String adaptorName, String portName, Parameters parameters, AdaptorType type,
                                        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        switch (type) {
            case LOCAL_ADAPTOR:
                return createLocalReadAdaptor(bufferID, concreteOperatorId, adaptorName, portName,
                        parameters, containerSessionID, sessionID);
            case REMOTE_ADAPTOR:
                return createRemoteReadAdaptor(bufferID, concreteOperatorId, adaptorName, portName,
                        parameters, containerSessionID, sessionID);
        }
        return null;
    }

    private AdaptorID createWriteAdaptor(ConcreteOperatorID concreteOperatorId, BufferID bufferID,
                                         String adaptorName, String portName, Parameters parameters, AdaptorType type,
                                         ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        switch (type) {
            case LOCAL_ADAPTOR:
                return createLocalWriteAdaptor(concreteOperatorId, bufferID, adaptorName, portName,
                        parameters, containerSessionID, sessionID);
            case REMOTE_ADAPTOR:
                return createRemoteWriteAdaptor(concreteOperatorId, bufferID, adaptorName, portName,
                        parameters, containerSessionID, sessionID);
        }
        return null;
    }

    private CombinedReadAdaptorProxy createReadAdaptorProxy(BufferID bufferID, AdaptorType type,
                                                            ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        switch (type) {
            case LOCAL_ADAPTOR:
                return createLocalReadAdaptorProxy(bufferID, containerSessionID, sessionID);
            case REMOTE_ADAPTOR:
                return createRemoteReadAdaptorProxy(bufferID, containerSessionID, sessionID);
        }
        return null;
    }

    private CombinedWriteAdaptorProxy createWriteAdaptorProxy(BufferID bufferID, AdaptorType type,
                                                              ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        switch (type) {
            case LOCAL_ADAPTOR:
                return createLocalWriteAdaptorProxy(bufferID, containerSessionID, sessionID);
            case REMOTE_ADAPTOR:
                return createRemoteWriteAdaptorProxy(bufferID, containerSessionID, sessionID);
        }
        return null;
    }

    @Override
    public void destroyContainerSession(ContainerSessionID containerSessionID,
                                        PlanSessionID sessionID) throws RemoteException {
        Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> toDel =
                adaptorManagerInterface.destroyContainerSession(containerSessionID, sessionID);
        destroyAll(toDel);
    }

    @Override
    public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> toDel =
                adaptorManagerInterface.destroySessions(sessionID);
        destroyAll(toDel);
    }

    @Override
    public void destroyAllSessions() throws RemoteException {
        Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> toDel =
                adaptorManagerInterface.destroyAllSessions();
        destroyAll(toDel);
    }

    @Override
    public void stopManager() throws RemoteException {
    }

    private AdaptorID createRemoteReadAdaptor(BufferID bufferId,
                                              ConcreteOperatorID concreteOperatorId, String adaptorName, String portName,
                                              Parameters parameters, ContainerSessionID containerSessionID, PlanSessionID sessionID)
            throws RemoteException {
        ContainerJobs jobs = new ContainerJobs();
        jobs.addJob(new CreateReadAdaptorJob(bufferId, AdaptorType.REMOTE_ADAPTOR));
        ContainerJobResults results = bufferId.session.execJobs(jobs);

        CombinedReadAdaptorProxy adaptorProxy =
                ((CreateReadAdaptorJobResult) results.getJobResults().get(0)).proxy;

        ReadRmiStreamAdaptor streamAdaptor = adaptorProxy.readRmiStreamAdaptorProxy.connect();
        ReadSocketStreamAdaptor streamAdaptor2 =
                adaptorProxy.readSocketStreamAdaptorProxy.connect();

        concreteOperatorManager
                .addReadAdaptor(adaptorProxy, adaptorName, portName, parameters, true,
                        concreteOperatorId, containerSessionID, sessionID);

        AdaptorID id = adaptorManagerInterface
                .addReadAdaptor(new CombinedReadAdaptor(streamAdaptor, streamAdaptor2),
                        containerSessionID, sessionID);

        return id;
    }

    private void destroyAll(
            Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> toDelete)
            throws RemoteException {
        for (CombinedReadAdaptor adaptor : toDelete.b) {
            try {
                RmiReadStreamAdaptor sa = (RmiReadStreamAdaptor) adaptor.readStreamAdaptor;
                sa.unregister();
            } catch (Exception _) {
            }
            try {
                RmiReadSocketStreamAdaptor sa =
                        (RmiReadSocketStreamAdaptor) adaptor.readSocketStreamAdaptor;
                sa.unregister();
            } catch (Exception _) {
            }
        }
        for (CombinedWriteAdaptor adaptor : toDelete.c) {
            try {
                RmiWriteStreamAdaptor sa = (RmiWriteStreamAdaptor) adaptor.writeStreamAdaptor;
                sa.unregister();
            } catch (Exception _) {
            }
            try {
                RmiWriteSocketStreamAdaptor sa =
                        (RmiWriteSocketStreamAdaptor) adaptor.writeStreamAdaptor2;
                sa.unregister();
            } catch (Exception _) {
            }
        }
    }

    private AdaptorID createLocalReadAdaptor(BufferID bufferId,
                                             ConcreteOperatorID concreteOperatorId, String adaptorName, String portName,
                                             Parameters parameters, ContainerSessionID containerSessionID, PlanSessionID sessionID)
            throws RemoteException {
        CombinedBuffer buffer =
                bufferManager.getLocalBuffer(bufferId, containerSessionID, sessionID);

        ReadRmiStreamAdaptor rmiStreamAdaptor =
                new RmiReadStreamAdaptor(UUID.randomUUID().toString(), buffer.stream, regEntityName);

        ReadSocketStreamAdaptor socketStreamAdaptor =
                new RmiReadSocketStreamAdaptor(UUID.randomUUID().toString(),
                        buffer.socket.getNetEntityName(), regEntityName);

        CombinedReadAdaptorProxy adaptor =
                new CombinedReadAdaptorProxy(rmiStreamAdaptor.createProxy(),
                        socketStreamAdaptor.createProxy());

        concreteOperatorManager
                .addReadAdaptor(adaptor, adaptorName, portName, parameters, false, concreteOperatorId,
                        containerSessionID, sessionID);

        AdaptorID id = adaptorManagerInterface
                .addReadAdaptor(new CombinedReadAdaptor(rmiStreamAdaptor, socketStreamAdaptor),
                        containerSessionID, sessionID);

        return id;
    }

    private AdaptorID createRemoteWriteAdaptor(ConcreteOperatorID concreteOperatorId,
                                               BufferID bufferId, String adaptorName, String portName, Parameters parameters,
                                               ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        ContainerJobs jobs = new ContainerJobs();
        jobs.addJob(new CreateWriteAdaptorJob(bufferId, AdaptorType.REMOTE_ADAPTOR));
        ContainerJobResults results = bufferId.session.execJobs(jobs);

        CombinedWriteAdaptorProxy adaptorProxy =
                ((CreateWriteAdaptorJobResult) results.getJobResults().get(0)).proxy;

        WriteRmiStreamAdaptor rmiStreamAdaptor = adaptorProxy.writeRmiStreamAdaptorProxy.connect();
        WriteSocketStreamAdaptor socketStreamAdaptor =
                adaptorProxy.writeSocketStreamAdaptorProxy.connect();

        concreteOperatorManager
                .addWriteAdaptor(adaptorProxy, adaptorName, portName, parameters, true,
                        concreteOperatorId, containerSessionID, sessionID);

        AdaptorID id = adaptorManagerInterface
                .addWriteAdaptor(new CombinedWriteAdaptor(rmiStreamAdaptor, socketStreamAdaptor),
                        containerSessionID, sessionID);

        return id;
    }

    private AdaptorID createLocalWriteAdaptor(ConcreteOperatorID concreteOperatorId,
                                              BufferID bufferId, String adaptorName, String portName, Parameters parameters,
                                              ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        CombinedBuffer buffer =
                bufferManager.getLocalBuffer(bufferId, containerSessionID, sessionID);

        WriteRmiStreamAdaptor rmiStreamAdaptor =
                new RmiWriteStreamAdaptor(UUID.randomUUID().toString(), buffer.stream, regEntityName);

        WriteSocketStreamAdaptor socketStreamAdaptor =
                new RmiWriteSocketStreamAdaptor(UUID.randomUUID().toString(), buffer.socket,
                        regEntityName);

        concreteOperatorManager.addWriteAdaptor(
                new CombinedWriteAdaptorProxy(rmiStreamAdaptor.createProxy(),
                        socketStreamAdaptor.createProxy()), adaptorName, portName, parameters, false,
                concreteOperatorId, containerSessionID, sessionID);

        AdaptorID id = adaptorManagerInterface
                .addWriteAdaptor(new CombinedWriteAdaptor(rmiStreamAdaptor, socketStreamAdaptor),
                        containerSessionID, sessionID);

        return id;
    }

    private CombinedReadAdaptorProxy createLocalReadAdaptorProxy(BufferID bufferId,
                                                                 ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        CombinedBuffer buffer =
                bufferManager.getLocalBuffer(bufferId, containerSessionID, sessionID);

        ReadRmiStreamAdaptor rmiStreamAdaptor =
                new RmiReadStreamAdaptor(UUID.randomUUID().toString(), buffer.stream, regEntityName);

        ReadSocketStreamAdaptor socketStreamAdaptor =
                new RmiReadSocketStreamAdaptor(UUID.randomUUID().toString(),
                        buffer.socket.getNetEntityName(), regEntityName);

        AdaptorID id = adaptorManagerInterface
                .addReadAdaptor(new CombinedReadAdaptor(rmiStreamAdaptor, socketStreamAdaptor),
                        containerSessionID, sessionID);

        return new CombinedReadAdaptorProxy(rmiStreamAdaptor.createProxy(),
                socketStreamAdaptor.createProxy());
    }

    private CombinedReadAdaptorProxy createRemoteReadAdaptorProxy(BufferID bufferId,
                                                                  ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        CombinedReadAdaptorProxy adaptorProxy =
                createLocalReadAdaptorProxy(bufferId, containerSessionID, sessionID);

        AdaptorID id = adaptorManagerInterface.addReadAdaptor(
                new CombinedReadAdaptor(adaptorProxy.readRmiStreamAdaptorProxy.connect(),
                        adaptorProxy.readSocketStreamAdaptorProxy.connect()), containerSessionID,
                sessionID);

        return adaptorProxy;
    }

    private CombinedWriteAdaptorProxy createLocalWriteAdaptorProxy(BufferID bufferId,
                                                                   ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        CombinedBuffer buffer =
                bufferManager.getLocalBuffer(bufferId, containerSessionID, sessionID);

        WriteRmiStreamAdaptor rmiStreamAdaptor =
                new RmiWriteStreamAdaptor(UUID.randomUUID().toString(), buffer.stream, regEntityName);

        WriteSocketStreamAdaptor socketStreamAdaptor =
                new RmiWriteSocketStreamAdaptor(UUID.randomUUID().toString(), buffer.socket,
                        regEntityName);

        AdaptorID id = adaptorManagerInterface
                .addWriteAdaptor(new CombinedWriteAdaptor(rmiStreamAdaptor, socketStreamAdaptor),
                        containerSessionID, sessionID);

        return new CombinedWriteAdaptorProxy(rmiStreamAdaptor.createProxy(),
                socketStreamAdaptor.createProxy());
    }

    private CombinedWriteAdaptorProxy createRemoteWriteAdaptorProxy(BufferID bufferId,
                                                                    ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        CombinedWriteAdaptorProxy adaptorProxy =
                createLocalWriteAdaptorProxy(bufferId, containerSessionID, sessionID);

        AdaptorID id = adaptorManagerInterface.addWriteAdaptor(
                new CombinedWriteAdaptor(adaptorProxy.writeRmiStreamAdaptorProxy.connect(),
                        adaptorProxy.writeSocketStreamAdaptorProxy.connect()), containerSessionID,
                sessionID);

        return adaptorProxy;
    }

}
