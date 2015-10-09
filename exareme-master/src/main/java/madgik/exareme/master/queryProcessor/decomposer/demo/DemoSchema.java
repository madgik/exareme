/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.demo;

import madgik.exareme.master.queryProcessor.decomposer.federation.DBInfoWriterDB;
import madgik.exareme.master.queryProcessor.decomposer.federation.QueryDecomposer;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQueryParser;

import java.util.ArrayList;

/**
 * @author dimitris
 */
public class DemoSchema {

    public static void main(String[] args) throws Exception {
        DBInfoWriterDB.write(
            "addFederatedEndpoint(NPD_MIRROR,OracleDriver:mysql://whale.di.uoa.gr:3306/bookstores,OracleDriver.mysql.jdbc.Driver,benchmark,gray769watt724!@#,bookstores)",
            "/tmp/");
        DBInfoWriterDB.write(
            "addFederatedEndpoint(EPDS,OracleDriver:mysql://whale.di.uoa.gr:3306/epds,OracleDriver.mysql.jdbc.Driver,benchmark,gray769watt724!@#,epds)",
            "/tmp/");

        String testAdpScehma =
            "select * from ( select substr(field, 4) as f from bench_bookstores) aaaa limit 0 ";

        String ontopDummy = " SELECT *\n" +
            "FROM (\n" +
            "SELECT DISTINCT \n" +
            "   1 AS \"typeQuestType\", NULL AS \"typeLang\", 'http://www.optique-project.eu/well-ontology/WellboreContent' AS \"type\"\n"
            +
            " FROM \n" +
            "(SELECT 1) tdummy \n" +
            ") SUB_QVIEW";

        String slegge = "SELECT *\n" +
            "FROM (\n" +
            "SELECT\n" +
            "   1 AS \"c1QuestType\", NULL AS \"c1Lang\",\n" +
            "('http://www.optique-project.eu/resource/' ||\n" +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(QVIEW1.\"WLBNAME\",'\n"
            +
            "', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&',\n" +
            "'%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'),\n" +
            "',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+',\n" +
            "'%2B'), '''', '%22'), '/', '%2F')) AS \"c1\"\n" +
            " FROM\n" +
            "adp.NPD_MIRROR_WELLBORE_MUD QVIEW1\n" +
            "WHERE\n" +
            "QVIEW1.\"WLBNAME\" IS NOT NULL\n" +
            "UNION ALL\n" +
            "SELECT\n" +
            "   1 AS \"c1QuestType\", NULL AS \"c1Lang\",\n" +
            "('http://www.optique-project.eu/resource/' ||\n" +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"wellboreid\"\n"
            +
            "AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$',\n" +
            "'%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'),\n" +
            "']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=',\n" +
            "'%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"c1\"\n" +
            " FROM\n" +
            "(SELECT substr(IDENTIFIER,4) as WELLBOREID, IDENTIFIER, WELL_IDENTIFIER,\n" +
            "REF_WELLBORE as KIND FROM adp.EPDS_WELLBORE) QVIEW1\n" +
            "WHERE\n" +
            "QVIEW1.\"wellboreid\" IS NOT NULL\n" +
            "UNION ALL\n" +
            "SELECT\n" +
            "   1 AS \"c1QuestType\", NULL AS \"c1Lang\",\n" +
            "('http://www.optique-project.eu/resource/' ||\n" +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"wellboreid\"\n"
            +
            "AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$',\n" +
            "'%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'),\n" +
            "']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=',\n" +
            "'%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"c1\"\n" +
            " FROM\n" +
            "(SELECT substr(IDENTIFIER,4) as WELLBOREID, IDENTIFIER, WELL_IDENTIFIER,\n" +
            "REF_WELLBORE as KIND FROM adp.EPDS_WELLBORE) QVIEW1\n" +
            "WHERE\n" +
            "QVIEW1.\"wellboreid\" IS NOT NULL\n" +
            ") SUB_QVIEW\n" +
            "LIMIT 100\n" +
            "OFFSET 0";

        String sleggeSimple = "SELECT QVIEW1.\"wellboreid\"\n" +
            " FROM\n" +
            "(SELECT substr(IDENTIFIER,4) as WELLBOREID,\n" +
            "REF_WELLBORE as KIND FROM adp.EPDS_WELLBORE) QVIEW1\n" +
            "WHERE\n" +
            "QVIEW1.\"wellboreid\" IS NOT NULL";


        String result = "SELECT\n" +
            "   QVIEW1.a as v\n" +
            " FROM\n" +
            "(SELECT b as a FROM adp.NPD_MIRROR_WELLBORE ) QVIEW1\n" +
            "WHERE\n" +
            "QVIEW1.\"a\" = '162F2-3'";

        String well = "SELECT\n" +
            "   1 AS \"oQuestType\", NULL AS \"oLang\",\n" +
            "'http://www.optique-project.eu/mini-federated-ontology#Wellbore' AS \"o\"\n" +
            " FROM\n" +
            "(SELECT substr(IDENTIFIER,4) as WELLBOREID, IDENTIFIER, WELL_IDENTIFIER,\n" +
            "REF_WELLBORE as KIND FROM adp.npd_WELLBORE) QVIEW1\n" +
            "WHERE\n" +
            "(QVIEW1.\"wellboreid\" = '162F2-10')\n";

        String testcast = "SELECT\n" +
            "   1 AS \"c1QuestType\", NULL AS \"c1Lang\",\n" +
            "('http://www.optique-project.eu/resource/' ||\n" +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"wellboreid\"\n"
            +
            "AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$',\n" +
            "'%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'),\n" +
            "']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=',\n" +
            "'%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"c1\"\n" +
            " FROM\n" +
            "(SELECT substr(IDENTIFIER,4) as WELLBOREID, IDENTIFIER, WELL_IDENTIFIER,\n" +
            "REF_WELLBORE as KIND FROM adp.EPDS_WELLBORE) QVIEW1\n" +
            "WHERE\n" +
            "QVIEW1.\"wellboreid\" IS NOT NULL";

        System.out.println(testcast);
        SQLQuery query = SQLQueryParser.parse(testcast);
        QueryDecomposer d = new QueryDecomposer(query, "/tmp/", 2, null);
        ArrayList<SQLQuery> subqueries = d.getSubqueries();
        for (SQLQuery q : subqueries) {
            System.out.println(q.toDistSQL());
        }


    }

}
