package madgik.exareme.master.engine.iterations.helper;

import org.apache.commons.io.FileUtils;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import madgik.exareme.master.engine.iterations.IterationsTestGenericUtils;
import madgik.exareme.master.gateway.async.handler.HttpAsyncMiningQueryHandler;

import static madgik.exareme.master.gateway.GatewayConstants.COOKIE_ALGORITHM_EXECUTION_ID;

/**
 * Helper for handling any algorithm's execution request and response from Exareme.
 * Currently "configured" ({@code EXAREME_MINING_QUERY_ENDPOINT}) to "hit" the mining query
 * endpoint.
 *
 * <h4>Directions</h4>
 * It can be used in two modes. Either adding algorithms and then executing them one by one, by
 * calling {@link AlgorithmTestExecutionHelper#sendAlgorithmRequestAndWaitResponse}, or adding
 * algorithms to be run in "batch"/concurrent mode, by calling
 * {@link AlgorithmTestExecutionHelper#sendConcurrentAlgorithmRequestsAndAwaitResponses()}.
 *
 * <p>
 * <b>To be set up and used for one test execution only => initialize it in SetUp method
 * (annotated with {@code @Before}).</b>
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class AlgorithmTestExecutionHelper {
    private static final Logger log = Logger.getLogger(AlgorithmTestExecutionHelper.class);

    private Integer algorithmExecutionsCounter;
    // Fixed strings for HTTP Message headers
    private static final String SET_COOKIE_HEADER_NAME = "Set-Cookie";
    private static final String[] CONTENT_TYPE_JSON_HEADER = {
            "Content-type", "application/json"};


    private static final String EXAREME_IP_AND_PORT = "localhost:9090";
    private static final String EXAREME_MINING_QUERY_ENDPOINT = "/mining/query/";

    // The three fields below represent an algorithm's test execution.
    private ArrayList<String> algorithmNamesList;
    private ArrayList<String> jsonParametersList;
    private ArrayList<String> expectedResponsesList;
    // Contains for each algorithm execution, whether the algorithm response matches the expected one.
    // Used only when doing concurrent batch execution of algorithms.
    private ArrayList<Boolean> testResults;
    private ArrayList<String> testResultsMessages;


    public AlgorithmTestExecutionHelper() {
        algorithmExecutionsCounter = 0;
        algorithmNamesList = new ArrayList<>();
        jsonParametersList = new ArrayList<>();
        expectedResponsesList = new ArrayList<>();
        testResults = new ArrayList<>();
        testResultsMessages = new ArrayList<>();
    }

    // Public API -------------------------------------------------------------------------------
    /**
     * Adds an algorithm to be later executed.
     * Can be used to either add one algorithm and then execute it using
     * {@link AlgorithmTestExecutionHelper#sendAlgorithmRequestAndWaitResponse} or for
     * adding many algorithm execution configurations and running them concurrently.
     *
     * @param algorithmName    the algorithm name parameter to be used for the request
     * @param jsonParameters   the algorithm's parameters (in JSON format) to be used for the
     *                         request
     * @param expectedResponse the expected response from the algorithm execution
     * @return the algorithm's execution id (to be used for calling {@link
     * AlgorithmTestExecutionHelper#sendAlgorithmRequestAndWaitResponse})
     */
    public Integer addAlgorithmExecutionTest(String algorithmName, String jsonParameters,
                                             String expectedResponse) {
        algorithmNamesList.add(algorithmExecutionsCounter, algorithmName);
        jsonParametersList.add(algorithmExecutionsCounter, jsonParameters);
        expectedResponsesList.add(algorithmExecutionsCounter, expectedResponse);
        algorithmExecutionsCounter++;
        return algorithmExecutionsCounter - 1;
    }

    /**
     * Performs an asyncPOST request to Exareme and awaits for a response synchronously.
     * Reads the response and verifies it is the same as the expected one.
     *
     * @param algorithmExecutionId the algorithm execution id for the algorithm to be executed.
     *                             Obtained from calling {@link AlgorithmTestExecutionHelper#addAlgorithmExecutionTest}.
     * @return true if response was equal to the expected one, false otherwise
     */
    public boolean sendAlgorithmRequestAndWaitResponse(int algorithmExecutionId)
            throws IOException, ExecutionException, InterruptedException {
        String endpointWithAlgorithm = EXAREME_MINING_QUERY_ENDPOINT
                + algorithmNamesList.get(algorithmExecutionId);

        try (CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault()) {
            httpclient.start();

            // Generate request & execute ---------------------------
            HttpPost request = new HttpPost(
                    "http://" + EXAREME_IP_AND_PORT + endpointWithAlgorithm);
            StringEntity stringEntity = new StringEntity(jsonParametersList.get(algorithmExecutionId));
            request.setHeader(CONTENT_TYPE_JSON_HEADER[0], CONTENT_TYPE_JSON_HEADER[1]);
            request.setEntity(stringEntity);
            request.addHeader(SET_COOKIE_HEADER_NAME,
                    COOKIE_ALGORITHM_EXECUTION_ID + "=" + algorithmExecutionId);
            Future<HttpResponse> future = httpclient.execute(request, null);

            // Expect response & read result ------------------------
            HttpResponse response = future.get();
            String algorithmResponse = retrieveAlgorithmResponseFromResponse(response);

            log.info("========================================================");
            log.info(generateAlgorithmExecutionResultMessage(algorithmExecutionId, algorithmResponse));
            log.info("========================================================");

            cleanUpGeneratedFiles(algorithmNamesList.get(algorithmExecutionId));

            return algorithmResponse != null &&
                    algorithmResponse.equals(expectedResponsesList.get(algorithmExecutionId));
        }
    }

    /**
     * Executes all added algorithms as concurrent async requests.
     */
    public void sendConcurrentAlgorithmRequestsAndAwaitResponses()
            throws IOException, InterruptedException {
        try (CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .build()) {
            httpclient.start();

            // Prepare requests
            final HttpPost[] requests = new HttpPost[algorithmNamesList.size()];
            int requestsIdx = 0;
            for (int executionId = 0; executionId < algorithmExecutionsCounter; executionId++) {

                String endpointWithAlgorithm =
                        EXAREME_MINING_QUERY_ENDPOINT + algorithmNamesList.get(executionId);
                HttpPost request = new HttpPost(
                        "http://" + EXAREME_IP_AND_PORT + endpointWithAlgorithm);
                StringEntity stringEntity = new StringEntity(jsonParametersList.get(executionId));
                // The content-type that is expected from Exareme's gateway
                request.setHeader("Content-type", "application/json");
                request.setEntity(stringEntity);
                // Setting an algorithm execution id, so as to have a map from algorithmId to
                // a particular algorithm execution.
                request.addHeader("Set-Cookie",
                        COOKIE_ALGORITHM_EXECUTION_ID + "=" + executionId);

                requests[requestsIdx++] = request;
            }

            // Prepare result lists for current execution set.
            ensureSize(testResults, algorithmNamesList.size());
            ensureSize(testResultsMessages, algorithmNamesList.size());

            // Execute all requests concurrently. Using a latch to make the "testing"-thread
            // wait for all the requests to call one of the 3 FutureCallback interface methods.
            final CountDownLatch latch = new CountDownLatch(requests.length);
            for (final HttpPost request : requests) {
                httpclient.execute(request, new FutureCallback<HttpResponse>() {

                    @Override
                    public void completed(final HttpResponse response) {
                        String algorithmResponse = retrieveAlgorithmResponseFromResponse(response);

                        if (algorithmResponse != null) {
                            Integer algorithmExecutionId =
                                    retrieveAlgorithmExecutionIdFromResponse(response);

                            log.info("========================================================");
                            String message = generateAlgorithmExecutionResultMessage(algorithmExecutionId,
                                    algorithmResponse);
                            log.info(message);
                            log.info("========================================================");

                            // Add expectedResponseTest result and message for later check by
                            // Junit.
                            testResultsMessages.set(algorithmExecutionId, message);
                            testResults.set(
                                    algorithmExecutionId,
                                    expectedResponsesList.get(algorithmExecutionId)
                                            .equals(algorithmResponse));
                        }

                        latch.countDown();
                    }

                    @Override
                    public void failed(final Exception ex) {
                        log.error(request.getRequestLine() + "->" + ex);
                        latch.countDown();
                    }

                    @Override
                    public void cancelled() {
                        log.error(request.getRequestLine() + " cancelled");
                        latch.countDown();
                    }

                });
            }
            latch.await();
            log.info("Shutting down client");
        }
        log.info("Done\nCleaning up");

        // Clean up -----------------------------------------------------------------------------
        final HashSet<String> uniqueAlgorithmNames = new HashSet<>(algorithmNamesList);
        for (String algorithmName : uniqueAlgorithmNames) {
            cleanUpGeneratedFiles(algorithmName);
        }
    }

    /**
     * Returns the value of the predicate "algorithm's response must match expected response".
     * <b>To be used only when concurrent batch of requests has been executed.</b>
     */
    public Boolean gotExpectedResponse(Integer algorithmExecutionId) {
        return testResults.get(algorithmExecutionId);
    }

    /**
     * Returns the execution info of the algorithm specified by the {@code algorithmExecutionId}
     * parameter.
     * <b>To be used only when concurrent batch of requests has been executed.</b>
     */
    public String getAlgorithmExecutionMessage(Integer algorithmExecutionId) {
        return testResultsMessages.get(algorithmExecutionId);
    }

    // Utilities --------------------------------------------------------------------------------
    /**
     * Removes the generated algorithm SQL templates and DFL files.
     *
     * @param algorithmName the algorithm name that was executed
     * @throws IOException if delete directory fails.
     */
    private static void cleanUpGeneratedFiles(final String algorithmName) throws IOException {
        File[] files = new File(IterationsTestGenericUtils.ALGORITHMS_DEV_DIRECTORY)
                .listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        String name = pathname.getName();
                        return pathname.isDirectory() &&
                                name.matches("^" + algorithmName + "_[0-9]+$");
                    }
                });
        if (files != null) {
            for (File file : files) {
                log.info("Cleaning up: " + file.getName());
                FileUtils.deleteDirectory(file);
            }
        }
    }

    /**
     * Retrieves the algorithm's execution id from the HttpResponse's cookie.
     * <p>
     * Solely for testing purposes, the requests to exareme have a cookie with the algorithm's
     * execution id, so that we can map the response to a particular algorithm execution.
     */
    private Integer retrieveAlgorithmExecutionIdFromResponse(HttpResponse response) {
        HeaderIterator it = response.headerIterator(SET_COOKIE_HEADER_NAME);

        // Parse "algorithm execution id" cookie
        StringBuilder echoCookieContent = new StringBuilder();
        while (it.hasNext()) {
            echoCookieContent.append(it.next());
        }

        String cookieContentStr = echoCookieContent.toString();
        if (!cookieContentStr.isEmpty()) {
            if (cookieContentStr.contains(COOKIE_ALGORITHM_EXECUTION_ID))
                return Integer.parseInt(cookieContentStr.substring(
                        cookieContentStr.indexOf(" ") + 1,
                        cookieContentStr.length())
                        .split("=")[1]);
            else
                throw new NoSuchElementException("Header for cookies (" + SET_COOKIE_HEADER_NAME
                        + ") does not contain \"" + COOKIE_ALGORITHM_EXECUTION_ID + "\" cookie.\n"
                        + "Maybe " + HttpAsyncMiningQueryHandler.class.getSimpleName()
                        + "#handle function's code has been changed.");
        }
        else
            throw new NoSuchElementException("Header for cookies (" + SET_COOKIE_HEADER_NAME + ")" +
                    " has not been set for testing. \nMaybe "
                    +  HttpAsyncMiningQueryHandler.class.getSimpleName() + "#handle function's "
                    + "code has been changed to not \"echo\" the algorithm execution id cookie.");
    }


    /**
     * Retrieves algorithm's response from HttpResponse.
     * <b>Reads all reply content in memory, use with caution.</b>
     */
    private String retrieveAlgorithmResponseFromResponse(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        ByteArrayOutputStream byteArrayOutputStream = null;
        boolean successToReadResponse = true;
        try {
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity);
            InputStream is = bufferedHttpEntity.getContent();
            byteArrayOutputStream = new ByteArrayOutputStream(is.available());
            int nRead;
            byte[] data = new byte[is.available()];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                byteArrayOutputStream.write(data, 0, nRead);
            }
            byteArrayOutputStream.flush();

        } catch (IOException e) {
            successToReadResponse = false;
            log.error("Failed to read response");
        }
        if (successToReadResponse)
            return byteArrayOutputStream.toString();
        else
            return null;
    }

    private String generateAlgorithmExecutionResultMessage(int algorithmExecutionId,
                                                           String algorithmResponse) {
        return "Received response for:\n"
                + algorithmNamesList.get(algorithmExecutionId)
                + "](id: " + algorithmExecutionId + ") with parameters ["
                + jsonParametersList.get(algorithmExecutionId) + "]\n" +
                "\tAlgorithm response was [" + algorithmResponse + "]\n" +
                "\tExpected response was ["
                + expectedResponsesList.get(algorithmExecutionId) + "]\n";
    }

    private String generateAlgorithmExecutionInfo(HttpResponse response) {
        Integer algorithmExecutionId = retrieveAlgorithmExecutionIdFromResponse(response);
        return "[" + algorithmNamesList.get(algorithmExecutionId)
                + "](id: " + algorithmExecutionId + ") with parameters ["
                + jsonParametersList.get(algorithmExecutionId) + "]";
    }

    private static void ensureSize(ArrayList<?> list, int size) {
        // Prevent excessive copying while we're adding
        list.ensureCapacity(size);
        while (list.size() < size) {
            list.add(null);
        }
    }
}
