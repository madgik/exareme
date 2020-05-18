package madgik.exareme.master.gateway.async.handler;

import com.google.gson.Gson;
import madgik.exareme.master.gateway.async.handler.Exceptions.DatasetsException;
import madgik.exareme.master.gateway.async.handler.Exceptions.PathologyException;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.xml.crypto.Data;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

public class HttpAsyncMiningQueryHelper {

    private static final Logger log = Logger.getLogger(HttpAsyncMiningQueryHandler.class);
    private static final String user_error = new String("text/plain+user_error");

    private static HashMap<String, String[]> getNodesForPathology(String pathology) throws IOException, PathologyException {
        Gson gson = new Gson();
        HashMap<String, String[]> nodeDatasets = new HashMap<>();
        List<String> pathologyNodes = new ArrayList<String>();

        String masterKey = searchConsul(System.getenv("EXAREME_MASTER_PATH") + "/?keys");
        String[] masterKeysArray = gson.fromJson(masterKey, String[].class);

        String masterName = masterKeysArray[0].replace(System.getenv("EXAREME_MASTER_PATH") + "/", "");
        String masterIP = searchConsul(System.getenv("EXAREME_MASTER_PATH") + "/" + masterName + "?raw");

        String pathologyKey = searchConsul(System.getenv("DATA") + "/" + masterName + "/" + pathology + "?keys");
        String[] pathologyKeyKeysArray = gson.fromJson(pathologyKey, String[].class);

        if (pathologyKeyKeysArray != null) {
            pathologyNodes.add(pathologyKeyKeysArray[0]);                 //Add Master Pathology
        }

        String datasetKey = searchConsul(System.getenv("DATA") + "/" + masterName + "/" + pathology + "?raw");
        String[] datasetKeysArray = gson.fromJson(datasetKey, String[].class);
        if (datasetKeysArray != null)
            nodeDatasets.put(masterIP, datasetKeysArray);                 //Map Master IP-> Matser Datasets

        String workersKey = searchConsul(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/?keys");
        if (workersKey == null)     //No workers running
            return nodeDatasets;             //return master's Datasets only
        String[] workerKeysArray = gson.fromJson(workersKey, String[].class);
        for (String worker : workerKeysArray) {
            String workerName = worker.replace(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/", "");
            String workerIP = searchConsul(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/" + workerName + "?raw");


            pathologyKey = searchConsul(System.getenv("DATA") + "/" + workerName + "/" + pathology + "?keys");
            pathologyKeyKeysArray = gson.fromJson(pathologyKey, String[].class);

            if (pathologyKeyKeysArray != null) {
                pathologyNodes.add(pathologyKeyKeysArray[0]);                 //Add worker Pathology
            }

            datasetKey = searchConsul(System.getenv("DATA") + "/" + workerName + "/" + pathology + "?raw");
            datasetKeysArray = gson.fromJson(datasetKey, String[].class);
            if (datasetKeysArray != null)
                nodeDatasets.put(workerIP, datasetKeysArray);        //Map Worker's IP-> Worker's Datasets
        }

        if (pathologyNodes.isEmpty()) {
            throw new PathologyException("Pathology " + pathology + " not found!");
        }

        return nodeDatasets;
    }



    static HashMap<String, String> getNamesOfActiveNodesInConsul() throws Exception {
        Gson gson = new Gson();
        HashMap<String, String> nodeNames = new HashMap<>();
        String masterKey = searchConsul(System.getenv("EXAREME_MASTER_PATH") + "/?keys");
        String[] masterKeysArray = gson.fromJson(masterKey, String[].class);    //Map Master's IP-> Master's Name

        String masterName = masterKeysArray[0].replace(System.getenv("EXAREME_MASTER_PATH") + "/", "");
        String masterIP = searchConsul(System.getenv("EXAREME_MASTER_PATH") + "/" + masterName + "?raw");
        nodeNames.put(masterIP, masterName);

        String workersKey = searchConsul(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/?keys");
        if (workersKey == null)             //No workers running
            return nodeNames;               //return master only
        String[] workerKeysArray = gson.fromJson(workersKey, String[].class);
        for (String worker : workerKeysArray) {
            String workerName = worker.replace(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/", "");
            String workerIP = searchConsul(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/" + workerName + "?raw");
            nodeNames.put(workerIP, workerName);         //Map Worker's IP-> Worker's Name
        }
        return nodeNames;
    }


    static HashMap<String, String> getAlgoParameters(HttpRequest request) throws IOException {

        log.debug("Parsing content ...");
        HashMap<String, String> inputContent = new HashMap<>();
        List<Map> parameters = new ArrayList();
        String content;

        if (request instanceof HttpEntityEnclosingRequest) {
            log.debug("Streaming ...");
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            content = EntityUtils.toString(entity);
            if (content != null && !content.isEmpty()) {
                parameters = new Gson().fromJson(content, List.class);
            }
        }
        if (!parameters.isEmpty()) {
            log.debug("All of the parameters: " + parameters);
            for (Map k : parameters) {
                String name = (String) k.get("name");
                String value = (String) k.get("value");
                if (name == null || name.isEmpty() || value == null || value.isEmpty()) continue;

                log.debug("Parameter in the json: ");
                log.debug(name + " = " + value);

                value = value.replaceAll("[^A-Za-z0-9,._~*+><=&|(){}:\\-\\\"\\[\\]]", "");    // ><=&| we no more need those for filtering
                value = value.replaceAll("\\s+", "");

                log.debug("Parameter after format: ");
                log.debug(name + " = " + value);

                inputContent.put(name, value);
            }
            return inputContent;
        }
        return null;
    }


    static String searchConsul(String query) throws IOException {
        String result = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String consulURL = System.getenv("CONSULURL");
        if (consulURL == null) throw new IOException("Consul url not set");
        if (!consulURL.startsWith("http://")) {
            consulURL = "http://" + consulURL;
        }

        HttpGet httpGet;
        httpGet = new HttpGet(consulURL + "/v1/kv/" + query);
        log.debug("Running: " + httpGet.getURI());
        CloseableHttpResponse response = null;
        if (httpGet.toString().contains(System.getenv("EXAREME_MASTER_PATH") + "/") || httpGet.toString().contains(System.getenv("DATA") + "/")) {    //if we can not contact : http://exareme-keystore:8500/v1/kv/master* or http://exareme-keystore:8500/v1/kv/datasets*
            try {   //then throw exception
                response = httpclient.execute(httpGet);
            } catch (Exception e) {
                response.close();
            }
            result = EntityUtils.toString(response.getEntity());
        }
        if (httpGet.toString().contains(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/")) {    //if we can not contact : http://exareme-keystore:8500/v1/kv/active_workers*
            //then maybe there are no workers running
            try {
                response = httpclient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() != 200) {
                    if (httpGet.toString().contains("?keys"))
                        log.debug("No workers running. Continue with master");
                } else {
                    result = EntityUtils.toString(response.getEntity());
                }
            }
            catch (Exception e){
                response.close();
            }
        }
        return result;
    }

     static List<ContainerProxy> getInputAlgo(HashMap<String, String> inputContent) throws IOException, DatasetsException, PathologyException {
        List<ContainerProxy> nodesToBeChecked = new ArrayList<>();
        String datasets;
        String[] userDatasets = null;
        String pathology = null;
        HashMap<String, String[]> nodeDatasets = new HashMap<>();

        if (inputContent == null ) {        //list_datasets
            nodesToBeChecked.addAll(Arrays.asList(ArtRegistryLocator.getArtRegistryProxy().getContainers()));
	        return nodesToBeChecked;
        }
        else if(inputContent.size()==1 && inputContent.containsKey("pathology")) {  //list_variables
            pathology = inputContent.get("pathology");
            nodeDatasets = getNodesForPathology(pathology);

            nodesToBeChecked.addAll(Arrays.asList(ArtRegistryLocator.getArtRegistryProxy().getContainers()));
            return nodesToBeChecked;
        }

        else {
            if (inputContent.containsKey("pathology")) {
                pathology = inputContent.get("pathology");
                nodeDatasets = getNodesForPathology(pathology);
            }
            if (inputContent.containsKey("dataset")) {
                datasets = inputContent.get("dataset");
                //Get datasets provided by user
                userDatasets = datasets.split(",");
            }
            nodesToBeChecked = checkDatasets(nodeDatasets, userDatasets, pathology);
        }
        return nodesToBeChecked;
    }


    private static List<ContainerProxy> checkDatasets(HashMap<String, String[]> nodeDatasets, String[] userDatasets, String pathology) throws DatasetsException, RemoteException {
        List<String> notFoundDatasets = new ArrayList<>();
        List<ContainerProxy> nodesToBeChecked = new ArrayList<>();
        Boolean flag;

        //for every dataset provided by the user
        for (String data : userDatasets) {
            Iterator<Map.Entry<String, String[]>> entries = nodeDatasets.entrySet().iterator();
            flag = false;
            //for each Exareme node (master/workers)
            while (entries.hasNext()) {
                Map.Entry<String, String[]> entry = entries.next();
                String IP = entry.getKey();
                String[] datasets = entry.getValue();
                //if dataset exist in that Exareme node
                if (Arrays.asList(datasets).contains(data)) {
                    //and Exareme node not already added to list nodesToBeChecked
                    for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
                        if (containerProxy.getEntityName().getIP().equals(IP) && !nodesToBeChecked.contains(containerProxy)) {
                            nodesToBeChecked.add(containerProxy);
                            flag = true;
                        }
                    }
                }
                //if flag=false then dataset(s) provided by user are not contained in ANY Exareme node
                if (!flag) {
                    notFoundDatasets.add(data);
                }
            }
            //if notFoundDatasets list is not empty, there are dataset(s) provided by user not contained in ANY Exareme node
            if (notFoundDatasets.size() != 0) {
                StringBuilder notFound = new StringBuilder();
                for (String ds : notFoundDatasets)
                    notFound.append(ds).append(", ");
                String notFoundSring = notFound.toString();
                notFoundSring = notFoundSring.substring(0, notFoundSring.length() - 2);
                //Show appropriate error message to user
                throw new DatasetsException("Dataset(s) " + notFoundSring + " not found for pathology " + pathology + "!");
            }
        }
            return nodesToBeChecked;
    }


    static String getAvailableDatasetsFromConsul(String pathology) throws Exception {
        HashMap<String,String> names = getNamesOfActiveNodesInConsul();
        StringBuilder datasets=new StringBuilder();
        Gson gson = new Gson();

        for (Map.Entry<String, String> entry : names.entrySet()) {
            String dataRaw = searchConsul(System.getenv("DATA") + "/" + entry.getValue() + "/" + pathology + "?raw");
            String[] data = gson.fromJson(dataRaw,String[].class);
            for (String d : data){
                if(!d.isEmpty())
                    datasets.append(d).append(",");
            }
        }
        if(!datasets.toString().isEmpty())
            return datasets.substring(0, datasets.length() - 1);
        else
            return null;
    }

    static String defaultOutputFormat(String data, String type) {
        return "{\"result\" : [{\"data\":" + "\"" + data + "\",\"type\":" + "\"" + type + "\"}]}";
    }
}


