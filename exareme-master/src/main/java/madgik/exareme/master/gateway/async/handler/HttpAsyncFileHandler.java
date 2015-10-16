package madgik.exareme.master.gateway.async.handler;

import org.apache.http.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author alex
 */
public class HttpAsyncFileHandler implements HttpAsyncRequestHandler<HttpRequest> {
    @Override public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request,
        HttpContext context) throws HttpException, IOException {
        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpAsyncExchange httpExchange, HttpContext context)
        throws HttpException, IOException {
        HttpResponse httpResponse = httpExchange.getResponse();
        System.out.println(""); // empty line before each request
        System.out.println(httpRequest.getRequestLine());
        System.out.println("-------- HEADERS --------");
        for (Header header : httpRequest.getAllHeaders()) {
            System.out.println(header.getName() + " : " + header.getValue());
        }
        System.out.println("--------");

        HttpEntity entity = null;
        if (httpRequest instanceof HttpEntityEnclosingRequest)
            entity = ((HttpEntityEnclosingRequest) httpRequest).getEntity();

        // For some reason, just putting the incoming entity into
        // the response will not work. We have to buffer the message.
        byte[] data;
        if (entity == null) {
            data = new byte[0];
        } else {
            data = EntityUtils.toByteArray(entity);
        }

        System.out.println(new String(data));
        System.out.println("--------");

        httpResponse.setEntity(
            new InputStreamEntity(new FileInputStream(new File("src/test/resources/emp.json"))));
        httpExchange.submitResponse(new BasicAsyncResponseProducer(httpResponse));

    }
}
