package madgik.exareme.master.queryProcessor.graph;

import madgik.exareme.common.optimizer.OperatorBehavior;
import org.junit.Test;

import java.util.PriorityQueue;

import static org.junit.Assert.assertEquals;

/**
 * @author heraldkllapi
 */
public class ConcreteOperatorTest {

    public ConcreteOperatorTest() {
    }

    @Test
    public void testOrdering() {
        PriorityQueue<ConcreteOperator> readyOps = new PriorityQueue<>();
        readyOps.offer(new ConcreteOperator("A", 10, 0, 0, OperatorBehavior.store_and_forward));
        readyOps.offer(new ConcreteOperator("B", 20, 0, 0, OperatorBehavior.store_and_forward));
        readyOps.offer(new ConcreteOperator("C", 5, 0, 0, OperatorBehavior.store_and_forward));

        assertEquals("B", readyOps.poll().operatorName);
        assertEquals("A", readyOps.poll().operatorName);
        assertEquals("C", readyOps.poll().operatorName);
    }
}
