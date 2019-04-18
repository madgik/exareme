package madgik.exareme.master.queryProcessor.composer;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.HashMap;

/**
 * AlgorithmProperties contains all the information about the properties in each algorithm's properties.json file.
 */
public class AlgorithmProperties {
    private String name;
    private String desc;
    private AlgorithmType type;
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

    public void validateAlgorithmPropertiesInitialization() throws AlgorithmsException {
        if (name == null) {
            throw new AlgorithmsException("The parameter field 'name' was not initialized in the properties.json file");
        }
        if (desc == null) {
            throw new AlgorithmsException("The parameter field 'desc' was not initialized in the properties.json file");
        }
        if (type == null) {
            throw new AlgorithmsException("The parameter field 'type' was not initialized in the properties.json file");
        }
        for (ParameterProperties parameterProperties : parameters)
            parameterProperties.validateParameterPropertiesInitialization();
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
     * Gets the AlgorithmProperties from the cached Algorithms.
     * Merges the default algorithm properties with the parameters given in the HashMap.
     *
     * @param inputContent a HashMap with the properties from the request
     * @return the merge algorithm's properties
     * @throws AlgorithmsException when algorithm's properties do not match the inputContent
     */
    public void mergeAlgorithmParametersWithInputContent(HashMap<String, String> inputContent)
            throws AlgorithmsException {

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
}
