package madgik.exareme.master.engine.executor;

import com.google.gson.JsonObject;
import madgik.exareme.worker.art.concreteOperator.manager.ProcessManager;
import madgik.exareme.worker.art.container.dataTransfer.rest.DataTransferConstants;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class ExecUtilsTest {
    private static final Logger log = Logger.getLogger(ExecUtilsTest.class);
    private String IP="localhost";
    private String Port="8086";
    private String MadisDB="/tmp/madisUnitTest.db";

    @Before
    public void setUp() throws Exception {
        Path madisPath =  Paths
                .get(System.getProperty("user.dir") + "/../exareme-tools/madis/src/mterm.py");
        if(!Files.exists(madisPath)){
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
        String query = "select * from range(10);";
        String HTTPresult = ExecUtils.runQueryOnTable(new StringBuilder(query),  MadisDB, new File("/"),
                IP, Port);

        String embededResult = ExecUtils.runQueryOnTable(new StringBuilder(query), MadisDB, new File("/tmp/"),
               new ProcessManager());
        log.info("Embeded: "+ embededResult);
        log.info("HTTP: " + HTTPresult);
    }
}