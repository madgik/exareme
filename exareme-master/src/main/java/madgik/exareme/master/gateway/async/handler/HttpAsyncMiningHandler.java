package madgik.exareme.master.gateway.async.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.connector.DataSerialization;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import madgik.exareme.master.queryProcessor.composer.*;
import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

/**
 * Mining  Handler.
 *
 * GET  /mining/algorithms                        - list algorithms
 *
 * GET  /mining/endpoints                         - list endpoints
 *
 * POST /mining/query/<query-key>                 - add new query
 * POST /mining/query/<query-key>/status          - get query status.
 * POST /mining/query/<query-key>/result          - get query result.
 *
 * Supports exa-view.
 *
 * @author alex
 * @since 0.1
 */
public class HttpAsyncMiningHandler implements HttpAsyncRequestHandler<HttpRequest> {
    private static final Logger log = Logger.getLogger(HttpAsyncMiningHandler.class);
    private static final String msg = "{ "
        + "\"schema\":[[\"error\",\"null\"]], "
        + "\"errors\":[[null]] "
        + "}\n"
        + "[\"Not supported.\" ]\n";

    private static final AdpDBManager manager = AdpDBManagerLocator.getDBManager();
    private static final Composer composer = Composer.getInstance();
    private static final HashMap<String, AdpDBClientQueryStatus> queries = new HashMap<>();

    public HttpAsyncMiningHandler() {
    }

    @Override
    public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request,
        HttpContext context) throws HttpException, IOException {
        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest request, HttpAsyncExchange httpExchange, HttpContext context)
        throws HttpException, IOException {
        HttpResponse response = httpExchange.getResponse();
        response.setHeader("Content-Type", String.valueOf(ContentType.APPLICATION_JSON));
        try {
            handleInternal(request, response, context);
        } catch (Exception e){
            log.error(e);
            throw new HttpException("Internal error\n", e);
        }
        httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private void handleInternal(HttpRequest request,
        HttpResponse response,
        HttpContext context) throws HttpException, IOException {

        log.debug("Validate method ...");
        RequestLine requestLine = request.getRequestLine();
        String uri = requestLine.getUri();
        String method = requestLine.getMethod().toUpperCase(Locale.ENGLISH);

        log.debug("Method : " + method);
        log.debug("URI : " + uri.toString());

        log.debug("Parsing content ...");
        String content = "";
        if (request instanceof HttpEntityEnclosingRequest) {
            log.debug("Stream ...");
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            content = EntityUtils.toString(entity);
        }

        HashMap<String, String> inputContent = new HashMap<String, String>();
        if (content != null && !content.isEmpty()) {
            ExaremeGatewayUtils.getValues(content, inputContent);
        }

        if (!"GET".equals(method) && !"POST".equals(method))
            throw new UnsupportedHttpVersionException(method + "not supported.");

        log.debug("Match uri ...");

        if (uri.matches("/mining/algorithm(.*)")){
            String substring = uri.substring("/mining/algorithms".length());
            if (substring.isEmpty()){             // list
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
            if (substring.isEmpty()){             // list
                try {
                    response.setEntity(new NStringEntity(composer.getEndpoints()));
                } catch (ComposerException e) {
                    log.error(e);
                    response.setEntity(new NStringEntity(msg));
                }
            } else {
                String algorithmKey = substring.substring(1);
                if ( method.equals("GET")) {        // view
                    response.setEntity(new NStringEntity(msg));
                } else if (method.equals("POST")){  // add
                    response.setEntity(new NStringEntity(msg));
                }
            }
        } else if (uri.matches("/mining/query(.*)")){
            String substring = uri.substring("/mining/query".length());
            if (substring.isEmpty()){             // list
                response.setEntity(new NStringEntity(msg));
            } else {
                if ( substring.matches("(.*)/status")){ // status
                    log.debug("Posting status ...");
                    String[] split = uri.split("/");

                    AdpDBClientQueryStatus queryStatus = queries.get(split[3]);

                    if (queryStatus == null) response.setEntity(new NStringEntity(msg));
                    else {
                        String status = queryStatus.getStatus();
                        int start = status.indexOf(':');
                        int end = status.indexOf('%');
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();

                        response.setEntity(new NStringEntity(
                            gson.toJson(new AlgorithmResult(null, status.substring(start + 2, end - 1))))
                        );
                        log.info(status.substring(start + 2, end - 1));
                    }
                } else if ( substring.matches("(.*)/result")){ //result
                    log.debug("Posting result ...");
                    String[] split = uri.split("/");
                    AdpDBClientQueryStatus queryStatus = queries.get(split[3]);
                    if (queryStatus == null) response.setEntity(new NStringEntity(msg));
                    else if (queryStatus.hasFinished() == false && queryStatus.hasError() == false)
                        response.setEntity(new NStringEntity(msg));

                    if (queryStatus.hasError() == false) {
                        try {
                            log.debug(queryStatus.getExecutionTime());
                            AdpDBClientProperties clientProperties =
                                new AdpDBClientProperties("/tmp/demo/db/" + split[3], "", "", false,
                                    false, -1, 10);
                            AdpDBClient dbClient =
                                AdpDBClientFactory.createDBClient(manager, clientProperties);
                            response.removeHeaders("Content-Type");
                            response.addHeader("Content-Type", "application/x-ldjson");

                            DataSerialization ds = DataSerialization.ldjson;
                            for (Header accept : request.getHeaders("Accept")) {
                                if (accept.getValue().equals("application/json")) {
                                    response.removeHeaders("Content-Type");
                                    response.addHeader("Content-Type", "application/json");
                                    ds = DataSerialization.avro;
                                    break;
                                }
                            }
                            Boolean format = Boolean.valueOf(inputContent.get("format"));

                            if (format)
                                ds = DataSerialization.summary;
                            // blocking reading TODO async like status
                            // TODO check error handling output format
                            response.setEntity(new InputStreamEntity(
                                dbClient.readTable("output_" + split[3], ds)));
                        }catch (Exception e){
                            throw new IOException("Unable to format result.", e);
                        }
                    } else response.setEntity(new NStringEntity(queryStatus.getError()));
                } else {
                    String algorithmKey = substring.substring(1);
                    if (method.equals("GET")) {        // view

                    } else if (method.equals("POST")) {  // add
                        log.debug("Posting alg ...\n" );
                        for (String k: inputContent.keySet()){
                            log.info( k + " = " + inputContent.get(k));
                        }
                        String r = null;
                        String[] split = uri.split("/");
                        String algorithmName = split[3];
                        String qKey = "query_" + String.valueOf(System.currentTimeMillis());
                        try {

                            String input_local_tbl = composer.getDefaultInputLocalTBL("");

                            String dfl = null;
                            inputContent.put(ComposerConstants.inputLocalTblKey, input_local_tbl);
                            inputContent.put(ComposerConstants.outputGlobalTblKey, "output_" + qKey);
                            inputContent.put(ComposerConstants.algorithmKey, algorithmName);
                            AlgorithmProperties algorithmProperties = AlgorithmProperties.createAlgorithmProperties(inputContent);
                            dfl = composer.composeVirtual(qKey, algorithmProperties);

                            log.debug(dfl);
                            AdpDBClientProperties clientProperties = new AdpDBClientProperties("/tmp/demo/db/" + qKey, "", "", false, false, -1, 10);
                            AdpDBClient dbClient = AdpDBClientFactory.createDBClient(manager, clientProperties);
                            AdpDBClientQueryStatus queryStatus = dbClient.query(qKey, dfl);
                            queries.put(qKey, queryStatus);
                            r = queryStatus.getStatus();
                        } catch (ComposerException e) {
                            log.error(e);
                            r = e.toString();
                        } catch (Exception e) {
                            log.error(e);
                            throw new IOException("Unable to compose dfl.");
                        }
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        response.setEntity(new NStringEntity(gson.toJson(new AlgorithmResult(qKey), AlgorithmResult.class)));
                    }
                }
            }
        } else {
            response.setEntity(new NStringEntity(msg));
        }
    }

}
