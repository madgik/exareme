package madgik.exareme.master.gateway.async.handler.HBP;

import com.google.gson.Gson;
import madgik.exareme.master.gateway.async.handler.HBP.Exceptions.ConsulException;
import madgik.exareme.master.gateway.async.handler.HBP.Exceptions.UserException;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.*;

import static madgik.exareme.master.gateway.async.handler.HBP.HBPQueryConstants.*;

public class HBPQueryHelper {

    private static final Logger log = Logger.getLogger(HBPQueryHelper.class);

    public static class ErrorResponse {
        public static class ErrorResponseTypes {
            // Error types could be error, user_error, warning regarding the error occurred along the process
            public static final String error = "text/plain+error";
            public static final String user_error = "text/plain+user_error";
            public static final String warning = "text/plain+warning";
        }

        public static String createErrorResponse(String data, String type) {
            return "{\"result\" : [{\"data\":" + "\"" + data + "\",\"type\":" + "\"" + type + "\"}]}";
        }
    }

    public static HashMap<String, String> getAlgorithmParameters(HttpRequest request) throws IOException {
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

    /**
     * This function finds the proper containers on which the algorithm should run.
     * It depends on the type of algorithm and on the combination of pathology/datasets:
     * 1) HEALTH_CHECK, LIST_DATASETS algorithms don't have pathology or datasets.
     * 2) LIST_VARIABLES has only a pathology.
     * 3) The "normal" algorithms with Pathology and Datasets.
     * <p>
     * Validation also happens on the pathology/datasets combination.
     * <p>
     * The information on nodes are taken from CONSUL if a pathology or dataset is provided.
     * <p>
     * The nodes then are checked if they are active or not.
     *
     * @param algorithmParameters are used to get the dataset/pathology
     * @return the containers on which the algorithm should run
     * @throws ConsulException    if consul is unreachable
     * @throws UserException   if dataset's node is inactive or doesn't exist in the pathology
     *                         or if the pathology is not available or not provided
     * @throws RemoteException    if the Exareme Registry is unreachable
     */
    public static ContainerProxy[] getAlgorithmNodes(HashMap<String, String> algorithmParameters)
            throws ConsulException, UserException, RemoteException {
        ConsulNodesPathologiesAndDatasetsInfo consulNodesPathologiesAndDatasetsInfo =
                new ConsulNodesPathologiesAndDatasetsInfo();

        if (algorithmParameters == null) {  // HEALTH_CHECK and LIST_DATASETS algorithms.
            // Get containers and log them
            ContainerProxy[] containers = getAllActiveExaremeContainers();
            logContainerNodes(containers, consulNodesPathologiesAndDatasetsInfo);
            return containers;
        }

        String pathology = algorithmParameters.get("pathology");

        // Get datasets in ArrayList
        String datasetsInString = algorithmParameters.get("dataset");
        ArrayList<String> datasets = null;
        if (datasetsInString != null) {
            datasets = new ArrayList<>(Arrays.asList(datasetsInString.split(",")));
        }
        log.info("Algorithm pathology: " + pathology + " and datasets: " + datasets + ".");

        validatePathologyAndDatasets(
                pathology,
                datasets,
                consulNodesPathologiesAndDatasetsInfo
        );

        if (datasets != null) {         // ALL actual algorithms are in this case
            // Get containers and log them
            ContainerProxy[] containers = getAlgorithmNodes(pathology, datasets, consulNodesPathologiesAndDatasetsInfo);
            logContainerNodes(containers, consulNodesPathologiesAndDatasetsInfo);
            return containers;

        } else if (pathology != null) {
            // LIST_VARIABLES algorithm. Will only run on master Node.
            // Fetching master nodes container.
            ArrayList<String> algorithmNodes = new ArrayList<>();
            algorithmNodes.add(consulNodesPathologiesAndDatasetsInfo.getMasterNodeIP());

            // Get containers and log them
            ContainerProxy[] containers = getContainersFromExaremeRegistry(algorithmNodes);
            logContainerNodes(containers, consulNodesPathologiesAndDatasetsInfo);
            return containers;

        } else {
            // If an algorithm parameter exists, a pathology should be provided.
            throw new UserException(pathologyNotProvided);
        }
    }

    /**
     * This function finds the proper containers on which the algorithm should run,
     * only for the case of "normal" algorithms with pathology and datasets.
     *
     * @param pathology                             of the algorithm
     * @param datasets                              of the algorithm
     * @param consulNodesPathologiesAndDatasetsInfo are the consul information needed
     * @return the containers to run the algorithm
     * @throws UserException if dataset's node is inactive or doesn't exist in the pathology
     * @throws RemoteException  if the Exareme Registry is unreachable
     */
    private static ContainerProxy[] getAlgorithmNodes(
            String pathology,
            ArrayList<String> datasets,
            ConsulNodesPathologiesAndDatasetsInfo consulNodesPathologiesAndDatasetsInfo
    ) throws RemoteException, UserException {

        HashMap<String, ArrayList<String>> algorithmNodeIPsAndDatasets = consulNodesPathologiesAndDatasetsInfo.getNodeDatasets(pathology, datasets);
        ArrayList<String> algorithmNodes = new ArrayList<>(algorithmNodeIPsAndDatasets.keySet());
        ArrayList<String> inactiveNodes = getInactiveNodes(algorithmNodes);

        if (inactiveNodes.isEmpty()) {
            return getContainersFromExaremeRegistry(algorithmNodes);
        }

        log.info("Inactive Nodes: " + String.join(", ", inactiveNodes));

        // Find the datasets that the user wanted but are inactive.
        ArrayList<String> inactiveDatasets = new ArrayList<>();
        for (String inactiveNode : inactiveNodes) {
            for (String inactiveDataset : algorithmNodeIPsAndDatasets.get(inactiveNode)) {
                if (datasets.contains(inactiveDataset)) {
                    inactiveDatasets.add(inactiveDataset);
                }
            }
        }
        throw new UserException(
                String.format(
                        datasetsXYZAreInactive,
                        String.join(", ", inactiveDatasets)
                )
        );
    }

    private static void validatePathologyAndDatasets(
            String pathology,
            ArrayList<String> datasets,
            ConsulNodesPathologiesAndDatasetsInfo nodesInformation
    ) throws UserException {

        if (pathology != null) {
            log.debug("Available pathologies: " + nodesInformation.getAllAvailablePathologies());
            if (!nodesInformation.getAllAvailablePathologies().contains(pathology)) {
                throw new UserException(String.format(pathologyXNotAvailable, pathology));
            }

            if (datasets != null) {
                ArrayList<String> datasetsOfPathology = nodesInformation.getDatasetsOfPathology(pathology);
                for (String dataset : datasets) {
                    if (!datasetsOfPathology.contains(dataset)) {
                        throw new UserException(String.format(datasetXDoesNotExistInPathologyY, dataset, pathology));
                    }
                }
            }
        } else {
            if (datasets != null) {
                throw new UserException(pathologyNotProvided);
            }
        }
    }

    /**
     * Get nodes that are inactive from the list provided.
     *
     * @param nodes to check
     * @return nodes that are inactive
     */
    private static ArrayList<String> getInactiveNodes(ArrayList<String> nodes) {
        ArrayList<String> inactiveNodes = new ArrayList<>();
        for (String node : nodes) {
            if (nodeUnreachable(node)) {
                inactiveNodes.add(node);
            }
        }
        return inactiveNodes;
    }

    /**
     * Fetches all the active containers in the exareme RMI registry.
     *
     * @return all the containers that are still active
     * @throws RemoteException if the Exareme Registry is unreachable
     */
    private static ContainerProxy[] getAllActiveExaremeContainers() throws RemoteException {
        removeInactiveExaremeContainers();
        return ArtRegistryLocator.getArtRegistryProxy().getContainers();
    }

    /**
     * Removes any container that is unreachable in the Exareme RMI registry.
     *
     * @throws RemoteException if the Exareme Registry is unreachable
     */
    private static void removeInactiveExaremeContainers() throws RemoteException {
        ContainerProxy[] containers = ArtRegistryLocator.getArtRegistryProxy().getContainers();
        for (ContainerProxy container : containers) {
            if (nodeUnreachable(container.getEntityName().getIP())) {
                log.info("Removing unreachable node with IP: " + container.getEntityName().getIP());
                ArtRegistryLocator.getArtRegistryProxy().removeContainer(container.getEntityName());
            }
        }
    }

    private static ContainerProxy[] getContainersFromExaremeRegistry(ArrayList<String> nodes) throws RemoteException {
        List<ContainerProxy> containers = new LinkedList<>();
        ContainerProxy[] allActiveExaremeContainers = getAllActiveExaremeContainers();
        for (ContainerProxy container : allActiveExaremeContainers) {
            if (nodes.contains(container.getEntityName().getIP())) {
                containers.add(container);
                nodes.remove(container.getEntityName().getIP());
            }
        }

        if (nodes.size() > 0) {
            throw new RemoteException("The following nodes are not active: " + String.join(", ", nodes));
        }

        return containers.toArray(new ContainerProxy[0]);
    }

    private static boolean nodeUnreachable(String IP) {
        try {
            InetAddress checkIP = InetAddress.getByName(IP);
            if (checkIP.isReachable(5000)) {
                log.debug("Node with IP: " + IP + "is reachable.");
                return false;
            } else {
                log.debug("Node with IP: " + IP + "is unreachable.");
                return true;
            }
        } catch (Exception e) {
            log.debug("Node with IP: " + IP + "is not reachable. Exception: " + e.getMessage());
            return true;
        }
    }

    private static void logContainerNodes(ContainerProxy[] containers,
                                          ConsulNodesPathologiesAndDatasetsInfo consulNodesPathologiesAndDatasetsInfo) {
        log.info("Algorithm Nodes: ");
        for (ContainerProxy algorithmContainer : containers) {
            String nodeIP = algorithmContainer.getEntityName().getIP();
            String nodeName = consulNodesPathologiesAndDatasetsInfo.getNodeName(nodeIP);
            log.info(" IP: " + nodeIP + " , NAME: " + nodeName);
        }
    }
}