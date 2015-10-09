package madgik.exareme.master.gateway.async.handler;

import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

//import org.apache.commons.lang.StringEscapeUtils;


/**
 * Exareme Table Handler.
 *
 * @author alex
 * @since 0.1
 */
public class HttpAsyncTableHandler implements HttpAsyncRequestHandler<HttpRequest> {
    private static final Logger log = Logger.getLogger(HttpAsyncTableHandler.class);

    private static final AdpDBManager manager = AdpDBManagerLocator.getDBManager();

    public HttpAsyncTableHandler() {
    }

    @Override public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request,
        HttpContext context) throws HttpException, IOException {
        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest request, HttpAsyncExchange httpExchange, HttpContext context)
        throws HttpException, IOException {
        HttpResponse response = httpExchange.getResponse();
        handleInternal(request, response, context);
        httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private void handleInternal(HttpRequest request, HttpResponse response, HttpContext context)
        throws UnsupportedHttpVersionException, IOException {
        // validate request
        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        log.trace("Validate request : " + method);
        if (!"GET".equals(method) && !"POST".equals(method))
            throw new UnsupportedHttpVersionException(method + "not supported.");

        // parse content
        String content = "";
        if (request instanceof HttpEntityEnclosingRequest) {
            log.debug("Stream ...");
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            content = EntityUtils.toString(entity);
        }

        HashMap<String, String> inputContent = new HashMap<String, String>();
        ExaremeGatewayUtils.getValues(content, inputContent);
        log.trace("Content: " + inputContent.toString());

        String dbname = inputContent.get(ExaremeGatewayUtils.REQUEST_DATABASE);
        String table = inputContent.get(ExaremeGatewayUtils.REQUEST_TABLE);

        log.debug("--DB " + dbname);
        log.debug("--Table " + table);

        HttpEntity body = null;
        try {
            AdpDBClient dbClient =
                AdpDBClientFactory.createDBClient(manager, new AdpDBClientProperties(dbname));
            body = new InputStreamEntity(dbClient.readTable(table), ContentType.TEXT_PLAIN);
            log.trace("entity is stream.");
        } catch (Exception e) {
            log.warn("|||", e);
            //      body = new StringEntity(String
            //          .format("{\"schema\":[[\"error\", \"null\"]]}\n[\"%s\"]\n",
            //              StringEscapeUtils.escapeJavaScript(e.getCause().getMessage())));
            body = new StringEntity(String
                .format("{\"schema\":[[\"error\", \"null\"]]}\n[\"%s\"]\n",
                    e.getCause().getMessage()));
        }
        response.setEntity(body);
    }

}
