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
