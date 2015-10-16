package madgik.exareme.master.gateway.async.handler;

import madgik.exareme.master.gateway.OptiqueStreamQueryMetadata.StreamRegisterQuery;
import org.apache.http.*;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Locale;

//import org.apache.commons.lang.StringEscapeUtils;


public class HttpAsyncResultStreamQueryHandler implements HttpAsyncRequestHandler<HttpRequest> {
    private static final Logger log = Logger.getLogger(HttpAsyncResultStreamQueryHandler.class);

    public HttpAsyncResultStreamQueryHandler() {
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

        if (!"GET".equals(method)) {
            throw new UnsupportedHttpVersionException(method + "not supported.");
        }

        String target = httpRequest.getRequestLine().getUri();
        String queryId = target.substring(target.lastIndexOf('/') + 1, target.lastIndexOf('?'));

        log.info("Result Query : " + queryId);

        StreamRegisterQuery.QueryInfo info = null;
        try {
            StreamRegisterQuery registerQuery = StreamRegisterQuery.getInstance();
            info = registerQuery.get(queryId);

            if (info == null) {
                HttpResponse response =
                    new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_FOUND, "OK");
                HttpEntity entity = new NStringEntity("Stream " + queryId + " Not Found");

                response.setEntity(entity);
                httpExchange.submitResponse(new BasicAsyncResponseProducer(response));

                return;
            }

        } catch (Exception ex) {
            log.error(ex);
            HttpResponse response =
                new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "");
            //            HttpEntity entity = new NStringEntity(
            //                    String.format("{\"schema\":[[\"status\", \"null\"]]}\n[\"%s\"]\n",
            //                    StringEscapeUtils.escapeJavaScript(ex.getMessage()))
            //            );

            HttpEntity entity = new NStringEntity(String
                .format("{\"schema\":[[\"status\", \"null\"]]}\n[\"%s\"]\n", ex.getMessage()));

            response.setEntity(entity);
            httpExchange.submitResponse(new BasicAsyncResponseProducer(response));

            return;
        }

        log.info("Redirect to URL: http://" + info.ip + ":" + info.port + target);
        HttpResponse response =
            new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_TEMPORARY_REDIRECT, "");
        response.addHeader("Location", "http://" + info.ip + ":" + info.port + target);
        httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }
}
