package madgik.exareme.master.engine.executor;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;

public class ExecUtilsTest {
    private static final Logger log = Logger.getLogger(ExecUtilsTest.class);
    private String IP = "localhost";
    private String Port = "8086";
    private String MadisDB = "/tmp/madisUnitTest.db";

    @Before
    public void setUp() throws Exception {
        Path madisPath = Paths
                .get(System.getProperty("user.dir") + "/../exareme-tools/madis/src/mterm.py");
        if (!Files.exists(madisPath)) {
            madisPath = Paths.get(System.getProperty("user.dir") + "/exareme-tools/madis/src/mterm.py");
        }
        String relMadisPath = madisPath.toString();
        if (System.getenv("EXAREME_MADIS") != null) {
            log.debug("**--" + System.getenv("EXAREME_MADIS"));
        } else if (new File(relMadisPath).exists()) {
            log.debug("Relative madis Path : " + relMadisPath);
            System.setProperty("EXAREME_PYTHON", "python");
            System.setProperty("EXAREME_MADIS", relMadisPath);
            System.setProperty("MADIS_PATH", relMadisPath);
            log.debug("**--" + System.getProperty("EXAREME_MADIS"));
            log.debug("**--" + relMadisPath);
        } else
            throw new RuntimeException("Provide valid engine path.(" + relMadisPath + ").");
    }

    @Test
    public void runQueryOnTable() {
    }

    @Test
    public void runQueryOnTableHttp() throws RemoteException {
//        String query = "select * from range(10);";
//        String HTTPresult = ExecUtils.runQueryOnTable(new StringBuilder(query),  MadisDB, new File("/"),
//                IP, Port);
//
//        String embededResult = ExecUtils.runQueryOnTable(new StringBuilder(query), MadisDB, new File("/tmp/"),
//               new ProcessManager());
//        log.info("Embeded: "+ embededResult);
//        log.info("HTTP: " + HTTPresult);
    }
}