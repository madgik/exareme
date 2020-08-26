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
import madgik.exareme.master.gateway.async.handler.entity.NQueryResultEntity;
import madgik.exareme.master.queryProcessor.composer.*;
import madgik.exareme.master.queryProcessor.composer.Exceptions.AlgorithmException;
import madgik.exareme.master.queryProcessor.composer.Exceptions.CDEsMetadataException;
import madgik.exareme.master.queryProcessor.composer.Exceptions.ComposerException;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.http.*;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

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
        } catch (AlgorithmException | CDEsMetadataException | ComposerException e) {
            e.printStackTrace();
        }
        httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
        }
    private void handleInternal(
             final HttpRequest request,
             final HttpResponse response,
             final HttpContext context) throws HttpException, IOException, AlgorithmException, CDEsMetadataException, ComposerException {

        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
        AdpDBClientQueryStatus queryStatus;
        String IP_MASTER = null;
        String IP_WORKER = null;
        DataSerialization ds = DataSerialization.summary;

        String[] getIPs = request.getRequestLine().getUri().split("\\?");

        if (getIPs[1].split("=")[0].equals("IP_MASTER"))
            IP_MASTER = getIPs[1].split("=")[1];

        if (getIPs[2].split("=")[0].equals("IP_WORKER"))
            IP_WORKER = getIPs[2].split("=")[1];

        log.debug("MASTER: " + IP_MASTER);
        log.debug("WORKER: " + IP_WORKER);


        //Execute LIST_DATASET for health checks in bootsrap.sh via "curl -s ${MASTER_IP}:9092/check/worker?IP_MASTER=${MY_IP}?IP_WORKER=${MY_IP}"
        //Retrieve json result and check of the NODE_NAME of the node exist in the result.
        String algorithmKey = algorithmName + "_" + System.currentTimeMillis();
        String dfl;
        HashMap<String, String> inputContent = new HashMap<>();
        AlgorithmProperties algorithmProperties = Algorithms.getInstance().getAlgorithmProperties(algorithmName);

        if (algorithmProperties == null)
            throw new AlgorithmException(algorithmName, "The algorithm does not exist.");

        algorithmProperties.mergeAlgorithmParametersWithInputContent(inputContent);


        dfl = Composer.composeDFLScript(algorithmKey, algorithmProperties, 2);  //each time a Worker that try to connect with Exareme and the Master
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

        if (Objects.equals(IP_MASTER, IP_WORKER)){
            log.debug("It seams like a health check for Master node["+IP_MASTER+"] only");
            nodesToBeChecked.add(IP_MASTER);
        }
        else {
            log.debug("It seams like a health check for Master node["+IP_MASTER+"] - Worker node["+IP_WORKER+"]");
            nodesToBeChecked.add(IP_MASTER);
            nodesToBeChecked.add(IP_WORKER);
        }

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
        queryStatus = dbClient.query(algorithmKey, dfl);
        BasicHttpEntity entity = new NQueryResultEntity(queryStatus, ds,
                ExaremeGatewayUtils.RESPONSE_BUFFER_SIZE);
        response.setStatusCode(HttpStatus.SC_OK);
        response.setEntity(entity);
    }
}
