package madgik.exareme.master.queryProcessor.composer;


import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class AlgorithmProperties {

    ParameterProperties parameterProperties;
    private String name;
    private String desc;
    private AlgorithmType type;
    private String responseContentType;
    private ParameterProperties[] parameters;

    public enum AlgorithmType {
        local,                      // exec single node local
        pipeline,                   // exec local on each endpoint
        local_global,               // exec global over the union of local results
        multiple_local_global,      // exec sequentially multiple local_global
        iterative,                   // exec iterative algorithm
        python_local,                   // exec python based local algorithm
        python_local_global,            // exec python based local global algorithm
        python_multiple_local_global    // exec python based multiple local global algorithm
    }

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



    /**
     * Initializes the AlgorithmProperties from the properties.json file.
     * It also checks if the parameter values given from the inputContent
     * match with the types specified in the properties.json
     *
     * @param inputContent a HashMap with the properties
     * @return algorithm properties
     * @throws IOException when algorithm property file does not exist
     */
    public AlgorithmProperties(String algorithmName, HashMap<String, String> inputContent) throws IOException, AlgorithmsException {
        loadAlgorithmProperties(algorithmName);

        for (ParameterProperties parameterProperties : this.getParameters()) {
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
    }

    public AlgorithmProperties(String algorithmName) throws IOException, AlgorithmsException {
        loadAlgorithmProperties(algorithmName);
    }


        /**
         * Returns the value of the parameter provided
         * If it doesn't exist null is returned.
         *
         * @param parameterName the name of a parameter
         * @return the value of the parameter provided
         */
    public String getParameterValue(String parameterName) {
        for (ParameterProperties parameter : parameters) {
            if (parameter.getName().equals(parameterName))
                return parameter.getValue();
        }
        return null;
    }

    /**
     * Checks if the parameterValue has the correct type
     *
     * @param value               the value of the parameter
     * @param parameterProperties the type of the value
     */
    private static void checkAlgorithmParameterValue(
            String value,
            ParameterProperties parameterProperties
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
    private void loadAlgorithmProperties(String algorithmName)
            throws IOException {

        String algorithmPropertyFilePath = Composer.getInstance().getAlgorithmFolderPath(algorithmName) + "/properties.json";

        File propertyFile = new File(algorithmPropertyFilePath);
        if (!propertyFile.exists())
            throw new IOException("Algorithm property file does not exist.");

        Gson gson = new Gson();

        AlgorithmProperties algorithmProperties = gson.fromJson(FileUtils.readFileToString(propertyFile), AlgorithmProperties.class);

        //All fields must be copy to this.
        this.name = algorithmProperties.name;
        this.desc = algorithmProperties.desc;
        this.type = algorithmProperties.type;
        this.responseContentType = algorithmProperties.responseContentType;
        this.parameters = algorithmProperties.parameters;
    }
}
