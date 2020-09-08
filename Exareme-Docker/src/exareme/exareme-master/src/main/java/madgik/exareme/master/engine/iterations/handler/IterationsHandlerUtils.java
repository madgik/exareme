package madgik.exareme.master.engine.iterations.handler;

import madgik.exareme.common.consts.HBPConstants;
import org.apache.log4j.Logger;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
public class IterationsHandlerUtils {
    private static final Logger log = Logger.getLogger(IterationsHandlerUtils.class);

    /**
     * Generates the iterationsDB absolute filename.
     *
     * @param algorithmKey the algorithm key generated for the current iterative algorithm
     * @return the absolute filename of iterationsDB for this algorithm.
     */
    public static String generateIterationsDBName(String algorithmKey) {
        return HBPConstants.DEMO_DB_WORKING_DIRECTORY
                + algorithmKey + "/"
                + IterationsConstants.iterationsParameterIterDBValueSuffix;
    }
}
