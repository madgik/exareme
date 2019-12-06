package madgik.exareme.master.queryProcessor.composer;

import madgik.exareme.master.queryProcessor.composer.Exceptions.AlgorithmException;
import madgik.exareme.master.queryProcessor.composer.Exceptions.ComposerException;
import madgik.exareme.master.queryProcessor.composer.Exceptions.CDEsMetadataException;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * AlgorithmProperties contains all the information about the properties in each algorithm's properties.json file.
 */
public class AlgorithmProperties {
    private static final Logger log = Logger.getLogger(AlgorithmProperties.class);

    private String name;
    private String desc;
    private AlgorithmType type;
    private ParameterProperties[] parameters;

    public enum AlgorithmType {
        local,                      // exec single node local
        pipeline,                   // exec local on each endpoint
        local_global,               // exec global over the union of local results
        multiple_local_global,      // exec sequentially multiple local_global
        iterative,                  // exec iterative algorithm
        python_local,                   // exec python based local algorithm
        python_local_global,            // exec python based local global algorithm
        python_multiple_local_global,    // exec python based multiple local global algorithm
        python_iterative                 // exec python based iterative algorithm
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
            throws AlgorithmException, CDEsMetadataException {
        if (inputContent == null)
            return;

        String pathology = inputContent.get(ComposerConstants.getPathologyPropertyName());

        for (ParameterProperties parameterProperties : this.getParameters()) {
            String value = inputContent.get(parameterProperties.getName());
            if (value != null && !value.equals("")) {
                validateAlgorithmParameterValueType(value, parameterProperties);
                validateAlgorithmParameterType(value, parameterProperties, pathology);
            } else {            // if value not given or it is blank
                if (parameterProperties.getValueNotBlank()) {
                    throw new AlgorithmException(
                            "The value of the parameter '" + parameterProperties.getName() + "' should not be blank.");
                }

                if (parameterProperties.getDefaultValue() != null) {
                    value = parameterProperties.getDefaultValue();
                } else {
                    value = "";
                }
            }
            parameterProperties.setValue(value);
        }
    }

    /**
     * Checks if the given parameter input has acceptable values for that specific parameter.
     *
     * @param value               the value given as input
     * @param parameterProperties the rules that the value should follow
     * @param pathology           the pathology that the algorithm will run on
     */
    private static void validateAlgorithmParameterType(
            String value,
            ParameterProperties parameterProperties,
            String pathology
    ) throws AlgorithmException, CDEsMetadataException {

        if (parameterProperties.getType().equals(ParameterProperties.ParameterType.column)) {
            String[] values = value.split(",");
            validateCDEVariables(values, parameterProperties, pathology);
        } else if (parameterProperties.getType().equals(ParameterProperties.ParameterType.formula)) {
            String[] values = value.split("[+\\-*:0]+");
            validateCDEVariables(values, parameterProperties, pathology);
        }
        // If value is not a column (type=other) then check for min-max-enumerations
        else if (parameterProperties.getType().equals(ParameterProperties.ParameterType.other)) {
            if (parameterProperties.getValueType().equals(ParameterProperties.ParameterValueType.integer)
                    || parameterProperties.getValueType().equals(ParameterProperties.ParameterValueType.real)) {
                String[] values = value.split(",");
                for (String curValue : values) {
                    if (parameterProperties.getValueMin() != null && Double.parseDouble(curValue) < parameterProperties.getValueMin())
                        throw new AlgorithmException("The value(s) of the parameter '" + parameterProperties.getName()
                                + "' should be greater than " + parameterProperties.getValueMin() + " .");
                    if (Double.parseDouble(curValue) > parameterProperties.getValueMax())
                        throw new AlgorithmException("The value(s) of the parameter '" + parameterProperties.getName()
                                + "' should be less than " + parameterProperties.getValueMax() + " .");
                }
            } else if (parameterProperties.getValueType().equals(ParameterProperties.ParameterValueType.string)) {
                if (parameterProperties.getValueEnumerations() == null)
                    return;
                List<String> enumerations = Arrays.asList(parameterProperties.getValueEnumerations());
                String[] values = value.split(",");
                for (String curValue : values) {
                    if (!enumerations.contains(curValue))
                        throw new AlgorithmException("The value '" + curValue + "' of the parameter '" + parameterProperties.getName()
                                + "' is not included in the valueEnumerations " + Arrays.toString(parameterProperties.getValueEnumerations()) + " .");
                }
            }
        }
    }

    /**
     * The given CDE variables must have proper SQL_Type and Categorical values in order to match with
     * the parameter property's columnValueType and columnValueCategorical.
     * The information about the CDEs are taken from the metadata.
     *
     * @param variables           a list with the variables
     * @param parameterProperties the rules that the variables should follow
     * @param pathology           the pathology that the algorithm will run on
     */
    private static void validateCDEVariables(
            String[] variables,
            ParameterProperties parameterProperties,
            String pathology
    ) throws AlgorithmException, CDEsMetadataException {
        CDEsMetadata.PathologyCDEsMetadata metadata = CDEsMetadata.getInstance().getPathologyCDEsMetadata(pathology);
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
