/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.entity;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * create bufferName containerName('QoS');
 *
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class BufferEntity implements Comparable<BufferEntity>, Serializable {

    public String bufferName = null;
    public String containerName = null;
    public EntityName container = null;
    public String QoS = null;
    public LinkedList<Parameter> paramList;

    public BufferEntity(String bufferName, String QoS, String containerName, EntityName container,
        LinkedList<Parameter> paramList) {
        this.bufferName = bufferName;
        this.containerName = containerName;
        this.container = container;
        this.QoS = QoS;
        this.paramList = paramList;
    }

    public int compareTo(BufferEntity entity) {
        return this.bufferName.compareTo(entity.bufferName);
    }

    @Override public boolean equals(Object entityObj) {
        BufferEntity entity = (BufferEntity) entityObj;
        return this.bufferName.equals(entity.bufferName);
    }

    @Override public int hashCode() {
        int hash = 5;
        hash = 43 * hash + (this.bufferName != null ? this.bufferName.hashCode() : 0);
        return hash;
    }
}
