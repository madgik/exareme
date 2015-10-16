package madgik.exareme.utils.embedded.process;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * @author alex
 */
public class MadisProcessTest {
    private static final Logger log = Logger.getLogger(MadisProcessTest.class);

    @Test public void testProccess() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);
        MadisProcess madisProcess =
            new MadisProcess("", "../exareme-tools/madis/src/main/python/madis/src/mterm.py");
        madisProcess.start();
        madisProcess.stop();

    }
}
