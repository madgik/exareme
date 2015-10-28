/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.demo;

import madgik.exareme.master.queryProcessor.decomposer.federation.QueryDecomposer;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQueryParser;
import madgik.exareme.master.queryProcessor.estimator.NodeSelectivityEstimator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author dimitris
 */
public class DemoDAG {
    private static final org.apache.log4j.Logger log =
        org.apache.log4j.Logger.getLogger(QueryDecomposer.class);

    public static void main(String[] args) throws Exception {

        //	String asa="asdfasfdsp:sadfasd as fasfasdf";
        //	String[] split=asa.split("p:");

        //	String rest=split[1].substring(split[1].indexOf(" "));
        //this.madisFunctionString=split[0]+"p:****"+rest;



        String testDAG = "Select n.name as name from names n, invoice i, producer p, test t  "
            + "where  i.id=n.id and p.name=i.name and t.id=n.id ";

        String testMultiSimple =
            "Select p.name as name from producer p, producer p2 " + "where p.id=p2.id " + "union "
                + "Select p.name as name " + "from producer p, producer p2 where "
                + "p.name=p2.name";
        String testMulti = "Select n.name as name from producer p, names n, invoice i "
            + "where p.id=i.id and i.id=n.id and (p.id=2 or p.id=1)" + "union "
            + "Select n.name as name " + "from producer2 p, names n, invoice i "
            + "where p.id=i.id and i.id=n.id and (i.id=5 or i.id=8)";

        String testOr = "Select n1.name as name from names n1 "
            + "where n1.id=\'a\' or n1.id=\'b\' or n1.id=\'c\' ";

        String testSelf = "Select n1.name as name from names n1, names n2 "
            + "where n1.id=n2.id and n1.name=n2.name ";

        String testCommutativity =
            "Select n.name as name, n.id as id from names n, invoice i, producer p, test t, test2 t2, test3 t3, test4 t4, test5 t5, tes6 t6, test7 t7, test8 t8 "
                + "where  i.id=n.id and n.name=p.name and i.id=t.id and t2.b=t.b and t3.b=t.b and t4.op=t2.op and t5.po=t4.po and t6.po=t5.po and t7.po=t6.po and t8.l=t4.l";

        String demoQuery = "SELECT \n"
            + "   3 AS \"wellboreQuestType\", NULL AS \"wellboreLang\", QVIEW1.\"WELLBORE_ID\" AS \"wellbore\", \n"
            + "   3 AS \"stratigraphic_zoneQuestType\", NULL AS \"stratigraphic_zoneLang\", QVIEW2.\"STRAT_ZONE_IDENTIFIER\" AS \"stratigraphic_zone\", \n"
            + "   5 AS \"formation_pressureQuestType\", NULL AS \"formation_pressureLang\", CAST(QVIEW3.\"DATA_VALUE\" AS VARCHAR(4000)) AS \"formation_pressure\"\n"
            + " FROM \n" + "SLEGGE_EPI.WELLBORE QVIEW1,\n" + "SLEGGE.STRATIGRAPHIC_ZONE QVIEW2\n"
            + "WHERE \n"
            + "(('actual' = QVIEW1.\"R_EXISTENCE_KD_NM\") OR (('nonactual' = QVIEW1.\"R_EXISTENCE_KD_NM\"))) AND\n"
            + "(QVIEW2.\"STRAT_ZONE_ENTRY_MD\" = QVIEW1.\"STRAT_ZONE_EXIT_MD\")";

        //String file = readFile("/home/dimitris/wellbore_tcl_depth_name.q.sql");
        String file = readFile("/home/dimitris/wellbore_tcl_depth_name.q-1.sql");
        // System.out.println(testCommutativity);
        // SQLQuery query = SQLQueryParser.parse(testCommutativity);
        // QueryDecomposer d = new QueryDecomposer(query, false, "distsqlDB/",
        // 2, false, true, true);
        // d.getSubqueries2();
        // Node root = d.getPlan();

        //
        //
        // System.out.println("=====>PLAN ITERATOR<=====");
        // PlanIterator ni = new PlanIterator(root);
        // while(ni.hasNext()){
        // Node n = ni.next();
        // System.out.println(n.computeHashID());
        // // inspectNode(n);
        // }
        // System.out.println("=====>PLAN ITERATOR END<=====");

        String testAlias =
            "SELECT \n" + "   QVIEW3.`wlbWellboreName`, QVIEW4.`length` \n" + " FROM \n"
                + "wellbore_core QVIEW1,\n" + "wellbore_exploration_all QVIEW3,\n"
                + "wellbore_core QVIEW4\n" + "WHERE \n"
                + "(QVIEW1.`wlbNpdidWellbore` = QVIEW3.`wlbNpdidWellbore`) AND\n"
                + "(QVIEW1.`wlbCoreNumber` = QVIEW4.`wlbCoreNumber`) "
                + "AND QVIEW1.`wlbCoreNumber` < 10 " + "AND QVIEW1.`wlbCoreNumber` > 3 " + "UNION "
                + "SELECT \n" + "   QVIEW3.`wlbWellboreName`, QVIEW4.`length` \n" + " FROM \n"
                + "wellbore_core QVIEW1,\n" + "wellbore_core QVIEW3,\n" + "wellbore_core QVIEW4\n"
                + "WHERE \n" + "(QVIEW1.`wlbNpdidWellbore` = QVIEW3.`wlbNpdidWellbore`) AND\n"
                + "(QVIEW1.`wlbCoreNumber` = QVIEW4.`wlbCoreNumber`)"; // System.out.println(file);

        String testNull =
            "SELECT QVIEW4.`dr` \n" + " FROM \n" + "a QVIEW1,\n" + "b QVIEW2,\n" + "c QVIEW3,\n"
                + "d QVIEW4,\n" + "f QVIEW6,\n" + "e QVIEW5\n" + "WHERE \n"
                + "(QVIEW1.`id` = QVIEW2.`id`) AND\n" + "(QVIEW6.`id` = QVIEW2.`id`) AND\n"
                + "(QVIEW3.`op` = QVIEW4.`dr`) AND\n" + "(QVIEW1.`id` = QVIEW4.`id`) AND\n"
                + "(QVIEW1.`id` = QVIEW5.`id`) \n";

        String testq11 = "SELECT \n" + "  QVIEW5.`wlbTotalCoreLength`  AS `year`\n" + " FROM \n"
            + "wellbore_core QVIEW1,\n" + "wellbore_npdid_overview QVIEW2,\n"
            + "wellbore_development_all QVIEW3,\n" + "company QVIEW4,\n"
            + "wellbore_core2 QVIEW5,\n" + "wellbore_shallow_all QVIEW6,\n"
            + "wellbore_core3 QVIEW7\n" + "WHERE \n"
            + "((QVIEW5.`wlbTotalCoreLength` > 50) AND (QVIEW6.`wlbCompletionYear` >= 2008)) AND\n"
            + "(QVIEW1.`wlbNpdidWellbore` = QVIEW2.`wlbNpdidWellbore`) AND\n"
            + "(QVIEW1.`wlbNpdidWellbore` = QVIEW3.`wlbNpdidWellbore`) AND\n"
            + "(QVIEW3.`wlbDrillingOperator` = QVIEW4.`cmpLongName`) AND\n"
            + "(QVIEW1.`wlbCoreNumber` = QVIEW5.`wlbCoreNumber`) AND\n"
            + "(QVIEW1.`wlbNpdidWellbore` = QVIEW5.`wlbNpdidWellbore`) AND\n"
            + "(QVIEW1.`wlbNpdidWellbore` = QVIEW6.`wlbNpdidWellbore`) AND\n"
            + "(QVIEW1.`wlbCoreNumber` = QVIEW7.`wlbCoreNumber`) AND\n"
            + "(QVIEW1.`wlbNpdidWellbore` = QVIEW7.`wlbNpdidWellbore`)";

        String q2 = "SELECT *\n" + "FROM (\n" + "SELECT \n"
            + "   1 AS \"licenceURIQuestType\", NULL AS \"licenceURILang\", CONCAT('http://sws.ifi.uio.no/data/npd-v2/licence/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.`prlNpdidLicence` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS `licenceURI`, \n"
            + "   3 AS \"companyQuestType\", NULL AS \"companyLang\", CAST(QVIEW2.`cmpLongName` AS CHAR(8000) CHARACTER SET utf8) AS `company`, \n"
            + "   8 AS \"dateQuestType\", NULL AS \"dateLang\", CAST(QVIEW1.`prlOperDateValidFrom` AS CHAR(8000) CHARACTER SET utf8) AS `date`\n"
            + " FROM \n" + "npd_licence_oper_hst QVIEW1,\n" + "npd_company QVIEW2,\n"
            + "npd_licence QVIEW3\n" + "WHERE \n"
            + "('9999-12-31T00:00:00' <> QVIEW1.`prlOperDateValidFrom`) AND\n"
            + "QVIEW1.`prlNpdidLicence` IS NOT NULL AND\n"
            + "QVIEW1.`cmpNpdidCompany` IS NOT NULL AND\n"
            + "QVIEW1.`prlOperDateValidFrom` IS NOT NULL AND\n"
            + "QVIEW1.`prlOperDateValidTo` IS NOT NULL AND\n"
            + "(QVIEW1.`cmpNpdidCompany` = QVIEW2.`cmpNpdidCompany`) AND\n"
            + "QVIEW2.`cmpLongName` IS NOT NULL AND\n"
            + "(QVIEW1.`prlNpdidLicence` = QVIEW3.`prlNpdidLicence`) \n" + ") SUB_QVIEW\n"
            + "ORDER BY SUB_QVIEW.`licenceURI`";

        String example =
            "Select A.id from A, B, C, D, E where E.id=B.id and  B.id=C.id and A.id=B.id and C.name=D.name";
        String example2 = "Select A.id from A, B, C where  B.id=C.id and A.n=B.n ";
        String fed =
            "select c.name from adp.npd_wellbore_core c, adp.npd_wellbore w where c.id = w.id ";

        String testProject =
            "select a.q as q from A a, B b where a.id=b.id union select a.q as q from A a, B b, C c where"
                + " a.id=b.id and a.q=c.q ";
        String testFilterjoin = "select a.q as q from A a, B b where a.id=b.id and a.name=b.name ";

        String s001 =
            "SELECT                                                                                    \n"
                + "   7 AS \"wellboreQuestType\", NULL AS \"wellboreLang\", QVIEW1.\"WELLBORE\" AS \"wellbore\" "
                + " FROM                                                                                                                                                       \n"
                + "adp.EPDS1_STRATIGRAPHIC_ZONE QVIEW1,                                                                                                                        \n"
                + "adp.EPDS_WELLBORE QVIEW2,                                                                                                                                   \n"
                + "adp.EPDS_WELL QVIEW3,                                                                                                                                       \n"
                + "adp.EPDS_ROCK_FEATURE QVIEW4,                                                                                                                               \n"
                + "adp.EPDS_MATERIAL_CMPS QVIEW5,                                                                                                                              \n"
                + "adp.EPDS_DATA_COLLECTION QVIEW6,                                                                                                                            \n"
                + "adp.EPDS_COLL_CNTN_X QVIEW7,\n" + "adp.EPDS_ROCK_FEATURE QVIEW8,\n"
                + "adp.EPDS_MATERIAL_CMPS QVIEW9,\n" + "adp.EPDS_DATA_COLLECTION QVIEW10,\n"
                + "adp.EPDS_COLL_CNTN_X QVIEW11,\n" + "adp.EPDS_DATA_COLLECTION QVIEW12\n"
                + "WHERE\n" + "(QVIEW1.\"WELLBORE\" = QVIEW2.\"WELLBORE_ID\") AND\n"
                + "(QVIEW2.\"R_EXISTENCE_KD_NM\" = 'actual') AND\n"
                + "QVIEW2.\"WELLBORE_S\" IS NOT NULL AND\n"
                + "QVIEW1.\"WELLBORE\" IS NOT NULL AND\n"
                + "QVIEW1.\"STRAT_COLUMN_IDENTIFIER\" IS NOT NULL AND\n"
                + "QVIEW1.\"STRAT_INTERP_VERSION\" IS NOT NULL AND\n"
                + "QVIEW1.\"STRAT_ZONE_IDENTIFIER\" IS NOT NULL AND\n"
                + "(QVIEW2.\"WELL_S\" = QVIEW3.\"WELL_S\") AND\n"
                + "QVIEW1.\"STRAT_UNIT_IDENTIFIER\" IS NOT NULL AND\n"
                + "(QVIEW1.\"STRAT_UNIT_IDENTIFIER\" = QVIEW4.\"DESCRIPTION\") AND\n"
                + "(QVIEW5.\"ENTITY_TYPE_NM\" = 'COMPONENT_MATERIAL') AND\n"
                + "(QVIEW4.\"ROCK_FEATURE_S\" = QVIEW5.\"INCORPORATE_S\") AND\n"
                + "(QVIEW1.\"STRAT_COLUMN_IDENTIFIER\" = QVIEW6.\"DATA_COLLECTION_NM\") AND\n"
                + "(QVIEW6.\"R_DATA_COLL_TY_NM\" = 'stratigraphic hierarchy') AND\n"
                + "(QVIEW4.\"ROCK_FEATURE_S\" = QVIEW7.\"E_AND_P_DATA_S\") AND\n"
                + "(QVIEW6.\"DATA_COLLECTION_S\" = QVIEW7.\"DATA_COLLECTION_S\") AND\n"
                + "(QVIEW1.\"STRAT_UNIT_IDENTIFIER\" = QVIEW8.\"DESCRIPTION\") AND\n"
                + "(QVIEW9.\"ENTITY_TYPE_NM\" = 'COMPONENT_MATERIAL') AND\n"
                + "(QVIEW8.\"ROCK_FEATURE_S\" = QVIEW9.\"INCORPORATE_S\") AND\n"
                + "(QVIEW1.\"STRAT_COLUMN_IDENTIFIER\" = QVIEW10.\"DATA_COLLECTION_NM\") AND\n"
                + "(QVIEW10.\"R_DATA_COLL_TY_NM\" = 'stratigraphic hierarchy') AND\n"
                + "(QVIEW8.\"ROCK_FEATURE_S\" = QVIEW11.\"E_AND_P_DATA_S\") AND\n"
                + "(QVIEW10.\"DATA_COLLECTION_S\" = QVIEW11.\"DATA_COLLECTION_S\") AND\n"
                + "(QVIEW1.\"STRAT_COLUMN_IDENTIFIER\" = QVIEW12.\"DATA_COLLECTION_NM\") AND\n"
                + "(QVIEW12.\"R_DATA_COLL_TY_NM\" = 'stratigraphic hierarchy') AND\n"
                + "QVIEW2.\"WELL_S\" IS NOT NULL";

        String s001original =
            "SELECT                                                                                    \n"
                + "   7 AS \"wellboreQuestType\", NULL AS \"wellboreLang\", QVIEW1.\"WELLBORE\" AS \"wellbore\",                                                                       \n"
                + "   7 AS \"unitQuestType\", NULL AS \"unitLang\", QVIEW1.\"STRAT_UNIT_IDENTIFIER\" AS \"unit\",                                                                      \n"
                + "   7 AS \"columnQuestType\", NULL AS \"columnLang\", QVIEW1.\"STRAT_COLUMN_IDENTIFIER\" AS \"column\",                                                              \n"
                + "   3 AS \"latQuestType\", NULL AS \"latLang\", CAST(QVIEW13.\"DATA_VALUE_1_O\" AS CHAR) AS \"lat\",                                                                 \n"
                + "   3 AS \"longQuestType\", NULL AS \"longLang\", CAST(QVIEW15.\"DATA_VALUE_2_O\" AS CHAR) AS \"long\"                                                               \n"
                + " FROM                                                                                                                                                       \n"
                + "adp.EPDS1_STRATIGRAPHIC_ZONE QVIEW1,                                                                                                                        \n"
                + "adp.EPDS_WELLBORE QVIEW2,                                                                                                                                   \n"
                + "adp.EPDS_WELL QVIEW3,                                                                                                                                       \n"
                + "adp.EPDS_ROCK_FEATURE QVIEW4,                                                                                                                               \n"
                + "adp.EPDS_MATERIAL_CMPS QVIEW5,                                                                                                                              \n"
                + "adp.EPDS_DATA_COLLECTION QVIEW6,                                                                                                                            \n"
                + "adp.EPDS_COLL_CNTN_X QVIEW7,\n" + "adp.EPDS_ROCK_FEATURE QVIEW8,\n"
                + "adp.EPDS_MATERIAL_CMPS QVIEW9,\n" + "adp.EPDS_DATA_COLLECTION QVIEW10,\n"
                + "adp.EPDS_COLL_CNTN_X QVIEW11,\n" + "adp.EPDS_DATA_COLLECTION QVIEW12,\n"
                + "adp.EPDS_P_LOCATION_2D QVIEW13,\n" + "adp.EPDS_WELL_SURFACE_PT QVIEW14,\n"
                + "adp.EPDS_P_LOCATION_2D QVIEW15,\n" + "adp.EPDS_WELL_SURFACE_PT QVIEW16\n"
                + "WHERE\n" + "(QVIEW1.\"WELLBORE\" = QVIEW2.\"WELLBORE_ID\") AND\n"
                + "(QVIEW2.\"R_EXISTENCE_KD_NM\" = 'actual') AND\n"
                + "QVIEW2.\"WELLBORE_S\" IS NOT NULL AND\n"
                + "QVIEW1.\"WELLBORE\" IS NOT NULL AND\n"
                + "QVIEW1.\"STRAT_COLUMN_IDENTIFIER\" IS NOT NULL AND\n"
                + "QVIEW1.\"STRAT_INTERP_VERSION\" IS NOT NULL AND\n"
                + "QVIEW1.\"STRAT_ZONE_IDENTIFIER\" IS NOT NULL AND\n"
                + "(QVIEW2.\"WELL_S\" = QVIEW3.\"WELL_S\") AND\n"
                + "QVIEW1.\"STRAT_UNIT_IDENTIFIER\" IS NOT NULL AND\n"
                + "(QVIEW1.\"STRAT_UNIT_IDENTIFIER\" = QVIEW4.\"DESCRIPTION\") AND\n"
                + "(QVIEW5.\"ENTITY_TYPE_NM\" = 'COMPONENT_MATERIAL') AND\n"
                + "(QVIEW4.\"ROCK_FEATURE_S\" = QVIEW5.\"INCORPORATE_S\") AND\n"
                + "(QVIEW1.\"STRAT_COLUMN_IDENTIFIER\" = QVIEW6.\"DATA_COLLECTION_NM\") AND\n"
                + "(QVIEW6.\"R_DATA_COLL_TY_NM\" = 'stratigraphic hierarchy') AND\n"
                + "(QVIEW4.\"ROCK_FEATURE_S\" = QVIEW7.\"E_AND_P_DATA_S\") AND\n"
                + "(QVIEW6.\"DATA_COLLECTION_S\" = QVIEW7.\"DATA_COLLECTION_S\") AND\n"
                + "(QVIEW1.\"STRAT_UNIT_IDENTIFIER\" = QVIEW8.\"DESCRIPTION\") AND\n"
                + "(QVIEW9.\"ENTITY_TYPE_NM\" = 'COMPONENT_MATERIAL') AND\n"
                + "(QVIEW8.\"ROCK_FEATURE_S\" = QVIEW9.\"INCORPORATE_S\") AND\n"
                + "(QVIEW1.\"STRAT_COLUMN_IDENTIFIER\" = QVIEW10.\"DATA_COLLECTION_NM\") AND\n"
                + "(QVIEW10.\"R_DATA_COLL_TY_NM\" = 'stratigraphic hierarchy') AND\n"
                + "(QVIEW8.\"ROCK_FEATURE_S\" = QVIEW11.\"E_AND_P_DATA_S\") AND\n"
                + "(QVIEW10.\"DATA_COLLECTION_S\" = QVIEW11.\"DATA_COLLECTION_S\") AND\n"
                + "(QVIEW1.\"STRAT_COLUMN_IDENTIFIER\" = QVIEW12.\"DATA_COLLECTION_NM\") AND\n"
                + "(QVIEW12.\"R_DATA_COLL_TY_NM\" = 'stratigraphic hierarchy') AND\n"
                + "QVIEW2.\"WELL_S\" IS NOT NULL AND\n"
                + "(QVIEW13.\"DATA_VALUE_1_OU\" = 'dega') AND\n"
                + "(QVIEW13.\"DATA_VALUE_2_OU\" = 'dega') AND\n"
                + "(QVIEW13.\"WELL_SURFACE_PT_S\" = QVIEW14.\"WELL_SURFACE_PT_S\") AND\n"
                + "(QVIEW2.\"WELL_S\" = QVIEW14.\"WELL_S\") AND\n"
                + "QVIEW13.\"DATA_VALUE_1_O\" IS NOT NULL AND\n"
                + "(QVIEW15.\"DATA_VALUE_1_OU\" = 'dega') AND\n"
                + "(QVIEW15.\"DATA_VALUE_2_OU\" = 'dega') AND\n"
                + "(QVIEW15.\"WELL_SURFACE_PT_S\" = QVIEW16.\"WELL_SURFACE_PT_S\") AND\n"
                + "(QVIEW2.\"WELL_S\" = QVIEW16.\"WELL_S\") AND\n"
                + "QVIEW15.\"DATA_VALUE_2_O\" IS NOT NULL AND\n"
                + "((((QVIEW15.\"DATA_VALUE_2_O\" < 3) AND (QVIEW15.\"DATA_VALUE_2_O\" > 2)) AND (QVIEW13.\"DATA_VALUE_1_O\" < 62)) AND (QVIEW13.\"DATA_VALUE_1_O\" > 60))";

        String stream =
            "SELECT DISTINCT wid as wid_join, _sens\n" + "FROM \n" + "(    SELECT wid, _sens FROM\n"
                + "    (\n" + "        SELECT wid, sens AS _sens,x AS _x\n" + "        FROM (\n"
                + "        SELECT DISTINCT wid, abox, \n"
                + "           6 AS \"xQuestType\", NULL AS \"xLang\", CAST(QVIEW1.\"value\" AS CHAR) AS \"x\", \n"
                + "           1 AS \"sensQuestType\", NULL AS \"sensLang\", ('http://www.siemens.com/Optique/OptiquePattern#' || QVIEW1.\"sensor\") AS \"sens\"\n"
                + "         FROM \n" + "        Measurements QVIEW1\n" + "        WHERE \n"
                + "        QVIEW1.\"value\" IS NOT NULL AND\n"
                + "        QVIEW1.\"sensor\" IS NOT NULL\n" + "        ) SUB_QVIEW\n" + "    \n"
                + "    ) SUB_QVIEW\n" + "     NATURAL JOIN \n" + "    (\n"
                + "        SELECT wid, y AS _y,sens AS _sens\n" + "        FROM (\n"
                + "        SELECT DISTINCT wid, abox, \n"
                + "           6 AS \"yQuestType\", NULL AS \"yLang\", CAST(QVIEW1.\"value\" AS CHAR) AS \"y\", \n"
                + "           1 AS \"sensQuestType\", NULL AS \"sensLang\", ('http://www.siemens.com/Optique/OptiquePattern#' || QVIEW1.\"sensor\") AS \"sens\"\n"
                + "         FROM \n" + "        Measurements QVIEW1\n" + "        WHERE \n"
                + "        QVIEW1.\"value\" IS NOT NULL AND\n"
                + "        QVIEW1.\"sensor\" IS NOT NULL\n" + "        ) SUB_QVIEW\n" + "    \n"
                + "    ) SUB_QVIEW\n" + "EXCEPT\n" + "    SELECT wid, _sens FROM(\n"
                + "        Select * FROM (\n" + "            Select * FROM (\n"
                + "                (    \n"
                + "                    SELECT wid, abox AS i, sens AS _sens,x AS _x\n"
                + "                    FROM (\n"
                + "                    SELECT DISTINCT wid, abox, \n"
                + "                       6 AS \"xQuestType\", NULL AS \"xLang\", CAST(QVIEW1.\"value\" AS CHAR) AS \"x\", \n"
                + "                       1 AS \"sensQuestType\", NULL AS \"sensLang\", ('http://www.siemens.com/Optique/OptiquePattern#' || QVIEW1.\"sensor\") AS \"sens\"\n"
                + "                     FROM \n" + "                    Measurements QVIEW1\n"
                + "                    WHERE \n"
                + "                    QVIEW1.\"value\" IS NOT NULL AND\n"
                + "                    QVIEW1.\"sensor\" IS NOT NULL\n"
                + "                    ) SUB_QVIEW\n" + "                ) AS A \n"
                + "                NATURAL JOIN \n" + "                 (    \n"
                + "                    SELECT wid, abox AS j, y AS _y,sens AS _sens\n"
                + "                    FROM (\n"
                + "                    SELECT DISTINCT wid, abox, \n"
                + "                       6 AS \"yQuestType\", NULL AS \"yLang\", CAST(QVIEW1.\"value\" AS CHAR) AS \"y\", \n"
                + "                       1 AS \"sensQuestType\", NULL AS \"sensLang\", ('http://www.siemens.com/Optique/OptiquePattern#' || QVIEW1.\"sensor\") AS \"sens\"\n"
                + "                     FROM \n" + "                    Measurements QVIEW1\n"
                + "                    WHERE \n"
                + "                    QVIEW1.\"value\" IS NOT NULL AND\n"
                + "                    QVIEW1.\"sensor\" IS NOT NULL\n"
                + "                    ) SUB_QVIEW\n" + "                ) AS B\n"
                + "            ) SUB_QVIEW WHERE _x > _y\n" + "        ) SUB_QVIEW WHERE i < j\n"
                + "    ) SUB_QVIEW\n" + ") SUB_QVIEW ";

        String fops = "SELECT *\n" + "FROM (\n" + "SELECT \n"
            + "   1 AS \"xQuestType\", NULL AS \"xLang\", ('http://www.example.org/A2/' || CAST(QVIEW1.\"unique22\" AS CHAR)) AS \"x\"\n"
            + " FROM \n" + "te12sixty QVIEW1,\n" + "db1_tab1 QVIEW2\n" + "WHERE \n"
            + "QVIEW1.\"unique22\" IS NOT NULL AND\n" + "QVIEW1.\"unique21\" IS NOT NULL AND\n"
            + "(QVIEW1.\"unique21\" = QVIEW2.\"unique2Tab1\")\n" + "UNION ALL\n" + "SELECT \n"
            + "   1 AS \"xQuestType\", NULL AS \"xLang\", ('http://www.example.org/A1/' || CAST(QVIEW1.\"unique2Tab1\" AS CHAR)) AS \"x\"\n"
            + " FROM \n" + "db1_tab1 QVIEW1\n" + "WHERE \n" + "QVIEW1.\"unique2Tab1\" IS NOT NULL\n"
            + ") SUB_QVIEW\n" + "LIMIT 1000\n" + "OFFSET 0";
        String s01 = "select * from ddd where 0 = 1";

        String a = "SELECT\n" + "   1 AS \"wQuestType\", NULL AS \"wLang\",\n"
            + "('http://www.optique-project.eu/ontology/subsurface-exploration/Wellbore-'\n"
            + "|| QVIEW1.\"WELLBORE_S\") AS \"w\",\n"
            + "   1 AS \"intQuestType\", NULL AS \"intLang\",\n"
            + "('http://www.optique-project.eu/ontology/subsurface-exploration/WellboreInterval-'\n"
            + "|| QVIEW1.\"WELLBORE_INTV_S\") AS \"int\",\n"
            + "   7 AS \"wellboreQuestType\", NULL AS \"wellboreLang\", QVIEW2.\"WELLBORE_ID\"\n"
            + "AS \"wellbore\",\n" + "   1 AS \"coreQuestType\", NULL AS \"coreLang\",\n"
            + "('http://www.optique-project.eu/ontology/subsurface-exploration/Core-'\n"
            + "|| QVIEW4.\"WELLBORE_INTV_S\") AS \"core\",\n"
            + "   1 AS \"iQuestType\", NULL AS \"iLang\",\n"
            + "('http://www.optique-project.eu/ontology/subsurface-exploration/WellboreInterval-'\n"
            + "|| QVIEW4.\"WELLBORE_INTV_S\") AS \"i\"\n" + " FROM\n"
            + "db1_WELLBORE_INTV QVIEW1,\n" + "db1_WELLBORE QVIEW2,\n" + "db1_WELL QVIEW3,\n"
            + "db1_WELLBORE_INTV QVIEW4\n" + "WHERE\n"
            + "QVIEW1.\"WELLBORE_INTV_S\" IS NOT NULL AND\n"
            + "QVIEW1.\"WELLBORE_S\" IS NOT NULL AND\n"
            + "(QVIEW1.\"WELLBORE_S\" = QVIEW2.\"WELLBORE_S\") AND\n"
            + "(QVIEW2.\"R_EXISTENCE_KD_NM\" = 'actual') AND\n"
            + "(QVIEW2.\"WELL_S\" = QVIEW3.\"WELL_S\") AND\n"
            + "QVIEW2.\"WELLBORE_ID\" IS NOT NULL AND\n"
            + "(QVIEW4.\"R_WELLBORE_INTV_NM\" = 'cored interval') AND\n"
            + "QVIEW4.\"WELLBORE_INTV_S\" IS NOT NULL\n" + "UNION ALL\n" + "SELECT\n"
            + "   1 AS \"wQuestType\", NULL AS \"wLang\",\n"
            + "('http://www.optique-project.eu/ontology/subsurface-exploration/Wellbore-'\n"
            + "|| QVIEW2.\"WELLBORE_S\") AS \"w\",\n"
            + "   1 AS \"intQuestType\", NULL AS \"intLang\",\n"
            + "('http://www.optique-project.eu/ontology/subsurface-exploration/StratigraphicZone-'\n"
            + "|| QVIEW1.\"WELLBORE\" || '-' || QVIEW1.\"STRAT_COLUMN_IDENTIFIER\" || '-'\n"
            + "|| QVIEW1.\"STRAT_INTERP_VERSION\" || '-' ||\n"
            + "QVIEW1.\"STRAT_ZONE_IDENTIFIER\") AS \"int\",\n"
            + "   7 AS \"wellboreQuestType\", NULL AS \"wellboreLang\", QVIEW1.\"WELLBORE\" AS\n"
            + "\"wellbore\",\n" + "   1 AS \"coreQuestType\", NULL AS \"coreLang\",\n"
            + "('http://www.optique-project.eu/ontology/subsurface-exploration/Core-'\n"
            + "|| QVIEW4.\"WELLBORE_INTV_S\") AS \"core\",\n"
            + "   1 AS \"iQuestType\", NULL AS \"iLang\",\n"
            + "('http://www.optique-project.eu/ontology/subsurface-exploration/WellboreInterval-'\n"
            + "|| QVIEW4.\"WELLBORE_INTV_S\") AS \"i\"\n" + " FROM\n"
            + "db2_STRATIGRAPHIC_ZONE QVIEW1,\n" + "db1_WELLBORE QVIEW2,\n" + "db1_WELL QVIEW3,\n"
            + "db1_WELLBORE_INTV QVIEW4\n" + "WHERE\n"
            + "(QVIEW1.\"WELLBORE\" = QVIEW2.\"WELLBORE_ID\") AND\n"
            + "(QVIEW2.\"R_EXISTENCE_KD_NM\" = 'actual') AND\n"
            + "QVIEW1.\"STRAT_INTERP_VERSION\" IS NOT NULL AND\n"
            + "QVIEW1.\"WELLBORE\" IS NOT NULL AND\n"
            + "QVIEW1.\"STRAT_COLUMN_IDENTIFIER\" IS NOT NULL AND\n"
            + "QVIEW1.\"STRAT_ZONE_IDENTIFIER\" IS NOT NULL AND\n"
            + "QVIEW2.\"WELLBORE_S\" IS NOT NULL AND\n"
            + "(QVIEW2.\"WELL_S\" = QVIEW3.\"WELL_S\") AND\n"
            + "(QVIEW4.\"R_WELLBORE_INTV_NM\" = 'cored interval') AND\n"
            + "QVIEW4.\"WELLBORE_INTV_S\" IS NOT NULL";

        String q1 = "SELECT\n" + "   1 AS \"wQuestType\", NULL AS \"wLang\",\n"
            + "('http://www.optique-project.eu/ontology/subsurface-exploration/Wellbore-'\n"
            + "|| QVIEW1.\"WELLBORE_S\") AS \"w\"\n" + " FROM\n" + "EPI_WELLBORE QVIEW1\n"
            + "WHERE\n" + "QVIEW1.\"WELLBORE_S\" IS NOT NULL\n" + "UNION ALL\n" + "SELECT\n"
            + "   1 AS \"wQuestType\", NULL AS \"wLang\",\n"
            + "('http://www.optique-project.eu/ontology/subsurface-exploration/Wellbore-'\n"
            + "|| CAST(QVIEW1.\"identifier\" AS CHAR)) AS \"w\"\n" + " FROM\n"
            + "(SELECT WELLBORE_S IDENTIFIER, to_char(COMPLETION_DATE, 'yyyy-mm-dd') AS\n"
            + "WLB_CMPL_DATE FROM EPI_WELLBORE WHERE r_existence_kd_nm = 'actual') QVIEW1\n"
            + "WHERE\n" + "QVIEW1.\"identifier\" IS NOT NULL\n" + "UNION ALL\n" + "SELECT\n"
            + "   1 AS \"wQuestType\", NULL AS \"wLang\",\n"
            + "('http://www.optique-project.eu/ontology/subsurface-exploration/Wellbore-'\n"
            + "|| QVIEW1.\"WELLBORE_S\") AS \"w\"\n" + " FROM\n" + "EPI_WELLBORE_INTV QVIEW1\n"
            + "WHERE\n" + "QVIEW1.\"WELLBORE_S\" IS NOT NULL";

        SQLQuery query = SQLQueryParser.parse(file);
        // System.out.println(file);
        NodeSelectivityEstimator nse = null;
        try {
            nse = new NodeSelectivityEstimator("./files/schema_primitive.json");
        } catch (Exception e) {
            log.error("Cannot read statistics. " + e.getMessage());
        }
        // System.out.println("lala");
        long aw = System.currentTimeMillis();
        QueryDecomposer d = new QueryDecomposer(query, "/tmp/", 1, nse);

        for (SQLQuery s : d.getSubqueries2()) {
            System.out.println(s.toDistSQL());
        }
        System.out.println((System.currentTimeMillis() - aw));
        // Node root = d.getPlan();

        // String dot = root.dotPrint();
        // System.out.print(dot);
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
        reader.close();
        return stringBuilder.toString();
    }
}
