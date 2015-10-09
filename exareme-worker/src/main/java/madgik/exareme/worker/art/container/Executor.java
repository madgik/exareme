/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorManager;
import madgik.exareme.worker.art.container.bufferMgr.BufferManager;
import madgik.exareme.worker.art.container.dataTrasferMgr.DataTransferMgrInterface;
import madgik.exareme.worker.art.container.job.AbstractContainerJob;
import madgik.exareme.worker.art.container.jobQueue.JobQueueInterface;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManager;
import madgik.exareme.worker.art.container.resources.ContainerResources;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManager;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

/**
 * @author John Chronis<br>
 * @author Vaggelis Nikolopoulos<br>
 */
public class Executor extends Thread {

    private static final Logger log = Logger.getLogger(Executor.class);
    public final Semaphore sem = new Semaphore(1);
    private final ContainerResources resources;

    private boolean stop;
    private JobQueueInterface jobQueue;
    private StatisticsManager statisticsManager;
    private ConcreteOperatorManager concreteOperatorManager;
    private BufferManager bufferManager;
    private AdaptorManager adaptorManager;
    private DataTransferMgrInterface dataTransferManagerDTP;

    Executor(ContainerResources resources) {
        this.resources = resources;//FIX
        stop = false;
    }

    public void freeResources(ConcreteOperatorID opID) {
        resources.freeResources(opID);
    }

    public void setManagers(StatisticsManager statisticsManager,
        ConcreteOperatorManager concreteOperatorManager, BufferManager bufferManager,
        AdaptorManager adaptorManager, DataTransferMgrInterface dataTransferManagerDTP) {
        this.statisticsManager = statisticsManager;
        this.concreteOperatorManager = concreteOperatorManager;
        this.bufferManager = bufferManager;
        this.adaptorManager = adaptorManager;
        this.dataTransferManagerDTP = dataTransferManagerDTP;
    }

    @Override public void run() {
        ArrayList<AbstractContainerJob> jobs = null;
        while (!stop) {
            try {
                sem.acquire();
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(Executor.class.getName()).
                    log(Level.SEVERE, null, ex);
            }
            jobs = jobQueue.getNextJob(resources);
            if (jobs != null) {
                for (AbstractContainerJob job : jobs) {
                    log.debug("Starting Job " + job + " " + job.getJob());
                    log.debug("Free memory: " + resources.getResourceArray()[0]);

                    try {
                        handleJob(job.getJob(), job.contSessionID(), job.sessionID());
                    } catch (RemoteException ex) {
                        java.util.logging.Logger.getLogger(Executor.class.getName()).
                            log(Level.SEVERE, null, ex);
                    }

                }
            }
        }
    }

    public void JobFinish() {
        sem.release();
    }

    public void JobAdded() {
        sem.release();
    }

    public void terminate() {//use at the right time
        stop = true;
        sem.release();
    }

    public void setJobQueue(JobQueueInterface jobQueue) {
        this.jobQueue = jobQueue;
    }

    private void handleJob(ContainerJob job, ContainerSessionID contSessionID,
        PlanSessionID sessionID) throws RemoteException {

        switch (job.getType()) {
            case getStatistics: {
                statisticsManager.execJob(job, contSessionID, sessionID);
                return;
            }
            case createOperator:
            case startOperator:
            case stopOperator:
            case destroyOperator: {
                log.debug("concreteOperatorManager.execJob  ");
                concreteOperatorManager.execJob(job, contSessionID, sessionID);
                return;
            }
            case createBuffer:
            case destroyBuffer: {
                bufferManager.execJob(job, contSessionID, sessionID);
                return;
            }
            case createReadAdaptor:
            case createWriteAdaptor: {
                adaptorManager.execJob(job, contSessionID, sessionID);
                return;
            }
        }
        throw new RemoteException("Job type not found: " + job.getType());
    }
}
