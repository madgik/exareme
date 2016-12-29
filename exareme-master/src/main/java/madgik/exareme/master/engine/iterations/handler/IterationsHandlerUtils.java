package madgik.exareme.master.engine.iterations.handler;

import org.apache.log4j.Logger;

import java.util.HashMap;

import madgik.exareme.master.queryProcessor.composer.AlgorithmsProperties;

import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerConstants.iterationsAlgorithmProperties;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
class IterationsHandlerUtils {
    private static final Logger log = Logger.getLogger(IterationsHandler.class);

    /**
     * Ensures all required iterative algorithm properties are defined.
     *
     * @param algorithmPropertiesMap The iterative algorithm properties converted to a HashMap.<br>
     *                               See {@link AlgorithmsProperties.AlgorithmProperties#toHashMap(AlgorithmsProperties.AlgorithmProperties)}
     * @return True if all required properties are defined, false otherwise.
     */
    static boolean ensureDefinedIterationsParameterProperties(HashMap<String, String> algorithmPropertiesMap) {
        for (IterationsHandlerConstants.iterativeAlgorithmProperties property :
                IterationsHandlerConstants.iterativeAlgorithmProperties.values()) {

            if (!algorithmPropertiesMap.containsKey(iterationsAlgorithmProperties[property.ordinal()])) {
                log.debug("Iterative algorithm property \""
                        + iterationsAlgorithmProperties[property.ordinal()] + "\" is missing.");
                return false;
            }
        }
        return true;
    }

    /**
     * Generates a unique algorithm key.
     * <p>
     * It provides a unique name with <b>granularity of a ms!</b>
     *
     * @param algorithmProperties The algorithm's properties object.
     * @return A unique algorithm key.
     */
    static String generateAlgorithmRequestKey(AlgorithmsProperties.AlgorithmProperties algorithmProperties) {
        return "request_" +
                algorithmProperties.getName() +
                "_" +
                String.valueOf(System.currentTimeMillis());
    }

    /**
     * Generates the iterationsDB absolute filename.
     *
     * @param algorithmKey The algorithm key generated for the current iterative algorithm.
     * @return The absolute filename of iterationsDB for this algorithm.
     */
    static String generateIterationsDBName(String algorithmKey) {
        return IterationsHandlerConstants.mipAlgorithmsDemoWorkingDirectory
                + algorithmKey + "/"
                + IterationsHandlerConstants.iterationsParameterIterDBValueSuffix;
    }
}
