/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author dimitris
 */
public class DBInfoWriter {

    public static void write(String query, String filename) throws IOException {
        Properties props;
        FileInputStream in = new FileInputStream(filename);
        props = new Properties();
        props.load(in);


        String sqlParams = query.substring(query.indexOf("(") + 1, query.lastIndexOf(")"));
        if (query.startsWith("addFederatedEndpoint")) {
            sqlParams = sqlParams.replaceAll(" ", "");
            String[] params = sqlParams.split(",");

            StringBuilder stringIds = new StringBuilder();
            FileOutputStream out = new FileOutputStream(filename);
            if (props.containsKey("DB_IDs")) {
                ArrayList<String> ids =
                    new ArrayList<String>(Arrays.asList(props.getProperty("DB_IDs").split(", ")));

                if (!ids.contains(params[0])) {
                    ids.add(params[0]);

                    String delimiter;
                    delimiter = "";
                    for (String s : ids) {
                        stringIds.append(delimiter);
                        stringIds.append(s);
                        delimiter = ", ";
                    }
                    props.setProperty("DB_IDs", stringIds.toString());
                }
            } else {
                stringIds.append(params[0]);
                props.setProperty("DB_IDs", stringIds.toString());
            }

            props.setProperty(params[0] + "_DBPASSWORD", params[4]);
            props.setProperty(params[0] + "_DBUSER", params[3]);
            props.setProperty(params[0] + "_JDBC_DRIVER", params[2]);
            props.setProperty(params[0] + "_JDBC_URL", params[1]);
            props.setProperty(params[0] + "_DBPASSWORD", params[4]);
            String madisString;
            madisString = "";
            String host = params[1].split(":")[2];
            if (host.startsWith("//")) {
                host = host.substring(2);
            }
            if (params[2].equalsIgnoreCase("com.mysql.jdbc.Driver")) {
                madisString =
                    "mysql " + "h:" + host + " u:" + params[3] + " p:" + params[4] + " db:"
                        + params[1].split("/")[params[1].split("/").length - 1];
            } else if (params[2].contains("OracleDriver")) {
                madisString = "oracle " + host + " u:" + params[3] + " p:" + params[4];
            }
            props.setProperty(params[0] + "_MADIS_STRING", madisString);
            props.store(out, null);


        }
    }
}
