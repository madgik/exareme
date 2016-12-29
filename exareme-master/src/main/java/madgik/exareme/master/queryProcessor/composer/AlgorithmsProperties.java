package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent the mip-algorithms repository properties,
 * able to interact throw gson.
 * @author alexpap
 */
public class AlgorithmsProperties {

    public static class ParameterProperties {

        private String name;
        private String desc;
        private String value;

        public ParameterProperties() {
        }

        public ParameterProperties(ParameterProperties orig) {
            name = orig.name;
            desc = orig.desc;
            value = orig.value;
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

        public ParameterProperties[] getParameters() {
            return parameters;
        }

        public void setParameters(ParameterProperties[] parameters) {this.parameters = parameters;}

        public static AlgorithmProperties createAlgorithmProperties(String algorithmPropertyFilePath)
            throws IOException {

            File propertyFile = new File(algorithmPropertyFilePath);
            if (!propertyFile.exists())
                throw new IOException("Algorithm property file does not exits.");

            Gson gson = new Gson();
            return gson.fromJson(FileUtils.readFileToString(propertyFile), AlgorithmProperties.class);
        }

        public static AlgorithmProperties createAlgorithmProperties(
            HashMap<String, String> inputContent) throws IOException {

            String algorithm_name = inputContent.get(ComposerConstants.algorithmKey);
            String path =  Composer.getInstance().getRepositoryPath() + algorithm_name + "/properties.json";

            AlgorithmProperties newAlgorithmParameters =
                AlgorithmProperties.createAlgorithmProperties(path);

            for (ParameterProperties algorithmParameter : newAlgorithmParameters.getParameters()) {
                String value = inputContent.get(algorithmParameter.getName());
                if (value != null) {
                    algorithmParameter.setValue(value);
                    inputContent.remove(algorithmParameter.getName());
                }
            }

            if(!inputContent.isEmpty()) {

                ArrayList<ParameterProperties> list = new ArrayList<>();
                for (Map.Entry<String, String> entry : inputContent.entrySet()) {
                    ParameterProperties properties = new ParameterProperties();
                    properties.setName(entry.getKey());
                    properties.setValue(entry.getValue());
                    list.add(properties);
                }
                int n = newAlgorithmParameters.getParameters().length + list.size();
                if ( n > 0) {
                    ParameterProperties[] parameterProperties = new ParameterProperties[n];
                    for (int i = 0; i < newAlgorithmParameters.getParameters().length; i++) {
                        parameterProperties[i] = newAlgorithmParameters.getParameters()[i];
                    }
                    for(int i =0; i < list.size(); i ++){
                        parameterProperties[newAlgorithmParameters.getParameters().length + i] = list.get(i);
                    }
                    newAlgorithmParameters.setParameters(parameterProperties);
                }
            }

            return newAlgorithmParameters;
        }

        public static HashMap<String, String> toHashMap(AlgorithmProperties algorithmProperties) {
            HashMap<String, String> map = new HashMap<>();
            for (ParameterProperties algorithmParameter : algorithmProperties.getParameters()) {
                map.put(algorithmParameter.getName(), algorithmParameter.getValue());
            }
            return map;
        }

        /**
         * Copies the <code>src</code> <code>ParameterProperties[]</code> entries to <code>dst</code>.
         *
         * @param src   The source array from which elements are copied.
         * @param dst   The destination array to which elements are copied. <b>Its capacity must
         *              be at least the same as <code>src</code>'s capacity. </b>
         * @return True on success, false in case the dst's capacity doesn't abide by the
         *              provided directions.
         */
        public static boolean copyParameterProperties(
                ParameterProperties[] src, ParameterProperties[] dst) {
            if (dst.length < src.length)
                return false;
            for (int i = 0; i < src.length; i++) {
                dst[i] = new ParameterProperties(src[i]);
            }
            return true;
        }
    }

    public enum EndpointStatus{
        up,
        down
    }

    public static class EndpointLocalEngine{

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

        public String toUDF(){
            StringBuilder builder = new StringBuilder();
            builder.append('(');
            builder.append(name);
            for (ParameterProperties parameter : parameters) {
                builder.append(" ");
                builder.append(parameter.name);
                builder.append(':');
                builder.append(parameter.value);
                builder.append(" ");
            }
            builder.append(query);
            builder.append(')');

            return builder.toString();
        }

        public String toUDF(String query){
            StringBuilder builder = new StringBuilder();
            builder.append('(');
            builder.append(name);
            for (ParameterProperties parameter : parameters) {
                builder.append(" ");
                builder.append(parameter.name);
                builder.append(':');
                builder.append(parameter.value);
                builder.append(" ");
            }
            if(query == null || query.isEmpty()) builder.append(this.query);
            else builder.append(query);
            builder.append(')');

            return builder.toString();
        }
        // TODO push filters efficiently
        public String toUDF(List<String> variables){
            StringBuilder builder = new StringBuilder();
            builder.append('(');
            builder.append(name);
            for (ParameterProperties parameter : parameters) {
                builder.append(" ");
                builder.append(parameter.name);
                builder.append(':');
                builder.append(parameter.value);
                builder.append(" ");
            }
            builder.append(query);
            builder.append(" where ");
            for (int i = 0; i < variables.size() - 1; i++) {
                builder.append("variable_name = \"");
                builder.append(variables.get(i));
                builder.append("\" or ");
            }
            builder.append("variable_name = \"");
            builder.append(variables.get(variables.size()-1));
            builder.append("\")");
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
                return pathname.isDirectory() && !pathname.getName().startsWith(".") ? true : false;
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
            if(endpointProperties.getLocal_engine() == null){
                endpointProperties.setLocal_engine(algorithms.getLocal_engine_default());
            }
        }
        return algorithms;
    }
}
