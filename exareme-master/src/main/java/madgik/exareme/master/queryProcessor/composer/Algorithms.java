package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents the mip-algorithms repository properties.
 * <p>
 * The properties.json file is an AlgorithmProperties class.
 */
public class Algorithms {
    private static final Logger log = Logger.getLogger(Composer.class);

    public static class AlgorithmProperties {

        public static class ParameterProperties {
            private String name;
            private String desc;
            private ParameterType type;
            private String value;
            private Boolean valueNotBlank;
            private Boolean valueMultiple;
            private ParameterValueType valueType;

            public enum ParameterType {
                database,              // used for querying the database
                filter,                // used for filtering on the database input
                dataset,               // used for choosing database input
                other                  // for any other reason
            }

            public enum ParameterValueType {
                string,
                integer,
                real,
                json
            }

            public ParameterProperties() {
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

            public ParameterType getType() {
                return type;
            }

            public void setType(ParameterType type) {
                this.type = type;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public Boolean getValueNotBlank() {
                return valueNotBlank;
            }

            public void setValueNotBlank(Boolean valueNotBlank) {
                this.valueNotBlank = valueNotBlank;
            }

            public Boolean getValueMultiple() {
                return valueMultiple;
            }

            public void setValueMultiple(Boolean valueMultiple) {
                this.valueMultiple = valueMultiple;
            }

            public ParameterValueType getValueType() {
                return valueType;
            }

            public void setValueType(ParameterValueType valueType) {
                this.valueType = valueType;
            }
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

        //public void setParameters(ParameterProperties[] parameters) {
        //    this.parameters = parameters;
        //}

        /**
         * Checks if the parameterValue has the correct type
         *
         * @param value               the value of the parameter
         * @param parameterProperties the type of the value
         */
        private static void checkAlgorithmParameterValue(
                String value,
                AlgorithmProperties.ParameterProperties parameterProperties
        ) throws AlgorithmsException {
            if (parameterProperties.getValueType().equals(ParameterProperties.ParameterValueType.real)) {
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException nfe) {
                    throw new AlgorithmsException(
                            "The value of the parameter '" + parameterProperties.getName() + "' should be a real number.");
                }
            } else if (parameterProperties.getValueType().equals(ParameterProperties.ParameterValueType.integer)) {
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new AlgorithmsException(
                            "The value of the parameter '" + parameterProperties.getName() + "' should be an integer.");
                }
            } else if (parameterProperties.getValueType().equals(ParameterProperties.ParameterValueType.json)) {
                try {
                    new JSONObject(value);
                } catch (JSONException ex) {
                    try {
                        new JSONArray(value);
                    } catch (JSONException ex1) {
                        throw new AlgorithmsException(
                                "The value of the parameter '"
                                        + parameterProperties.getName() + "' cannot be parsed into json.");
                    }
                }
            } else if (parameterProperties.getValueType().equals(ParameterProperties.ParameterValueType.string)) {
                if (!parameterProperties.getValueMultiple() && value.contains(",")) {
                    throw new AlgorithmsException(
                            "The value of the parameter '" + parameterProperties.getName()
                                    + "' should contain only one value.");
                }
            }
        }

        /**
         * Initializes the algorithm properties with the values from it's properties.json file
         *
         * @param algorithmName the name of the algorithm
         * @return an AlgorithmProperties class with the default values
         * @throws IOException when algorithm property file does not exist
         */
        private static AlgorithmProperties loadAlgorithmProperties(String algorithmName)
                throws IOException {

            String algorithmPropertyFilePath = Composer.getInstance().getAlgorithmFolderPath(algorithmName) + "/properties.json";

            File propertyFile = new File(algorithmPropertyFilePath);
            if (!propertyFile.exists())
                throw new IOException("Algorithm property file does not exist.");

            Gson gson = new Gson();
            return gson.fromJson(FileUtils.readFileToString(propertyFile), AlgorithmProperties.class);
        }

        /**
         * Initializes the AlgorithmProperties from the properties.json file.
         * It also checks if the parameter values given from the inputContent
         * match with the types specified in the properties.json
         *
         * @param inputContent a HashMap with the properties
         * @return algorithm properties
         * @throws IOException when algorithm property file does not exist
         */
        public static AlgorithmProperties createAlgorithmProperties(
                String algorithmName, HashMap<String, String> inputContent
        ) throws IOException, AlgorithmsException {

            AlgorithmProperties algorithmProperties =
                    AlgorithmProperties.loadAlgorithmProperties(algorithmName);

            for (ParameterProperties parameterProperties : algorithmProperties.getParameters()) {
                String value = inputContent.get(parameterProperties.getName());
                if (value != null) {
                    checkAlgorithmParameterValue(value, parameterProperties);
                } else {            // if value is null
                    if (parameterProperties.getValueNotBlank()) {
                        throw new AlgorithmsException(
                                "The value of the parameter '" + parameterProperties.getName() + "' should not be blank.");
                    }
                    value = "";
                }
                parameterProperties.setValue(value);
            }
            return algorithmProperties;
        }

        /**
         * Returns the value of the parameter provided
         * If it doesn't exist null is returned.
         *
         * @param parameterName the name of a parameter
         * @return the value of the parameter provided
         */
        public String getParameterValue(String parameterName){
            for(ParameterProperties parameter: parameters){
                if(parameter.getName().equals(parameterName))
                    return parameter.getValue();
            }
            return null;
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

        Gson gson = new Gson();
        Algorithms algorithms = new Algorithms();

        File repoFile = new File(repoPath);
        if (!repoFile.exists()) throw new IOException("Unable to locate property file.");

        // read per algorithm property.json
        ArrayList<AlgorithmProperties> currentAlgorithms = new ArrayList<>();
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
            currentAlgorithms.add(algorithm);
        }
        algorithms.setAlgorithms(currentAlgorithms.toArray(new AlgorithmProperties[0]));

        return algorithms;
    }
}
