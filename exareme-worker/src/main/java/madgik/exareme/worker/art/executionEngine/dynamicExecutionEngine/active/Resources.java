/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active;

import madgik.exareme.utils.check.Check;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;

import java.text.DecimalFormat;

/**
 * @author herald
 */
public class Resources {
    private static final DecimalFormat DF = new DecimalFormat("#.##");
    private final Object lock = new Object();
    private int memory = 100;

    public Resources() {
    }

    public boolean hasAvailable(OperatorEntity op) {
        return memory >= op.memory;
    }

    public boolean accuireIfAvailable(OperatorEntity op) {
        synchronized (lock) {
            if (memory >= op.memory) {
                memory -= op.memory;
                return true;
            }
            return false;
        }
    }

    public void releaseMemory(OperatorEntity op) {
        synchronized (lock) {
            memory += op.memory;
            try {
                Check.True(memory <= 100, "Memory not set correctly: " + memory);
            } catch (RuntimeException e){
                memory = 100;
            }
        }
    }

    @Override public String toString() {
        synchronized (lock) {
            return DF.format(memory);
        }
    }
}
