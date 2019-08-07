package madgik.exareme.master.gateway.control.handler;

import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Locale;

/**
 * @author alexpap
 * @version 0.1
 */
public class HttpAsyncRemoveWorkerHandler implements HttpAsyncRequestHandler<HttpRequest> {

    private static final Logger log = Logger.getLogger(HttpAsyncRemoveWorkerHandler.class);

    public HttpAsyncRemoveWorkerHandler() {
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

        String workerIP = null;
        for (String param : request.getRequestLine().getUri().split("\\?")[1].split("&")) {
            if (param.split("=")[0].equals("IP")) {
                workerIP = param.split("=")[1];
            }
        }
        log.trace("Worker IP: " + workerIP);

        if (workerIP == null) {
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            NStringEntity entity = new NStringEntity(
                    "{\"error\":\"IP parameter in not set\"}",
                    ContentType.create("application/json", "UTF-8"));
            response.setEntity(entity);
            log.error("IP parameter is not set");

        } else {
            log.debug("Searching for worker with IP: " + workerIP);
            for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
                log.debug("Container: " + containerProxy.getEntityName().getIP() + " : " +
                        containerProxy.getEntityName().getName());
                if (containerProxy.getEntityName().getIP().equals(workerIP)) {
                    ArtRegistryLocator.getArtRegistryProxy().removeContainer(containerProxy.getEntityName());
                    response.setStatusCode(HttpStatus.SC_OK);
                    NStringEntity entity = new NStringEntity("{\"success\":\"Worker removed successfully\"}",
                            ContentType.create("application/json", "UTF-8"));
                    response.setEntity(entity);
                    log.debug("Worker removed successfully from the registry");
                    return;
                }

            }
            log.error("Worker with IP: " + workerIP + " not found");
            response.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
            NStringEntity entity = new NStringEntity(
                    "{\"error\":\"Worker with IP: " + workerIP + " not found\"}",
                    ContentType.create("application/json", "UTF-8"));
            response.setEntity(entity);

        }
    }

}
