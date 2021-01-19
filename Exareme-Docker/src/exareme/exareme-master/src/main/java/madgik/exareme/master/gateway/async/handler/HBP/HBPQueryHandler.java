package madgik.exareme.master.gateway.async.handler.HBP;

import com.google.gson.JsonSyntaxException;
import madgik.exareme.common.consts.HBPConstants;
import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.connector.DataSerialization;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.engine.iterations.exceptions.IterationsFatalException;
import madgik.exareme.master.engine.iterations.handler.IterationsHandler;
import madgik.exareme.master.engine.iterations.handler.NIterativeAlgorithmResultEntity;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import madgik.exareme.master.gateway.async.handler.HBP.Exceptions.UserException;
import madgik.exareme.master.gateway.async.handler.entity.NQueryResultEntity;
import madgik.exareme.master.queryProcessor.HBP.AlgorithmProperties;
import madgik.exareme.master.queryProcessor.HBP.Algorithms;
import madgik.exareme.master.queryProcessor.HBP.Composer;
import madgik.exareme.master.queryProcessor.HBP.Exceptions.AlgorithmException;
import madgik.exareme.worker.art.container.ContainerProxy;
import org.apache.http.*;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static madgik.exareme.master.gateway.GatewayConstants.COOKIE_ALGORITHM_EXECUTION_ID;
import static madgik.exareme.master.gateway.async.handler.HBP.HBPQueryConstants.serverErrorOccurred;

public class HBPQueryHandler implements HttpAsyncRequestHandler<HttpRequest> {

    private static final Logger log = Logger.getLogger(HBPQueryHandler.class);
    private static final String SET_COOKIE_HEADER_NAME = "Set-Cookie";
    private static final AdpDBManager manager = AdpDBManagerLocator.getDBManager();
    private static final IterationsHandler iterationsHandler = IterationsHandler.getInstance();

    public HBPQueryHandler() {
    }

    @Override
    public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request,
                                                                HttpContext context) {

        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest request, HttpAsyncExchange httpExchange, HttpContext context)
            throws HttpException, IOException {

        HttpResponse response = httpExchange.getResponse();
        response.setHeader("Content-Type", String.valueOf(ContentType.APPLICATION_JSON));

        // When under testing the Set-Cookie header has been used with the "algorithm execution id"
        // parameter for differentiating between concurrent executions of algorithms.
        if (request.containsHeader(SET_COOKIE_HEADER_NAME)) {
            HeaderIterator it = request.headerIterator(SET_COOKIE_HEADER_NAME);

            // Parse "algorithm execution id" cookie
            StringBuilder echoCookieContent = new StringBuilder();
            while (it.hasNext()) {
                echoCookieContent.append(it.next());
            }

            String cookieContentStr = echoCookieContent.toString();
            if (!cookieContentStr.isEmpty() &&
                    cookieContentStr.contains(COOKIE_ALGORITHM_EXECUTION_ID)) {

                String algorithmExecIdStr =
                        cookieContentStr.substring(cookieContentStr.indexOf(" ")).split("=")[1];

                response.addHeader(SET_COOKIE_HEADER_NAME,
                        COOKIE_ALGORITHM_EXECUTION_ID + "=" + algorithmExecIdStr);
            }
        }
        try {
            handleHBPAlgorithmExecution(request, response);
        } catch (Exception e) {
            log.error(e.getMessage());
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            String errorType = HBPQueryHelper.ErrorResponse.ErrorResponseTypes.user_error;
            response.setEntity(createErrorResponseEntity(e.getMessage(), errorType));
        }
        httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private void handleHBPAlgorithmExecution(HttpRequest request, HttpResponse response) {

        try {
            preExecutionChecks(request);

            String algorithmName = getAlgorithmName(request);
            String algorithmKey = algorithmName + "_" + System.currentTimeMillis();
            log.info("Executing algorithm: " + algorithmName + " with key: " + algorithmKey);

            HashMap<String, String> algorithmParameters = HBPQueryHelper.getAlgorithmParameters(request);
            log.info("Request for algorithm: " + algorithmName);
            if (algorithmParameters != null) {
                for (Map.Entry<String, String> parameter : algorithmParameters.entrySet())
                    log.info("Parameter: " + parameter.getKey() + ", with value: " + parameter.getValue());
            }

            ContainerProxy[] algorithmContainers = HBPQueryHelper.getAlgorithmNodes(algorithmParameters);

            AdpDBClientQueryStatus queryStatus;

            AlgorithmProperties algorithmProperties = Algorithms.getInstance().getAlgorithmProperties(algorithmName);
            if (algorithmProperties == null)
                throw new AlgorithmException(algorithmName, "The algorithm '" + algorithmName + "' does not exist.");

            algorithmProperties.mergeWithAlgorithmParameters(algorithmParameters);

            DataSerialization ds = DataSerialization.summary;

            // Bypass direct composer call in case of iterative algorithm.
            if (algorithmProperties.getType().equals(AlgorithmProperties.AlgorithmType.iterative) ||
                    algorithmProperties.getType().equals(AlgorithmProperties.AlgorithmType.python_iterative)) {

                final IterativeAlgorithmState iterativeAlgorithmState =
                        iterationsHandler.handleNewIterativeAlgorithmRequest(
                                manager, algorithmKey, algorithmProperties, algorithmContainers);

                log.info("Iterative algorithm " + algorithmKey + " execution started.");

                BasicHttpEntity entity = new NIterativeAlgorithmResultEntity(
                        iterativeAlgorithmState, ds, ExaremeGatewayUtils.RESPONSE_BUFFER_SIZE);

                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(entity);
            } else {
                String dfl = Composer.composeDFLScript(algorithmKey, algorithmProperties, algorithmContainers.length);
                try {
                    Composer.persistDFLScriptToAlgorithmsDemoDirectory(
                            HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY + "/" + algorithmKey
                                    + "/" + algorithmKey,
                            dfl, null);
                } catch (IOException e) {
                    // Ignoring error if failed to persist DFL Scripts - it's not something fatal.
                    log.error(e);
                }

                AdpDBClientProperties clientProperties =
                        new AdpDBClientProperties(
                                HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey,
                                "", "", false, false,
                                -1, 10);
                clientProperties.setContainerProxies(algorithmContainers);
                AdpDBClient dbClient =
                        AdpDBClientFactory.createDBClient(manager, clientProperties);
                queryStatus = dbClient.query(algorithmKey, dfl);

                log.info("Algorithm " + algorithmKey + " with queryID "
                        + queryStatus.getQueryID().getQueryID() + " execution started.");
                log.debug("DFL Script: \n " + dfl);

                BasicHttpEntity entity = new NQueryResultEntity(queryStatus, ds,
                        ExaremeGatewayUtils.RESPONSE_BUFFER_SIZE);
                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(entity);
            }
        } catch (IterationsFatalException e) {
            log.error(e);
            if (e.getErroneousAlgorithmKey() != null)
                iterationsHandler.removeIterativeAlgorithmStateInstanceFromISM(
                        e.getErroneousAlgorithmKey());
            log.error(e);
            String errorType = HBPQueryHelper.ErrorResponse.ErrorResponseTypes.error;
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(createErrorResponseEntity(e.getMessage(), errorType));

        } catch (UserException e) {
            log.error(e.getMessage());
            String errorType = HBPQueryHelper.ErrorResponse.ErrorResponseTypes.user_error;
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(createErrorResponseEntity(e.getMessage(), errorType));

        } catch (JsonSyntaxException e) {
            log.error("Could not parse the algorithms properly.");
            String errorType = HBPQueryHelper.ErrorResponse.ErrorResponseTypes.error;
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(createErrorResponseEntity(serverErrorOccurred, errorType));

        } catch (Exception e) {
            log.error(e.getMessage());
            for (StackTraceElement stack : e.getStackTrace()) {
                log.error("Stack: " + stack.toString());
                log.error("Stack class: " + stack.getClassName() + ", name: " + stack.getMethodName() + ", line: " + stack.getLineNumber());
            }
            log.error(e.getStackTrace());
            String errorType = HBPQueryHelper.ErrorResponse.ErrorResponseTypes.error;
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(createErrorResponseEntity(serverErrorOccurred, errorType));
        }
    }

    ///  -----     Helper functions     ----- ///

    /**
     * Checks if the request is a POST request
     *
     * @param request the request of the algorithm
     * @throws UnsupportedHttpVersionException if != POST
     */
    private void preExecutionChecks(HttpRequest request) throws UnsupportedHttpVersionException {
        log.debug("Validate method ...");
        RequestLine requestLine = request.getRequestLine();
        String method = requestLine.getMethod().toUpperCase(Locale.ENGLISH);

        if (!"POST".equals(method)) {
            throw new UnsupportedHttpVersionException(method + " not supported.");
        }
    }

    private String getAlgorithmName(HttpRequest request) {
        RequestLine requestLine = request.getRequestLine();
        String uri = requestLine.getUri();
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    private BasicHttpEntity createErrorResponseEntity(String data, String type) {
        BasicHttpEntity entity = new BasicHttpEntity();
        String result = HBPQueryHelper.ErrorResponse.createErrorResponse(data, type);
        entity.setContent(new ByteArrayInputStream(result.getBytes()));
        return entity;
    }
}



