package madgik.exareme.master.gateway.async.handler;

import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

public class HttpAsyncDeleteStreamQueryHandler implements HttpAsyncRequestHandler<HttpRequest> {
    private static final Logger log = Logger.getLogger(HttpAsyncDeleteStreamQueryHandler.class);

    public HttpAsyncDeleteStreamQueryHandler() {
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

        if (!"DELETE".equals(method)) {
            throw new UnsupportedHttpVersionException(method + "not supported.");
        }

        String target = httpRequest.getRequestLine().getUri();

        // TODO: DIRTY! ADD PROXY
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://127.0.0.1:9595" + target);
        httpGet.addHeader("accept", (httpRequest.getFirstHeader("accept") == null) ? "" : httpRequest.getFirstHeader("accept").getValue());

        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                httpResponse.setStatusCode(response.getStatusLine().getStatusCode());
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(entity.getContent(), writer, "UTF-8");
                httpResponse.setEntity(new StringEntity(writer.toString()));
                httpExchange.submitResponse(new BasicAsyncResponseProducer(httpResponse));
            }
        } catch (IOException e) {
            log.error(e);
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setEntity(new NStringEntity("ERROR"));
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
