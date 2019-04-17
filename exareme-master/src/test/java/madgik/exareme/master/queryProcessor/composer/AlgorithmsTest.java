package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;
import madgik.exareme.master.engine.iterations.handler.IterationsConstants;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Algorithms.class)
public class AlgorithmsTest {
    private static final Logger log = Logger.getLogger(AlgorithmsTest.class);

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.DEBUG);
        URL resource = Algorithms.class.getResource("mip-algorithms");
        PowerMockito.spy(Algorithms.class);
        PowerMockito.doReturn(resource.getPath()).when(Algorithms.class, "getAlgorithmsFolderPath");
    }

    @Test
    public void testCreateAlgorithms() throws Exception {
        Algorithms algorithms = Algorithms.getInstance();
        String result = new Gson().toJson(algorithms, Algorithms.class);
        log.debug(result);
        //Test resources contains mip-algorithms directory with 6 algorithms.
        assertEquals(4,algorithms.getAlgorithms().length);
    }
}
