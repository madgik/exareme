package madgik.exareme.master.queryProcessor.composer;

import madgik.exareme.master.queryProcessor.composer.Exceptions.AlgorithmException;
import madgik.exareme.master.queryProcessor.composer.Exceptions.ComposerException;
import madgik.exareme.master.queryProcessor.composer.Exceptions.VariablesMetadataException;
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
    private String responseContentType;
    private ParameterProperties[] parameters;

    public enum AlgorithmType {
        local,                      // exec single node local
        pipeline,                   // exec local on each endpoint
        local_global,               // exec global over the union of local results
        multiple_local_global,      // exec sequentially multiple local_global
        iterative,                  // exec iterative algorithm
        python_local,                   // exec python based local algorithm
        python_local_global,            // exec python based local global algorithm
        python_multiple_local_global    // exec python based multiple local global algorithm
    }

    public AlgorithmProperties() {
    }

    public void validateAlgorithmPropertiesInitialization() throws AlgorithmException {
        if (name == null) {
            throw new AlgorithmException("The parameter field 'name' was not initialized in the properties.json file");
        }
        if (desc == null) {
            throw new AlgorithmException("The parameter field 'desc' was not initialized in the properties.json file");
        }
        if (type == null) {
            throw new AlgorithmException("The parameter field 'type' was not initialized in the properties.json file");
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

    public String getResponseContentType() {
        return responseContentType;
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
     * Changes the value of an algorithm parameter
     * to the given value
     *
     * @param parameterName     the name of a parameter
     * @param newParameterValue the new value of the parameter
     * @return true if it was changed, false otherwise
     */
    public void setParameterValue(String parameterName, String newParameterValue) throws ComposerException {
        String allowedDynamicParameters = ComposerConstants.dbIdentifierKey;

        // Not all parameters are allowed to be changed.
        // This is a safety check
        if (!allowedDynamicParameters.contains(parameterName)) {
            throw new ComposerException("The value of the parameter " + parameterName + " should not be set manually.");
        }

        for (ParameterProperties parameter : parameters) {
            if (parameter.getName().equals(parameterName)) {
                parameter.setValue(newParameterValue);
                return;
            }
        }
        throw new ComposerException("The parameter " + parameterName + " does not exist.");
    }

    /**
     * Gets the AlgorithmProperties from the cached Algorithms.
     * Merges the default algorithm properties with the parameters given in the HashMap.
     *
     * @param inputContent a HashMap with the properties from the request
     * @return the merge algorithm's properties
     * @throws AlgorithmException when algorithm's properties do not match the inputContent
     */
    public void mergeAlgorithmParametersWithInputContent(HashMap<String, String> inputContent)
            throws AlgorithmException, VariablesMetadataException {

        for (ParameterProperties parameterProperties : this.getParameters()) {
            String value = inputContent.get(parameterProperties.getName());
            if (value != null) {
                validateAlgorithmParameterValueType(value, parameterProperties);
                validateAlgorithmParameterType(value, parameterProperties);
            } else {            // if value is null
                if (parameterProperties.getValueNotBlank()) {
                    throw new AlgorithmException(
                            "The value of the parameter '" + parameterProperties.getName() + "' should not be blank.");
                }
                value = "";
            }
            parameterProperties.setValue(value);
        }
    }

    /**
     * Checks if the given input has acceptable values for that specific parameter.
     *
     * @param value               the value given as input
     * @param parameterProperties the rules that the value should follow
     */
    private static void validateAlgorithmParameterType(
            String value,
            ParameterProperties parameterProperties
    ) throws AlgorithmException, VariablesMetadataException {

        if (parameterProperties.getType().equals(ParameterProperties.ParameterType.column)) {
            String[] values = value.split(",");
            validateCDEVariables(values, parameterProperties);
        } else if (parameterProperties.getType().equals(ParameterProperties.ParameterType.formula)) {
            String[] values = value.split("[+\\-*:0]+");
            validateCDEVariables(values, parameterProperties);
        }
    }

    /**
     * The given CDE variables must have proper SQL_Type and Categorical values in order to match with
     * the parameter property's columnValueType and columnValueCategorical.
     * The information about the CDEs are taken from the metadata.
     *
     * @param variables           a list with the variables
     * @param parameterProperties the rules that the variables should follow
     */
    private static void validateCDEVariables(
            String[] variables,
            ParameterProperties parameterProperties
    ) throws AlgorithmException, VariablesMetadataException {
        VariablesMetadata metadata = VariablesMetadata.getInstance();
        for (String curValue : variables) {
            if (!metadata.columnExists(curValue)) {
                throw new AlgorithmException("The CDE '" + curValue + "' does not exist.");
            }

            String allowedSQLTypeValues = parameterProperties.getColumnValuesSQLType();
            String columnValuesSQLType = metadata.getColumnValuesSQLType(curValue);
            if (!allowedSQLTypeValues.contains(columnValuesSQLType) && !allowedSQLTypeValues.equals("")) {
                throw new AlgorithmException("The CDE '" + curValue + "' does not have one of the allowed SQL Types '"
                        + allowedSQLTypeValues + "' for the algorithm.");
            }

            String allowedIsCategoricalValue = parameterProperties.getColumnValuesIsCategorical();
            String columnValuesIsCategorical = metadata.getColumnValuesIsCategorical(curValue);
            if (!allowedIsCategoricalValue.equals(columnValuesIsCategorical) && !allowedIsCategoricalValue.equals("")) {
                throw new AlgorithmException("The CDE '" + curValue + "' does not match the categorical value '"
                        + allowedIsCategoricalValue + "' specified for the algorithm.");
            }

            String allowedNumOfEnumerationsValue = parameterProperties.getColumnValuesNumOfEnumerations();
            if (!allowedNumOfEnumerationsValue.equals("")) {
                int numOfEnumerationsIntegerValue = Integer.parseInt(allowedNumOfEnumerationsValue);
                int columnValuesNumOfEnumerations = metadata.getColumnValuesNumOfEnumerations(curValue);
                if (columnValuesNumOfEnumerations != numOfEnumerationsIntegerValue) {
                    throw new AlgorithmException("The CDE '" + curValue + "' does not match the numOfEnumerations value '"
                            + allowedNumOfEnumerationsValue + "' specified for the algorithm.");
                }
            }
        }
    }

    /**
     * Checks if the parameterValue has the correct type
     *
     * @param value               the value of the parameter
     * @param parameterProperties the type of the value
     */
    private static void validateAlgorithmParameterValueType(
            String value,
            ParameterProperties parameterProperties
    ) throws AlgorithmException {
        if (parameterProperties.getValueType().equals(ParameterProperties.ParameterValueType.real)) {
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException nfe) {
                throw new AlgorithmException(
                        "The value of the parameter '" + parameterProperties.getName() + "' should be a real number.");
            }
        } else if (parameterProperties.getValueType().equals(ParameterProperties.ParameterValueType.integer)) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new AlgorithmException(
                        "The value of the parameter '" + parameterProperties.getName() + "' should be an integer.");
            }
        } else if (parameterProperties.getValueType().equals(ParameterProperties.ParameterValueType.json)) {
            try {
                new JSONObject(value);
            } catch (JSONException ex) {
                try {
                    new JSONArray(value);
                } catch (JSONException ex1) {
                    throw new AlgorithmException(
                            "The value of the parameter '"
                                    + parameterProperties.getName() + "' cannot be parsed into json.");
                }
            }
        } else if (parameterProperties.getValueType().equals(ParameterProperties.ParameterValueType.string)) {
            if (!parameterProperties.getValueMultiple() && value.contains(",")) {
                throw new AlgorithmException(
                        "The value of the parameter '" + parameterProperties.getName()
                                + "' should contain only one value.");
            }
        }
    }
}
