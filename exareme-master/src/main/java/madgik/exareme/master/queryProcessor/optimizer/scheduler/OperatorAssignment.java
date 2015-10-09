/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduler;

import madgik.exareme.common.optimizer.OperatorBehavior;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;

import java.io.Serializable;

/**
 * @author Herald Kllapi <br>
 * @since 1.0
 */
public class OperatorAssignment implements Serializable {
    private static final long serialVersionUID = 1L;
    public final boolean dataTransfer;
    public final String operatorName;
    public final OperatorBehavior behavior;
    private final int opID;
    public int container;

    /* Initial properties */
    public double processTime;
    public double cpuUtil;
    public double memory;

    /* Estimated behavior */
    public double start;
    public double startInQuantums;
    public double end;
    public double endInQuantums;

    public OperatorAssignment(ConcreteOperator co, int container) {
        this(co, container, false);
    }

    public OperatorAssignment(ConcreteOperator co, int container, boolean dataTransfer) {
        this.opID = co.opID;
        this.operatorName = co.operatorName;
        this.dataTransfer = dataTransfer;
        this.behavior = co.behavior;
        this.container = container;
        this.processTime = co.runTime_SEC;
        this.cpuUtil = co.cpuUtilization;
        this.memory = co.memory_MB;
    }

    @Override public String toString() {
        return opID + ":" + operatorName + "," + container + "," + behavior;
    }

    @Override public boolean equals(Object obj) {
        OperatorAssignment oa = (OperatorAssignment) obj;
        return oa.opID == opID;
    }

    @Override public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.opID;
        return hash;
    }

    public int getOpID() {
        return opID;
    }
}
