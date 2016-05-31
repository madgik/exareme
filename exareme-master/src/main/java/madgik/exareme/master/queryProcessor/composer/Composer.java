package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import madgik.exareme.utils.properties.AdpProperties;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible to produce data flows (dfl)
 * by combining repository templates and custom parameters.
 * @author alexpap
 * @version 0.1
 */
public class Composer {

    private static final Logger log = Logger.getLogger(Composer.class);

    private Composer() {
    }

    private static final Composer instance = new Composer();
    private static String repoPath = null;
    private static AlgorithmsProperties algorithms = null;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static {
        try {
            repoPath = AdpProperties.getGatewayProperties().getString("composer.repository.path");
            log.trace(repoPath);
            algorithms = AlgorithmsProperties.createAlgorithms(repoPath);
        } catch (IOException e) {
            log.error("Unable to locate repository properties (*.json).", e);
        }
    }

    public static Composer getInstance() {
        return instance;
    }


    public String getRepositoryPath() {
        return repoPath;
    }

    public String getEndpoints() throws ComposerException {
        return gson.toJson(algorithms.getEndpoints(), AlgorithmsProperties.EndpointProperties[].class);
    }

    public String getAlgorithms() throws ComposerException {
        return gson.toJson(algorithms.getAlgorithms(), AlgorithmsProperties.AlgorithmProperties[].class);
    }

    public String getAlgorithmsProperties() throws ComposerException{
        return gson.toJson(algorithms, AlgorithmsProperties.class);
    }

    public String composeVirtual(String qKey, AlgorithmsProperties.AlgorithmProperties algorithmProperties)
        throws Exception {

        StringBuilder dflScript = new StringBuilder();

        String workingDir = repoPath + algorithmProperties.getName();

        HashMap<String, String> parameters = AlgorithmsProperties.AlgorithmProperties.toHashMap(algorithmProperties);

        String inputLocalTbl = algorithms.getLocal_engine_default().toUDF();
        parameters.put(ComposerConstants.inputLocalTblKey, inputLocalTbl);
        String outputGlobalTbl = parameters.get(ComposerConstants.outputGlobalTblKey);
        parameters.put(ComposerConstants.defaultDBKey, "/tmp/demo/db/" + qKey + "_defaultDB.db");
        switch (algorithmProperties.getType()) {

            case local:
                parameters.remove(ComposerConstants.outputGlobalTblKey);

                String lp = repoPath + algorithmProperties.getName() + "/local.template.sql";

                // format local
                dflScript.append("distributed create table " + outputGlobalTbl + " as external \n");
                dflScript.append(
                    String.format("select * from (\n    execnselect 'path:%s' ", workingDir));
                for (String key : parameters.keySet()) {
                    dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
                }
                dflScript.append(String.format("\n    select filetext('%s')\n", lp));
                dflScript.append(");\n");
                break;

            case local_global:
                parameters.remove(ComposerConstants.outputGlobalTblKey);

                String localScriptPath =
                    repoPath + algorithmProperties.getName() + "/local.template.sql";
                String globalScriptPath =
                    repoPath + algorithmProperties.getName() + "/global.template.sql";

                // format local
                dflScript
                    .append("distributed create temporary table output_local_tbl as virtual \n");
                dflScript.append(
                    String.format("select * from (\n    execnselect 'path:%s' ", workingDir));
                for (String key : parameters.keySet()) {
                    dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
                }
                dflScript.append(String.format("\n    select filetext('%s')\n", localScriptPath));
                dflScript.append(");\n");

                // format union
                dflScript
                    .append("\ndistributed create temporary table input_global_tbl to 1 as  \n");
                dflScript.append("select * from output_local_tbl;\n");

                // format global
                if (parameters.containsKey(ComposerConstants.inputLocalTblKey)) {
                    parameters.remove(ComposerConstants.inputLocalTblKey);
                    parameters.put(ComposerConstants.inputGlobalTblKey, "input_global_tbl");
                }


                dflScript.append(String
                    .format("\nusing input_global_tbl \ndistributed create table %s as external \n",
                        outputGlobalTbl));
                dflScript.append("select * \n");
                dflScript.append("from (\n");
                dflScript.append(String.format("  execnselect 'path:%s' ", workingDir));
                for (String key : parameters.keySet()) {
                    dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
                }
                dflScript.append(String.format("\n  select filetext('%s')\n", globalScriptPath));
                dflScript.append(");\n");

                break;
            case multiple_local_global:
                //        throw new ComposerException("Not supported yet.");
                File[] listFiles = new File(workingDir).listFiles(new FileFilter() {
                    @Override public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });
                Arrays.sort(listFiles);
                for (int i = 0; i < listFiles.length; i++) {

                    parameters.put(ComposerConstants.inputLocalTblKey, inputLocalTbl);
                    parameters.put(ComposerConstants.algorithmKey,
                        algorithmProperties.getName() + "/" + listFiles[i].getName());
                    parameters.put(ComposerConstants.outputGlobalTblKey, outputGlobalTbl);
                    parameters.put(ComposerConstants.outputPrvGlobalTblKey, "output_global_tbl_"
                        .concat(String.valueOf(Integer.valueOf(listFiles[i].getName()) - 1)));
                    parameters.put(ComposerConstants.algorithmIterKey, String.valueOf(i + 1));
                    if (listFiles.length - 1 == i) {
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

    private static String composeLocalGlobal(HashMap<String, String> parameters)
        throws ComposerException {
        StringBuilder dflScript = new StringBuilder();

        String algorithmKey = parameters.get(ComposerConstants.algorithmKey);
        boolean isTmp = Boolean.valueOf(parameters.get(ComposerConstants.isTmpKey));
        String inputLocalTbl = parameters.get(ComposerConstants.inputLocalTblKey);
        String outputGlobalTbl = parameters.get(ComposerConstants.outputGlobalTblKey);
        String prvOutputGlobalTbl = parameters.get(ComposerConstants.outputPrvGlobalTblKey);
        String algorithmIterstr = parameters.get(ComposerConstants.algorithmIterKey);

        int algorithmIter = Integer.valueOf(algorithmIterstr);
        parameters.remove(ComposerConstants.outputGlobalTblKey);

        String workingDir = repoPath + algorithmKey;

        String localScriptPath = workingDir + "/local.template.sql";
        String globalScriptPath = workingDir + "/global.template.sql";

        // format local
        if (algorithmIter > 1) {
            dflScript.append(String.format("using output_global_tbl_%d\n", algorithmIter - 1));
        }

        dflScript.append(String
            .format("distributed create temporary table output_local_tbl_%d as virtual \n",
                algorithmIter));
        dflScript.append(String.format("select * from (\n    execnselect 'path:%s' ", workingDir));
        for (String key : parameters.keySet()) {
            dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
        }
        dflScript.append(String.format("\n    select filetext('%s')\n", localScriptPath));
        dflScript.append(");\n");

        // format union
        dflScript.append(String
            .format("\ndistributed create temporary table input_global_tbl_%d to 1 as \n",
                algorithmIter));
        dflScript.append(String.format("select * from output_local_tbl_%d;\n", algorithmIter));

        // format global
        parameters.remove(ComposerConstants.outputPrvGlobalTblKey);
        for (String key : parameters.keySet()) {
            if (key.equals("input_local_tbl")) {
                parameters.remove("input_local_tbl");
                parameters
                    .put("input_global_tbl", String.format("input_global_tbl_%d", algorithmIter));
                break;
            }
        }
        if (isTmp)
            dflScript.append(String.format(
                "\nusing input_global_tbl_%d \ndistributed create temporary table output_global_tbl_%d as external \n",
                algorithmIter, algorithmIter));
        else
            dflScript.append(String
                .format("\nusing input_global_tbl_%d \ndistributed create table %s as external \n",
                    algorithmIter, outputGlobalTbl));

        dflScript.append("select * \n");
        dflScript.append("from (\n");
        dflScript.append(String.format("  execnselect 'path:%s' ", workingDir));
        for (String key : parameters.keySet()) {
            dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
        }
        dflScript.append(String.format("\n  select filetext('%s')\n", globalScriptPath));
        dflScript.append(");\n");
        parameters.remove("input_global_tbl");
        return dflScript.toString();
    }
}
