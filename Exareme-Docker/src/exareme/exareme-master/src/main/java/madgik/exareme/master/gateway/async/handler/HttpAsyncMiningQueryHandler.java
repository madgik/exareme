package madgik.exareme.master.gateway.async.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import madgik.exareme.common.consts.HBPConstants;
import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.connector.DataSerialization;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.engine.iterations.exceptions.IterationsFatalException;
import madgik.exareme.master.engine.iterations.handler.IterationsHandler;
import madgik.exareme.master.engine.iterations.handler.NIterativeAlgorithmResultEntity;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import madgik.exareme.master.gateway.async.handler.Exceptions.DatasetsException;
import madgik.exareme.master.gateway.async.handler.Exceptions.PathologyException;
import madgik.exareme.master.gateway.async.handler.entity.NQueryResultEntity;
import madgik.exareme.master.queryProcessor.composer.AlgorithmProperties;
import madgik.exareme.master.queryProcessor.composer.Algorithms;
import madgik.exareme.master.queryProcessor.composer.Composer;
import madgik.exareme.master.queryProcessor.composer.Exceptions.AlgorithmException;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.print.DocFlavor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.ServerException;
import java.util.*;
import static madgik.exareme.master.gateway.GatewayConstants.COOKIE_ALGORITHM_EXECUTION_ID;
import madgik.exareme.common.art.entity.EntityName;

public class HttpAsyncMiningQueryHandler implements HttpAsyncRequestHandler<HttpRequest> {

    private static final Logger log = Logger.getLogger(HttpAsyncMiningQueryHandler.class);
    private static final String SET_COOKIE_HEADER_NAME = "Set-Cookie";
    private static final AdpDBManager manager = AdpDBManagerLocator.getDBManager();
    private static final IterationsHandler iterationsHandler = IterationsHandler.getInstance();
    private static final String error = new String("text/plain+error");
    private static final String user_error = new String("text/plain+user_error");

    public HttpAsyncMiningQueryHandler() {
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

        // When under testing the Set-Cookie header has been used with the "algorithm execution id"
        // parameter for differentiating between concurrent executions of algorithms.
        if (request.containsHeader(SET_COOKIE_HEADER_NAME)) {
            HeaderIterator it = request.headerIterator(SET_COOKIE_HEADER_NAME);

            // Parse "algorithm execution id" cookie
            StringBuilder echoCookieContent = new StringBuilder();
            while (it.hasNext()) {
                echoCookieContent.append(it.next());
            }

            String cookieContentStr = echoCookieContent.toString();
            if (!cookieContentStr.isEmpty() &&
                    cookieContentStr.contains(COOKIE_ALGORITHM_EXECUTION_ID)) {

                String algorithmExecIdStr =
                        cookieContentStr.substring(
                                cookieContentStr.indexOf(" "),
                                cookieContentStr.length())
                                .split("=")[1];

                response.addHeader(
                        SET_COOKIE_HEADER_NAME,
                        COOKIE_ALGORITHM_EXECUTION_ID + "=" + algorithmExecIdStr);
            }
        }
        try {
            handleInternal(request, response, context);
        } catch (Exception e) {
            log.error(e.getMessage());
            String data = e.getMessage();
            String type = user_error;        //type could be error, user_error, warning regarding the error occurred along the process
            String result = HttpAsyncMiningQueryHelper.defaultOutputFormat(data, type);
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(result.getBytes()));
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(entity);
        }
        httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private String preALgoExecutionChecks(HttpRequest request) throws UnsupportedHttpVersionException {
        log.debug("Validate method ...");
        RequestLine requestLine = request.getRequestLine();
        String uri = requestLine.getUri();
        String algorithmName = uri.substring(uri.lastIndexOf('/') + 1);
        String method = requestLine.getMethod().toUpperCase(Locale.ENGLISH);

        if (!"POST".equals(method)) {
            throw new UnsupportedHttpVersionException(method + "not supported.");
        }
        log.debug("Posting " + algorithmName + " ...\n");
        return algorithmName;
    }

    private void handleInternal(HttpRequest request, HttpResponse response, HttpContext context)
            throws Exception {

        List<String> nodesToBeChecked;
        int numberOfContainers;

        //Check given method
        String algorithmName = preALgoExecutionChecks(request);

        //Get parameters of given algorithm
        HashMap<String, String> inputContent = HttpAsyncMiningQueryHelper.getAlgoParameters(request);

        try {
            nodesToBeChecked=HttpAsyncMiningQueryHelper.getInputAlgo(inputContent);

            for(String node : nodesToBeChecked) {
                pingContainer(node, inputContent.get("pathology"));
            }


	    ContainerProxy[] usedContainerProxies;
            
            List<ContainerProxy> usedContainerProxiesList = new ArrayList<>();
            for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
                if (nodesToBeChecked.contains(containerProxy.getEntityName().getIP())) {
                    usedContainerProxiesList.add(containerProxy);
                }
            }
            usedContainerProxies = usedContainerProxiesList.toArray(new ContainerProxy[usedContainerProxiesList.size()]);
            


            numberOfContainers = usedContainerProxies.length;
            log.info("Containers: " + numberOfContainers);
            String algorithmKey = algorithmName + "_" + System.currentTimeMillis();
            String dfl;
            AdpDBClientQueryStatus queryStatus;

            AlgorithmProperties algorithmProperties = Algorithms.getInstance().getAlgorithmProperties(algorithmName);

            if (algorithmProperties == null)
                throw new AlgorithmException("The algorithm '" + algorithmName + "' does not exist.");

            algorithmProperties.mergeAlgorithmParametersWithInputContent(inputContent);

            DataSerialization ds = DataSerialization.summary;

            // Bypass direct composer call in case of iterative algorithm.
            if (algorithmProperties.getType().equals(AlgorithmProperties.AlgorithmType.iterative) ||
                    algorithmProperties.getType().equals(AlgorithmProperties.AlgorithmType.python_iterative)) {

                final IterativeAlgorithmState iterativeAlgorithmState =
                        iterationsHandler.handleNewIterativeAlgorithmRequest(
                                manager, algorithmKey, algorithmProperties, usedContainerProxies);

                log.info("Iterative algorithm " + algorithmKey + " execution started.");

                BasicHttpEntity entity = new NIterativeAlgorithmResultEntity(
                        iterativeAlgorithmState, ds, ExaremeGatewayUtils.RESPONSE_BUFFER_SIZE);

                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(entity);
            } else {
                dfl = Composer.composeDFLScript(algorithmKey, algorithmProperties, numberOfContainers);
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
                clientProperties.setContainerProxies(usedContainerProxies);
                AdpDBClient dbClient =
                        AdpDBClientFactory.createDBClient(manager, clientProperties);
                queryStatus = dbClient.query(algorithmKey, dfl);

                log.info("Algorithm " + algorithmKey + " with queryID "
                        + queryStatus.getQueryID() + " execution started. DFL Script: \n " + dfl);

                BasicHttpEntity entity = new NQueryResultEntity(queryStatus, ds,
                        ExaremeGatewayUtils.RESPONSE_BUFFER_SIZE);
                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(entity);
            }
        } catch (IterationsFatalException e) {
            log.error(e);
            if (e.getErroneousAlgorithmKey() != null)
                iterationsHandler.removeIterativeAlgorithmStateInstanceFromISM(
                        e.getErroneousAlgorithmKey());
            log.error(e);
            BasicHttpEntity entity = new BasicHttpEntity();
            String data = e.getMessage();
            String type = error;        //type could be error, user_error, warning regarding the error occured along the process
            String result = HttpAsyncMiningQueryHelper.defaultOutputFormat(data, type);
            entity.setContent(new ByteArrayInputStream(result.getBytes()));
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(entity);
        } catch (JsonSyntaxException e) {
            log.error("Could not parse the algorithms properly.");
            String data = "Could not parse the algorithms properly.";
            String type = error;        //type could be error, user_error, warning regarding the error occured along the process
            String result = HttpAsyncMiningQueryHelper.defaultOutputFormat(data, type);
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(result.getBytes()));
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(entity);
        } catch (PathologyException | DatasetsException | IOException e) {
            log.error(e.getMessage());
            String data = e.getMessage();
            String type = user_error;        //type could be error, user_error, warning regarding the error occured along the process
            String result = HttpAsyncMiningQueryHelper.defaultOutputFormat(data, type);
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(result.getBytes()));
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(entity);
        }
        catch (Exception e) {
            log.error(e);
            String data = e.getMessage();
            String type = error;        //type could be error, user_error, warning regarding the error occured along the process
            String result = HttpAsyncMiningQueryHelper.defaultOutputFormat(data, type);
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(result.getBytes()));
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(entity);
        }
    }


    private boolean pingContainer(String IP, String pathology) throws Exception {
        InetAddress checkIP = InetAddress.getByName(IP);
        Gson gson = new Gson();
        String availableDatasets;
        log.debug("Checking worker with IP "+IP);
        if (checkIP.isReachable(5000)) {
            log.info("Host is reachable");
            return true;
        }
        else {
            for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
                if (IP.equals(containerProxy.getEntityName().getIP())) {
                    log.info("Going to delete "+containerProxy.getEntityName()+" with IP "+IP);
                    ArtRegistryLocator.getArtRegistryProxy().removeContainer(containerProxy.getEntityName());
                }
            }

            HashMap<String, String> names = HttpAsyncMiningQueryHelper.getNamesOfActiveNodesInConsul();
            String name = names.get(IP);

            //Delete pathologies and IP of the node
            String pathologyKey = HttpAsyncMiningQueryHelper.searchConsul(System.getenv("DATA") + "/" + name + "?keys");
            String[] pathologyKeyArray = gson.fromJson(pathologyKey, String[].class);
            for (String p : pathologyKeyArray) {
                deleteFromConsul(p);            //Delete every pathology for node with name $name
            }
            //Delete IP of active_worker with name $name
            deleteFromConsul(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/" + name);
            //TODO maybe add the datasets and not the name
            availableDatasets = HttpAsyncMiningQueryHelper.getAvailableDatasetsFromConsul(pathology);
            if (availableDatasets!=null){
                throw new Exception("Re run your experiment using available data: '"+availableDatasets+"'");
            }
            else{
                throw new Exception("No data available to run any other experiments. Consult your system administration.");
            }
        }
    }

    //Some times infos regarding Exareme nodes exist in Consul-Key-Value store], but the nodes are not part of Exareme's registry. We delete the infos from Consul[Key-Value store]
    private void deleteFromConsul(String query) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String consulURL = System.getenv("CONSULURL");
        if (consulURL == null) throw new IOException("Consul url not set");
        if (!consulURL.startsWith("http://")) {
            consulURL = "http://" + consulURL;
        }
        HttpDelete httpDelete;
        httpDelete = new HttpDelete(consulURL + "/v1/kv/" + query);

        //curl -X DELETE $CONSULURL/v1/kv/$DATASETS/$NODE_NAME
        //curl -X DELETE $CONSULURL/v1/kv/$1/$NODE_NAME

        log.info("Running: " + httpDelete.getURI());

        CloseableHttpResponse response = null;
        if (httpDelete.toString().contains(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/") || httpDelete.toString().contains(System.getenv("DATA") + "/")) {    //if we can not contact : http://exareme-keystore:8500/v1/kv/master* or http://exareme-keystore:8500/v1/kv/datasets*
            try {   //then throw exception
                response = httpclient.execute(httpDelete);
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new ServerException("Cannot contact consul", new Exception(EntityUtils.toString(response.getEntity())));
                }
            } finally {
                response.close();
            }
        }
    }

}


