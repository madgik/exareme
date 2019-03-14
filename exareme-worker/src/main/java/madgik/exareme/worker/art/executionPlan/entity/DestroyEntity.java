/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.entity;

import madgik.exareme.common.art.entity.EntityName;

import java.io.Serializable;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class DestroyEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String objectName;
    public final String containerName;
    public final EntityName container;

    public OperatorEntity operatorEntity;
    public BufferEntity bufferEntity;
    public StateEntity stateEntity;
    public BufferLinkEntity connectEntity;
    public StateLinkEntity linkEntity;

    private DestroyEntity(String objectName, String containerName, EntityName container) {
        this.objectName = objectName;
        this.containerName = containerName;
        this.container = container;
    }

    public DestroyEntity(String objectName, OperatorEntity operatorEntity, String containerName,
                         EntityName container) {
        this(objectName, containerName, container);
        this.operatorEntity = operatorEntity;
    }

    public DestroyEntity(String objectName, BufferEntity bufferEntity, String containerName,
                         EntityName container) {
        this(objectName, containerName, container);
        this.bufferEntity = bufferEntity;
    }

    public DestroyEntity(String objectName, StateEntity stateEntity, String containerName,
                         EntityName container) {
        this(objectName, containerName, container);
        this.stateEntity = stateEntity;
    }

    public DestroyEntity(String objectName, BufferLinkEntity connectEntity, String containerName,
                         EntityName container) {
        this(objectName, containerName, container);
        this.connectEntity = connectEntity;
    }

    public DestroyEntity(String objectName, StateLinkEntity linkEntity, String containerName,
                         EntityName container) {
        this(objectName, containerName, container);
        this.linkEntity = linkEntity;
    }
}
