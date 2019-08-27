package madgik.exareme.master.queryProcessor.composer;

import java.io.File;
import java.io.FileFilter;
import java.sql.*;
import java.util.HashMap;
import java.util.Set;

import madgik.exareme.master.queryProcessor.composer.Exceptions.CDEsMetadataException;
import org.apache.log4j.Logger;

/**
 * Loads the variables' metadata from the database.
 */

public class CDEsMetadata {
    private static Logger log = Logger.getLogger(Composer.class);
    private static CDEsMetadata instance = null;
    private HashMap<String, PathologyCDEsMetadata> cdesMetadata;

    public class PathologyCDEsMetadata{
        private HashMap<String, CDEProperties> pathologyCDEsMetadata;

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

        private PathologyCDEsMetadata(String databasePath) throws CDEsMetadataException {
            String metadataTablename = ComposerConstants.getDBMetadataTablename();
            pathologyCDEsMetadata = new HashMap<>();

            try {

                Class.forName("org.sqlite.JDBC");
                Connection c = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
                c.setAutoCommit(false);
                log.debug("Opened database at '" + databasePath + "' successfully");

                Statement stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + metadataTablename + ";");

                while (rs.next()) {
                    String code = rs.getString("code");
                    String sql_type = rs.getString("sql_type");
                    int categorical = rs.getInt("isCategorical");
                    String enumerations = rs.getString("enumerations");
                    if (enumerations == null)
                        enumerations = "";

                    pathologyCDEsMetadata.put(code, new CDEProperties(sql_type, categorical, enumerations));
                }
                rs.close();
                stmt.close();
                c.close();
            } catch (Exception e) {
                throw new CDEsMetadataException("Could not load variables metadata at '" + databasePath + "' . Error: " + e.getMessage());
            }
            log.debug("Metadata at '" + databasePath + "' loaded successfully");
        }

        public Boolean columnExists(String code) {
            return pathologyCDEsMetadata.containsKey(code);
        }

        public String getColumnValuesSQLType(String code) {
            return pathologyCDEsMetadata.get(code).getSql_type();
        }

        public String getColumnValuesIsCategorical(String code) {
            return pathologyCDEsMetadata.get(code).getCategorical();
        }

        public int getColumnValuesNumOfEnumerations(String code) {
            return pathologyCDEsMetadata.get(code).getEnumerationsCount();
        }
    }

    public static CDEsMetadata getInstance() throws CDEsMetadataException {
        if (instance == null) {
            instance = new CDEsMetadata();
        }
        return instance;
    }

    private CDEsMetadata() throws CDEsMetadataException {
        String dataPath = ComposerConstants.getDataPath();
        String datasetsDBName = ComposerConstants.getDatasetsDBName();
        cdesMetadata = new HashMap<>();

        File[] pathologiesDirectory = new File(dataPath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        if(pathologiesDirectory.length == 0){
            throw new CDEsMetadataException("There are no pathologies inside the dataset folder.");
        }

        for(File pathology: pathologiesDirectory){
            String pathologyName = pathology.getName();
            log.error("Line 130 " + pathologyName);
            File pathologyDatabase = new File(pathology.getAbsolutePath(), datasetsDBName);
            log.error("Line 132 " + pathologyDatabase.getAbsolutePath());

            if(!pathologyDatabase.exists()) {
                log.error("The '" + pathologyDatabase.getAbsolutePath() + "' file does not exist.");
                throw new CDEsMetadataException("There was an error loading the metadata. Please consult the administrator.");
            }

            cdesMetadata.put(pathologyName,new PathologyCDEsMetadata(pathologyDatabase.getAbsolutePath()));
            log.debug("Metadata '" + pathologyName + "' loaded successfully");
        }

        log.debug("Metadata loaded successfully");
    }

    public PathologyCDEsMetadata getPathologyCDEsMetadata(String pathology){
        return cdesMetadata.get(pathology);
    }
}
