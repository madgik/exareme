package madgik.exareme.master.gateway.control.handler;

import madgik.exareme.common.consts.HBPConstants;
import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.connector.DataSerialization;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import madgik.exareme.master.gateway.async.handler.HBP.Exceptions.BadRequestException;
import madgik.exareme.master.gateway.async.handler.HBP.Exceptions.BadUserInputException;
import madgik.exareme.master.gateway.async.handler.entity.NQueryResultEntity;
import madgik.exareme.master.queryProcessor.HBP.AlgorithmProperties;
import madgik.exareme.master.queryProcessor.HBP.Algorithms;
import madgik.exareme.master.queryProcessor.HBP.Composer;
import madgik.exareme.master.queryProcessor.HBP.Exceptions.AlgorithmException;
import madgik.exareme.master.queryProcessor.HBP.Exceptions.CDEsMetadataException;
import madgik.exareme.master.queryProcessor.HBP.Exceptions.ComposerException;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.http.*;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HttpAsyncCheckWorker implements HttpAsyncRequestHandler<HttpRequest> {
    private static final Logger log = Logger.getLogger(madgik.exareme.master.gateway.control.handler.HttpAsyncCheckWorker.class);
    private static final AdpDBManager manager = AdpDBManagerLocator.getDBManager();
    private static final String algorithmName = "HEALTH_CHECK";

    public HttpAsyncCheckWorker() {
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
        try {
            handleInternal(request, response, context);
        } catch (AlgorithmException | CDEsMetadataException | ComposerException | BadUserInputException | BadRequestException e) {
            e.printStackTrace();
        }
        httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private void handleInternal(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context
    ) throws HttpException, IOException, AlgorithmException, CDEsMetadataException, ComposerException, BadUserInputException, BadRequestException {

        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
        String NODE_IP = null;
        String NODE_NAME = null;
        DataSerialization ds = DataSerialization.summary;

        String[] url = request.getRequestLine().getUri().split("\\?");
        String[] urlParameters = url[1].split("&");

        if (urlParameters[0].split("=")[0].equals("NODE_IP"))
            NODE_IP = urlParameters[0].split("=")[1];

        if (urlParameters[1].split("=")[0].equals("NODE_NAME"))
            NODE_NAME = urlParameters[1].split("=")[1];

        // Execute HEALTH_CHECK algorithm for health checks in bootstrap.sh via "curl -s ${MASTER_IP}:9092/check/worker?NODE_IP=${NODE_IP}"
        // Retrieve json result and check of the NODE_NAME of the node exist in the result.
        String algorithmKey = algorithmName + "_" + System.currentTimeMillis();
        log.info("Executing algorithm: " + algorithmName + " with key: " + algorithmKey);

        log.info("Algorithm Nodes: ");
        log.info(" IP: " + NODE_IP + " , NAME: " + NODE_NAME);

        String dfl;
        HashMap<String, String> inputContent = new HashMap<>();
        AlgorithmProperties algorithmProperties = Algorithms.getInstance().getAlgorithmProperties(algorithmName);

        if (algorithmProperties == null)
            throw new BadRequestException(algorithmName, "The algorithm does not exist.");

        algorithmProperties.mergeWithAlgorithmParameters(inputContent);

        dfl = Composer.composeDFLScript(algorithmKey, algorithmProperties, 1);  //each time a Worker that try to connect with Exareme and the Master
        log.debug(dfl);
        try {
            Composer.persistDFLScriptToAlgorithmsDemoDirectory(
                    HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY + "/" + algorithmKey
                            + "/" + algorithmKey,
                    dfl, null);
        } catch (IOException e) {
            // Ignoring error if failed to persist DFL Scripts - it's not something fatal.
            log.error(e);
        }

        AdpDBClientProperties clientProperties =
                new AdpDBClientProperties(
                        HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey,
                        "", "", false, false,
                        -1, 10);

        ContainerProxy[] usedContainerProxies;
        List<ContainerProxy> usedContainerProxiesList = new ArrayList<>();
        List<String> nodesToBeChecked = new ArrayList<>();
        nodesToBeChecked.add(NODE_IP);
        for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
            if (nodesToBeChecked.contains(containerProxy.getEntityName().getIP())) {
                usedContainerProxiesList.add(containerProxy);
                log.debug(containerProxy.getEntityName().getIP());
            }
        }

        usedContainerProxies = usedContainerProxiesList.toArray(new ContainerProxy[usedContainerProxiesList.size()]);

        clientProperties.setContainerProxies(usedContainerProxies);
        AdpDBClient dbClient =
                AdpDBClientFactory.createDBClient(manager, clientProperties);

        AdpDBClientQueryStatus queryStatus = dbClient.query(algorithmKey, dfl);
        log.info("Executing algorithm " + algorithmKey +
                " started with queryId " + queryStatus.getQueryID().getQueryID());

        BasicHttpEntity entity = new NQueryResultEntity(queryStatus, ds,
                ExaremeGatewayUtils.RESPONSE_BUFFER_SIZE);
        response.setStatusCode(HttpStatus.SC_OK);
        response.setEntity(entity);
    }
}