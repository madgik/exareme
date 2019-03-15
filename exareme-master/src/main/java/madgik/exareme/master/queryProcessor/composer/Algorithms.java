package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represent the mip-algorithms repository properties,
 * able to interact through gson.
 *
 * @author alexpap
 */
public class Algorithms {
    private static final Logger log = Logger.getLogger(Composer.class);

    public static class AlgorithmProperties {

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

            public ParameterProperties(){

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

        public enum AlgorithmType {
            local,                      // exec single node local
            pipeline,                   // exec local on each endpoint
            local_global,               // exec global over the union of local results
            multiple_local_global,      // exec sequentially multiple local_global
            iterative                   // exec iterative algorithm
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
         * @throws IOException                  when algorithm property file does not exist
         */
        private static AlgorithmProperties loadAlgorithmProperties(String algorithmName)
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
         * @throws IOException  when algorithm property file does not exist
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

        public static HashMap<String, String> toHashMap(@NotNull ParameterProperties[] parameterProperties) {
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
        public static boolean updateParameterProperty(@NotNull AlgorithmProperties algorithmProperties,
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
    }

    private AlgorithmProperties[] algorithms;

    public AlgorithmProperties[] getAlgorithms() {
        return algorithms;
    }

    public void setAlgorithms(AlgorithmProperties[] algorithms) {
        this.algorithms = algorithms;
    }

    public static Algorithms createAlgorithms(String repoPath) throws IOException {

        File repoFile = new File(repoPath);
        if (!repoFile.exists()) throw new IOException("Unable to locate property file.");

        // read property.json
        Gson gson = new Gson();
        Algorithms algorithms = gson.fromJson(new BufferedReader(new FileReader(repoPath + "/properties.json")), Algorithms.class);

        // read per algorithm property.json
        ArrayList<AlgorithmProperties> algos = new ArrayList<>();
        for (File file : Objects.requireNonNull(repoFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && !pathname.getName().startsWith(".") && !pathname.getName().contains("unit_tests");
            }
        }))) {
            AlgorithmProperties algorithm =
                    gson.fromJson(
                            new BufferedReader(
                                    new FileReader(file.getAbsolutePath() + "/properties.json")),
                            AlgorithmProperties.class);
            algos.add(algorithm);
        }
        algorithms.setAlgorithms(algos.toArray(new AlgorithmProperties[0]));

        return algorithms;
    }
}
