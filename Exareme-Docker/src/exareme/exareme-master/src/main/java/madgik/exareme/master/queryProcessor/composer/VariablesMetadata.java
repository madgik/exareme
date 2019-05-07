package madgik.exareme.master.queryProcessor.composer;

import java.sql.*;
import java.util.HashMap;

import madgik.exareme.master.queryProcessor.composer.Exceptions.VariablesMetadataException;
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
        String enumerations;
        int enumerationsCount;

        private VariableProperties(String sql_type, int categorical, String enumerations) throws VariablesMetadataException {
            this.sql_type = sql_type;
            if (categorical == 1)
                this.categorical = "true";
            else if (categorical == 0)
                this.categorical = "false";
            else
                throw new VariablesMetadataException("Categorical values can be 0 or 1 in the metadata.");
            this.enumerations = enumerations;
            enumerationsCount = enumerations.split(",").length;
        }

        private String getSql_type() {
            return sql_type;
        }

        private String getCategorical() {
            return categorical;
        }

        private int getEnumerationsCount() {
            return enumerationsCount;
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
            log.debug("Opened database successfully");

            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + metadataTablename + ";");

            while (rs.next()) {
                String code = rs.getString("code");
                String sql_type = rs.getString("sql_type");
                int categorical = rs.getInt("categorical");
                String enumerations = rs.getString("enumerations");
                if (enumerations == null)
                    enumerations = "";

                variablesMetadata.put(code, new VariableProperties(sql_type, categorical, enumerations));
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            throw new VariablesMetadataException("Could not load variables metadata. Error: " + e.getMessage());
        }
        log.debug("Metadata loaded successfully");
    }

    public Boolean columnExists(String code) {
        return variablesMetadata.containsKey(code);
    }

    public String getColumnValuesSQLType(String code) {
        return variablesMetadata.get(code).getSql_type();
    }

    public String getColumnValuesIsCategorical(String code) {
        return variablesMetadata.get(code).getCategorical();
    }

    public int getColumnValuesNumOfEnumerations(String code) {
        return variablesMetadata.get(code).getEnumerationsCount();
    }
}
