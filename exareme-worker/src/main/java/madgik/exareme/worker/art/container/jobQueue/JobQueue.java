/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.jobQueue;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobResult;
import madgik.exareme.worker.art.container.Executor;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorManager;
import madgik.exareme.worker.art.container.bufferMgr.BufferManager;
import madgik.exareme.worker.art.container.dataTrasferMgr.DataTransferMgrInterface;
import madgik.exareme.worker.art.container.job.AbstractContainerJob;
import madgik.exareme.worker.art.container.job.StartOperatorJob;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManager;
import madgik.exareme.worker.art.container.resources.ContainerJobResources;
import madgik.exareme.worker.art.container.resources.ContainerResources;
import madgik.exareme.worker.art.container.resources.Resources;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManager;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

/**
 * @author John Chronis<br>
 * @author Vaggelis Nikolopoulos<br>
 */
public class JobQueue implements JobQueueInterface {

    private static final Logger log = Logger.getLogger(JobQueue.class);
    //queue
    public final Semaphore jobQueueSem = new Semaphore(0);
    private final Queue<ContainerJobResult> results;
    private final TreeMap<Resources, Queue<AbstractContainerJob>> jobMap;
    private StatisticsManager statisticsManager;
    private ConcreteOperatorManager concreteOperatorManager;
    private BufferManager bufferManager;
    private AdaptorManager adaptorManager;
    private DataTransferMgrInterface dataTransferManagerDTP;
    private Executor executor;

    public JobQueue() {
        this.results = new LinkedList<>();
        this.jobMap = new TreeMap<>();
    }

    @Override public void setManagers(StatisticsManager statisticsManager,
        ConcreteOperatorManager concreteOperatorManager, BufferManager bufferManager,
        AdaptorManager adaptorManager, DataTransferMgrInterface dataTransferManagerDTP) {
        this.statisticsManager = statisticsManager;
        this.concreteOperatorManager = concreteOperatorManager;
        this.bufferManager = bufferManager;
        this.adaptorManager = adaptorManager;
        this.dataTransferManagerDTP = dataTransferManagerDTP;
    }

    // TODO(DSD) den ta exoyn kanei destroy oi managers ??
    @Override public void destroyContainerSession(ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {

        synchronized (jobMap) {
            for (Entry<Resources, Queue<AbstractContainerJob>> entry : jobMap.entrySet()) {
                for (AbstractContainerJob job : entry.getValue()) {
                    if (job.contSessionID() == containerSessionID && job.sessionID() == sessionID) {
                        entry.getValue().remove(job);
                    }
                }
                if (entry.getValue().isEmpty()) {
                    jobMap.remove(entry.getKey());
                }
            }
        }
    }

    @Override public void destroySessions(PlanSessionID sessionID) throws RemoteException {

        synchronized (jobMap) {
            for (Entry<Resources, Queue<AbstractContainerJob>> entry : jobMap.entrySet()) {
                for (AbstractContainerJob job : entry.getValue()) {
                    if (job.sessionID() == sessionID) {
                        entry.getValue().remove(job);
                    }

                }
                if (entry.getValue().isEmpty()) {
                    jobMap.remove(entry.getKey());
                }
            }
        }
    }

    @Override public void destroyAllSessions() throws RemoteException {
        synchronized (jobMap) {
            jobMap.clear();
        }
    }

    @Override
    public ArrayList<AbstractContainerJob> getNextJob(ContainerResources availableResources) {
        ArrayList<AbstractContainerJob> ret = new ArrayList<>();

        synchronized (jobMap) {
            Entry<Resources, Queue<AbstractContainerJob>> entry;

            while ((entry = jobMap.floorEntry(availableResources)) != null) {
                ret.add(entry.getValue().peek());
                if (entry.getValue().peek().getResources().hasResources()) {
                    availableResources.allocateResources(entry.getValue().
                            peek().getResources(),
                        ((StartOperatorJob) entry.getValue().peek().getJob()).opID);
                }
                entry.getValue().poll();
                if (entry.getValue().isEmpty()) {
                    jobMap.remove(entry.getKey());
                }

            }

        }
        return ret;
    }

    @Override public ContainerJobResult addJob(ContainerJob job,//TODO(jv) clean
        ContainerSessionID contSessionID, PlanSessionID sessionID) throws RemoteException {
        AbstractContainerJob abstactContainerJob = null;
        double mem = 0.0;
        switch (job.getType()) {
            case dataTransferRegister: {
                abstactContainerJob =
                    new AbstractContainerJob(job, new ContainerJobResources(mem), contSessionID,
                        sessionID);
                break;
            }
            case stopOperator: {
                abstactContainerJob =
                    new AbstractContainerJob(job, new ContainerJobResources(mem), contSessionID,
                        sessionID);
                break;
            }
            case createOperator:
                //TODO(DS) na ginooun ena ta create + start operator sto DSimpl
            case getStatistics:
            case destroyOperator:
            case createBuffer:
            case destroyBuffer:
            case createReadAdaptor:
            case createWriteAdaptor: {
                log.debug("ADD:  " + job.getType().name());
                abstactContainerJob =
                    new AbstractContainerJob(job, new ContainerJobResources(mem), contSessionID,
                        sessionID);
                break;
            }
            case startOperator: {
                abstactContainerJob =
                    new AbstractContainerJob(job, new ContainerJobResources(mem), contSessionID,
                        sessionID);
                break;
            }
        }
        ContainerJobResult result = prepareJob(job, contSessionID, sessionID);
        if (hasExec(job)) {
            synchronized (jobMap) {
                if (jobMap.containsKey(abstactContainerJob.getResources())) {
                    jobMap.get(abstactContainerJob.getResources()).
                        add(abstactContainerJob);
                } else {
                    Queue<AbstractContainerJob> jobQueue = new LinkedList<AbstractContainerJob>();
                    jobQueue.add(abstactContainerJob);
                    jobMap.put(abstactContainerJob.getResources(), jobQueue);
                }
            }
            log.debug("Job Added " + job);
        }
        executor.JobAdded();
        return result;
    }

    private ContainerJobResult prepareJob(ContainerJob job, ContainerSessionID contSessionID,
        PlanSessionID sessionID) throws RemoteException {
        switch (job.getType()) {
            case dataTransferRegister: {
                return concreteOperatorManager.prepareJob(job, contSessionID, sessionID);
            }
            case getStatistics: {
                return statisticsManager.prepareJob(job, contSessionID, sessionID);
            }
            case createOperator:
            case startOperator:
            case stopOperator:
            case destroyOperator: {
                return concreteOperatorManager.prepareJob(job, contSessionID, sessionID);
            }
            case createBuffer:
            case destroyBuffer: {
                return bufferManager.prepareJob(job, contSessionID, sessionID);
            }
            case createReadAdaptor:
            case createWriteAdaptor:
            case createOperatorLink: {
                log.debug("Prepare adaptor " + job.getType());
                return adaptorManager.prepareJob(job, contSessionID, sessionID);
            }
        }
        throw new RemoteException("Job type not found: " + job.getType());
    }

    private boolean hasExec(ContainerJob job) {
        switch (job.getType()) {
            case getStatistics: {
                return statisticsManager.hasExec(job);
            }
            case createOperator:
            case startOperator:
            case stopOperator:
            case destroyOperator: {
                return concreteOperatorManager.hasExec(job);
            }
            case createBuffer:
            case destroyBuffer: {
                return bufferManager.hasExec(job);
            }
            case createReadAdaptor:
            case createWriteAdaptor: {
                return adaptorManager.hasExec(job);
            }
        }
        return false;
    }

    public boolean isEmpty() {
        boolean ret;
        synchronized (jobMap) {
            ret = jobMap.isEmpty();
        }
        return ret;
    }

    @Override public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override public void freeResources(ConcreteOperatorID opID) {
        executor.freeResources(opID);
        executor.JobFinish();
    }

}
