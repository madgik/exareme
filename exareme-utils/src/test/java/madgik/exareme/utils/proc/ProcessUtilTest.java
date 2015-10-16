/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.proc;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author heraldkllapi
 */
public class ProcessUtilTest extends TestCase {

    public ProcessUtilTest() {
    }

    /**
     * Test of ExecAndGetResult method, of class ProcessUtil.
     */
    public void testExecAndGetResult() throws Exception {
        // Execute cat
        ProcessDefn proc = new ProcessDefn();
        proc.setProcess("cat").setDirectory("/tmp").setStdin("test");
        ProcessResult result = ProcessUtil.Exec(proc);
        // Check if the results are as expected
        Assert.assertEquals("", result.stderr.trim());
        Assert.assertEquals("test", result.stdout.trim());
        Assert.assertEquals(0, result.exitCode);
    }
}
