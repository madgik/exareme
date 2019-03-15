package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent the mip-algorithms repository properties,
 * able to interact throw gson.
 *
 * @author alexpap
 */
public class AlgorithmsProperties {
    private static final Logger log = Logger.getLogger(Composer.class);

    public static class ParameterProperties {

        private String name;
        private String desc;
        private String value;
        private Boolean notBlank;
        private Boolean multiValue;
        private ParameterType type;

        public enum ParameterType {
            database_parameter,         // used for querying the database
            filter,                     // used for filtering on the database input
            dataset,                    // used for choosing database input
            algorithm_parameter,        // used from the algorithm
            generic                     // other usage
        }

        public ParameterProperties() {
        }

        ParameterProperties(ParameterProperties orig) {
            name = orig.name;
            desc = orig.desc;
            value = orig.value;
            notBlank = orig.notBlank;
            multiValue = orig.multiValue;

        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Boolean getNotBlank() { return notBlank; }

        public void setNotBlank(Boolean notBlank) { this.notBlank = notBlank; }

        public Boolean getMultiValue() { return multiValue; }

        public void setMultiValue(Boolean multiValue) { this.multiValue = multiValue; }

        public ParameterType getType() { return type; }

        public void setType(ParameterType type) { this.type = type; }

    }

    public static class AlgorithmProperties {

        public enum AlgorithmType {
            local,                      // exec single node local
            pipeline,                   // exec local on each endpoint
            local_global,               // exec global over the union of local results
            multiple_local_global,      // exec sequentially multiple local_global
            iterative                   // exec iterative algorithm
        }

        public enum AlgorithmVisualizationType {
            piechart,
            linechart,
            clusterplot,
            none
        }

        public enum AlgorithmStatus {
            enabled,
            disabled
        }

        private String name;
        private String desc;
        private AlgorithmType type;
        private String responseContentType;
        private ParameterProperties[] parameters;

        public AlgorithmProperties() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public AlgorithmType getType() {
            return type;
        }

        public void setType(AlgorithmType type) {
            this.type = type;
        }

        public String getResponseContentType() {
            return responseContentType;
        }

        public void setResponseContentType(String returnContentType) {
            this.responseContentType = returnContentType;
        }

        public ParameterProperties[] getParameters() {
            return parameters;
        }

        public void setParameters(ParameterProperties[] parameters) {
            this.parameters = parameters;
        }

        /**
         * Initializes the algorithm properties with the values from it's properties.json file
         *
         * @param algorithmName                 the name of the algorithm
         * @return                              an AlgorithmProperties class with the default values
         * @throws IOException
         */
        public static AlgorithmProperties loadAlgorithmProperties(String algorithmName)
                throws IOException {

            String algorithmPropertyFilePath = Composer.getInstance().getRepositoryPath() + algorithmName + "/properties.json";

            File propertyFile = new File(algorithmPropertyFilePath);
            if (!propertyFile.exists())
                throw new IOException("Algorithm property file does not exist.");

            Gson gson = new Gson();
            return gson.fromJson(FileUtils.readFileToString(propertyFile), AlgorithmProperties.class);
        }

        /**
         * Initializes the AlgorithmProperties from the properties.json file
         *  and the ParameterProperties of the algorithm from the inputContent.
         *  It also checks if the parameter values given from the inputContent
         *   match with the types specified in the properties.json
         *
         * @param inputContent  a HashMap with the properties
         * @return              algorithm properties
         * @throws IOException
         */
        public static AlgorithmProperties createAlgorithmProperties(
                HashMap<String, String> inputContent, String algorithmName) throws IOException {

            AlgorithmProperties algorithmProperties =
                    AlgorithmProperties.loadAlgorithmProperties(algorithmName);

            for (ParameterProperties parameterProperties : algorithmProperties.getParameters()) {
                String value = inputContent.get(parameterProperties.getName());
                if (value == null) {
                    if (parameterProperties.getNotBlank()) {
                        // TODO Throw Exception
                        throw new IOException("blablbabafb");
                    }
                    value = "";
                }
                parameterProperties.setValue(value);
            }

            log.debug("Line 197 - algorithmProperties");
            for (ParameterProperties parameter : algorithmProperties.getParameters()) {
                log.debug("Property name: " + parameter.name + ", value: " + parameter.value + ", notBlank: "
                        + parameter.notBlank + ", multiValue: " + parameter.multiValue  + ", type: " + parameter.type);
            }

            log.debug("Line 203 - inputContent");
            for (String s : inputContent.keySet()) {
                log.debug("Input name: " + s + ", value: " + inputContent.get(s));
            }

            return algorithmProperties;
        }

        public static HashMap<String, String> toHashMap(ParameterProperties[] parameterProperties) {
            HashMap<String, String> map = new HashMap<>();
            for (ParameterProperties algorithmParameter : parameterProperties) {
                map.put(algorithmParameter.getName(), algorithmParameter.getValue());
            }
            return map;
        }

        /**
         * Updates the given {@code ParameterProperty} of the {@code algorithmProperties} argument.
         *
         * @param algorithmProperties the algorithm properties whose parameter will be updated
         * @param propertyName        the name of the parameter property to be updated
         * @param propertyValue       the updated value of the parameter property
         * @return true on success, false if the given {@code propertyName} hasn't been found
         */
        public static boolean updateParameterProperty(AlgorithmProperties algorithmProperties,
                                                      String propertyName,
                                                      String propertyValue) {
            for (ParameterProperties property :
                    algorithmProperties.getParameters()) {
                if (property.getName().equals(propertyName)) {
                    property.setValue(propertyValue);
                    return true;
                }
            }
            return false;
        }

        /**
         * Copies the {@code src} {@code ParameterProperties[]} entries to {@code dst}.
         *
         * @param src the source parameter properties, not null
         * @return a copy of the {@code src} parameter properties
         */
        public static ParameterProperties[] copyParameterProperties(ParameterProperties[] src) {
            if (src == null)
                return null;

            ParameterProperties[] copy = new ParameterProperties[src.length];
            for (int i = 0; i < src.length; i++) {
                copy[i] = new ParameterProperties(src[i]);
            }
            return copy;
        }
    }

    public enum EndpointStatus {
        up,
        down
    }

    public static class EndpointLocalEngine {

        private String name;
        private ParameterProperties[] parameters;
        private String query;

        public EndpointLocalEngine() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ParameterProperties[] getParameters() {
            return parameters;
        }

        public void setParameters(ParameterProperties[] parameters) {
            this.parameters = parameters;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String toUDF(String query) {
            StringBuilder builder = new StringBuilder();
            builder.append("(select __rid ,__colname ,__val from (file header:t file:/root/mip-algorithms/input_tbl.csv))");
           /* builder.append(name);
            builder.append(" ");
            builder.append("))");*/
            return builder.toString();
        }

        // TODO push filters efficiently
        public String toUDF(List<String> variables) {
            StringBuilder builder = new StringBuilder();
            builder.append("(select __rid ,__colname ,__val from (file header:t file:/root/mip-algorithms/input_tbl.csv))");
            /*builder.append(name);
            builder.append(" ");

            for (int i = 0; i < variables.size() - 1; i++) {
                if(variables.get(i).contains("filter:")){
                    builder.append(variables.get(i));
                    builder.append(" ");
                }
                else{
                    builder.append(variables.get(i));
                    builder.append(",");
                }
            }
            builder.append(variables.get(variables.size()-1));
            builder.append("))");*/
            return builder.toString();
        }
    }

    public static class EndpointProperties {

        private String name;
        private String desc;
        private String host;
        private String port;
        private EndpointStatus status;
        private EndpointLocalEngine local_engine;

        public EndpointProperties() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public EndpointStatus getStatus() {
            return status;
        }

        public void setStatus(EndpointStatus status) {
            this.status = status;
        }

        public EndpointLocalEngine getLocal_engine() {
            return local_engine;
        }

        public void setLocal_engine(EndpointLocalEngine local_engine) {
            this.local_engine = local_engine;
        }
    }

    private AlgorithmProperties[] algorithms;
    private EndpointProperties[] endpoints;
    private EndpointLocalEngine local_engine_default;

    public AlgorithmsProperties() {
    }

    public AlgorithmProperties[] getAlgorithms() {
        return algorithms;
    }

    public void setAlgorithms(AlgorithmProperties[] algorithms) {
        this.algorithms = algorithms;
    }

    public EndpointProperties[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(EndpointProperties[] endpoints) {
        this.endpoints = endpoints;
    }

    public EndpointLocalEngine getLocal_engine_default() {
        return local_engine_default;
    }

    public void setLocal_engine_default(EndpointLocalEngine local_engine_default) {
        this.local_engine_default = local_engine_default;
    }

    public static AlgorithmsProperties createAlgorithms(String repoPath) throws IOException {

        File repoFile = new File(repoPath);
        if (!repoFile.exists()) throw new IOException("Unable to locate property file.");

        // read property.json
        Gson gson = new Gson();
        AlgorithmsProperties algorithms = gson.fromJson(new BufferedReader(new FileReader(repoPath + "/properties.json")), AlgorithmsProperties.class);

        // read per algorithm property.json
        ArrayList<AlgorithmProperties> algs = new ArrayList<>();
        for (File file : repoFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && !pathname.getName().startsWith(".") && !pathname.getName().contains("unit_tests") ? true : false;
            }
        })) {
            AlgorithmProperties algorithm =
                    gson.fromJson(
                            new BufferedReader(
                                    new FileReader(file.getAbsolutePath() + "/properties.json")),
                            AlgorithmProperties.class);
            algs.add(algorithm);
        }
        algorithms.setAlgorithms(algs.toArray(new AlgorithmProperties[algs.size()]));
        for (EndpointProperties endpointProperties : algorithms.getEndpoints()) {
            if (endpointProperties.getLocal_engine() == null) {
                endpointProperties.setLocal_engine(algorithms.getLocal_engine_default());
            }
        }
        return algorithms;
    }
}
