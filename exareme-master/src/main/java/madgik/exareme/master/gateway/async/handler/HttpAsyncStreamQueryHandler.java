package madgik.exareme.master.gateway.async.handler;

import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        // parse content
        String content = "";
        if (httpRequest instanceof HttpEntityEnclosingRequest) {
            log.info("Streamming request ...");
            HttpEntity entity = ((HttpEntityEnclosingRequest) httpRequest).getEntity();
            content = EntityUtils.toString(entity);
        }

        HashMap<String, String> inputContent = new HashMap<String, String>();
        ExaremeGatewayUtils.getValues(content, inputContent);

        String query = inputContent.get("register_query");
        String target = httpRequest.getRequestLine().getUri();

        // TODO: DIRTY! ADD PROXY
        CloseableHttpClient httpclient = HttpClients.createDefault();
        List<NameValuePair> formparams = new ArrayList<>();
        formparams.add(new BasicNameValuePair("register_query", query));
        UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        HttpPost httpPost = new HttpPost("http://127.0.0.1:9595" + target);
        httpPost.setEntity(postEntity);

        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                httpResponse.setStatusCode(response.getStatusLine().getStatusCode());
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                httpResponse.setEntity(new InputStreamEntity(entity.getContent()));
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
