/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.rmi;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.Container;
import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobResult;
import madgik.exareme.worker.art.container.buffer.BufferID;
import madgik.exareme.worker.art.container.bufferMgr.BufferManager;
import madgik.exareme.worker.art.container.bufferMgr.BufferManagerInterface;
import madgik.exareme.worker.art.container.job.CreateBufferJob;
import madgik.exareme.worker.art.container.job.CreateBufferJobResult;
import madgik.exareme.worker.art.container.job.DestroyBufferJob;
import madgik.exareme.worker.art.container.job.DestroyBufferJobResult;

import java.rmi.RemoteException;

/**
 * University of Athens / Department of Informatics and Telecommunications.
 *
 * @author Herald Kllapi <br>
 * @author John Chronis<br>
 * @author Vaggelis Nikolopoulos<br>
 * @since 1.0
 */
public class RmiBufferManager implements BufferManager {

    private Container container = null;
    private BufferManagerInterface bufferManagerInterface = null;
    private EntityName regEntityName = null;

    public RmiBufferManager(Container container, BufferManagerInterface bufferManagerInterface,
        EntityName regEntityName) throws RemoteException {
        this.container = container;
        this.bufferManagerInterface = bufferManagerInterface;
        this.regEntityName = regEntityName;
    }

    @Override
    public ContainerJobResult prepareJob(ContainerJob job, ContainerSessionID contSessionID,
        PlanSessionID sessionID) throws RemoteException {
        switch (job.getType()) {
            case createBuffer: {
                return createBuffer((CreateBufferJob) job, contSessionID, sessionID);
            }
            case destroyBuffer: {
                return destroyBuffer((DestroyBufferJob) job, contSessionID, sessionID);
            }
        }
        throw new RemoteException("Job type not supported: " + job.getType());
    }

    @Override public boolean hasExec(ContainerJob job) {
        return false;
    }

    @Override public void execJob(ContainerJob job, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        //TODO(DSQ)
    }

    private CreateBufferJobResult createBuffer(CreateBufferJob job,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        BufferID bufferID = bufferManagerInterface
            .createBuffer(job.bufferName, job.quality, containerSessionID, sessionID);

        bufferID.session = container.createProxy().createSession(containerSessionID, sessionID);

        return new CreateBufferJobResult(bufferID);
    }

    private DestroyBufferJobResult destroyBuffer(DestroyBufferJob job,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        bufferManagerInterface.destroyBuffer(job.bufferID, containerSessionID, sessionID);
        return new DestroyBufferJobResult();
    }

    @Override public void destroyContainerSession(ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        bufferManagerInterface.destroyContainerSession(containerSessionID, sessionID);
    }

    @Override public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        bufferManagerInterface.destroySessions(sessionID);
    }

    @Override public void destroyAllSessions() throws RemoteException {
        bufferManagerInterface.destroyAllSessions();
    }

    @Override public void stopManager() throws RemoteException {
    }

}
