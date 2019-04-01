package madgik.exareme.master.gateway.async.handler;

import madgik.exareme.utils.properties.AdpProperties;
import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NFileEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Locale;

/**
 * @author alexpap
 * @version 0.1
 */
public class HttpAsyncFileHandler implements HttpAsyncRequestHandler<HttpRequest> {

    private static final Logger log = Logger.getLogger(HttpAsyncFileHandler.class);
    private static final File docRoot;

    static {
        String staticPath = AdpProperties.getGatewayProperties().getString("static.path");
        log.debug("Static Path : " + staticPath);
        docRoot = new File(staticPath);
    }

    public HttpAsyncFileHandler() {
        super();
    }

    public HttpAsyncRequestConsumer<HttpRequest> processRequest(
            final HttpRequest request,
            final HttpContext context) {
        // Buffer request content in memory for simplicity
        return new BasicAsyncRequestConsumer();
    }

    public void handle(
            final HttpRequest request,
            final HttpAsyncExchange httpexchange,
            final HttpContext context) throws HttpException, IOException {
        HttpResponse response = httpexchange.getResponse();
        handleInternal(request, response, context);
        httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private void handleInternal(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {

        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }

        String target = request.getRequestLine().getUri();
        final File file = new File(this.docRoot, URLDecoder.decode(target, "UTF-8"));
        if (!file.exists()) {

            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            NStringEntity entity = new NStringEntity(
                    "<html><body><h1>File" +
                            " not found</h1></body></html>",
                    ContentType.create("text/html", "UTF-8"));
            response.setEntity(entity);
            System.out.println("File " + file.getPath() + " not found");

        } else if (!file.canRead() || file.isDirectory() || !file.getCanonicalPath().startsWith(this.docRoot.getCanonicalPath())) {

            response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            NStringEntity entity = new NStringEntity(
                    "<html><body><h1>Access denied</h1></body></html>",
                    ContentType.create("text/html", "UTF-8"));
            response.setEntity(entity);
            System.out.println("Cannot read file " + file.getPath());

        } else if (file.getPath().endsWith(".ser")) {
            HttpCoreContext coreContext = HttpCoreContext.adapt(context);
            HttpConnection conn = coreContext.getConnection(HttpConnection.class);
            response.setStatusCode(HttpStatus.SC_OK);
            NFileEntity body = new NFileEntity(file, ContentType.create("application/octet-stream"));
            response.setEntity(body);
            System.out.println(conn + ": serving binary file " + file.getPath());
        } else {

            HttpCoreContext coreContext = HttpCoreContext.adapt(context);
            HttpConnection conn = coreContext.getConnection(HttpConnection.class);
            response.setStatusCode(HttpStatus.SC_OK);
            NFileEntity body = new NFileEntity(file, ContentType.create("text/html"));
            response.setEntity(body);
            System.out.println(conn + ": serving file " + file.getPath());
        }
    }

}
