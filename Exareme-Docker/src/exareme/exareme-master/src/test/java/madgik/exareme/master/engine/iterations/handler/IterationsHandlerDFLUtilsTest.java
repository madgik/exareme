package madgik.exareme.master.engine.iterations.handler;

import madgik.exareme.common.consts.HBPConstants;
import madgik.exareme.master.engine.iterations.IterationsTestGenericUtils;
import madgik.exareme.master.engine.iterations.exceptions.IterationsFatalException;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.master.queryProcessor.composer.AlgorithmProperties;
import madgik.exareme.master.queryProcessor.composer.Algorithms;
import madgik.exareme.master.queryProcessor.composer.AlgorithmsException;
import madgik.exareme.master.queryProcessor.composer.Composer;
import madgik.exareme.utils.file.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.IOException;

import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerDFLUtils.copyAlgorithmTemplatesToDemoDirectory;
import static madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel.termination_condition;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class IterationsHandlerDFLUtilsTest {
    private static final Logger log = Logger.getLogger(IterationsHandlerTest.class);

    // Test algorithms --------------------------------------------------------------------------
    // Select-ok iterative algorithm simply does 'select "ok";' in each phase for X number of
    // iterations. It does NOT contain a termination_condition_query.
    private static final String SELECT_OK_ITERATIVE = "SELECT_OK_ITERATIVE";
    private static final String SELECT_OK_ITERATIVE_ERRONEOUS_TERM_COND
            = "SELECT_OK_ITERATIVE_ERRONEOUS_TERM_COND";
    // Sample-iterative contains a termination_condition_query.
    // Sample-iterative contains a termination_condition_query.
    private static final String SAMPLE_ITERATIVE = "SAMPLE_ITERATIVE";
    private static final String HANDWRITTEN_EXTENSION = ".handwritten";
    private static final String TEMPLATE_FILES_SUFFIX = ".template.sql";
    // ------------------------------------------------------------------------------------------

    private static String algorithmName;
    private AlgorithmProperties algorithmProperties;


    @BeforeClass
    public static void SetUp() throws Exception {
        // These overwrites must happen before initializing any of the below iteration related
        // fields (since they statically get an instance of Composer and thus force its
        // initialization, which we want to avoid until having done the overwrites below).
        IterationsTestGenericUtils.overwriteHBPConstantsDEMO_ALGOR_WORKDIR();
        IterationsTestGenericUtils.overwriteDemoRepositoryPathGatewayProperty();
    }

    /**
     * If termination condition query provided property is set to true, and a termination cond.
     * query isn't provided, then prepareDFLScripts function must throw an IterationsFatalException.
     */
    @Test
    public void ensureErrorIfTerminationCondQueryInconsistencyAmongPropertyAndTemplate()
            throws IOException, AlgorithmsException {
        // Preparation phase ----------------------
        // Testing with [condition_query_provided=false] with a provided condition query in
        // termination_condition template file.
        algorithmName = SELECT_OK_ITERATIVE;
        algorithmProperties = Algorithms.getInstance().getAlgorithmProperties(algorithmName);
        try {
            algorithmProperties.mergeAlgorithmParametersWithInputContent(
                    IterationsTestGenericUtils.prepareParameterProperties(
                            algorithmName, "true", "2"));
        }catch(Exception e){
            fail("mergeAlgorithmParametersWithInputContent should not throw Exception.");
        }

        // Mimicking IterationsHandler first steps:
        String algorithmKey = IterationsTestGenericUtils.generateAlgorithmKey(algorithmProperties);
        IterativeAlgorithmState iterativeAlgorithmState =
                new IterativeAlgorithmState(algorithmKey, algorithmProperties, null);

        // Run prepareDFLScripts
        String algorithmDemoDestinationDirectory =
                copyAlgorithmTemplatesToDemoDirectory(algorithmProperties.getName(), algorithmKey);

        try {
            IterationsHandlerDFLUtils.prepareDFLScripts(algorithmDemoDestinationDirectory,
                    algorithmKey, algorithmProperties, iterativeAlgorithmState);
            fail("IterationsHandlerDFLUtils.prepareDFLScripts should fail, since condition query " +
                    "property is set to true, while a termination condition query is NOT provided.");
        } catch (IterationsFatalException e) {
            // This is what we want - but we specifically need a ConditionQueryProvided related error.
            if (!e.getMessage().contains("ConditionQueryProvided"))
                fail("Should have received \"ConditionQueryProvided\" related exception.\n" +
                        "Received [" + e.getCause() + "]: " + e.getMessage());
        }

        // Remove generated files
        FileUtils.deleteDirectory(new File(
                HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY + "/" + algorithmKey));

        // Testing with [condition_query_provided=true] with no provided condition query in
        // termination_condition template file.
        algorithmName = SAMPLE_ITERATIVE;
        algorithmProperties = Algorithms.getInstance().getAlgorithmProperties(algorithmName);

        try {
            algorithmProperties.mergeAlgorithmParametersWithInputContent(
                    IterationsTestGenericUtils.prepareParameterProperties(
                            algorithmName, "false", "2"));
        }catch(Exception e){
            fail("mergeAlgorithmParametersWithInputContent should not throw Exception.");
        }

        // Mimicking IterationsHandler first steps:
        algorithmKey = IterationsTestGenericUtils.generateAlgorithmKey(algorithmProperties);
        iterativeAlgorithmState =
                new IterativeAlgorithmState(algorithmKey, algorithmProperties, null);

        // Run prepareDFLScripts
        algorithmDemoDestinationDirectory =
                copyAlgorithmTemplatesToDemoDirectory(algorithmProperties.getName(), algorithmKey);

        try {
            IterationsHandlerDFLUtils.prepareDFLScripts(algorithmDemoDestinationDirectory,
                    algorithmKey, algorithmProperties, iterativeAlgorithmState);
            fail("IterationsHandlerDFLUtils.prepareDFLScripts should fail, since condition query " +
                    "property is set to false, while a termination condition query is provided.");
        } catch (IterationsFatalException e) {
            // This is what we want - but we specifically need a ConditionQueryProvided related error.
            if (!e.getMessage().contains("ConditionQueryProvided"))
                fail("Should have received \"ConditionQueryProvided\" related exception\n" +
                        "Received: [" + e.getCause() + "]: " + e.getMessage());
        }

        // Remove generated files
        FileUtils.deleteDirectory(new File(
                HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY + "/" + algorithmKey));
    }

    @Test
    public void ensureIterativeAlgorithmTerminationConditionDirectoryFormat()
            throws IOException, AlgorithmsException {
        // Preparation phase ----------------------
        // [Ensure termination condition under its required directory exists]
        algorithmName = SELECT_OK_ITERATIVE_ERRONEOUS_TERM_COND;
        algorithmProperties = Algorithms.getInstance().getAlgorithmProperties(algorithmName);

        try {
            algorithmProperties.mergeAlgorithmParametersWithInputContent(
                    IterationsTestGenericUtils.prepareParameterProperties(
                            algorithmName, "false", "2"));
        }catch(Exception e){
            fail("mergeAlgorithmParametersWithInputContent should not throw Exception.");
        }

        // Mimicking IterationsHandler first steps:
        String algorithmKey = IterationsTestGenericUtils.generateAlgorithmKey(algorithmProperties);
        IterativeAlgorithmState iterativeAlgorithmState =
                new IterativeAlgorithmState(algorithmKey, algorithmProperties, null);

        // Run prepareDFLScripts
        String algorithmDemoDestinationDirectory =
                copyAlgorithmTemplatesToDemoDirectory(algorithmProperties.getName(), algorithmKey);

        try {
            IterationsHandlerDFLUtils.prepareDFLScripts(algorithmDemoDestinationDirectory,
                    algorithmKey, algorithmProperties, iterativeAlgorithmState);
            fail("IterationsHandlerDFLUtils.prepareDFLScripts should fail, since its ["
                    + termination_condition.name() + "] phase " +
                    "doesn't exist under its " + termination_condition.name() + "] directory.");
        } catch (IterationsFatalException e) {
            // This is what we want - but we specifically need a term. condition related error.
            if (!e.getMessage().contains(termination_condition.name()))
                fail("Should have received [" + termination_condition.name()
                        + "] related exception.\nReceived [" + e.getCause() + "]: "
                        + e.getMessage());
        }

        // Remove generated files
        FileUtils.deleteDirectory(new File(
                HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY + "/" + algorithmKey));
    }

    @Test
    public void ensureGeneratedTemplateFilesMatchHandwrittenOnes()
            throws Exception, AlgorithmsException {
        // Preparation phase ----------------------
        algorithmName = SAMPLE_ITERATIVE;
        algorithmProperties = Algorithms.getInstance().getAlgorithmProperties(algorithmName);
        algorithmProperties.mergeAlgorithmParametersWithInputContent(
                IterationsTestGenericUtils.prepareParameterProperties(
                        algorithmName, "true", "2"));

        // Mimicking IterationsHandler first steps:
        String algorithmKey = IterationsTestGenericUtils.generateAlgorithmKey(algorithmProperties);
        IterativeAlgorithmState iterativeAlgorithmState =
                new IterativeAlgorithmState(algorithmKey, algorithmProperties, null);

        // Run prepareDFLScripts
        String algorithmDemoDestinationDirectory =
                copyAlgorithmTemplatesToDemoDirectory(algorithmProperties.getName(), algorithmKey);

        IterationsHandlerDFLUtils.prepareDFLScripts(algorithmDemoDestinationDirectory,
                algorithmKey, algorithmProperties, iterativeAlgorithmState);

        boolean outputDiffers = false;
        // Template files being updated to include iterations-control logic are only global files
        // and especially the last global files of a multiple-local-global.
        for (IterativeAlgorithmState.IterativeAlgorithmPhasesModel phase :
                IterativeAlgorithmState.IterativeAlgorithmPhasesModel.values()) {

            File generatedFile, handwrittenFile;
            if (phase.equals(termination_condition)) {
                generatedFile =
                        new File(HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY + "/"
                                + algorithmKey + "/" + phase.name() + "/" + phase.name()
                                + TEMPLATE_FILES_SUFFIX);
                handwrittenFile =
                        new File(HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY + "/"
                                + algorithmName + HANDWRITTEN_EXTENSION + "/"
                                + phase.name() + "/"
                                + phase.name() + TEMPLATE_FILES_SUFFIX);
            } else {
                generatedFile = Whitebox.invokeMethod(
                        IterationsHandlerDFLUtils.class, "getLastGlobalFromMultipleLocalGlobal",
                        new File(HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY + "/"
                                + algorithmKey + "/" + phase.name()));

                handwrittenFile = Whitebox.invokeMethod(
                        IterationsHandlerDFLUtils.class, "getLastGlobalFromMultipleLocalGlobal",
                        new File(HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY + "/"
                                + algorithmName + HANDWRITTEN_EXTENSION + "/" + phase.name()));
            }

            // Dummy parsing follows
            String generatedFileContents =
                    dummyParseSQLTemplateFiles(FileUtil.readFile(generatedFile));
            String handwrittenFileContents =
                    dummyParseSQLTemplateFiles(FileUtil.readFile(handwrittenFile));

            if (!generatedFileContents.equals(handwrittenFileContents)) {
                log.error("Phase [" + phase.name() + "] output differs: \n" +
                        "\nGenerated file contents: \n" + generatedFileContents +
                        "\nHandwritten file contents: \n" + handwrittenFileContents);
                outputDiffers = true;
            }
        }

        // Remove generated files
        FileUtils.deleteDirectory(new File(
                HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY + "/" + algorithmKey));

        assertFalse("The \"handwritten\" template SQL files (template SQL files with " +
                "iterations control logic) differ from the generated ones.", outputDiffers);
    }


    // Utilities --------------------------------------------------------------------------------
    private String dummyParseSQLTemplateFiles(String str) {
        final String[] lines = str.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (!line.startsWith("--"))
                sb.append(line.replaceAll("\\s+", "_")
                        .replaceAll("\n", "").replaceAll("_+", "_").toLowerCase());
        }
        return sb.toString();
    }
}
