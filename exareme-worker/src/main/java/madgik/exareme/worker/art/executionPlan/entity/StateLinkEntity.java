/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.entity;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author herald
 */
public class StateLinkEntity implements Serializable {

    public OperatorEntity operatorEntity;
    public StateEntity stateEntity;
    public String containerName = null;
    public EntityName container = null;
    public LinkedList<Parameter> paramList;

    public StateLinkEntity(OperatorEntity operatorEntity, StateEntity stateEntity,
        String containerName, EntityName container, LinkedList<Parameter> paramList) {
        this.containerName = containerName;
        this.container = container;
        this.operatorEntity = operatorEntity;
        this.stateEntity = stateEntity;
        this.paramList = paramList;
    }
}
