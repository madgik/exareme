package madgik.exareme.master.gateway.async.handler;

import com.google.gson.Gson;
import madgik.exareme.master.gateway.OptiqueStreamQueryMetadata.StreamRegisterQuery;
import org.apache.http.*;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Locale;

public class HttpAsyncStreamQueryInfoHandler implements HttpAsyncRequestHandler<HttpRequest> {
    private static final Logger log = Logger.getLogger(HttpAsyncStreamQueryInfoHandler.class);

    public HttpAsyncStreamQueryInfoHandler() {
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

        String infoJson = null;
        try {
            Gson gson = new Gson();
            StreamRegisterQuery registerQuery = StreamRegisterQuery.getInstance();
            infoJson = gson.toJson(registerQuery.getQueriesInfo());
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
