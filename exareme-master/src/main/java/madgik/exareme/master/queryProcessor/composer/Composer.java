package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import madgik.exareme.utils.properties.AdpProperties;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author alex
 */
public class Composer {
  private static final Logger log = Logger.getLogger(Composer.class);

  private Composer(){}
  private static final Composer instance = new Composer();
  private static String repoPath = null;
  private static Algorithms algorithms = null;

  static {
    try {
      repoPath = AdpProperties.getGatewayProperties().getString("composer.repository.path");
      algorithms = Algorithms.createAlgorithms(repoPath);
    } catch (IOException e) {
      log.error("Unable to locate repository properties (*.json).", e);
    }
  }

  public static Composer getInstance(){return instance;}


  public String getRepositoryPath(){
    return repoPath;
  }

  public String getEndpoints() throws ComposerException {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      return gson.toJson(algorithms.getEndpoints(), Algorithms.Endpoint[].class);
  }
  public String getAlgorithms() throws ComposerException {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      return gson.toJson(algorithms.getAlgorithms(), Algorithms.Algorithm[].class);
  }

  public String getDefaultInputLocalTBL(){
    return algorithms.getInput_local_tbl();
  }

  public String compose(AlgorithmProperties algorithmProperties) throws ComposerException {

    StringBuilder dflScript = new StringBuilder();

    switch (algorithmProperties.getType()) {
      case local_global:
        String workingDir = repoPath  + algorithmProperties.getName();
        String localScriptPath = repoPath + algorithmProperties.getName() + "/local.template.sql";
        String globalScriptPath = repoPath + algorithmProperties.getName() + "/global.template.sql";

        // format local
        ArrayList<String> local_tbls = new ArrayList<>();
        for (Algorithms.Endpoint endpoint : algorithms.getEndpoints()) {
          String local_tbl = String.format("output_local_tbl_%s_%s",
              endpoint.getHost().replace('.','_'),
              endpoint.getPort()
          );
          local_tbls.add(local_tbl);

          dflScript.append("distributed create temporary table " + local_tbl + " as external \n");
          dflScript.append("select * \n");
          dflScript.append("from (\n");
          dflScript.append(
              String.format( "  madserver 'host:%s' 'port:%s' 'db:%s' \n",
                  endpoint.getHost(),
                  endpoint.getPort(),
                  endpoint.getPath()
              )
          );

          dflScript.append(String.format("    execnselect 'path:%s' ", workingDir));
          for (AlgorithmProperties.AlgorithmParameter algorithmParameter : algorithmProperties.getParameters()) {
            dflScript.append(String.format("'%s:%s' ", algorithmParameter.getName(), algorithmParameter.getValue()));
          }
          dflScript.append(String.format("\n    select filetext('%s')\n",localScriptPath));
          dflScript.append(");\n");

        }

        // format union
        dflScript.append("\ndistributed create temporary table  input_global_tbl to 1 as  \n");
        dflScript.append("select * \n");
        dflScript.append("from (\n");

        for (int i = 0; i < local_tbls.size(); i++){
          dflScript.append(
              String.format("  select %d as __local_id, * from %s\n", i + 1, local_tbls.get(i))
          );
          if ( i == (local_tbls.size() -1)) continue;
          else dflScript.append("  union all\n");
        }
        dflScript.append(");\n");

        // format global
        for (AlgorithmProperties.AlgorithmParameter algorithmParameter : algorithmProperties.getParameters()) {
          if ( algorithmParameter.getName().equals("input_local_tbl")){
            algorithmParameter.setName("input_global_tbl");
            algorithmParameter.setValue("input_global_tbl");
          }
        }

        dflScript.append("\nusing input_global_tbl \ndistributed create table output_global_tbl as external \n");
        dflScript.append("select * \n");
        dflScript.append("from (\n");
        dflScript.append(String.format("  execnselect 'path:%s' ", workingDir));
        for (AlgorithmProperties.AlgorithmParameter algorithmParameter : algorithmProperties.getParameters()) {
          dflScript.append(String.format("'%s:%s' ", algorithmParameter.getName(), algorithmParameter.getValue()));
        }
        dflScript.append(String.format("\n  select filetext('%s')\n",globalScriptPath));
        dflScript.append(");\n");

        break;
      case multiple_local_global:
        throw new ComposerException("Not supported yet.");
//        break;
      case pipeline:
        throw new ComposerException("Not supported yet.");
      default:
        throw new ComposerException("Unable to determinated algorithm type.");
    }
    return dflScript.toString();
  }

  public String composeVirtual(AlgorithmProperties algorithmProperties) throws ComposerException {

    StringBuilder dflScript = new StringBuilder();

    String workingDir = repoPath  + algorithmProperties.getName();

    HashMap<String, String> parameters = AlgorithmProperties.toHashMap(algorithmProperties);

    String inputLocalTbl = parameters.get(ComposerConstants.inputLocalTblKey);
    String outputGlobalTbl = parameters.get(ComposerConstants.outputGlobalTblKey);
    parameters.put(ComposerConstants.defaultDBKey, "/tmp/defaultDB.db");
    switch (algorithmProperties.getType()) {
      case local_global:
        parameters.remove(ComposerConstants.outputGlobalTblKey);

        String localScriptPath = repoPath + algorithmProperties.getName() + "/local.template.sql";
        String globalScriptPath = repoPath + algorithmProperties.getName() + "/global.template.sql";

        // format local
        dflScript.append("distributed create temporary table output_local_tbl as virtual \n");
        dflScript.append(String.format("select * from (\n    execnselect 'path:%s' ", workingDir));
        for (String key : parameters.keySet()) {
          dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
        }
        dflScript.append(String.format("\n    select filetext('%s')\n",localScriptPath));
        dflScript.append(");\n");

        // format union
        dflScript.append("\ndistributed create temporary table input_global_tbl to 1 as  \n");
        dflScript.append("select * from output_local_tbl;\n");

        // format global
        if ( parameters.containsKey(ComposerConstants.inputLocalTblKey)){
            parameters.remove(ComposerConstants.inputLocalTblKey);
            parameters.put(ComposerConstants.inputGlobalTblKey, "input_global_tbl");
        }


        dflScript.append(String.format("\nusing input_global_tbl \ndistributed create table %s as external \n", outputGlobalTbl));
        dflScript.append("select * \n");
        dflScript.append("from (\n");
        dflScript.append(String.format("  execnselect 'path:%s' ", workingDir));
        for (String key : parameters.keySet()) {
          dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
        }
        dflScript.append(String.format("\n  select filetext('%s')\n",globalScriptPath));
        dflScript.append(");\n");

        break;
      case multiple_local_global:
//        throw new ComposerException("Not supported yet.");
        File[] listFiles = new File(workingDir).listFiles(new FileFilter() {
          @Override
          public boolean accept(File pathname) {
            return pathname.isDirectory();
          }
        });
        Arrays.sort(listFiles);
        for (int i = 0; i< listFiles.length; i++){

          parameters.put(ComposerConstants.inputLocalTblKey, inputLocalTbl);
          parameters.put(ComposerConstants.algorithmKey,  algorithmProperties.getName() + "/" + listFiles[i].getName());
          parameters.put(ComposerConstants.outputGlobalTblKey, outputGlobalTbl);
          parameters.put(ComposerConstants.outputPrvGlobalTblKey, "output_global_tbl_".concat(String.valueOf(Integer.valueOf(listFiles[i].getName()) - 1)));
          parameters.put(ComposerConstants.algorithmIterKey, String.valueOf(i+1));
          if ( listFiles.length -1 == i) {
            parameters.put(ComposerConstants.isTmpKey, "false");
          } else {
            parameters.put(ComposerConstants.isTmpKey, "true");
          }
          dflScript.append(composeLocalGlobal(parameters));
        }
        break;
      case pipeline:

        break;
      default:
        throw new ComposerException("Unable to determinated algorithm type.");
    }
    return dflScript.toString();

  }

    private static String composeLocalGlobal(HashMap<String, String> parameters) throws ComposerException {
      StringBuilder dflScript = new StringBuilder();

      String algorithmKey = parameters.get(ComposerConstants.algorithmKey);
      boolean isTmp = Boolean.valueOf(parameters.get(ComposerConstants.isTmpKey));
      String inputLocalTbl = parameters.get(ComposerConstants.inputLocalTblKey);
      String outputGlobalTbl = parameters.get(ComposerConstants.outputGlobalTblKey);
      String prvOutputGlobalTbl = parameters.get(ComposerConstants.outputPrvGlobalTblKey);
      String algorithmIterstr = parameters.get(ComposerConstants.algorithmIterKey);

      int algorithmIter = Integer.valueOf(algorithmIterstr);
      parameters.remove(ComposerConstants.outputGlobalTblKey);

      String workingDir = repoPath  + algorithmKey;

      String localScriptPath = workingDir + "/local.template.sql";
      String globalScriptPath = workingDir + "/global.template.sql";

      // format local
      if ( algorithmIter > 1 ) {
        dflScript.append(String.format("using output_global_tbl_%d\n", algorithmIter-1));
      }

      dflScript.append(String.format("distributed create temporary table output_local_tbl_%d as virtual \n", algorithmIter));
      dflScript.append(String.format("select * from (\n    execnselect 'path:%s' ", workingDir));
      for (String key : parameters.keySet()) {
        dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
      }
      dflScript.append(String.format("\n    select filetext('%s')\n",localScriptPath));
      dflScript.append(");\n");

      // format union
      dflScript.append(String.format("\ndistributed create temporary table input_global_tbl_%d to 1 as \n",algorithmIter));
      dflScript.append(String.format("select * from output_local_tbl_%d;\n", algorithmIter));

      // format global
      parameters.remove(ComposerConstants.outputPrvGlobalTblKey);
      for (String key : parameters.keySet()) {
        if ( key.equals("input_local_tbl")){
          parameters.remove("input_local_tbl");
          parameters.put("input_global_tbl", String.format("input_global_tbl_%d", algorithmIter));
          break;
        }
      }
      if (isTmp)
        dflScript.append(String.format("\nusing input_global_tbl_%d \ndistributed create temporary table output_global_tbl_%d as external \n", algorithmIter, algorithmIter));
      else
        dflScript.append(String.format("\nusing input_global_tbl_%d \ndistributed create table %s as external \n", algorithmIter, outputGlobalTbl));

      dflScript.append("select * \n");
      dflScript.append("from (\n");
      dflScript.append(String.format("  execnselect 'path:%s' ", workingDir));
      for (String key : parameters.keySet()) {
        dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
      }
      dflScript.append(String.format("\n  select filetext('%s')\n",globalScriptPath));
      dflScript.append(");\n");
      parameters.remove("input_global_tbl");
    return dflScript.toString();
  }
}
