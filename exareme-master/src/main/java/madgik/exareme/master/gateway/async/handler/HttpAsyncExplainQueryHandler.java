package madgik.exareme.master.gateway.async.handler;

import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import org.apache.http.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

/**
 * Query Handler.
 *
 * @author alex
 * @since 0.1
 */
public class HttpAsyncExplainQueryHandler implements HttpAsyncRequestHandler<HttpRequest> {
    private static final Logger log = Logger.getLogger(HttpAsyncExplainQueryHandler.class);
    private static final AdpDBManager manager = AdpDBManagerLocator.getDBManager();

    public HttpAsyncExplainQueryHandler() {
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
        String dbname = null;
        String query = null;
        String mode = "";
        try {
            log.info("Validating request ..");
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);

            if (!"GET".equals(method) && !"POST".equals(method))
                throw new UnsupportedHttpVersionException(method + "not supported.");

            // parse content
            String content = "";
            if (request instanceof HttpEntityEnclosingRequest) {
                log.info("Streamming request ...");
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                content = EntityUtils.toString(entity);
            }

            HashMap<String, String> inputContent = new HashMap<String, String>();
            ExaremeGatewayUtils.getValues(content, inputContent);

            dbname = inputContent.get(ExaremeGatewayUtils.REQUEST_DATABASE);
            query = inputContent.get(ExaremeGatewayUtils.REQUEST_QUERY);
            mode = inputContent.get(ExaremeGatewayUtils.REQUEST_EXPLAINMODE);
            log.info("Database : " + dbname);
            query = query.replaceAll("; ", ";\n\n");
            log.info("Query : " + query);
            log.info("Mode : " + mode);
        } catch (Exception ex) {
            log.error(ex);
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new NStringEntity("ERROR"));
            return;
        }

        String queryExplain = null;
        try {
            AdpDBClient dbClient =
                AdpDBClientFactory.createDBClient(manager, new AdpDBClientProperties(dbname));
            queryExplain = dbClient.explain(query, mode);
            StringEntity entity = new StringEntity(queryExplain);
            response.setStatusCode(HttpStatus.SC_OK);
            response.setHeader("Content-Type", "text/html; charset=utf-8");
            response.setEntity(entity);
        } catch (Exception ex) {
            log.error(ex);
            response.setEntity(new NStringEntity(ex.getMessage()));
        }

    }
}
