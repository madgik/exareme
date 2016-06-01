package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author alexpap
 */
public class AlgorithmsPropertiesTest {

    private static final Logger log = Logger.getLogger(AlgorithmsPropertiesTest.class);

    @Before public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    @Test public void testCreateAlgorithms() throws Exception {
        URL resource = AlgorithmsProperties.class.getResource("properties.json");
        String path = new File(resource.getFile()).getParentFile().getAbsolutePath();
        log.debug(path);
        AlgorithmsProperties algorithms = AlgorithmsProperties
            .createAlgorithms(path);
        String result = new Gson().toJson(algorithms, AlgorithmsProperties.class);
        log.debug(result);
        assertNotNull(result, resource.toString());
    }
}
