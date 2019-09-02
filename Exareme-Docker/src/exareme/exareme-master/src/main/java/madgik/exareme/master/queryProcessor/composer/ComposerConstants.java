package madgik.exareme.master.queryProcessor.composer;

import madgik.exareme.utils.properties.AdpProperties;

public class ComposerConstants {
    public static final String algorithmKey = "algorithm_key";
    public static final String inputLocalDBKey = "input_local_DB";
    public static final String dbQueryKey = "db_query";
    public static final String inputGlobalTblKey = "input_global_tbl";
    public static final String outputGlobalTblKey = "output_tbl";
    public static final String prevOutputGlobalTblKey = "prv_output_global_tbl";
    public static final String prevOutputLocalTblKey = "prv_output_local_tbl";
    public static final String defaultDBKey = "defaultDB";
    public static final String dbIdentifierKey = "dbIdentifier";
    public static final String DFL_SCRIPT_FILE_EXTENSION = ".dfl";
    public static final String localDBsKey = "local_step_dbs";
    public static final String globalDBKey = "global_step_db";
    public static final String curStatePKLKey = "cur_state_pkl";
    public static final String prevStatePKLKey = "prev_state_pkl";

    public static String getAlgorithmsFolderPath() {
        return AdpProperties.getGatewayProperties().getString("algorithms.path");
    }

    public static String getAlgorithmFolderPath(String algorithmName) {
        return getAlgorithmsFolderPath() + algorithmName;
    }

    public static String getDataPath() {
        return AdpProperties.getGatewayProperties().getString("data.path");
    }

    public static String getDatasetsDBName() {
        return AdpProperties.getGatewayProperties().getString("db.name");
    }

    public static String getDBDataTablename() {
        return AdpProperties.getGatewayProperties().getString("db.tablename.data");
    }

    public static String getDBMetadataTablename() {
        return AdpProperties.getGatewayProperties().getString("db.tablename.metadata");
    }

    public static String getPathologyPropertyName() {
        return AdpProperties.getGatewayProperties().getString("pathology.property.name");
    }
}
