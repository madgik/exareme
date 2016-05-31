package madgik.exareme.master.gateway.async.handler;

import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import madgik.exareme.master.gateway.async.handler.entity.NQueryResultEntity;
import madgik.exareme.master.gateway.async.handler.entity.NQueryStatusEntity;
import madgik.exareme.master.queryProcessor.composer.AlgorithmsProperties;
import madgik.exareme.master.queryProcessor.composer.Composer;
import madgik.exareme.master.queryProcessor.composer.ComposerConstants;
import madgik.exareme.master.queryProcessor.composer.ComposerException;
import org.apache.http.*;
import org.apache.http.entity.BasicHttpEntity;
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
        if (!"POST".equals(method)) {
            throw new UnsupportedHttpVersionException(method + "not supported.");
        }


        String algorithm = uri.substring(uri.lastIndexOf('/')+1);
        log.debug("Posting " + algorithm + " ...\n");
        for (String k : inputContent.keySet()) {
            log.info(k + " = " + inputContent.get(k));
        }
        String qKey = "query_" + algorithm + "_" +String.valueOf(System.currentTimeMillis());
        try {

            String dfl = null;
            inputContent.put(ComposerConstants.outputGlobalTblKey, "output_" + qKey);
            inputContent.put(ComposerConstants.algorithmKey, algorithm);
            AlgorithmsProperties.AlgorithmProperties algorithmProperties =
                AlgorithmsProperties.AlgorithmProperties.createAlgorithmProperties(inputContent);

            dfl = composer.composeVirtual(qKey, algorithmProperties);

            log.debug(dfl);
            AdpDBClientProperties clientProperties =
                new AdpDBClientProperties("/tmp/demo/db/" + qKey, "", "", false, false, -1, 10);

            AdpDBClient dbClient =
                AdpDBClientFactory.createDBClient(manager, clientProperties);
            AdpDBClientQueryStatus queryStatus = dbClient.query(qKey, dfl);
            BasicHttpEntity entity = new NQueryResultEntity(queryStatus);
            response.setStatusCode(HttpStatus.SC_OK);
            response.setEntity(entity);

        } catch (ComposerException e) {
            log.error(e);
        } catch (Exception e) {
            log.error(e);
            throw new IOException("Unable to compose dfl.");
        }
    }
}

