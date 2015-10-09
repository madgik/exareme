/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.art;

import madgik.exareme.common.optimizer.OperatorType;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author herald
 */
public class ContainerSessionStatistics implements Serializable {

    private static final long serialVersionUID = 1L;
    public final LinkedList<AdaptorStatistics> adaptors = new LinkedList<>();
    public final LinkedList<ConcreteOperatorStatistics> operators = new LinkedList<>();
    public final LinkedList<BufferStatistics> buffer = new LinkedList<>();
    public final LinkedList<BufferStatistics> links = new LinkedList<>();
    public final String containerName;
    private final ContainerSessionID containerSessionID;
    private final PlanSessionID sessionID;

    public ContainerSessionStatistics(ContainerSessionID containerSessionID,
        PlanSessionID sessionID, String containerName) {
        this.containerSessionID = containerSessionID;
        this.sessionID = sessionID;
        this.containerName = containerName;
    }

    public ConcreteOperatorStatistics createOperatorStatistics(String name, String category,
        OperatorType type) {
        synchronized (containerSessionID) {
            ConcreteOperatorStatistics stats = new ConcreteOperatorStatistics(name, category, type);
            operators.add(stats);
            return stats;
        }
    }

    public AdaptorStatistics createAdaptorStatistics(String name, String from, String to) {
        synchronized (containerSessionID) {
            AdaptorStatistics stats = new AdaptorStatistics(name, from, to);
            adaptors.add(stats);
            return stats;
        }
    }

    public BufferStatistics createBufferStatistics(String bufferName) {
        synchronized (containerSessionID) {
            BufferStatistics stats = new BufferStatistics(bufferName);
            buffer.add(stats);
            return stats;
        }
    }

    public BufferStatistics createLinkStatistics(String linkName) {
        synchronized (containerSessionID) {
            BufferStatistics stats = new BufferStatistics(linkName);
            buffer.add(stats);
            return stats;
        }
    }

    public ContainerSessionID getSessionID() {
        return containerSessionID;
    }

    public ContainerSessionID getContainerSessionID() {
        return containerSessionID;
    }

    public void destroy() {
        synchronized (containerSessionID) {
            adaptors.clear();
            operators.clear();
            buffer.clear();
            links.clear();
        }
    }
}
