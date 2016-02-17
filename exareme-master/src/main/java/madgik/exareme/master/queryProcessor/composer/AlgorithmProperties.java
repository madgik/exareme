package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author alex
 */
public class AlgorithmProperties {

  private String name;
  private String desc;
  private AlgorithmType type;
  private AlgorithmParameter[] parameters;

  public static class AlgorithmParameter {
    private String name;
    private String desc;
    private String value;

    public AlgorithmParameter() {
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

    public String getName() {

      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
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

  public AlgorithmParameter[] getParameters() {
    return parameters;
  }

  public void setParameters(AlgorithmParameter[] parameters) {
    this.parameters = parameters;
  }

  public static AlgorithmProperties createAlgorithmProperties(String algorithmPropertyFilePath) throws IOException {

    File propertyFile = new File(algorithmPropertyFilePath);
    if ( !propertyFile.exists() )
      throw new IOException("Algorithm property file does not exits.");

    Gson gson = new Gson();
    return gson.fromJson(FileUtils.readFileToString(propertyFile), AlgorithmProperties.class);
  }
  public static AlgorithmProperties createAlgorithmProperties(HashMap<String, String> inputContent) throws IOException {
    String algorithm_name = inputContent.get(ComposerConstants.algorithmKey);
    AlgorithmProperties newAlgorithmParameters = AlgorithmProperties
        .createAlgorithmProperties(Composer.getInstance().getRepositoryPath() + algorithm_name + "/properties.json");
    for (AlgorithmParameter algorithmParameter : newAlgorithmParameters.getParameters()) {
      String value = inputContent.get(algorithmParameter.getName());
      if ( value != null ) {
        algorithmParameter.setValue(value);
      }
    }
    return newAlgorithmParameters;
  }

  public static HashMap<String, String> toHashMap(AlgorithmProperties algorithmProperties) {
    HashMap<String, String> map = new HashMap<>();
    for (AlgorithmParameter algorithmParameter : algorithmProperties.getParameters()) {
      map.put(algorithmParameter.getName(), algorithmParameter.getValue());
    }
    return map;
  }

}
