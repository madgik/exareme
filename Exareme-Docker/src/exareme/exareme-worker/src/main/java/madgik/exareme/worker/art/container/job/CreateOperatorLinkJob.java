/*
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobType;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorType;
import madgik.exareme.worker.art.container.buffer.BufferID;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;

import java.util.LinkedList;

/**
 * @author John Chronis
 */
public class CreateOperatorLinkJob implements ContainerJob {

    public final ConcreteOperatorID fromConcreteOperatorID;
    public final ConcreteOperatorID toConcreteOperatorID;
    public final LinkedList<Parameter> paramList;
    public final AdaptorType adaptorType;
    public final BufferID bufferID;
    public final String bufferName;
    public final String IpProducer;
    public final String IpConsumer;
    public final ContainerSessionID contSessionID;

    public CreateOperatorLinkJob(ConcreteOperatorID fromOperator, ConcreteOperatorID toOperator,
                                 LinkedList<Parameter> params, AdaptorType adaptorType, BufferID bufferID, String bufferName,
                                 String IpProducer, String IpConsumer, ContainerSessionID contSessionID) {
        this.adaptorType = adaptorType;
        this.toConcreteOperatorID = toOperator;
        this.fromConcreteOperatorID = fromOperator;
        this.paramList = params;
        this.bufferID = bufferID;
        this.bufferName = bufferName;
        this.IpProducer = IpProducer;
        this.IpConsumer = IpConsumer;
        this.contSessionID = contSessionID;
    }

    @Override
    public ContainerJobType getType() {
        return ContainerJobType.createOperatorLink;
    }

}
