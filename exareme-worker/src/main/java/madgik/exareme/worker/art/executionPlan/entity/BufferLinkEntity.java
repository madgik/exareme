/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.entity;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class BufferLinkEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public final ConnectType type;
    public final ConnectDirection direction;
    public final String containerName;
    public final EntityName container;
    public final LinkedList<Parameter> paramList;
    public OperatorEntity operatorEntity;
    public BufferEntity bufferEntity;

    public BufferLinkEntity(OperatorEntity operatorEntity, BufferEntity bufferEntity,
        ConnectType type, ConnectDirection direction, String containerName, EntityName container,
        LinkedList<Parameter> paramList) {
        this.containerName = containerName;
        this.container = container;
        this.type = type;
        this.direction = direction;
        this.operatorEntity = operatorEntity;
        this.bufferEntity = bufferEntity;
        this.paramList = paramList;
    }

    public enum ConnectType {
        local,
        remote
    }


    public enum ConnectDirection {
        reader,
        writer
    }
}
