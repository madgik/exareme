/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.art;

import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.utils.units.Metrics;

import java.io.Serializable;
import java.util.*;

/**
 * @author herald
 */
public class PlanSessionStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    public final PlanSessionID sessionID;
    // Containers
    public long containersUsed = 0;
    public long containerSessions = 0;
    // Operators
    public long processingOperatorsCompleted = 0;
    public long totalProcessingOperators = 0;
    public long dataTransferCompleted = 0;
    public long totalDataTransfer = 0;
    public Set<String> running = new HashSet<String>();
    public Set<String> completed = new HashSet<String>();
    public Set<String> error = new HashSet<String>();
    public long operatorsInstantiated = 0;
    public long processingOperatorsInstantiated = 0;
    public long operatorCompleted = 0;
    public long operatorsError = 0;
    // Buffers
    public long buffersCreated = 0;
    public long linksCreated = 0;
    // Time
    public long startTime = 0;
    public long endTime = 0;
    // Exec plan
    public long controlMessagesCount = 0;
    public long independentMessages = 0;
    public long totalEventProcessTime = 0;
    public long maxWaitTime = 0;
    public long maxEventProcessTime = 0;
    public long maxIndependentMsgCount = 0;

    public ArrayList<ContainerSessionStatistics> containerStats =
            new ArrayList<ContainerSessionStatistics>();

    public PlanSessionStatistics(PlanSessionID sessionID) {
        this.sessionID = sessionID;
    }

    public void incrBuffersCreated() {
        buffersCreated++;
    }

    public long buffersCreated() {
        return buffersCreated;
    }

    public void incrLinksCreated() {
        linksCreated++;
    }

    public long linksCreated() {
        return linksCreated;
    }

    public void incrContainersUsed() {
        containersUsed++;
    }

    public void incrOperatorsInstantiated() {
        operatorsInstantiated++;
    }

    public void incrContainerSessions() {
        containerSessions++;
    }

    public void incrOperatorCompleted() {
        operatorCompleted++;
    }

    public void incrDataTransferCompleted() {
        dataTransferCompleted++;
    }

    public long operatorCompleted() {
        return operatorCompleted;
    }

    public void incrOperatorsError() {
        operatorsError++;
    }

    public void setTotalProcessingOperators(long totalProcessingOperators) {
        this.totalProcessingOperators = totalProcessingOperators;
    }

    public long totalProcessingOperators() {
        return totalProcessingOperators;
    }

    public long getTotalDataTransfer() {
        return totalDataTransfer;
    }

    public void setTotalDataTransfer(long totalDataTransfer) {
        this.totalDataTransfer = totalDataTransfer;
    }

    public long getDataTransferCompleted() {
        return dataTransferCompleted;
    }

    public void setDataTransferCompleted(long dataTransferCompleted) {
        this.dataTransferCompleted = dataTransferCompleted;
    }

    public long processingOperatorsInstantiated() {
        return processingOperatorsInstantiated;
    }

    public void IncrProcessingOperatorsInstantiated() {
        processingOperatorsInstantiated++;
    }

    public long processingOperatorsCompleted() {
        return processingOperatorsCompleted;
    }

    public void incrProcessingOperatorsCompleted() {
        processingOperatorsCompleted++;
    }

    public void incrIndependentMessages() {
        independentMessages++;
    }

    public long independentMessages() {
        return independentMessages;
    }

    public long totalEventProcessTime() {
        return totalEventProcessTime;
    }

    public void addTotalEventProcessTime(long time, long waitTime) {
        totalEventProcessTime += time;
        if (maxEventProcessTime < time) {
            maxEventProcessTime = time;
        }
        if (maxWaitTime < waitTime) {
            maxWaitTime = waitTime;
        }
    }

    public long maxIndependentMsgCount() {
        return maxIndependentMsgCount;
    }

    public void setXaxIndependentMsgCount(long count) {
        if (maxIndependentMsgCount < count) {
            maxIndependentMsgCount = count;
        }
    }

    public long maxWaitTime() {
        return maxWaitTime;
    }

    public long maxEventProcessTime() {
        return maxEventProcessTime;
    }

    public void incrControlMessagesCount() {
        controlMessagesCount++;
    }

    public void incrControlMessagesCountBy(long delta) {
        controlMessagesCount += delta;
    }

    public long controlMessagesCount() {
        return controlMessagesCount;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long startTime() {
        return startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long endTime() {
        return endTime;
    }

    public double computeMoney(RunTimeParameters params) {
        return computeMoney(params, true);
    }

    public double computeMoney(RunTimeParameters params, boolean frag) {
        HashMap<String, BitSet> containerQuantaMap = new HashMap<String, BitSet>();
        for (ContainerSessionStatistics cStats : containerStats) {
            BitSet usedQuanta = containerQuantaMap.get(cStats.containerName);
            if (usedQuanta == null) {
                usedQuanta = new BitSet();
                containerQuantaMap.put(cStats.containerName, usedQuanta);
            }
            for (ConcreteOperatorStatistics opStats : cStats.operators) {
                double start = (opStats.getStartTime_ms() - startTime) / Metrics.MiliSec;
                double end = (opStats.getEndTime_ms() - startTime) / Metrics.MiliSec;
                int startQuantum = (int) Math.floor(start / params.quantum__SEC);
                if (startQuantum < 0) {
                    startQuantum = 0;
                }
                int endQuantum = (int) Math.ceil(end / params.quantum__SEC);
                if (endQuantum < 0) {
                    startQuantum = 0;
                    endQuantum = 0;
                }
                usedQuanta.set(startQuantum, endQuantum);
            }
        }
        // Compute the money from the used quanta
        double money = 0.0;
        for (BitSet quanta : containerQuantaMap.values()) {
            money += quanta.cardinality();
        }
        return money;
    }
}
