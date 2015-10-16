/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.entity;

import madgik.exareme.common.art.entity.EntityName;

import java.io.Serializable;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class StopEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String operatorName;
    public final OperatorEntity operatorEntity;
    public final String containerName;
    public final EntityName container;

    public StopEntity(String operatorName, OperatorEntity operatorEntity, String containerName,
        EntityName container) {
        this.containerName = containerName;
        this.container = container;
        this.operatorName = operatorName;
        this.operatorEntity = operatorEntity;
    }
}
