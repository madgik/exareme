/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.art;

import madgik.exareme.common.optimizer.OperatorType;

import java.io.Serializable;

/**
 * @author herald
 */
public class ConcreteOperatorStatistics implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String operatorName;
    private final String operatorCategory;
    private final OperatorType operatorType;
    private long startTime_ms = 0;
    private long endTime_ms = 0;
    private Exception exception = null;
    private long userTime_ms = 0;
    private long userCpuTime_ms = 0;
    private long systemTime_ms = 0;
    private long systemCpuTime_ms = 0;
    private long blockTime_ms = 0;
    private long totalTime_ms = 0;
    private long totalCpuTime_ms = 0;
    private int exitCode = -1;
    private Serializable exitMessage = null;

    public ConcreteOperatorStatistics(String operatorName, String operatorCategory,
                                      OperatorType operatorType) {
        this.operatorName = operatorName;
        this.operatorCategory = operatorCategory;
        this.operatorType = operatorType;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public String getOperatorCategory() {
        return operatorCategory;
    }

    public OperatorType getOperatorType() {
        return operatorType;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        synchronized (operatorName) {
            this.exception = exception;
        }
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public Serializable getExitMessage() {
        return exitMessage;
    }

    public void setExitMessage(Serializable exitMessage) {
        this.exitMessage = exitMessage;
    }

    public long getStartTime_ms() {
        return startTime_ms;
    }

    public void setStartTime_ms(long startTime_ms) {
        synchronized (operatorName) {
            this.startTime_ms = startTime_ms;
        }
    }

    public long getEndTime_ms() {
        return endTime_ms;
    }

    public void setEndTime_ms(long endTime_ms) {
        synchronized (operatorName) {
            this.endTime_ms = endTime_ms;
        }
    }

    // TOTAL TIME
    public void setTotalTime_ms(long time, long cpuTime) {
        synchronized (operatorName) {
            totalTime_ms = time;
            this.totalCpuTime_ms = cpuTime;
        }
    }

    public long getTotalTime_ms() {
        synchronized (operatorName) {
            return totalTime_ms;
        }
    }

    public long getTotalCpuTime_ms() {
        synchronized (operatorName) {
            return totalCpuTime_ms;
        }
    }

    // USER TIME
    public void addUserTime_ms(long time, long cpuTime) {
        synchronized (operatorName) {
            userTime_ms += time;
            this.userCpuTime_ms += cpuTime;
        }
    }

    public long getUserTime_ms() {
        synchronized (operatorName) {
            return userTime_ms;
        }
    }

    public long getUserCpuTime_ms() {
        synchronized (operatorName) {
            return userCpuTime_ms;
        }
    }

    // SYSTEM TIME
    public void addSystemTime_ms(long time, long cpuTime) {
        synchronized (operatorName) {
            systemTime_ms += time;
            this.systemCpuTime_ms += cpuTime;
        }
    }

    public long getSystemTime_ms() {
        synchronized (operatorName) {
            return systemTime_ms;
        }
    }

    public long getSystemCpuTime_ms() {
        synchronized (operatorName) {
            return systemCpuTime_ms;
        }
    }

    // BLOCK TIME
    public void addBlockTime_ms(long time) {
        synchronized (operatorName) {
            blockTime_ms += time;
        }
    }

    public long getBlockTime_ms() {
        synchronized (operatorName) {
            return blockTime_ms;
        }
    }
}
