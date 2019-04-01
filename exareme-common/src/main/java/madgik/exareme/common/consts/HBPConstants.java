package madgik.exareme.common.consts;

import madgik.exareme.utils.properties.AdpProperties;

/**
 * Contains HumanBrainProject related constants.
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
public class HBPConstants {
    private static final String DB_DIR = "db/";
    private static final String ALGORITHMS_GENERATION_DIR = "algorithms-generation";

    private static final String DEMO_DIRECTORY;
    public static final String DEMO_ALGORITHMS_WORKING_DIRECTORY;
    public static final String DEMO_DB_WORKING_DIRECTORY;

    static {
        DEMO_DIRECTORY = AdpProperties.getGatewayProperties().getString("demo.directory");
        DEMO_DB_WORKING_DIRECTORY = DEMO_DIRECTORY + DB_DIR;
        DEMO_ALGORITHMS_WORKING_DIRECTORY = DEMO_DIRECTORY + ALGORITHMS_GENERATION_DIR;
    }
}
