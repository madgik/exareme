package madgik.exareme.master.gateway.async.handler;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @author alex
 */
public class HttpAsyncMainHandler implements HttpAsyncRequestHandler<HttpRequest> {

    private static final Logger log = Logger.getLogger(HttpAsyncMainHandler.class);
    private static final String msg = "{ "
        + "\"schema\":[[\"error\",\"text\"]], "
        + "\"errors\":[[null]] " + "}\n"
        + "[\"Not supported.\" ]";

    @Override public HttpAsyncRequestConsumer<HttpRequest> processRequest(
        HttpRequest request,
        HttpContext context) throws HttpException, IOException {

        return new BasicAsyncRequestConsumer();
    }

    @Override public void handle(
        HttpRequest httpRequest,
        HttpAsyncExchange httpExchange,
        HttpContext context) throws HttpException, IOException {

        log.debug("New request on main handler");
        HttpResponse httpResponse = httpExchange.getResponse();
        httpResponse.setEntity(new NStringEntity(msg));
        httpExchange.submitResponse(new BasicAsyncResponseProducer(httpResponse));
    }
}
