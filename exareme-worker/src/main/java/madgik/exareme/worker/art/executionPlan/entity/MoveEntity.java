/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.entity;

import madgik.exareme.common.art.entity.EntityName;

import java.util.List;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class MoveEntity {

    public String fromContainerName;
    public EntityName fromContainer;
    public String toContainerName;
    public EntityName toContainer;
    public String operatorName;
    public OperatorEntity oldOperatorEntity;
    public OperatorEntity newOperatorEntity;
    public List<BufferLinkEntity> oldLinks;
    public List<BufferLinkEntity> newLinks;

    public MoveEntity(String operatorName, OperatorEntity oldOperatorEntity,
        OperatorEntity newOperatorEntity, List<BufferLinkEntity> oldLinks,
        List<BufferLinkEntity> newLinks, String fromContainerName, EntityName fromContainer,
        String toContainerName, EntityName toContainer) {
        this.fromContainerName = fromContainerName;
        this.fromContainer = fromContainer;
        this.toContainerName = toContainerName;
        this.toContainer = toContainer;
        this.operatorName = operatorName;
        this.oldOperatorEntity = oldOperatorEntity;
        this.newOperatorEntity = newOperatorEntity;
        this.oldLinks = oldLinks;
        this.newLinks = newLinks;
    }
}
