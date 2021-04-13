package madgik.exareme.master.queryProcessor.HBP;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import madgik.exareme.master.queryProcessor.HBP.Exceptions.AlgorithmException;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Loads the available algorithms and their properties from the algorithm's path.
 */
public class Algorithms {
    private static Algorithms instance = null;
    private final HashMap<String, AlgorithmProperties> algorithmsHashMap;
    private final AlgorithmProperties[] algorithmsArray;

    private Algorithms(String repoPath) throws IOException, AlgorithmException {
        Gson gson = new Gson();
        File repoFile = new File(repoPath);
        if (!repoFile.exists()) throw new IOException("Unable to locate property file.");
        // Read every algorithm's property.json
        ArrayList<AlgorithmProperties> currentAlgorithms = new ArrayList<>();
        algorithmsHashMap = new HashMap<>();

        for (File file : Objects.requireNonNull(repoFile.listFiles(pathname -> {
            if (!pathname.isDirectory())
                return false;
            return new File(pathname, "properties.json").exists();
        }))) {
            AlgorithmProperties algorithm;
            try {
                algorithm = gson.fromJson(new BufferedReader(
                        new FileReader(file.getAbsolutePath() + "/properties.json")), AlgorithmProperties.class);
            } catch (JsonSyntaxException e) {
                throw new AlgorithmException("Unknown", "Could not parse algorithms properly: " + e.getMessage());
            }
            algorithm.validateAlgorithmPropertiesInitialization();
            algorithmsHashMap.put(algorithm.getName(), algorithm);
            currentAlgorithms.add(algorithm);
        }
        algorithmsArray = currentAlgorithms.toArray(new AlgorithmProperties[0]);
    }

    public static Algorithms getInstance() throws AlgorithmException {
        if (instance == null) {
            try {
                instance = new Algorithms(ComposerConstants.getAlgorithmsFolderPath());
            } catch (IOException e) {
                Logger log = Logger.getLogger(Composer.class);
                log.error("Unable to locate repository properties (*.json).", e);
            }
        }
        return instance;
    }

    public AlgorithmProperties[] getAlgorithms() {
        return algorithmsArray;
    }

    public AlgorithmProperties getAlgorithmProperties(String algorithmName) {
        return algorithmsHashMap.get(algorithmName);
    }
}
