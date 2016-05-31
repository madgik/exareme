package madgik.exareme.master.gateway.async.handler;

import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import madgik.exareme.master.queryProcessor.composer.AlgorithmsProperties;
import madgik.exareme.master.queryProcessor.composer.Composer;
import madgik.exareme.master.queryProcessor.composer.ComposerConstants;
import madgik.exareme.master.queryProcessor.composer.ComposerException;
import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

/**
 * Mining  Handler
 * <p/>
 * GET  /mining/algorithms                        - list algorithms properties
 * <p/>
 * POST /mining/query/<algorithm-name>            - exec algorithm and returns results
 *
 * @author alex
 * @since 0.1
 */
public class HttpAsyncMiningQueryHandler implements HttpAsyncRequestHandler<HttpRequest> {

    private static final Logger log = Logger.getLogger(HttpAsyncMiningQueryHandler.class);
    private static final String msg =
        "{ " + "\"schema\":[[\"error\",\"text\"]], " + "\"errors\":[[null]] " + "}\n";

    private static final AdpDBManager manager = AdpDBManagerLocator.getDBManager();
    private static final Composer composer = Composer.getInstance();

    public HttpAsyncMiningQueryHandler() {
    }

    @Override public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request,
        HttpContext context) throws HttpException, IOException {

        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest request, HttpAsyncExchange httpExchange, HttpContext context)
        throws HttpException, IOException {

        HttpResponse response = httpExchange.getResponse();
        response.setHeader("Content-Type", String.valueOf(ContentType.APPLICATION_JSON));
        handleInternal(request, response, context);
        httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private void handleInternal(HttpRequest request, HttpResponse response, HttpContext context)
        throws HttpException, IOException {

        log.debug("Validate method ...");
        RequestLine requestLine = request.getRequestLine();
        String uri = requestLine.getUri();
        String method = requestLine.getMethod().toUpperCase(Locale.ENGLISH);

        log.debug("Parsing content ...");
        String content = "";
        if (request instanceof HttpEntityEnclosingRequest) {

            log.debug("Streaming ...");
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            content = EntityUtils.toString(entity);
        }
        HashMap<String, String> inputContent = new HashMap<String, String>();
        if (content != null && !content.isEmpty()) {
            ExaremeGatewayUtils.getValues(content, inputContent);
        }
        if (!"GET".equals(method) && !"POST".equals(method)) {
            throw new UnsupportedHttpVersionException(method + "not supported.");
        }

        log.debug("Match uri ...");
        if (uri.matches("/mining/algorithm(.*)")) {

            String substring = uri.substring("/mining/algorithms".length());
            if (substring.isEmpty()) {             // list

                try {
                    response.setEntity(new NStringEntity(composer.getAlgorithms()));
                } catch (ComposerException e) {
                    log.error(e);
                    response.setEntity(new NStringEntity(msg));
                }
            } else {
                response.setEntity(new NStringEntity(msg));
            }
        } else if (uri.matches("/mining/endpoint(.*)")) {

            String substring = uri.substring("/mining/endpoints".length());
            if (substring.isEmpty()) {             // list

                try {
                    response.setEntity(new NStringEntity(composer.getEndpoints()));
                } catch (ComposerException e) {
                    log.error(e);
                    response.setEntity(new NStringEntity(msg));
                }
            } else {
                String algorithmKey = substring.substring(1);
                if (method.equals("GET")) {        // view
                    response.setEntity(new NStringEntity(msg));
                } else if (method.equals("POST")) {  // add
                    response.setEntity(new NStringEntity(msg));
                }
            }
        } else if (uri.matches("/mining/query(.*)")) {

            String substring = uri.substring("/mining/query".length());
            if (substring.isEmpty()) {             // list

                response.setEntity(new NStringEntity(msg));
            } else {

//                if (substring.matches("(.*)/status")) { // status
//
//                    log.debug("Posting status ...");
//                    String[] split = uri.split("/");
//
//                    AdpDBClientQueryStatus queryStatus = queries.get(split[3]);
//
//                    if (queryStatus == null)
//                        response.setEntity(new NStringEntity(msg));
//                    else {
//                        String status = queryStatus.getStatus();
//                        int start = status.indexOf(':');
//                        int end = status.indexOf('%');
//                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//
//                        response.setEntity(new NStringEntity(gson.toJson(
//                            new AlgorithmResult(null, status.substring(start + 2, end - 1)))));
//                        log.info(status.substring(start + 2, end - 1));
//                    }
//                } else if (substring.matches("(.*)/result")) { //result
//                    log.debug("Posting result ...");
//                    String[] split = uri.split("/");
//                    AdpDBClientQueryStatus queryStatus = queries.get(split[3]);
//                    if (queryStatus == null) {
//                        response.setEntity(new NStringEntity(msg));
//                        log.warn("query status null!");
//                    } else if (queryStatus.hasFinished() == true
//                        && queryStatus.hasError() == true) {
//                        response.setEntity(new NStringEntity(msg));
//                        log.warn("query status finsish with error");
//                    } else if (queryStatus.hasFinished() == true
//                        && queryStatus.hasError() == false) {
//                        try {
//                            log.debug(queryStatus.getExecutionTime());
//                            AdpDBClientProperties clientProperties =
//                                new AdpDBClientProperties("/tmp/demo/db/" + split[3], "", "", false,
//                                    false, -1, 10);
//                            AdpDBClient dbClient =
//                                AdpDBClientFactory.createDBClient(manager, clientProperties);
//                            response.removeHeaders("Content-Type");
//                            response.addHeader("Content-Type", "application/x-ldjson");
//
//                            DataSerialization ds = DataSerialization.ldjson;
//                            for (Header accept : request.getHeaders("Accept")) {
//                                if (accept.getValue().equals("application/json")) {
//                                    response.removeHeaders("Content-Type");
//                                    response.addHeader("Content-Type", "application/json");
//                                    ds = DataSerialization.avro;
//                                    break;
//                                }
//                            }
//                            Boolean format = Boolean.valueOf(inputContent.get("format"));
//
//                            if (format)
//                                ds = DataSerialization.summary;
//                            // blocking reading TODO async like status
//                            // TODO check error handling output format
//
//                            InputStream readTable = dbClient.readTable("output_" + split[3], ds);
//                            if (readTable == null) {
//                                log.error("Registry not updated yet.");
//                                response.setEntity(new NStringEntity(msg));
//                                return;
//                            }
//                            response.setEntity(new InputStreamEntity(readTable));
//                        } catch (Exception e) {
//                            log.error("Unable to format result.", e);
//                            response.setEntity(new NStringEntity(msg));
//                        }
//                    } else {
//                        response.setEntity(new NStringEntity(queryStatus.getError()));
//                    }
//                } else {
                    String algorithmKey = substring.substring(1);
                    if (method.equals("GET")) {        // view

                    } else if (method.equals("POST")) {  // add
                        log.debug("Posting alg ...\n");
                        for (String k : inputContent.keySet()) {
                            log.info(k + " = " + inputContent.get(k));
                        }
                        String r = null;
                        String[] split = uri.split("/");
                        String algorithmName = split[3];
                        String qKey = "query_" + String.valueOf(System.currentTimeMillis());
                        try {

                            String input_local_tbl = "";

                            String dfl = null;
                            inputContent.put(ComposerConstants.inputLocalTblKey, input_local_tbl);
                            inputContent
                                .put(ComposerConstants.outputGlobalTblKey, "output_" + qKey);
                            inputContent.put(ComposerConstants.algorithmKey, algorithmName);
                            AlgorithmsProperties.AlgorithmProperties algorithmProperties =
                                AlgorithmsProperties.AlgorithmProperties.createAlgorithmProperties(inputContent);
                            dfl = composer.composeVirtual(qKey, algorithmProperties);

                            log.debug(dfl);
                            AdpDBClientProperties clientProperties =
                                new AdpDBClientProperties("/tmp/demo/db/" + qKey, "", "", false,
                                    false, -1, 10);
//                            AdpDBClient dbClient =
//                                AdpDBClientFactory.createDBClient(manager, clientProperties);
//                            AdpDBClientQueryStatus queryStatus = dbClient.query(qKey, dfl);


                        } catch (ComposerException e) {
                            log.error(e);
                            r = e.toString();
                        } catch (Exception e) {
                            log.error(e);
                            throw new IOException("Unable to compose dfl.");
                        }
                        response.setEntity(new NStringEntity(msg));
                    }
//                }
            }
        } else {
            response.setEntity(new NStringEntity(msg));
        }
    }

}
