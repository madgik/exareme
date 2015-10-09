/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine;

/**
 * @author herald
 */
public class ExecEngineConstants {

    public final static String MATERIALIZED_BUFFER_READER = null;
    public final static String MATERIALIZED_BUFFER_WRITER = null;

    public static final String PRAGMA_MATERIALIZED_BUFFER_READER = "materialized_reader";
    public static final String PRAGMA_MATERIALIZED_BUFFER_WRITER = "materialized_writer";
    public static final String PRAGMA_INTER_CONTAINER_MEDIATOR_FROM =
        "inter_container_mediator_from";
    public static final String PRAGMA_INTER_CONTAINER_MEDIATOR_TO = "inter_container_mediator_to";
    public static final String PRAGMA_INTER_CONTAINER_DATA_TRANSFER =
        "inter_container_data_transfer";

    public static final int THREADS_PER_INDEPENDENT_TASKS = 1024;
    public static double DATA_TRANSFER_MEM = 0.0;

    private ExecEngineConstants() {
        throw new RuntimeException("Cannot create instances of this class");
    }
}
