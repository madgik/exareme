package madgik.exareme.master.gateway.async.handler;

import madgik.exareme.master.queryProcessor.composer.Composer;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.UnsupportedHttpVersionException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Locale;

/**
 * Mining  Handler
 *
 * @author alex
 * @since 0.1
 */
public class HttpAsyncMiningAlgorithmsHandler implements HttpAsyncRequestHandler<HttpRequest> {

    private static final Logger log = Logger.getLogger(HttpAsyncMiningAlgorithmsHandler.class);

    private static final Composer composer = Composer.getInstance();

    public HttpAsyncMiningAlgorithmsHandler() {
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
        handleInternal(request, response, context);
        httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private void handleInternal(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {

        log.debug("Validate method ...");
        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);

        if (!"GET".equals(method)) {
            throw new UnsupportedHttpVersionException(method + "not supported.");
        }

        response.setEntity(new NStringEntity(composer.getAlgorithms()));
    }
}
