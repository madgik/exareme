package madgik.exareme.master.engine.executor;

import junit.framework.TestCase;
import madgik.exareme.common.app.engine.MadisExecutorResult;
import madgik.exareme.master.engine.executor.remote.operator.ExecuteQueryState;

/**
 * @author alex
 */
public class MadisProcessExecutorTest extends TestCase {

    public MadisProcessExecutorTest(String testName) {
        super(testName);
    }

    @Override protected void setUp() throws Exception {
        super.setUp();
    }

    @Override protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of exec method, of class MadisProcessExecutor.
     */
    public void testExec() throws Exception {
        System.out.println("exec");
        MadisExecutorResult expResult = new MadisExecutorResult();
        expResult.setTableInfo(null);
        expResult.setExecStats(null);
        ExecuteQueryState state = null;
        MadisProcessExecutor instance = null;
        MadisExecutorResult result = instance.exec(state);
        assertEquals(expResult, result);
    }
}
