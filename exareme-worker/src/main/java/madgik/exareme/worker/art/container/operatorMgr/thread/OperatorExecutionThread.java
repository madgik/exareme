/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.operatorMgr.thread;

import madgik.exareme.common.art.entity.OperatorImplementationEntity;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.worker.art.concreteOperator.AbstractOperatorImpl;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.jobQueue.JobQueueInterface;
import org.apache.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.util.Date;

/**
 * @author Herald Kllapi <br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class OperatorExecutionThread extends Thread {

    private static final Logger log = Logger.getLogger(OperatorExecutionThread.class);
    public JobQueueInterface jobQueueInterface;
    ConcreteOperatorID opID;
    private AbstractOperatorImpl abstractOperator = null;
    private boolean shutdown = false;


    public OperatorExecutionThread(OperatorImplementationEntity operator,
                                   AbstractOperatorImpl abstractOperator, JobQueueInterface jobQueueInterface,
                                   ConcreteOperatorID opID) {
        this.setName(operator.getClassName());
        this.abstractOperator = abstractOperator;
        this.jobQueueInterface = jobQueueInterface;
        this.opID = opID;
    }

    public OperatorExecutionThread(OperatorImplementationEntity operator,
                                   AbstractOperatorImpl abstractOperator) {
        this.setName(operator.getClassName());
        this.abstractOperator = abstractOperator;
        this.jobQueueInterface = null;
        this.opID = null;
    }

    public void shutdown() {
        this.shutdown = true;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        try {

            abstractOperator.getSessionManager().getOperatorStatistics().setStartTime_ms(start);
            abstractOperator.run();
            abstractOperator.start = start;
            if (!abstractOperator.getSessionManager().getOperatorType()
                    .equals(OperatorType.dataTransfer)) {//dt ops do not terminate now
                abstractOperator.exit(0);
                abstractOperator.finalizeOperator();

                long end = System.currentTimeMillis();
                abstractOperator.getSessionManager().getOperatorStatistics().setEndTime_ms(end);
                abstractOperator.getSessionManager().getOperatorStatistics()
                        .setExitCode(abstractOperator.getExitCode());
                abstractOperator.getSessionManager().getOperatorStatistics()
                        .setExitMessage(abstractOperator.getExitMessage());

                abstractOperator.getSessionManager().getOperatorStatistics().
                        setTotalTime_ms(end - start,
                                ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() / 1000000);

                //                abstractOperator.getSessionManager().getSessionReportID().reportManagerProxy
                //                    .operatorSuccess(abstractOperator.getSessionManager().getOpID(),
                //                        abstractOperator.getExitCode(), abstractOperator.getExitMessage(),
                //                        new Date(), abstractOperator.getSessionManager().getContainerID());

                //log.trace("Will report operator success: " + abstractOperator.getSessionManager().getOpID().operatorName
                //        + "\nGID: " +abstractOperator.getSessionManager().getContainerSessionID().getLongId()
                //            + " OpsInGroup: "+abstractOperator.getParameterManager().getParameter("OpsInGroup").iterator().next().getValue());
                abstractOperator.getOperatorGroupManager()
                        .setTerminated(abstractOperator.getSessionManager().getSessionID(),
                                abstractOperator.getSessionManager().getContainerSessionID(),
                                abstractOperator.getSessionManager().getOpID().operatorName);
                int operatorsInGroup = -1;
                try {
                    operatorsInGroup = Integer.parseInt(
                            abstractOperator.getParameterManager().getParameter("OpsInGroup").iterator()
                                    .next().getValue());
                } catch (Exception e) {  //Ignore exception and report operator terminated
                    abstractOperator.getSessionManager().getSessionReportID().reportManagerProxy
                            .operatorSuccess(abstractOperator.getSessionManager().getOpID(),
                                    abstractOperator.getExitCode(), abstractOperator.getExitMessage(),
                                    new Date(), abstractOperator.getSessionManager().getContainerID(),
                                    false);
                }
                if (operatorsInGroup != -1) {
                    synchronized (abstractOperator.getOperatorGroupManager()) {
                        if (abstractOperator.getOperatorGroupManager()
                                .getNumberOfTerminatedOperators(
                                        abstractOperator.getSessionManager().getSessionID(),
                                        abstractOperator.getSessionManager().getContainerSessionID())
                                == operatorsInGroup) {
                            //This is the last in group operator
                            // log.trace("Terminated operators map: " + abstractOperator.getOperatorGroupManager().toString());
                            log.trace(
                                    "This is the last operator in group. OPName: " + abstractOperator
                                            .getSessionManager().getOpID().operatorName + " GROUPID: "
                                            + abstractOperator.getSessionManager().getContainerSessionID()
                                            .getLongId());
                            abstractOperator.getOperatorGroupManager()
                                    .clear(abstractOperator.getSessionManager().getSessionID(),
                                            abstractOperator.getSessionManager().getContainerSessionID());

                            abstractOperator.getSessionManager()
                                    .getSessionReportID().reportManagerProxy
                                    .operatorSuccess(abstractOperator.getSessionManager().getOpID(),
                                            abstractOperator.getExitCode(),
                                            abstractOperator.getExitMessage(), new Date(),
                                            abstractOperator.getSessionManager().getContainerID(), true);

                        } else {
                            log.trace("This is NOT the last operator in group. OPName: "
                                    + abstractOperator.getSessionManager().getOpID().operatorName
                                    + " GROUPID: " + abstractOperator.getSessionManager()
                                    .getContainerSessionID().getLongId());
                        }
                    }
                }

                log.trace("Terminated operators map: " + abstractOperator.getOperatorGroupManager()
                        .toString());
            }
        } catch (Exception exception) {
            if (shutdown == false) {
                abstractOperator.getSessionManager().getOperatorStatistics()
                        .setException(exception);
                abstractOperator.error(exception);
            }
        }
        if (jobQueueInterface != null && !abstractOperator.getSessionManager().getOperatorType()
                .equals(OperatorType.dataTransfer)) {
            jobQueueInterface.freeResources(opID);
        } else {
            abstractOperator.jobQueueInterface = jobQueueInterface;
        }
        // TODO(DSD): free resources
        if (shutdown == false) {
            long end = System.currentTimeMillis();
            abstractOperator.getSessionManager().getOperatorStatistics().setEndTime_ms(end);
            abstractOperator.getSessionManager().getOperatorStatistics()
                    .setTotalTime_ms(end - start,
                            ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() / 1000000);
        }
    }
}
