package madgik.exareme.master.gateway.async.handler;

import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import madgik.exareme.master.gateway.OptiqueStreamQueryMetadata.StreamRegisterQuery;
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

public class HttpAsyncStreamQueryHandler implements HttpAsyncRequestHandler<HttpRequest> {
    private static final Logger log = Logger.getLogger(HttpAsyncStreamQueryHandler.class);

    public HttpAsyncStreamQueryHandler() {
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

        if (!"PUT".equals(method) && !"POST".equals(method)) {
            throw new UnsupportedHttpVersionException(method + "not supported.");
        }

        String target = httpRequest.getRequestLine().getUri();
        String queryId = target.substring(target.lastIndexOf('/') + 1);

        // parse content
        String content = "";
        if (httpRequest instanceof HttpEntityEnclosingRequest) {
            log.info("Streamming request ...");
            HttpEntity entity = ((HttpEntityEnclosingRequest) httpRequest).getEntity();
            content = EntityUtils.toString(entity);
        }

        HashMap<String, String> inputContent = new HashMap<String, String>();
        ExaremeGatewayUtils.getValues(content, inputContent);

        String query = inputContent.get(ExaremeGatewayUtils.REQUEST_STREAMQUERY);
        log.info("Query : " + query);

        try {
            StreamRegisterQuery registerQuery = StreamRegisterQuery.getInstance();
            registerQuery.add(queryId, query);
        } catch (Exception ex) {
            log.error(ex);
            HttpResponse response =
                new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ex.getMessage());
            HttpEntity entity = new NStringEntity(ex.getMessage());

            response.setEntity(entity);
            httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
            return;
        }

        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }
}
