package madgik.exareme.master.engine.iterations;

import junit.framework.Assert;

import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import madgik.exareme.master.app.cluster.ExaremeCluster;
import madgik.exareme.master.app.cluster.ExaremeClusterFactory;
import madgik.exareme.master.engine.iterations.helper.AlgorithmTestExecutionHelper;
import madgik.exareme.master.gateway.ExaremeGateway;
import madgik.exareme.master.gateway.ExaremeGatewayFactory;

/**
 * Functional tests for iterations module.
 *
 * To be used in conjunction with {@link AlgorithmTestExecutionHelper}.
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class IterationsFunctionalTests {
    private static final Logger log = Logger.getLogger(IterationsFunctionalTests.class);

    private static final String PLACEHOLDER_MAX_ITERATIONS = "placeholder_max_iterations";
    private static final String PLACEHOLDER_CONDITION_QUERY = "placeholder_query_condition";
    private static final String ALGORITHM_JSON_PARAMETERS =
            "[{\"name\":\"iterations_max_number\",\"value\":\"${"
                    + PLACEHOLDER_MAX_ITERATIONS + "}\"}," +
                    "{\"name\":\"iterations_condition_query_provided\",\"value\":\"${"
                    + PLACEHOLDER_CONDITION_QUERY + "}\"}]";
    private static final String SELECT_OK_ITERATIVE = "SELECT_OK_ITERATIVE";
    private static final String SAMPLE_ITERATIVE = "SAMPLE_ITERATIVE";


    private HashMap<String, String> placeholdersMap;
    private AlgorithmTestExecutionHelper executionTestsHelper;

    @BeforeClass
    public static void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.ALL);
        // These overwrites must happen before initializing any of the below iteration related
        // fields (since they statically get an instance of Composer and thus force its
        // initialization, which we want to avoid until having done the overwrites below).
        IterationsTestGenericUtils.overwriteHBPConstantsDEMO_ALGOR_WORKDIR();
        IterationsTestGenericUtils.overwriteDemoRepositoryPathGatewayProperty();

        final ExaremeCluster cluster = ExaremeClusterFactory.createMiniCluster(1098, 8088, 0);
        cluster.start();
        final ExaremeGateway gateway =
                ExaremeGatewayFactory.createHttpServer(cluster.getDBManager());
        gateway.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
                gateway.stop();
                try {
                    cluster.stop(false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        log.info("Mini cluster & gateway started.");
    }

    @Before
    public void SetUp() {
        placeholdersMap = new HashMap<>();
        executionTestsHelper = new AlgorithmTestExecutionHelper();
    }

    @Test
    public void ensureSelectOKResult() throws IOException, InterruptedException, ExecutionException {
        final Integer executionId = submitSelectOkAlgorithm(2);
        Assert.assertTrue(
                executionTestsHelper.sendAlgorithmRequestAndWaitResponse(executionId));
    }

    @Test
    public void ensureSampleIterativeResultStopDueMaxIterations()
            throws IOException, InterruptedException, ExecutionException {
        final Integer algorithmExecId = submitSampleIterativeAlgorithm(2);
        Assert.assertTrue(
                executionTestsHelper.sendAlgorithmRequestAndWaitResponse(algorithmExecId));
    }

    @Test
    public void ensureSampleIterativeResultStopDueConditionQuery()
            throws IOException, InterruptedException, ExecutionException {
        final Integer algorithmExecId = submitSampleIterativeAlgorithm(10);
        Assert.assertTrue(
                executionTestsHelper.sendAlgorithmRequestAndWaitResponse(algorithmExecId));
    }

    @Test
    public void testConcurrentSelectOkAlgorithm() throws IOException, InterruptedException {
        ArrayList<Integer> algorithmExecutionIds = new ArrayList<>();

        algorithmExecutionIds.add(submitSelectOkAlgorithm(1));
        algorithmExecutionIds.add(submitSelectOkAlgorithm(1));
        algorithmExecutionIds.add(submitSelectOkAlgorithm(2));

        executionTestsHelper.sendConcurrentAlgorithmRequestsAndAwaitResponses();

        for (Integer algorithmExecutionId : algorithmExecutionIds) {
            Assert.assertTrue(
                    executionTestsHelper.getAlgorithmExecutionMessage(algorithmExecutionId),
                    executionTestsHelper.gotExpectedResponse(algorithmExecutionId));
        }
    }

    @Test
    public void testConcurrentIterativeAlgorithms() throws IOException, InterruptedException {
        ArrayList<Integer> algorithmExecutionIds = new ArrayList<>();

        // Algorithms preparation and submission
        algorithmExecutionIds.add(submitSelectOkAlgorithm(1));
        algorithmExecutionIds.add(submitSampleIterativeAlgorithm(10));
        algorithmExecutionIds.add(submitSampleIterativeAlgorithm(2));

        executionTestsHelper.sendConcurrentAlgorithmRequestsAndAwaitResponses();

        for (Integer algorithmExecutionId : algorithmExecutionIds) {
            Assert.assertTrue(
                    executionTestsHelper.getAlgorithmExecutionMessage(algorithmExecutionId),
                    executionTestsHelper.gotExpectedResponse(algorithmExecutionId));
        }
    }


    // Utilities ------------------------------------------------------------------------------
    private String prepareJSONParameters(String maxIterations, String conditionQueryProvided) {
        placeholdersMap.put(PLACEHOLDER_MAX_ITERATIONS, maxIterations);
        placeholdersMap.put(PLACEHOLDER_CONDITION_QUERY, conditionQueryProvided);
        return StrSubstitutor.replace(ALGORITHM_JSON_PARAMETERS, placeholdersMap);
    }


    /**
     * Wrapper for submitting a SelectOk algorithm with a given maximum iterations number.
     *
     * @return the algorithm's execution id
     */
    private Integer submitSelectOkAlgorithm(int maxIterationsNumber) {
        return executionTestsHelper.addAlgorithmExecutionTest(
                SELECT_OK_ITERATIVE,
                prepareJSONParameters(
                        String.valueOf(maxIterationsNumber),
                        "false"),
                "{\"reply\":\"sufficient\"}");
    }

    /**
     * Wrapper for submitting a SampleIterative algorithm with a given maximum iterations number.
     *
     * @return the algorithm's execution id
     */
    private Integer submitSampleIterativeAlgorithm(int maxIterationsNumber) {
        // Generate expected response
        float sumValue = 0;
        int currentIterationsNumber = 0;
        // Write SampleIterative's termination condition to the respective Java Code
        // so that we can generate the correct expected answer.
        while (currentIterationsNumber < maxIterationsNumber && sumValue < 5) {
            sumValue += currentIterationsNumber + 1;
            currentIterationsNumber++;
        }

        // currentIterationsNumber needs to be cast as float, since this is how it is retrieved
        // from data source
        return executionTestsHelper.addAlgorithmExecutionTest(
                SAMPLE_ITERATIVE,
                prepareJSONParameters(String.valueOf(maxIterationsNumber), "true"),
                "{\"sum_value\":" + String.valueOf(sumValue)
                        + ",\"number_of_iterations\":"
                        + String.valueOf((float) currentIterationsNumber)
                        + "}");
    }
}
