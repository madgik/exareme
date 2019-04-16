package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents the mip-algorithms repository properties.
 * <p>
 * The properties.json file is an AlgorithmProperties class.
 */
public class Algorithms {

    private AlgorithmProperties[] algorithms;

    public Algorithms(String repoPath) throws IOException {

        Gson gson = new Gson();
        File repoFile = new File(repoPath);
        if (!repoFile.exists()) throw new IOException("Unable to locate property file.");

        // read per algorithm property.json
        ArrayList<AlgorithmProperties> currentAlgorithms = new ArrayList<>();

        for (File file : Objects.requireNonNull(repoFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if(!pathname.isDirectory())
                    return false;
                return new File(pathname, "properties.json").exists();            }
        }))) {
            AlgorithmProperties algorithm =
                    gson.fromJson(
                            new BufferedReader(
                                    new FileReader(file.getAbsolutePath() + "/properties.json")),
                            AlgorithmProperties.class);
            currentAlgorithms.add(algorithm);
        }
        setAlgorithms(currentAlgorithms.toArray(new AlgorithmProperties[0]));
    }

    public AlgorithmProperties[] getAlgorithms() {
        return algorithms;
    }

    public void setAlgorithms(AlgorithmProperties[] algorithms) {
        this.algorithms = algorithms;
    }
}
