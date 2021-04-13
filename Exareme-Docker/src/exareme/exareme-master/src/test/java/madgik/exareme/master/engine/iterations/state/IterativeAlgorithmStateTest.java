package madgik.exareme.master.engine.iterations.state;

import junit.framework.Assert;
import madgik.exareme.common.consts.HBPConstants;
import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.engine.iterations.handler.IterationsConstants;
import madgik.exareme.master.engine.iterations.state.exceptions.IterationsStateFatalException;
import madgik.exareme.master.queryProcessor.HBP.AlgorithmProperties;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.powermock.api.mockito.PowerMockito.when;

/**
 * IterativeAlgorithmState tests
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest(AlgorithmProperties.class)
public class IterativeAlgorithmStateTest {
    private static final String algorithmName = "SAMPLE_ITERATIVE";
    private static AdpDBClient adpDBClient;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private AlgorithmProperties algorithmPropertiesMock = new PowerMockito().mock(AlgorithmProperties.class);

    @BeforeClass
    public static void setUp() throws Exception {
        AdpDBManager adpDBManager = AdpDBManagerLocator.getDBManager();

        String database = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmName;
        AdpDBClientProperties clientProperties =
                new AdpDBClientProperties(database, "", "",
                        false, false, -1, 10);
        adpDBClient = AdpDBClientFactory.createDBClient(adpDBManager, clientProperties);
    }


    // Unit testing -----------------------------------------------------------------------------
    // Iterations control properties ------------
    @Test(expected = IterationsStateFatalException.class)
    public void ensureExceptionOnMissingIterationsMaxNumberProperty() {
        // Omit iterations maximum number property.
        PowerMockito.mock(AlgorithmProperties.class);
        when(algorithmPropertiesMock.getParameterValue(IterationsConstants.iterationsPropertyMaximumNumber))
                .thenReturn(null);
        when(algorithmPropertiesMock.getType()).thenReturn(AlgorithmProperties.AlgorithmType.iterative);

        new IterativeAlgorithmState(algorithmName, algorithmPropertiesMock, adpDBClient);
    }

    @Test(expected = IterationsStateFatalException.class)
    public void ensureExceptionOnErroneousIterationsMaxNumberPropertyValue() {
        // Have condition query property with erroneous value (must be a Long)
        PowerMockito.mock(AlgorithmProperties.class);
        when(algorithmPropertiesMock.getParameterValue(IterationsConstants.iterationsPropertyMaximumNumber))
                .thenReturn("X");
        when(algorithmPropertiesMock.getType()).thenReturn(AlgorithmProperties.AlgorithmType.iterative);

        new IterativeAlgorithmState(algorithmName, algorithmPropertiesMock, adpDBClient);
    }


    @Test
    public void ensureThatIncrementIterNumberIsForced() {
        // Correct algorithm parameters
        PowerMockito.mock(AlgorithmProperties.class);
        when(algorithmPropertiesMock.getParameterValue(IterationsConstants.iterationsPropertyMaximumNumber))
                .thenReturn("3");
        when(algorithmPropertiesMock.getType()).thenReturn(AlgorithmProperties.AlgorithmType.iterative);

        IterativeAlgorithmState ias =
                new IterativeAlgorithmState(algorithmName, algorithmPropertiesMock, adpDBClient);
        // Set DFL scripts
        String dflScripts[] = {"initDFLScript", "stepDFLScript",
                "terminationConditionDFLScript", "finalizeDFLScript"};
        Whitebox.setInternalState(ias, "dflScripts", dflScripts);

        try {
            ias.lock();
            // Before the 1st step phase, iterations number is null
            Assert.assertNull(ias.getCurrentIterationsNumber());
            // Even after having requested the init DFL
            ias.getDFLScript(IterativeAlgorithmState.IterativeAlgorithmPhasesModel.init);
            Assert.assertNull(ias.getCurrentIterationsNumber());
            // After requesting the 1st step phase, iterations number is set to 0
            ias.getDFLScript(IterativeAlgorithmState.IterativeAlgorithmPhasesModel.step);
            Assert.assertEquals(ias.getCurrentIterationsNumber(), Long.valueOf(0L));
            // Let's assume we forget calling incrementIterationsNumber, before requesting the
            // step DFL script. This should throw an exception and log a warning.
            expectedException.expect(IterationsStateFatalException.class);
            ias.getDFLScript(IterativeAlgorithmState.IterativeAlgorithmPhasesModel.step);
            // Let's assert the correct behavior
            ias.incrementIterationsNumber();
            ias.getDFLScript(IterativeAlgorithmState.IterativeAlgorithmPhasesModel.step);
        } finally {
            ias.releaseLock();
        }
    }
}
