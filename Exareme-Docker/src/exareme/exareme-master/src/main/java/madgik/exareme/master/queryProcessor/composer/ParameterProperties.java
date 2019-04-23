package madgik.exareme.master.queryProcessor.composer;

public class ParameterProperties {
    private String name;
    private String desc;
    private ParameterType type;
    private String value;
    private Boolean valueNotBlank;
    private Boolean valueMultiple;
    private ParameterValueType valueType;

    public enum ParameterType {
        column,                // used for selecting specific columns from the database
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

    public void validateParameterPropertiesInitialization() throws AlgorithmsException {
        if (name == null) {
            throw new AlgorithmsException("The parameter field 'name' was not initialized in the properties.json file");
        }
        if (desc == null) {
            throw new AlgorithmsException("The parameter field 'desc' was not initialized in the properties.json file");
        }
        if (type == null) {
            throw new AlgorithmsException("The parameter field 'type' was not initialized in the properties.json file");
        }
        if (value == null) {
            throw new AlgorithmsException("The parameter field 'value' was not initialized in the properties.json file");
        }
        if (valueNotBlank == null) {
            throw new AlgorithmsException("The parameter field 'valueNotBlank' was not initialized in the properties.json file");
        }
        if (valueMultiple == null) {
            throw new AlgorithmsException("The parameter field 'valueMultiple' was not initialized in the properties.json file");
        }
        if (valueType == null) {
            throw new AlgorithmsException("The parameter field 'valueType' was not initialized in the properties.json file");
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
