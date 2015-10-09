/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.entity;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author John Chronis
 */
public class OperatorLinkEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    public final ConnectType type;
    public final String containerName;
    public final EntityName container;
    public final LinkedList<Parameter> paramList;
    public OperatorEntity fromOperator;
    public OperatorEntity toOperator;

    public OperatorLinkEntity(OperatorEntity fromOperator, OperatorEntity toOperator,
        ConnectType type, String containerName, EntityName container,
        LinkedList<Parameter> paramList) {
        this.containerName = containerName;
        this.container = container;
        this.type = type;
        this.fromOperator = fromOperator;
        this.toOperator = toOperator;
        this.paramList = paramList;
    }

    @Override public String toString() {
        return "OperatorLinkEntity{" + "type=" + type + ", containerName=" + containerName
            + ", container=" + container + ", paramList=" + paramList + ", fromOperator="
            + fromOperator + ", toOperator=" + toOperator + '}';
    }

    public enum ConnectType {

        local,
        remote
    }
}
