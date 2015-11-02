package madgik.exareme.master.gateway.async.handler;

import com.google.gson.Gson;
import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import madgik.exareme.master.gateway.OptiqueStreamQueryMetadata.StreamRegisterQuery;
import madgik.exareme.master.registry.Registry;
import org.apache.http.*;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

public class HttpAsyncTablesMetadataHandler implements HttpAsyncRequestHandler<HttpRequest> {
    private static final Logger log = Logger.getLogger(HttpAsyncStreamQueryInfoHandler.class);

    public HttpAsyncTablesMetadataHandler() {
    }

    @Override public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request,
        HttpContext context) throws HttpException, IOException {
        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpAsyncExchange httpExchange, HttpContext context)
        throws HttpException, IOException {
        HttpResponse httpResponse = httpExchange.getResponse();

        log.info("Validating request ..");
        String method = httpRequest.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);

        if (!"GET".equals(method) && !"POST".equals(method)) {
            throw new UnsupportedHttpVersionException(method + "not supported.");
        }

        // parse content
        String content = "";
        if (httpRequest instanceof HttpEntityEnclosingRequest) {
            log.debug("Stream ...");
            HttpEntity entity = ((HttpEntityEnclosingRequest) httpRequest).getEntity();
            content = EntityUtils.toString(entity);
        }

        HashMap<String, String> inputContent = new HashMap<String, String>();
        ExaremeGatewayUtils.getValues(content, inputContent);
        log.trace("Content: " + inputContent.toString());

        String dbname = inputContent.get(ExaremeGatewayUtils.REQUEST_DATABASE);

        log.debug("--DB " + dbname);

        String infoJson = null;
        try {
            Gson gson = new Gson();
            StreamRegisterQuery registerQuery = StreamRegisterQuery.getInstance();
            infoJson = gson.toJson(Registry.getInstance(dbname).getMetadata());
        } catch (Exception ex) {
            log.error(ex);
            HttpResponse response =
                new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ex.getMessage());

            httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
            return;
        }

        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        HttpEntity entity = new NStringEntity(infoJson);

        response.setEntity(entity);
        httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }
}
