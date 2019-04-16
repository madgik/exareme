package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;
import madgik.exareme.master.engine.iterations.IterationsTestGenericUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AlgorithmsTest {

    private static final Logger log = Logger.getLogger(AlgorithmsTest.class);

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    @Test
    public void testCreateAlgorithms() throws Exception {
        URL resource = Algorithms.class.getResource("mip-algorithms");
        log.debug(resource);

        String path = resource.getPath();
        log.debug(path);
        Algorithms algorithms = new Algorithms(path);
        log.debug(algorithms.getAlgorithms().length);
        String result = new Gson().toJson(algorithms, Algorithms.class);
        log.debug(result);
        //Test resources contains mip-algorithms directory with 6 algorithms.
        assertEquals(4,algorithms.getAlgorithms().length);
    }


}
