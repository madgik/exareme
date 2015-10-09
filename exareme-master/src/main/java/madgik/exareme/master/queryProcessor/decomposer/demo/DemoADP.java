/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.demo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

/**
 * @author dimitris
 */
public class DemoADP {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {


        Class.forName("madgik.adp.federatedjdbc.AdpDriver");

        String conString =
            "jdbc:fedadp:http://127.0.0.1:9090/tmp/-fedDB-npd-next-jdbc:mysql://10.240.0.10:3306/npd-next-com.mysql.jdbc.Driver-next-benchmark-next-gray769watt724!@#-next-npd";

        Connection conn2 = DriverManager.getConnection(conString, "adp", "adp");

        Statement st = conn2.createStatement();

        //     ResultSet rs2=st.executeQuery("addFederatedEndpoint(DB1, jdbc:mysql://10.254.11.17:3306/npd-all, com.mysql.jdbc.Driver, mysql,mysql)");
        //rs2.close();

        // ResultSet rs4=st.executeQuery("addFederatedEndpoint(DB2, jdbc:mysql://10.254.11.17:3306/npd-all, com.mysql.jdbc.Driver, mysql,mysql)");

        //  rs4.close();

        // String q2="select fldName as c1 from NPD_discovery";
        //System.out.println(q);
        // rs2.close();
     /*   String fromFile = "";
        try {
            fromFile = readFile("/home/dimitris/sleggeSQL/001/01.q.sql");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

        String well = "SELECT\n" +
            "   1 AS \"oQuestType\", NULL AS \"oLang\",\n" +
            "'http://www.optique-project.eu/mini-federated-ontology#Wellbore' AS \"o\"\n" +
            " FROM\n" +
            "adp.npd_wellbore_core QVIEW1\n" +
            "WHERE\n" +
            "(QVIEW1.\"wlbCoreIntervalUom\" = '162F2-10')\n" +
            "UNION ALL\n" +
            "SELECT\n" +
            "   1 AS \"oQuestType\", NULL AS \"oLang\",\n" +
            "'http://www.optique-project.eu/mini-federated-ontology#Wellbore' AS \"o\"\n" +
            " FROM\n" +
            "(SELECT substr(IDENTIFIER,4) as WELLBOREID, IDENTIFIER, WELL_IDENTIFIER,\n" +
            "REF_WELLBORE as KIND FROM adp.EPDS_WELLBORE) QVIEW1\n" +
            "WHERE\n" +
            "(QVIEW1.\"wellboreid\" = '162F2-10')\n" +
            "UNION ALL\n" +
            "SELECT\n" +
            "   1 AS \"oQuestType\", NULL AS \"oLang\",\n" +
            "'http://www.optique-project.eu/mini-federated-ontology#Wellbore' AS \"o\"\n" +
            " FROM\n" +
            "(SELECT substr(IDENTIFIER,4) as WELLBOREID, IDENTIFIER, WELL_IDENTIFIER,\n" +
            "REF_WELLBORE as KIND FROM adp.EPDS_WELLBORE) QVIEW1\n" +
            "WHERE\n" +
            "(QVIEW1.\"wellboreid\" = '162F2-10')";


        String q = "select * from db1_vendor";
        String q1 = "select * from npd_wellbore_core";
        String demoSemicolon =
            "Select replace(a.wlbCoreIntervalUom , ';', '%3B') from npd_wellbore_core a";
        String testFed = "SELECT \n" +
            "  QVIEW5.`wlbTotalCoreLength`  AS `year`\n" +
            " FROM \n" +
            "npd_wellbore_core QVIEW5,\n" +
            "npd_wellbore_shallow_all QVIEW6 \n" +
            "WHERE \n" +
            "(QVIEW5.`wlbNpdidWellbore` = QVIEW6.`wlbNpdidWellbore`)";
        Statement st2 = conn2.createStatement();
        ResultSet rs3 = st2.executeQuery(testFed);
        //    ResultSet rs2=st.executeQuery("addFederatedEndpoint(DB1, jdbc:mysql://10.254.11.17:3306/npd-all, com.mysql.jdbc.Driver, mysql,mysql)");



        //String q="select fldName as c1 from NPD_discovery";
        //System.out.println(q);
        //rs2.close();
        // rs2=st.executeQuery(q);
        System.out.println("Columns: " + rs3.getMetaData().getColumnCount());
        int count = 0;
        int size = 0;
        for (int c = 0; c < rs3.getMetaData().getColumnCount(); ++c) {
            System.out.println(rs3.getMetaData().getColumnName(c + 1));
        }
        while (rs3.next()) {
            String[] next = new String[rs3.getMetaData().getColumnCount()];
            for (int c = 0; c < next.length; ++c) {
                next[c] = "" + rs3.getObject(c + 1);
                size += next[c].length();
            }
            for (int i = 0; i < next.length; i++) {
                System.out.print(next[i] + "\t");
            }
            System.out.println("");
            ++count;
        }
        System.out.println("Count: " + count + "\n\tSize: " + size);
        rs3.close();
    }

    private static String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        return stringBuilder.toString();
    }
}
