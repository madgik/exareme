package madgik.exareme.master.queryProcessor.composer;

import java.sql.*;
import java.util.HashMap;

import madgik.exareme.master.queryProcessor.composer.Exceptions.CDEsMetadataException;
import org.apache.log4j.Logger;

/**
 * Loads the variables' metadata from the database.
 */
public class CDEsMetadata {
    private static Logger log = Logger.getLogger(Composer.class);
    private static CDEsMetadata instance = null;
    private HashMap<String, CDEProperties> CDEsMetadata;

    private class CDEProperties {
        String sql_type;
        String categorical;
        String enumerations;
        int enumerationsCount;

        private CDEProperties(String sql_type, int categorical, String enumerations) throws CDEsMetadataException {
            this.sql_type = sql_type;
            if (categorical == 1)
                this.categorical = "true";
            else if (categorical == 0)
                this.categorical = "false";
            else
                throw new CDEsMetadataException("Categorical values can be 0 or 1 in the metadata.");
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

    public static CDEsMetadata getInstance() throws CDEsMetadataException {
        if (instance == null) {
            instance = new CDEsMetadata();
        }
        return instance;
    }

    private CDEsMetadata() throws CDEsMetadataException {
        String databasePath = ComposerConstants.getDatasetDBDirectory();
        String metadataTablename = ComposerConstants.getDBMetadataTablename();
        CDEsMetadata = new HashMap<>();

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
                int categorical = rs.getInt("isCategorical");
                String enumerations = rs.getString("enumerations");
                if (enumerations == null)
                    enumerations = "";

                CDEsMetadata.put(code, new CDEProperties(sql_type, categorical, enumerations));
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            throw new CDEsMetadataException("Could not load variables metadata. Error: " + e.getMessage());
        }
        log.debug("Metadata loaded successfully");
    }

    public Boolean columnExists(String code) {
        return CDEsMetadata.containsKey(code);
    }

    public String getColumnValuesSQLType(String code) {
        return CDEsMetadata.get(code).getSql_type();
    }

    public String getColumnValuesIsCategorical(String code) {
        return CDEsMetadata.get(code).getCategorical();
    }

    public int getColumnValuesNumOfEnumerations(String code) {
        return CDEsMetadata.get(code).getEnumerationsCount();
    }
}
