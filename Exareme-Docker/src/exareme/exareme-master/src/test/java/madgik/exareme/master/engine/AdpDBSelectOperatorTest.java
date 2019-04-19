package madgik.exareme.master.engine;

import junit.framework.Assert;
import junit.framework.TestCase;
import madgik.exareme.common.app.engine.AdpDBOperatorType;
import madgik.exareme.common.app.engine.AdpDBSelectOperator;

import java.util.BitSet;
import java.util.List;

/**
 * @author heraldkllapi
 */
public class AdpDBSelectOperatorTest extends TestCase {

    public AdpDBSelectOperatorTest(String testName) {
        super(testName);
    }


    public void testAddInput() {
        AdpDBSelectOperator op = new AdpDBSelectOperator(AdpDBOperatorType.runQuery, null, 0);
        op.addInput("A", 0);
        op.addInput("A", 1);
        op.addInput("B", 1);

        // Table A
        assertEquals(2, op.getInputNumOfPartitions("A"));
        assertEquals(0, op.getOutputNumOfPartitions("A"));
        // Table B
        assertEquals(1, op.getInputNumOfPartitions("B"));
        assertEquals(0, op.getOutputNumOfPartitions("B"));
        // IO counts
        assertEquals(3, op.getTotalInputs());
        assertEquals(0, op.getTotalOutputs());
        assertEquals(2, op.getInputTables().size());
        assertEquals(0, op.getOutputTables().size());
        // Partitions
        List<Integer> AParts = op.getInputPartitions("A");
        Assert.assertEquals(2, AParts.size());
        Assert.assertEquals(0, (int) AParts.get(0));
        Assert.assertEquals(1, (int) AParts.get(1));
        List<Integer> BParts = op.getInputPartitions("B");
        Assert.assertEquals(1, BParts.size());
        Assert.assertEquals(1, (int) BParts.get(0));
    }

    public void testCommon() {
        AdpDBSelectOperator opFrom = new AdpDBSelectOperator(AdpDBOperatorType.runQuery, null, 0);
        opFrom.addOutput("A", 0);
        opFrom.addOutput("A", 1);

        AdpDBSelectOperator opTo = new AdpDBSelectOperator(AdpDBOperatorType.runQuery, null, 0);
        opTo.addInput("A", 1);
        opTo.addInput("A", 3);
        opTo.addInput("A", 4);

        BitSet common = AdpDBSelectOperator.findCommonPartitions(opFrom, opTo, "A");
        Assert.assertEquals(1, common.cardinality());
        Assert.assertFalse(common.get(0));
        Assert.assertTrue(common.get(1));
    }

    public void testClearInputs() {
        AdpDBSelectOperator opFrom = new AdpDBSelectOperator(AdpDBOperatorType.runQuery, null, 0);
        opFrom.addOutput("A", 0);
        opFrom.addOutput("A", 1);
        opFrom.addOutput("A", 3);

        AdpDBSelectOperator opTo = new AdpDBSelectOperator(AdpDBOperatorType.runQuery, null, 0);
        opTo.addInput("A", 1);
        opTo.addInput("A", 3);
        opTo.addInput("A", 3);
        opTo.addInput("A", 4);

        opTo.clearInputs("A", opFrom);
        List<Integer> AParts = opTo.getInputPartitions("A");
        Assert.assertEquals(2, AParts.size());
        Assert.assertEquals(3, (int) AParts.get(0));
        Assert.assertEquals(4, (int) AParts.get(1));
        assertEquals(2, opTo.getTotalInputs());

        opTo.clearOutputs("A");
        assertEquals(0, opTo.getTotalOutputs());
    }

    public void testAddInputsToInputs() {
        AdpDBSelectOperator opFrom = new AdpDBSelectOperator(AdpDBOperatorType.runQuery, null, 0);
        opFrom.addOutput("A", 0);
        opFrom.addOutput("A", 1);

        AdpDBSelectOperator opTo = new AdpDBSelectOperator(AdpDBOperatorType.runQuery, null, 0);
        opTo.addInput("A", 1);
        opTo.addInput("A", 3);
        opTo.addInput("A", 4);

        assertEquals(3, opFrom.addToInputsAllInputsOf(opTo));
        List<Integer> AParts = opFrom.getInputPartitions("A");
        Assert.assertEquals(3, AParts.size());
        Assert.assertEquals(1, (int) AParts.get(0));
        Assert.assertEquals(3, (int) AParts.get(1));
        Assert.assertEquals(4, (int) AParts.get(2));

        assertEquals(3, opTo.getTotalInputs());
    }

    public void testAddInputsToOutputs() {
        AdpDBSelectOperator opFrom = new AdpDBSelectOperator(AdpDBOperatorType.runQuery, null, 0);
        opFrom.addOutput("A", 0);
        opFrom.addOutput("A", 1);

        AdpDBSelectOperator opTo = new AdpDBSelectOperator(AdpDBOperatorType.runQuery, null, 0);
        opTo.addInput("A", 1);
        opTo.addInput("A", 3);
        opTo.addInput("A", 4);

        assertEquals(2, opTo.addToInputsAllOutputsOf(opFrom));
        List<Integer> AParts = opTo.getInputPartitions("A");
        Assert.assertEquals(5, AParts.size());
        Assert.assertEquals(0, (int) AParts.get(0));
        Assert.assertEquals(1, (int) AParts.get(1));
        Assert.assertEquals(1, (int) AParts.get(2));
        Assert.assertEquals(3, (int) AParts.get(3));
        Assert.assertEquals(4, (int) AParts.get(4));

        assertEquals(5, opTo.getTotalInputs());
    }
}
