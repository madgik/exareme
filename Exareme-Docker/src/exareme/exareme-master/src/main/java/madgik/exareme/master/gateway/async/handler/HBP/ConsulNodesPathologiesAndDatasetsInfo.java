package madgik.exareme.master.gateway.async.handler.HBP;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import madgik.exareme.master.gateway.async.handler.HBP.Exceptions.ConsulException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * A class that contains the information of every node.
 * A map between the node IP and it's data (pathologies/datasets).
 */
public class ConsulNodesPathologiesAndDatasetsInfo {

    private static final Logger log = Logger.getLogger(ConsulNodesPathologiesAndDatasetsInfo.class);

    private final HashMap<String, NodeData> nodesData;
    private final HashMap<String, String> nodeIPsToNames;
    private String masterNodeIP;

    /**
     * Fetches the node information from CONSUL.
     *
     * @throws ConsulException when a communication problem with consul occurs
     */
    ConsulNodesPathologiesAndDatasetsInfo() throws ConsulException {
        nodesData = new HashMap<>();
        nodeIPsToNames = new HashMap<>();
        Gson gson = new Gson();
        String activeWorkersPathsJson = searchConsulKeys(getConsulActiveWorkersPath());
        String[] activeWorkersPaths = gson.fromJson(activeWorkersPathsJson, String[].class);

        for (String workerPath : activeWorkersPaths) {
            String nodeName = workerPath.substring(workerPath.lastIndexOf("/") + 1);
            String nodeIP = getConsulData(workerPath);
            nodesData.put(nodeIP, new NodeData(nodeName));
            nodeIPsToNames.put(nodeIP, nodeName);
        }

        log.debug("CONSUL DATA");
        log.debug("Node IPs to Names");
        for (Map.Entry<String, String> node : nodeIPsToNames.entrySet()) {
            log.debug("NodeIp: " + node.getKey() + " , NodeName: " + node.getValue());
        }
        log.debug("Node Data");
        for (Map.Entry<String, NodeData> nodeData : nodesData.entrySet()) {
            log.debug("NodeIp: " + nodeData.getKey() + " , NODEDATA ---> ");
            for (Map.Entry<String, ArrayList<String>> pathology : nodeData.getValue().pathologyDatasets.entrySet()) {
                log.debug("Pathology: " + pathology.getKey() + ", Datasets: " + String.join(",", pathology.getValue()));
            }
            log.debug("<----- NODE DATA");
        }
    }

    public ArrayList<String> getDatasetsOfPathology(String pathology) {
        ArrayList<String> datasets = new ArrayList<>();
        for (NodeData nodeData : nodesData.values()) {
            if (nodeData.pathologyDatasets.containsKey(pathology)) {
                datasets.addAll(nodeData.pathologyDatasets.get(pathology));
            }
        }
        return datasets;
    }

    /**
     * Fetches the node IP's and datasets that include any of the datasets provided.
     *
     * @param pathology is used to limit the search
     * @param datasets  to search for in nodes
     * @return the node IP's and datasets
     */
    public HashMap<String, ArrayList<String>> getNodeDatasets(String pathology, ArrayList<String> datasets) {
        HashMap<String, ArrayList<String>> nodeIPToDatasets = new HashMap<>();

        for (Map.Entry<String, NodeData> nodeData : nodesData.entrySet()) {

            // Get the datasets for the specified pathology only
            ArrayList<String> nodeDatasets = nodeData.getValue().pathologyDatasets.get(pathology);

            // Skip nodes without datasets on that pathology
            if (nodeDatasets == null) continue;

            // If the nodeDatasets contains any of the required datasets
            if (!Collections.disjoint(nodeDatasets, datasets)) {
                nodeIPToDatasets.put(nodeData.getKey(), nodeDatasets);
            }
        }
        return nodeIPToDatasets;
    }

    public String getNodeName(String nodeIP) {
        return nodeIPsToNames.get(nodeIP);
    }

    /**
     * Fetches all the available pathologies in the nodes
     *
     * @return the pathologies
     */
    public ArrayList<String> getAllAvailablePathologies() {
        ArrayList<String> nodesPathologies = new ArrayList<>();
        for (NodeData nodeData : nodesData.values()) {
            nodesPathologies.addAll(nodeData.pathologyDatasets.keySet());
        }
        return nodesPathologies;
    }

    /**
     * Fetches the master node's IP only if not already fetched
     *
     * @return master node's IP
     */
    public String getMasterNodeIP() throws ConsulException {
        if (masterNodeIP != null) {
            return masterNodeIP;
        }

        Gson gson = new Gson();
        String masterPathJson = searchConsulKeys(getConsulMasterPath());
        String masterPath = gson.fromJson(masterPathJson, String[].class)[0];
        masterNodeIP = getConsulData(masterPath);
        return masterNodeIP;
    }

    /**
     * A class that contains the information of every node.
     * A map between the pathologies of the node and the
     * datasets in each pathology.
     */
    public static class NodeData {
        public HashMap<String, ArrayList<String>> pathologyDatasets;

        /**
         * Fetches the node information from CONSUL.
         *
         * @param nodeName the name of the node in CONSUL
         * @throws ConsulException when a communication problem with consul occurs
         */
        NodeData(String nodeName) throws ConsulException {
            pathologyDatasets = new HashMap<>();

            // Get the available pathologies of the node from CONSUL.
            Gson gson = new Gson();
            try {
                String nodePathologiesPathsJson = searchConsulKeys(getConsulDataPath() + "/" + nodeName);
                if (nodePathologiesPathsJson == null) {
                    return;
                }
                String[] nodePathologiesPaths = gson.fromJson(nodePathologiesPathsJson, String[].class);

                // Get the available datasets for each pathology and add it to the hash.
                for (String nodePathologyPath : nodePathologiesPaths) {
                    String pathology = nodePathologyPath.substring(nodePathologyPath.lastIndexOf("/") + 1);
                    String nodePathologyDatasetsJson = getConsulData(nodePathologyPath);
                    String[] nodePathologyDatasets = gson.fromJson(nodePathologyDatasetsJson, String[].class);
                    pathologyDatasets.put(pathology, new ArrayList<>(Arrays.asList(nodePathologyDatasets)));
                }
            } catch (JsonSyntaxException e) {
                throw new ConsulException("There was a problem parsing the response from consul: " + e.getMessage());
            } catch (ConsulException e) {
                // The node is up but the data are not added yet.
                // continue;
            }
        }
    }


    /***  -----     Helper functions     ----- ***/
    private static String searchConsulKeys(String query) throws ConsulException {
        return searchConsul(query + "?keys");
    }

    private static String getConsulData(String query) throws ConsulException {
        return searchConsul(query + "?raw");
    }

    private static String searchConsul(String query) throws ConsulException {
        log.debug("Consul Query: " + query);

        String consulURL = getConsulUrl();
        if (!consulURL.startsWith("http://")) {
            consulURL = "http://" + consulURL;
        }

        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();

            HttpGet httpGet = new HttpGet(consulURL + "/v1/kv/" + query);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                log.error("Failed consul query: " + consulURL + "/v1/kv/" + query);
                throw new ConsulException(
                        "There was an error contacting consul. StatusCode: " + response.getStatusLine().getStatusCode());
            }

            return EntityUtils.toString(response.getEntity());

        } catch (IOException e) {
            log.error("Failed consul query: " + consulURL + "/v1/kv/" + query);
            throw new ConsulException(
                    "An exception occurred while contacting Consul. Exception: " + e.getMessage());
        }
    }

    private static String getConsulUrl() throws ConsulException {
        String consulURL = System.getenv("CONSULURL");
        if (consulURL == null) throw new ConsulException("CONSULURL environment variable is not set.");
        return consulURL;
    }

    private static String getConsulDataPath() throws ConsulException {
        String dataPath = System.getenv("CONSUL_DATA_PATH");
        if (dataPath == null) throw new ConsulException("CONSUL_DATA_PATH environment variable is not set.");
        return dataPath;
    }

    private static String getConsulActiveWorkersPath() throws ConsulException {
        String activeWorkersPath = System.getenv("CONSUL_ACTIVE_WORKERS_PATH");
        if (activeWorkersPath == null) throw new ConsulException("CONSUL_DATA_PATH environment variable is not set.");
        return activeWorkersPath;
    }

    private static String getConsulMasterPath() throws ConsulException {
        String masterPath = System.getenv("CONSUL_MASTER_PATH");
        if (masterPath == null) throw new ConsulException("CONSUL_MASTER_PATH environment variable is not set.");
        return masterPath;
    }


}
