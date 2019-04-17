package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;
import madgik.exareme.utils.properties.AdpProperties;
import org.apache.log4j.Logger;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Loads the available algorithms and their properties from the algorithm's path.
 *
 */
public class Algorithms {
    private static Algorithms instance = null;
    private HashMap<String,AlgorithmProperties> algorithmsHashMap;
    private AlgorithmProperties[] algorithmsArray;

    private Algorithms(String repoPath) throws IOException {
        Gson gson = new Gson();
        File repoFile = new File(repoPath);
        if (!repoFile.exists()) throw new IOException("Unable to locate property file.");

        // Read every algorithm's property.json
        ArrayList<AlgorithmProperties> currentAlgorithms = new ArrayList<>();
        algorithmsHashMap = new HashMap<>();
        for (File file : Objects.requireNonNull(repoFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (!pathname.isDirectory())
                    return false;
                return new File(pathname, "properties.json").exists();
            }
        }))) {
            AlgorithmProperties algorithm = gson.fromJson(new BufferedReader(
                            new FileReader(file.getAbsolutePath() + "/properties.json")), AlgorithmProperties.class);
            algorithmsHashMap.put(algorithm.getName(),algorithm);
            currentAlgorithms.add(algorithm);
        }
        algorithmsArray = currentAlgorithms.toArray(new AlgorithmProperties[0]);
    }

    public static Algorithms getInstance() {
        if (instance == null) {
            try {
                instance = new Algorithms(getAlgorithmsFolderPath());
            } catch (IOException e) {
                Logger log = Logger.getLogger(Composer.class);
                log.error("Unable to locate repository properties (*.json).", e);
            }
        }
        return instance;
    }

    private static String getAlgorithmsFolderPath() {
        return AdpProperties.getGatewayProperties().getString("algorithms.path");
    }

    public static String getAlgorithmFolderPath(String algorithmName) {
        return getAlgorithmsFolderPath() + algorithmName;
    }

    public AlgorithmProperties[] getAlgorithms() {
        return algorithmsArray;
    }

    public AlgorithmProperties getAlgorithmProperties(String algorithmName) {
        return algorithmsHashMap.get(algorithmName);
    }
}
