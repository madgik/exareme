package madgik.exareme.worker.arm;

import madgik.exareme.worker.arm.storage.client.local.LocalArmStorageClient;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;

/**
 * @author alex
 */
public class LocalArmStorageClientTest {
    private Logger log = Logger.getLogger(LocalArmStorageClientTest.class);

    @Test public void testLocal() throws Exception {
        log.info("----- TEST ----");
        LocalArmStorageClient storageClient = new LocalArmStorageClient();
        File testFile = new File("/tmp/test.db");
        FileWriter fileWriter = new FileWriter(testFile);
        fileWriter.write("test file\n");
        fileWriter.flush();
        fileWriter.close();

        File file = new File("/tmp/exaclient-db/test/" + testFile.getName());
        file.delete();
        log.info("File " + testFile.getAbsolutePath() + " created.");
        storageClient.connect();
        storageClient
            .put(testFile.getAbsolutePath(), "/tmp/exaclient-db/test/" + testFile.getName());
        storageClient.disconnect();

        log.info("----- TEST ----");
    }
}
