package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

/**
 * @author alexpap
 */
public class AlgorithmsTest {

    private static final Logger log = Logger.getLogger(AlgorithmsTest.class);

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    @Test
    public void testCreateAlgorithms() throws Exception {
        URL resource = Algorithms.class.getResource("properties.json");
        String path = new File(resource.getFile()).getParentFile().getAbsolutePath();
        log.debug(path);
        Algorithms algorithms = Algorithms
                .createAlgorithms(path);
        String result = new Gson().toJson(algorithms, Algorithms.class);
        log.debug(result);
        assertNotNull(result, resource.toString());
    }
}
