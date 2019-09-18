package madgik.exareme.master.queryProcessor.composer;

import madgik.exareme.master.queryProcessor.composer.Exceptions.AlgorithmException;

public class ParameterProperties {
    private String name;
    private String desc;
    private ParameterType type;
    private String columnValuesSQLType;
    private String columnValuesIsCategorical;
    private String columnValuesNumOfEnumerations;
    private String value;
    private String defaultValue;
    private Boolean valueNotBlank;
    private Boolean valueMultiple;
    private ParameterValueType valueType;

    public enum ParameterType {
        column,                // used for selecting specific columns from the database
        formula,               // used for parsig the input as a formula of R, '+ - * : 0' are allowed.
        filter,                // used for filtering on the database input
        dataset,               // used for choosing database input
        pathology,             // used for specifying what database to use
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

    public void validateParameterPropertiesInitialization() throws AlgorithmException {
        if (name == null) {
            throw new AlgorithmException("The parameter field 'name' was not initialized in the properties.json file.");
        }
        if (desc == null) {
            throw new AlgorithmException("The parameter field 'desc' was not initialized in the properties.json file.");
        }
        if (type == null) {
            throw new AlgorithmException("The parameter field 'type' was not initialized in the properties.json file.");
        } else if (type.equals(ParameterType.column) || type.equals(ParameterType.formula)) {
            if (columnValuesSQLType == null) {
                throw new AlgorithmException("The parameter field 'columnValuesSQLType' was not initialized in the properties.json file.");
            }

            if (columnValuesIsCategorical == null) {
                throw new AlgorithmException("The parameter field 'columnValuesIsCategorical' was not initialized in the properties.json file.");
            }

            if (columnValuesNumOfEnumerations == null) {
                throw new AlgorithmException("The parameter field 'columnValuesNumOfEnumerations' was not initialized in the properties.json file.");
            } else if (!columnValuesNumOfEnumerations.equals("")) {
                try {
                    Integer.parseInt(columnValuesNumOfEnumerations);
                } catch (NumberFormatException e) {
                    throw new AlgorithmException("The parameter field 'columnValuesNumOfEnumerations' cannot be parsed to an Integer.");
                }
            }
        }
        if (value == null) {
            throw new AlgorithmException("The parameter field 'value' was not initialized in the properties.json file");
        }
        if (valueNotBlank == null) {
            throw new AlgorithmException("The parameter field 'valueNotBlank' was not initialized in the properties.json file");
        }
        if (valueMultiple == null) {
            throw new AlgorithmException("The parameter field 'valueMultiple' was not initialized in the properties.json file");
        }
        if (valueType == null) {
            throw new AlgorithmException("The parameter field 'valueType' was not initialized in the properties.json file");
        }
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

    public String getColumnValuesSQLType() {
        return columnValuesSQLType;
    }

    public String getColumnValuesIsCategorical() {
        return columnValuesIsCategorical;
    }

    public String getColumnValuesNumOfEnumerations() {
        return columnValuesNumOfEnumerations;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String value) {
        this.defaultValue = defaultValue;
    }

    public Boolean getValueNotBlank() {
        return valueNotBlank;
    }

    public Boolean getValueMultiple() {
        return valueMultiple;
    }

    public ParameterValueType getValueType() {
        return valueType;
    }
}
