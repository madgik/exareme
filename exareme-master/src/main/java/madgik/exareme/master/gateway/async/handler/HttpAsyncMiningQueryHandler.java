package madgik.exareme.master.gateway.async.handler;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.UnsupportedHttpVersionException;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import madgik.exareme.master.gateway.async.handler.entity.NQueryResultEntity;
import madgik.exareme.master.queryProcessor.composer.AlgorithmsProperties;
import madgik.exareme.master.queryProcessor.composer.Composer;
import madgik.exareme.master.queryProcessor.composer.ComposerConstants;
import madgik.exareme.master.queryProcessor.composer.ComposerException;
import madgik.exareme.utils.encoding.Base64Util;

/**
 * Mining  Handler
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
    private static final IterationsHandler iterationsHandler = IterationsHandler.getInstance();

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
        List<Map> parameters = new ArrayList();
        if (content != null && !content.isEmpty()) {
//            ExaremeGatewayUtils.getValues(content, inputContent);
            parameters = new Gson().fromJson(content, List.class);
        }
        if (!"POST".equals(method)) {
            throw new UnsupportedHttpVersionException(method + "not supported.");
        }

        String query = null;
        String algorithm = uri.substring(uri.lastIndexOf('/')+1);
        boolean format = false;
        log.debug("Posting " + algorithm + " ...\n");
        for (Map k : parameters) {
            String name = (String) k.get("name");
            String value = (String) k.get("value");
            if(name == null || name.isEmpty() || value == null || value.isEmpty()) continue;
            if("local_pfa".equals(name)) {
                Map map = new Gson().fromJson(value, Map.class);
                query = (String) ((Map) ((Map)((Map) map.get("cells")).get("query")).get("init")).get("sql");
                value = Base64Util.simpleEncodeBase64(value);
            } else if("format".equals(name)){
                format = Boolean.parseBoolean(value);
            }
            inputContent.put(name, value);
            log.debug(name + " = " + value);
        }
        String qKey = "query_" + algorithm + "_" +String.valueOf(System.currentTimeMillis());
        try {
            String dfl;
            AdpDBClientQueryStatus queryStatus;

            inputContent.put(ComposerConstants.outputGlobalTblKey, "output_" + qKey);
            inputContent.put(ComposerConstants.algorithmKey, algorithm);
            AlgorithmsProperties.AlgorithmProperties algorithmProperties =
                    AlgorithmsProperties.AlgorithmProperties.createAlgorithmProperties(inputContent);

            // Was initialized to "DataSerialization.ldjson", and that was followed with
            // if(format) ds = DataSerialization.summary; but was commented-out.
            DataSerialization ds = DataSerialization.summary;

            // Bypass direct composer call in case of iterative algorithm.
            if (algorithmProperties.getType() ==
                    AlgorithmsProperties.AlgorithmProperties.AlgorithmType.iterative) {

                final IterativeAlgorithmState iterativeAlgorithmState =
                        iterationsHandler.handleNewIterativeAlgorithmRequest(
                                manager, algorithmProperties);

                BasicHttpEntity entity = new NIterativeAlgorithmResultEntity(
                        iterativeAlgorithmState, ds, ExaremeGatewayUtils.RESPONSE_BUFFER_SIZE);
                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(entity);
            } else {
                dfl = composer.composeVirtual(qKey, algorithmProperties, query, null);
                log.debug(dfl);
                AdpDBClientProperties clientProperties =
                        new AdpDBClientProperties(
                                ComposerConstants.demoDbWorkingDirectory + qKey,
                                "", "", false, false,
                                -1, 10);
                AdpDBClient dbClient =
                        AdpDBClientFactory.createDBClient(manager, clientProperties);
                queryStatus = dbClient.query(qKey, dfl);
                BasicHttpEntity entity = new NQueryResultEntity(queryStatus, ds,
                        ExaremeGatewayUtils.RESPONSE_BUFFER_SIZE);
                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(entity);
            }

        } catch (ComposerException e) {
            log.error(e);
        } catch (IterationsFatalException e) {
            if (e.getErroneousAlgorithmKey() != null)
                iterationsHandler.removeIterativeAlgorithmStateInstanceFromISM(
                        e.getErroneousAlgorithmKey());
            log.error(e);
        } catch (Exception e) {
            log.error(e);
            throw new IOException(e.getMessage(), e);
        }
    }
}

