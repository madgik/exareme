package madgik.exareme.master.queryProcessor.composer;

import java.sql.*;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * Loads the variables' metadata from the database.
 */
public class VariablesMetadata {
    private static Logger log = Logger.getLogger(Composer.class);
    private static VariablesMetadata instance = null;
    private HashMap<String, VariableProperties> variablesMetadata;

    private class VariableProperties {
        String sql_type;
        String categorical;

        private VariableProperties(String sql_type, int categorical) throws VariablesMetadataException {
            this.sql_type = sql_type;
            if (categorical == 1)
                this.categorical = "true";
            else if (categorical == 0)
                this.categorical = "false";
            else
                throw new VariablesMetadataException("Categorical values can be 0 or 1 in the metadata.");
        }

        private String getSql_type() {
            return sql_type;
        }

        private String getCategorical() {
            return categorical;
        }
    }

    public static VariablesMetadata getInstance() throws VariablesMetadataException {
        if (instance == null) {
            instance = new VariablesMetadata();
        }
        return instance;
    }

    private VariablesMetadata() throws VariablesMetadataException {
        String databasePath = ComposerConstants.getDatasetDBDirectory();
        String metadataTablename = ComposerConstants.getDBMetadataTablename();
        variablesMetadata = new HashMap<>();

        try {

            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            c.setAutoCommit(false);
            log.error("Opened database successfully");

            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + metadataTablename + ";");

            while (rs.next()) {
                String code = rs.getString("code");
                String sql_type = rs.getString("sql_type");
                int categorical = rs.getInt("categorical");

                variablesMetadata.put(code, new VariableProperties(sql_type, categorical));
                log.error("code :" + code);
                log.error("sql_type :" + sql_type);
                log.error("categorical :" + categorical);
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            throw new VariablesMetadataException("Could not load variables metadata. Error: " + e.getMessage());
        }
        log.error("Metadata loaded successfully");
    }

    public Boolean columnExists(String code){
        return variablesMetadata.containsKey(code);
    }

    public String getColumnValuesSQLType(String code) {
        return variablesMetadata.get(code).getSql_type();
    }

    public String getColumnValuesCategorical(String code) {
        return variablesMetadata.get(code).getCategorical();
    }
}
