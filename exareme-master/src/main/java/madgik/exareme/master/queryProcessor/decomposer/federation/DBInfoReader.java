/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author dimitris
 */
public class DBInfoReader {

    public static DBInfo dbInfo;

    public static void read(String filename) {
        Properties prop = new Properties();
        InputStream input = null;
        dbInfo = new DBInfo();
        try {

            input = new FileInputStream(filename);

            // load a properties file
            prop.load(input);
            String[] dbids = prop.getProperty("DB_IDs").split(", ");

            for (String db : dbids) {
                dbInfo.addDB(db);
                dbInfo.setUserForDB(db, prop.getProperty(db + "_DBUSER"));
                dbInfo.setPassForDB(db, prop.getProperty(db + "_DBPASSWORD"));
                dbInfo.setDriverForDB(db, prop.getProperty(db + "_JDBC_DRIVER"));
                dbInfo.setURLForDB(db, prop.getProperty(db + "_JDBC_URL"));
                dbInfo.setMadisStringForDB(db, prop.getProperty(db + "_MADIS_STRING"));
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
