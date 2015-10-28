/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.demo;

import madgik.exareme.master.queryProcessor.decomposer.federation.QueryDecomposer;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQueryParser;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author heraldkllapi
 */
public class DemoMain {

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);
        String query2 = " select t1.id as id1, t3.id as id3, " + "count(t1.id) as id1_count, "
            + "        sum(t2.id) as w1, " + "        count(t1.id) as w2, "
            + "        max(t1.id) as w3, " + "        f(t2.id, t1.id, t3.id) as w4 "
            + " from a t1, b t2, c t3, d t4 " + " where t3.id>0 "
            //+ "and t1.id >4  "
            + "and t2.id>=0 " + "and t4.id < 100 " + " and t1.id = t2.id " + " and t3.id = t4.id "
            + " and t1.id = t3.id " + " group by t1.id " + " order by t2.id " + " limit 10";

        String limit = "SELECT * FROM ( select WLB.WELLBORE_S WLB_KEY, WLB.WELLBORE_ID WLB_NAME\n" +
            "from adp.SLEGGE_EPI_WELLBORE WLB, adp.SLEGGE_EPI_WELL W where\n" +
            "WLB.R_EXISTENCE_KD_NM = 'actual' and WLB.WELL_S = W.WELL_S) s limit 1";


        String tpc_h_q10 = "select\n" + "  c.c_custkey,\n" + "  c.c_name,\n"
            + "  sum(l.l_extendedprice * (1 - l.l_discount)) as revenue,\n" + "  c.c_acctbal,\n"
            + "  n.n_name,\n" + "  c.c_address,\n" + "  c.c_phone,\n" + "  c.c_comment\n" + "from\n"
            + "  customer c,\n" + "  orders o,\n" + "  lineitem l,\n" + "  nation n\n" + "where\n"
            + "  c.c_custkey = o.o_custkey\n" + "  and l.l_orderkey = o.o_orderkey\n"
            + "  and c.c_nationkey = n.n_nationkey\n" + "  and o.o_orderdate >= '1993-04-01'\n"
            + "  and o.o_orderdate < '1993-07-01'\n" + "  and l.l_returnflag = 'R'\n" + "group by\n"
            + "  c.c_custkey,\n" + "  c.c_name,\n" + "  c.c_acctbal,\n" + "  c.c_phone,\n"
            + "  n.n_name,\n" + "  c.c_address,\n" + "  c.c_comment\n" + "order by\n"
            + "  revenue desc\n" + "limit 20";
        String tpc_h_q9 =
            "select\n" + "  nation,\n" + "  o.o_year\n" + //"  sum(amount) as sum_profit\n" +
                "from\n" + "  (\n" + "    select\n" + "      n.n_name as nation,\n" +
                //"      strftime('%Y', o.o_orderdate) as o.o_year,\n" +
                "      l.l_extendedprice * (1 - l.l_discount) - ps.ps_supplycost * l.l_quantity as amount\n"
                + "    from\n" + "      part p,\n" + "      supplier s,\n" + "      lineitem l,\n"
                + "      partsupp ps,\n" + "      orders o,\n" + "      nation n\n" + "    where\n"
                + "      s.s_suppkey = l.l_suppkey\n" + "      and ps.ps_suppkey = l.l_suppkey\n"
                + "      and ps.ps_partkey = l.l_partkey\n"
                + "      and p.p_partkey = l.l_partkey\n"
                + "      and o.o_orderkey = l.l_orderkey\n"
                + "      and s.s_nationkey = n.n_nationkey\n" +
                //"      and p.p_name like '%sky%'\n" +
                "  ) as profit\n" + "group by\n" + "  n.nation,\n" + "  o.o_year\n" + "order by\n"
                + "  nation,\n" + "  o.o_year desc";

        String testConcat = "Select 'a' || 'b' || 'c' from NPD2_t";

        String testCat = "SELECT *\n" + "FROM (\n" + "SELECT DISTINCT \n"
            + "   1 AS \"c1QuestType\", NULL AS \"c1Lang\", ('http://sws.ifi.uio.no/data/npd-v2/wellbore/' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"wlbNpdidWellbore\" AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"c1\"\n"
            + " FROM \n" + "asdasdasd_wellbore_shallow_all QVIEW1\n" + "WHERE \n"
            + "QVIEW1.\"wlbNpdidWellbore\" IS NOT NULL\n" + ") SUB_QVIEW";

        String testUnderscore = "SELECT *\n" + "FROM (\n" + "SELECT \n"
            + "   1 AS \"c1QuestType\", NULL AS \"c1Lang\", ('http://sws.ifi.uio.no/data/npd-v2/wellbore/' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"wlbNpdidWellbore\" AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"c1\", \n"
            + "   1 AS \"c2QuestType\", NULL AS \"c2Lang\", ('http://sws.ifi.uio.no/data/npd-v2/field/' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"fldNpdidField\" AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"c2\"\n"
            + " FROM \n" + "asdasdasd_wellbore_exploration_all QVIEW1,\n"
            + "asdasdasd_wellbore_shallow_all QVIEW2\n" + "WHERE \n"
            + "QVIEW1.\"fldNpdidField\" IS NOT NULL AND\n"
            + "QVIEW1.\"wlbNpdidWellbore\" IS NOT NULL AND\n"
            + "(QVIEW1.\"wlbNpdidWellbore\" = QVIEW2.\"wlbNpdidWellbore\")\n" + "UNION\n" + "\n"
            + "SELECT \n"
            + "   1 AS \"c1QuestType\", NULL AS \"c1Lang\", ('http://sws.ifi.uio.no/data/npd-v2/wellbore/' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"wlbNpdidWellbore\" AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"c1\", \n"
            + "   1 AS \"c2QuestType\", NULL AS \"c2Lang\", ('http://sws.ifi.uio.no/data/npd-v2/field/' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"fldNpdidField\" AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"c2\"\n"
            + " FROM \n" + "asdasdasd_wellbore_development_all QVIEW1,\n"
            + "asdasdasd_wellbore_shallow_all QVIEW2\n" + "WHERE \n"
            + "QVIEW1.\"fldNpdidField\" IS NOT NULL AND\n"
            + "QVIEW1.\"wlbNpdidWellbore\" IS NOT NULL AND\n"
            + "(QVIEW1.\"wlbNpdidWellbore\" = QVIEW2.\"wlbNpdidWellbore\")\n" + ") SUB_QVIEW";
        String testPar = "select c1QuestType, c1Lang, c1, c2QuestType, c2Lang, c2 from"
            + " (mysql h:whale.di.uoa.gr u:optique p:gray769watt724 db:newnpd "
            + "select 1 as c1QuestType, null as c1Lang, "
            + "CONCAT('http://sws.ifi.uio.no/data/npd-v2/wellbore/', "
            + "replace(replace(replace(replace(replace(replace(replace(replace(replace"
            + "(replace(replace(replace(replace(replace(replace(replace(replace(replace"
            + "(replace(cast(QVIEW1.wlbNpdidWellbore as CHAR(8000)), ' ', '%20'), '!', '%21')"
            + ", '@', '%40'), '#', '%23'), '$', '%24'), '&', '%26'), '*', '%42'), '(', '%28'),"
            + " ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), "
            + "'?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'))"
            + " as c1, 1 as c2QuestType, null as c2Lang, CONCAT('http://sws.ifi.uio.no/data/npd-v2/field/', "
            + "replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace"
            + "(replace(replace(replace(replace(replace(replace(replace"
            + "(replace(cast(QVIEW1.fldNpdidField as CHAR(8000)), ' ', '%20'), '!', '%21'), '@', '%40'), '#',"
            + " '%23'), '$', '%24'), '&', '%26'), '*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'),"
            + " ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F'))"
            + " as c2 from wellbore_exploration_all QVIEW1, wellbore_shallow_all QVIEW2 where "
            + "QVIEW1.wlbNpdidWellbore = QVIEW2.wlbNpdidWellbore and QVIEW1.fldNpdidField IS NOT NULL "
            + "and QVIEW1.wlbNpdidWellbore IS NOT NULL);";

        String tpc_h_q21 = "select\n" +
            "  s.s_acctbal,\n" +
            "  s.s_name,\n" +
            "  n.n_name,\n" +
            "  p.p_partkey,\n" +
            "  p.p_mfgr,\n" +
            "  s.s_address,\n" +
            "  s.s_phone,\n" +
            "  s.s_comment\n" +
            "from\n" +
            "  part p,\n" +
            "  supplier s,\n" +
            "  partsupp ps,\n" +
            "  nation n,\n" +
            "  region r,\n" +
            "  (select\n" +
            "      min(ps2.ps_supplycost) as m,\n" +
            "      ps2.ps_partkey as f \n" +
            "    from\n" +
            "      partsupp ps2,\n" +
            "      supplier s2,\n" +
            "      nation n2,\n" +
            "      region r2\n" +
            "    where s2.s_suppkey = ps2.ps_suppkey\n" +
            "      and s2.s_nationkey = n2.n_nationkey\n" +
            "      and n2.n_regionkey = r2.r_regionkey\n" +
            "      and r2.r_name = 'AFRICA'\n" +
            "    group by f) as sub\n" +
            "where\n" +
            "  p.p_partkey = ps.ps_partkey\n" +
            "  and s.s_suppkey = ps.ps_suppkey\n" +
            "  and p.p_size = 7\n" +
            //"  and p.p_type like '%STEEL'\n" +
            "  and s.s_nationkey = n.n_nationkey\n" +
            "  and n.n_regionkey = r.r_regionkey\n" +
            "  and r.r_name = 'AFRICA'\n" +
            "  and p.p_partkey = sub.f\n" +
            "  and ps.ps_supplycost = sub.m\n" +
            "order by\n" +
            "  s.s_acctbal desc,\n" +
            "  n.n_name,\n" +
            "  s.s_name,\n" +
            "  p.p_partkey\n" +
            "limit 100";

        String qc = "SELECT *\n" +
            "\n" +
            "FROM (\n" +
            "\n" +
            "SELECT DISTINCT\n" +
            "\n" +
            "   1 AS \"licenceURIQuestType\", NULL AS \"licenceURILang\",\n" +
            "('http://sws.ifi.uio.no/data/npd-v2/licence/' ||\n" +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"prlNpdidLicence\"\n"
            +
            "AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$',\n" +
            "'%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'),\n" +
            "']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=',\n" +
            "'%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"licenceURI\",\n" +
            "\n" +
            "   1 AS \"interestQuestType\", NULL AS \"interestLang\",\n" +
            "('http://example.com/base/' ||\n" +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"prlLicenseeInterest\"\n"
            +
            "AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$',\n" +
            "'%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'),\n" +
            "']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=',\n" +
            "'%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"interest\",\n" +
            "\n" +
            "   1 AS \"dateQuestType\", NULL AS \"dateLang\",\n" +
            "('http://example.com/base/' ||\n" +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"prlLicenseeDateValidFrom\"\n"
            +
            "AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$',\n" +
            "'%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'),\n" +
            "']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=',\n" +
            "'%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"date\"\n" +
            "\n" +
            " FROM\n" +
            "\n" +
            "NPD_licence_licensee_hst QVIEW1\n" +
            "\n" +
            "WHERE\n" +
            "\n" +
            "(('9999-12-31T00:00:00' <> QVIEW1.\"prlLicenseeDateValidFrom\") OR\n" +
            "(('9999-12-31T00:00:00' <> QVIEW1.\"prlLicenseeDateValidFrom\") OR\n" +
            "('9999-12-31T00:00:00' <> QVIEW1.\"prlLicenseeDateValidFrom\"))) AND\n" +
            "\n" +
            "QVIEW1.\"prlNpdidLicence\" IS NOT NULL AND\n" +
            "\n" +
            "QVIEW1.\"cmpNpdidCompany\" IS NOT NULL AND\n" +
            "\n" +
            "QVIEW1.\"prlLicenseeDateValidFrom\" IS NOT NULL AND\n" +
            "\n" +
            "QVIEW1.\"prlLicenseeDateValidTo\" IS NOT NULL AND\n" +
            "\n" +
            "QVIEW1.\"prlLicenseeInterest\" IS NOT NULL AND\n" +
            "\n" +
            "(QVIEW1.\"prlNpdidLicence\" IS NOT NULL OR (QVIEW1.\"prlNpdidLicence\" IS\n" +
            "NOT NULL OR QVIEW1.\"prlNpdidLicence\" IS NOT NULL)) AND\n" +
            "\n" +
            "(('http://example.com/base/' ||\n" +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"prlLicenseeDateValidFrom\"\n"
            +
            "AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$',\n" +
            "'%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'),\n" +
            "']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=',\n" +
            "'%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) >\n" +
            "'1979-12-31T00:00:00')\n" +
            "\n" +
            ") SUB_QVIEW";

        String qc2 = " SELECT *\n" +
            "FROM (\n" +
            "SELECT DISTINCT\n" +
            "   1 AS \"licenceURIQuestType\", NULL AS \"licenceURILang\",\n" +
            "('http://sws.ifi.uio.no/data/npd-v2/licence/' ||\n" +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"prlNpdidLicence\"\n"
            +
            "AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$',\n" +
            "'%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'),\n" +
            "']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=',\n" +
            "'%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"licenceURI\",\n" +
            "   1 AS \"interestQuestType\", NULL AS \"interestLang\",\n" +
            "('http://example.com/base/' ||\n" +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"prlLicenseeInterest\"\n"
            +
            "AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$',\n" +
            "'%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'),\n" +
            "']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=',\n" +
            "'%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"interest\",\n" +
            "   1 AS \"dateQuestType\", NULL AS \"dateLang\",\n" +
            "('http://example.com/base/' ||\n" +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"prlLicenseeDateValidFrom\"\n"
            +
            "AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$',\n" +
            "'%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'),\n" +
            "']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=',\n" +
            "'%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"date\"\n" +
            " FROM\n" +
            "NPD_licence_licensee_hst QVIEW1\n" +
            "WHERE\n" +
            "(('9999-12-31T00:00:00' <> QVIEW1.\"prlLicenseeDateValidFrom\") OR\n" +
            "(('9999-12-31T00:00:00' <> QVIEW1.\"prlLicenseeDateValidFrom\") OR\n" +
            "('9999-12-31T00:00:00' <> QVIEW1.\"prlLicenseeDateValidFrom\"))) AND\n" +
            "QVIEW1.\"prlNpdidLicence\" IS NOT NULL AND\n" +
            "QVIEW1.\"cmpNpdidCompany\" IS NOT NULL AND\n" +
            "QVIEW1.\"prlLicenseeDateValidFrom\" IS NOT NULL AND\n" +
            "QVIEW1.\"prlLicenseeDateValidTo\" IS NOT NULL AND\n" +
            "QVIEW1.\"prlLicenseeInterest\" IS NOT NULL AND\n" +
            "(QVIEW1.\"prlNpdidLicence\" IS NOT NULL OR (QVIEW1.\"prlNpdidLicence\" IS\n" +
            "NOT NULL OR QVIEW1.\"prlNpdidLicence\" IS NOT NULL)) AND\n" +
            "(('http://example.com/base/' ||\n" +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"prlLicenseeDateValidFrom\"\n"
            +
            "AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$',\n" +
            "'%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'),\n" +
            "']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=',\n" +
            "'%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) >\n" +
            "'1979-12-31T00:00:00')\n" +
            ") SUB_QVIEW";

        String slegge1 = "SELECT \n" +
            "   3 AS \"wellboreQuestType\", NULL AS \"wellboreLang\", QVIEW1.\"WELLBORE_ID\" AS \"wellbore\", \n"
            +
            "   1 AS \"wiQuestType\", NULL AS \"wiLang\", ('http://www.optique-project.eu/well-ontology#StratigraphicZone-' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(QVIEW1.\"WELLBORE_ID\",' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F') || '-' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(QVIEW2.\"STRAT_ZONE_IDENTIFIER\",' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"wi\", \n"
            +
            "   5 AS \"formation_pressureQuestType\", NULL AS \"formation_pressureLang\", CAST(QVIEW3.\"DATA_VALUE\" AS VARCHAR(4000)) AS \"formation_pressure\"\n"
            +
            " FROM \n" +
            "SLEGGE_EPI.WELLBORE QVIEW1,\n" +
            "SLEGGE.STRATIGRAPHIC_ZONE QVIEW2,\n" +
            "slegge_epi.p_pressure QVIEW3,\n" +
            "slegge_epi.activity QVIEW4,\n" +
            "slegge_epi.wellbore QVIEW5,\n" +
            "slegge_epi.activity_class QVIEW6,\n" +
            "slegge_epi.activity QVIEW7,\n" +
            "slegge_epi.activity_class QVIEW8\n" +
            "WHERE \n" +
            "(('actual' = QVIEW1.\"R_EXISTENCE_KD_NM\") OR (('actual' = QVIEW1.\"R_EXISTENCE_KD_NM\") OR ('actual' = QVIEW1.\"R_EXISTENCE_KD_NM\"))) AND\n"
            +
            "QVIEW1.\"WELLBORE_ID\" IS NOT NULL AND\n" +
            "(QVIEW1.\"WELLBORE_ID\" = QVIEW2.\"WELLBORE\") AND\n" +
            "QVIEW2.\"STRAT_ZONE_IDENTIFIER\" IS NOT NULL AND\n" +
            "(QVIEW1.\"WELLBORE_ID\" = QVIEW5.\"WELLBORE_ID\") AND\n" +
            "((('formation pressure depth data' = QVIEW6.\"CLSN_CLS_NAME\") AND ((QVIEW6.\"ACTIVITY_CLASS_S\" = QVIEW4.\"KIND_S\") AND ((QVIEW5.\"WELLBORE_S\" = QVIEW4.\"FACILITY_S\") AND (QVIEW4.\"ACTIVITY_S\" = QVIEW3.\"ACTIVITY_S\")))) OR ((('formation pressure depth data' = QVIEW6.\"CLSN_CLS_NAME\") AND ((QVIEW6.\"ACTIVITY_CLASS_S\" = QVIEW4.\"KIND_S\") AND ((QVIEW5.\"WELLBORE_S\" = QVIEW4.\"FACILITY_S\") AND (QVIEW4.\"ACTIVITY_S\" = QVIEW3.\"ACTIVITY_S\")))) OR (('formation pressure depth data' = QVIEW6.\"CLSN_CLS_NAME\") AND ((QVIEW6.\"ACTIVITY_CLASS_S\" = QVIEW4.\"KIND_S\") AND ((QVIEW5.\"WELLBORE_S\" = QVIEW4.\"FACILITY_S\") AND (QVIEW4.\"ACTIVITY_S\" = QVIEW3.\"ACTIVITY_S\")))))) AND\n"
            +
            "QVIEW3.\"P_PRESSURE_S\" IS NOT NULL AND\n" +
            "((('bar' = QVIEW3.\"DATA_VALUE_U\") AND (('formation pressure depth data' = QVIEW8.\"CLSN_CLS_NAME\") AND ((QVIEW8.\"ACTIVITY_CLASS_S\" = QVIEW7.\"KIND_S\") AND (QVIEW7.\"ACTIVITY_S\" = QVIEW3.\"ACTIVITY_S\")))) OR ((('bar' = QVIEW3.\"DATA_VALUE_U\") AND (('formation pressure depth data' = QVIEW8.\"CLSN_CLS_NAME\") AND ((QVIEW8.\"ACTIVITY_CLASS_S\" = QVIEW7.\"KIND_S\") AND (QVIEW7.\"ACTIVITY_S\" = QVIEW3.\"ACTIVITY_S\")))) OR (('bar' = QVIEW3.\"DATA_VALUE_U\") AND (('formation pressure depth data' = QVIEW8.\"CLSN_CLS_NAME\") AND ((QVIEW8.\"ACTIVITY_CLASS_S\" = QVIEW7.\"KIND_S\") AND (QVIEW7.\"ACTIVITY_S\" = QVIEW3.\"ACTIVITY_S\")))))) AND\n"
            +
            "QVIEW3.\"DATA_VALUE\" IS NOT NULL\n";

        String slegge2 = "SELECT \n" +
            "   3 AS \"wellboreQuestType\", NULL AS \"wellboreLang\", QVIEW1.\"WELLBORE_ID\" AS \"wellbore\", \n"
            +
            "   3 AS \"stratigraphic_zoneQuestType\", NULL AS \"stratigraphic_zoneLang\", QVIEW2.\"STRAT_ZONE_IDENTIFIER\" AS \"stratigraphic_zone\", \n"
            +
            "   5 AS \"formation_pressureQuestType\", NULL AS \"formation_pressureLang\", CAST(QVIEW3.\"DATA_VALUE\" AS VARCHAR(4000)) AS \"formation_pressure\"\n"
            +
            " FROM \n" +
            "SLEGGE_EPI.WELLBORE QVIEW1,\n" +
            "SLEGGE.STRATIGRAPHIC_ZONE QVIEW2,\n" +
            "slegge_epi.p_pressure QVIEW3,\n" +
            "slegge_epi.activity QVIEW4,\n" +
            "slegge_epi.wellbore QVIEW5,\n" +
            "slegge_epi.activity_class QVIEW6,\n" +
            "slegge_epi.activity QVIEW7,\n" +
            "slegge_epi.activity_class QVIEW8,\n" +
            "(SELECT WELLBORE, STRAT_ZONE_IDENTIFIER ZONE_ID, SLEGGE.FRAME_LEAF.MDTVD(WELLBORE, STRAT_ZONE_ENTRY_MD) as STRAT_ZONE_TOP_TVD, SLEGGE.FRAME_LEAF.MDTVD(WELLBORE, STRAT_ZONE_EXIT_MD) as STRAT_ZONE_BASE_TVD, STRAT_ZONE_DEPTH_UOM UNIT FROM SLEGGE.STRATIGRAPHIC_ZONE) QVIEW9,\n"
            +
            "(SELECT WELLBORE, STRAT_ZONE_IDENTIFIER ZONE_ID, SLEGGE.FRAME_LEAF.MDTVD(WELLBORE, STRAT_ZONE_ENTRY_MD) as STRAT_ZONE_TOP_TVD, SLEGGE.FRAME_LEAF.MDTVD(WELLBORE, STRAT_ZONE_EXIT_MD) as STRAT_ZONE_BASE_TVD, STRAT_ZONE_DEPTH_UOM UNIT FROM SLEGGE.STRATIGRAPHIC_ZONE) QVIEW10,\n"
            +
            "SLEGGE.STRATIGRAPHIC_ZONE QVIEW11,\n" +
            "SLEGGE.STRATIGRAPHIC_ZONE QVIEW12,\n" +
            "slegge_epi.p_pressure QVIEW13,\n" +
            "slegge_epi.activity QVIEW14,\n" +
            "slegge_epi.p_location_1d QVIEW15,\n" +
            "slegge_epi.wellbore QVIEW16,\n" +
            "slegge_epi.activity_class QVIEW17,\n" +
            "slegge_epi.p_pressure QVIEW18,\n" +
            "slegge_epi.activity QVIEW19,\n" +
            "slegge_epi.p_location_1d QVIEW20,\n" +
            "slegge_epi.wellbore QVIEW21,\n" +
            "slegge_epi.activity_class QVIEW22\n" +
            "WHERE \n" +
            "(('actual' = QVIEW1.\"R_EXISTENCE_KD_NM\") OR (('actual' = QVIEW1.\"R_EXISTENCE_KD_NM\") OR ('actual' = QVIEW1.\"R_EXISTENCE_KD_NM\"))) AND\n"
            +
            "QVIEW1.\"WELLBORE_ID\" IS NOT NULL AND\n" +
            "(QVIEW1.\"WELLBORE_ID\" = QVIEW2.\"WELLBORE\") AND\n" +
            "QVIEW2.\"STRAT_ZONE_IDENTIFIER\" IS NOT NULL AND\n" +
            "(QVIEW1.\"WELLBORE_ID\" = QVIEW5.\"WELLBORE_ID\") AND\n" +
            "((('formation pressure depth data' = QVIEW6.\"CLSN_CLS_NAME\") AND ((QVIEW6.\"ACTIVITY_CLASS_S\" = QVIEW4.\"KIND_S\") AND ((QVIEW5.\"WELLBORE_S\" = QVIEW4.\"FACILITY_S\") AND (QVIEW4.\"ACTIVITY_S\" = QVIEW3.\"ACTIVITY_S\")))) OR ((('formation pressure depth data' = QVIEW6.\"CLSN_CLS_NAME\") AND ((QVIEW6.\"ACTIVITY_CLASS_S\" = QVIEW4.\"KIND_S\") AND ((QVIEW5.\"WELLBORE_S\" = QVIEW4.\"FACILITY_S\") AND (QVIEW4.\"ACTIVITY_S\" = QVIEW3.\"ACTIVITY_S\")))) OR (('formation pressure depth data' = QVIEW6.\"CLSN_CLS_NAME\") AND ((QVIEW6.\"ACTIVITY_CLASS_S\" = QVIEW4.\"KIND_S\") AND ((QVIEW5.\"WELLBORE_S\" = QVIEW4.\"FACILITY_S\") AND (QVIEW4.\"ACTIVITY_S\" = QVIEW3.\"ACTIVITY_S\")))))) AND\n"
            +
            "QVIEW3.\"P_PRESSURE_S\" IS NOT NULL AND\n" +
            "((('bar' = QVIEW3.\"DATA_VALUE_U\") AND (('formation pressure depth data' = QVIEW8.\"CLSN_CLS_NAME\") AND ((QVIEW8.\"ACTIVITY_CLASS_S\" = QVIEW7.\"KIND_S\") AND (QVIEW7.\"ACTIVITY_S\" = QVIEW3.\"ACTIVITY_S\")))) OR ((('bar' = QVIEW3.\"DATA_VALUE_U\") AND (('formation pressure depth data' = QVIEW8.\"CLSN_CLS_NAME\") AND ((QVIEW8.\"ACTIVITY_CLASS_S\" = QVIEW7.\"KIND_S\") AND (QVIEW7.\"ACTIVITY_S\" = QVIEW3.\"ACTIVITY_S\")))) OR (('bar' = QVIEW3.\"DATA_VALUE_U\") AND (('formation pressure depth data' = QVIEW8.\"CLSN_CLS_NAME\") AND ((QVIEW8.\"ACTIVITY_CLASS_S\" = QVIEW7.\"KIND_S\") AND (QVIEW7.\"ACTIVITY_S\" = QVIEW3.\"ACTIVITY_S\")))))) AND\n"
            +
            "QVIEW3.\"DATA_VALUE\" IS NOT NULL AND\n" +
            "(QVIEW1.\"WELLBORE_ID\" = QVIEW9.\"WELLBORE\") AND\n" +
            "(QVIEW2.\"STRAT_ZONE_IDENTIFIER\" = QVIEW9.\"ZONE_ID\") AND\n" +
            "QVIEW9.\"STRAT_ZONE_TOP_TVD\" IS NOT NULL AND\n" +
            "(QVIEW1.\"WELLBORE_ID\" = QVIEW10.\"WELLBORE\") AND\n" +
            "(QVIEW2.\"STRAT_ZONE_IDENTIFIER\" = QVIEW10.\"ZONE_ID\") AND\n" +
            "QVIEW10.\"STRAT_ZONE_BASE_TVD\" IS NOT NULL AND\n" +
            "(QVIEW1.\"WELLBORE_ID\" = QVIEW11.\"WELLBORE\") AND\n" +
            "(QVIEW2.\"STRAT_ZONE_IDENTIFIER\" = QVIEW11.\"STRAT_ZONE_IDENTIFIER\") AND\n" +
            "(QVIEW1.\"WELLBORE_ID\" = QVIEW12.\"WELLBORE\") AND\n" +
            "(QVIEW2.\"STRAT_ZONE_IDENTIFIER\" = QVIEW12.\"STRAT_ZONE_IDENTIFIER\") AND\n" +
            "QVIEW12.\"STRAT_COLUMN_IDENTIFIER\" IS NOT NULL AND\n" +
            "QVIEW12.\"STRAT_UNIT_IDENTIFIER\" IS NOT NULL AND\n" +
            "(QVIEW9.\"STRAT_ZONE_TOP_TVD\" = QVIEW13.\"P_PRESSURE_S\") AND\n" +
            "((('m' = QVIEW15.\"DATA_VALUE_1_OU\") AND (('formation pressure depth data' = QVIEW17.\"CLSN_CLS_NAME\") AND ((QVIEW17.\"ACTIVITY_CLASS_S\" = QVIEW14.\"KIND_S\") AND ((QVIEW16.\"WELLBORE_S\" = QVIEW14.\"FACILITY_S\") AND ((QVIEW15.\"ACTIVITY_S\" = QVIEW14.\"ACTIVITY_S\") AND (QVIEW14.\"ACTIVITY_S\" = QVIEW13.\"ACTIVITY_S\")))))) OR (('m' = QVIEW15.\"DATA_VALUE_1_OU\") AND (('formation pressure depth data' = QVIEW17.\"CLSN_CLS_NAME\") AND ((QVIEW17.\"ACTIVITY_CLASS_S\" = QVIEW14.\"KIND_S\") AND ((QVIEW16.\"WELLBORE_S\" = QVIEW14.\"FACILITY_S\") AND ((QVIEW15.\"ACTIVITY_S\" = QVIEW14.\"ACTIVITY_S\") AND (QVIEW14.\"ACTIVITY_S\" = QVIEW13.\"ACTIVITY_S\"))))))) AND\n"
            +
            "(QVIEW10.\"STRAT_ZONE_BASE_TVD\" = QVIEW18.\"P_PRESSURE_S\") AND\n" +
            "((('m' = QVIEW20.\"DATA_VALUE_1_OU\") AND (('formation pressure depth data' = QVIEW22.\"CLSN_CLS_NAME\") AND ((QVIEW22.\"ACTIVITY_CLASS_S\" = QVIEW19.\"KIND_S\") AND ((QVIEW21.\"WELLBORE_S\" = QVIEW19.\"FACILITY_S\") AND ((QVIEW20.\"ACTIVITY_S\" = QVIEW19.\"ACTIVITY_S\") AND (QVIEW19.\"ACTIVITY_S\" = QVIEW18.\"ACTIVITY_S\")))))) OR (('m' = QVIEW20.\"DATA_VALUE_1_OU\") AND (('formation pressure depth data' = QVIEW22.\"CLSN_CLS_NAME\") AND ((QVIEW22.\"ACTIVITY_CLASS_S\" = QVIEW19.\"KIND_S\") AND ((QVIEW21.\"WELLBORE_S\" = QVIEW19.\"FACILITY_S\") AND ((QVIEW20.\"ACTIVITY_S\" = QVIEW19.\"ACTIVITY_S\") AND (QVIEW19.\"ACTIVITY_S\" = QVIEW18.\"ACTIVITY_S\")))))))";


        String benchmark12 = "SELECT \n" +
            "   5 AS \"wellboreQuestType\", NULL AS \"wellboreLang\", CAST(QVIEW6.`wlbTotalCoreLength` AS CHAR(8000) CHARACTER SET utf8) AS `wellbore`, \n"
            +
            "   1 AS \"lenghtMQuestType\", NULL AS \"lenghtMLang\", CONCAT('http://sws.ifi.uio.no/data/npd-v2/company/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW3.`cmpNpdidCompany` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS `lenghtM`, \n"
            +
            "   4 AS \"companyQuestType\", NULL AS \"companyLang\", CAST(QVIEW2.`wlbCompletionYear` AS CHAR(8000) CHARACTER SET utf8) AS `company`, \n"
            +
            "   5 AS \"yearQuestType\", NULL AS \"yearLang\", CAST(QVIEW6.`wlbTotalCoreLength` AS CHAR(8000) CHARACTER SET utf8) AS `year`\n"
            +
            " FROM \n" +
            "wellbore_exploration_all QVIEW1,\n" +
            "wellbore_shallow_all QVIEW2,\n" +
            "company QVIEW3,\n" +
            "wellbore_core QVIEW4,\n" +
            "wellbore_npdid_overview QVIEW5,\n" +
            "wellbore_core QVIEW6,\n" +
            "wellbore_core QVIEW7\n" +
            "WHERE \n" +
            "QVIEW1.`wlbNpdidWellbore` IS NOT NULL AND\n" +
            "QVIEW1.`wlbWellboreName` IS NOT NULL AND\n" +
            "(QVIEW1.`wlbNpdidWellbore` = QVIEW2.`wlbNpdidWellbore`) AND\n" +
            "QVIEW2.`wlbCompletionYear` IS NOT NULL AND\n" +
            "(QVIEW1.`wlbDrillingOperator` = QVIEW3.`cmpLongName`) AND\n" +
            "QVIEW3.`cmpNpdidCompany` IS NOT NULL AND\n" +
            "QVIEW1.`wlbDrillingOperator` IS NOT NULL AND\n" +
            "(QVIEW1.`wlbNpdidWellbore` = QVIEW4.`wlbNpdidWellbore`) AND\n" +
            "(QVIEW1.`wlbNpdidWellbore` = QVIEW5.`wlbNpdidWellbore`) AND\n" +
            "QVIEW4.`wlbCoreNumber` IS NOT NULL AND\n" +
            "(QVIEW4.`wlbCoreNumber` = QVIEW6.`wlbCoreNumber`) AND\n" +
            "(QVIEW1.`wlbNpdidWellbore` = QVIEW6.`wlbNpdidWellbore`) AND\n" +
            "QVIEW6.`wlbTotalCoreLength` IS NOT NULL AND\n" +
            "(QVIEW4.`wlbCoreNumber` = QVIEW7.`wlbCoreNumber`) AND\n" +
            "(QVIEW7.`wlbCoreIntervalUom` = '[ft  ]') AND\n" +
            "(QVIEW1.`wlbNpdidWellbore` = QVIEW7.`wlbNpdidWellbore`) AND\n" +
            "((QVIEW6.`wlbTotalCoreLength` > 50) AND (QVIEW2.`wlbCompletionYear` >= 2008))";

        //        Path path = Paths.get("/home/dimitris/npd-benchmark-master/sql/1.sql");

        //     String qs="";
        //     for(String l:Files.readAllLines(path, StandardCharsets.UTF_8)){
        //         qs+="\n"+l;
        //     }

        String presentation = "SELECT \n" +
            "   5 AS \"wellboreQuestType\", NULL AS \"wellboreLang\", CAST(QVIEW7.`wlbTotalCoreLength` AS CHAR(8000) CHARACTER SET utf8) AS `wellbore`, \n"
            +
            "   1 AS \"lenghtMQuestType\", NULL AS \"lenghtMLang\", CONCAT('http://sws.ifi.uio.no/data/npd-v2/company/', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW3.`cmpNpdidCompany` AS CHAR(8000) CHARACTER SET utf8),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS `lenghtM`, \n"
            +
            "   4 AS \"companyQuestType\", NULL AS \"companyLang\", CAST(QVIEW2.`wlbCompletionYear` AS CHAR(8000) CHARACTER SET utf8) AS `company`, \n"
            +
            "   5 AS \"yearQuestType\", NULL AS \"yearLang\", CAST(QVIEW7.`wlbTotalCoreLength` AS CHAR(8000) CHARACTER SET utf8) AS `year`\n"
            +
            " FROM \n" +
            "DB1_wellbore_shallow_all QVIEW1,\n" +
            "DB1_wellbore_development_all QVIEW2,\n" +
            "DB2_company QVIEW3,\n" +
            "DB1_wellbore_exploration_all QVIEW4,\n" +
            "DB2_wellbore_core QVIEW5,\n" +
            "DB1_wellbore_npdid_overview QVIEW6,\n" +
            "DB2_wellbore_core QVIEW7,\n" +
            "DB1_wellbore_core QVIEW8\n" +
            "WHERE \n" +
            "QVIEW1.`wlbNpdidWellbore` IS NOT NULL AND\n" +
            "QVIEW1.`wlbWellboreName` IS NOT NULL AND\n" +
            "(QVIEW1.`wlbNpdidWellbore` = QVIEW2.`wlbNpdidWellbore`) AND\n" +
            "QVIEW2.`wlbCompletionYear` IS NOT NULL AND\n" +
            "QVIEW3.`cmpNpdidCompany` IS NOT NULL AND\n" +
            "QVIEW3.`cmpLongName` IS NOT NULL AND\n" +
            "(QVIEW3.`cmpLongName` = QVIEW4.`wlbDrillingOperator`) AND\n" +
            "(QVIEW1.`wlbNpdidWellbore` = QVIEW4.`wlbNpdidWellbore`) AND\n" +
            "(QVIEW1.`wlbNpdidWellbore` = QVIEW5.`wlbNpdidWellbore`) AND\n" +
            "(QVIEW1.`wlbNpdidWellbore` = QVIEW6.`wlbNpdidWellbore`) AND\n" +
            "QVIEW5.`wlbCoreNumber` IS NOT NULL AND\n" +
            "(QVIEW5.`wlbCoreNumber` = QVIEW7.`wlbCoreNumber`) AND\n" +
            "(QVIEW1.`wlbNpdidWellbore` = QVIEW7.`wlbNpdidWellbore`) AND\n" +
            "QVIEW7.`wlbTotalCoreLength` IS NOT NULL AND\n" +
            "(QVIEW5.`wlbCoreNumber` = QVIEW8.`wlbCoreNumber`) AND\n" +
            "(QVIEW8.`wlbCoreIntervalUom` = '[ft  ]') AND\n" +
            "(QVIEW1.`wlbNpdidWellbore` = QVIEW8.`wlbNpdidWellbore`) AND\n" +
            "((QVIEW7.`wlbTotalCoreLength` > 50) AND (QVIEW2.`wlbCompletionYear` >= 2008))";


        String sl = "SELECT \n" +
            "   3 AS \"wellboreQuestType\", NULL AS \"wellboreLang\", QVIEW1.\"WELLBORE\" AS \"wellbore\", \n"
            +
            "   3 AS \"unitQuestType\", NULL AS \"unitLang\",QVIEW3.\"STRAT_UNIT_IDENTIFIER\" AS \"unit\", \n"
            +
            "   3 AS \"columnQuestType\", NULL AS \"columnLang\",QVIEW3.\"STRAT_COLUMN_IDENTIFIER\" AS \"column\"\n"
            +
            " FROM \n" +
            "DB1_SLEGGE.STRATIGRAPHIC_ZONE QVIEW1,\n" +
            "DB1_SLEGGE.WELLBORE QVIEW2,\n" +
            "DB1_SLEGGE.STRATIGRAPHIC_ZONE QVIEW3,\n" +
            "DB1_SLEGGE.STRATIGRAPHIC_HIERARCHY QVIEW4,\n" +
            "DB1_SLEGGE.STRATIGRAPHIC_HIERARCHY QVIEW5,\n" +
            "DB1_SLEGGE.data_collection QVIEW6\n" +
            "WHERE \n" +
            "QVIEW1.\"WELLBORE\" IS NOT NULL AND\n" +
            "QVIEW1.\"STRAT_ZONE_IDENTIFIER\" IS NOT NULL AND\n" +
            "(QVIEW1.\"WELLBORE\" = QVIEW2.\"WELLBORE_ID\") AND\n" +
            "'actual' = QVIEW2.\"R_EXISTENCE_KD_NM\" AND\n" +
            "(QVIEW1.\"WELLBORE\" = QVIEW3.\"WELLBORE\") AND\n" +
            "(QVIEW1.\"STRAT_ZONE_IDENTIFIER\" = QVIEW3.\"STRAT_ZONE_IDENTIFIER\") AND\n" +
            "QVIEW3.\"STRAT_COLUMN_IDENTIFIER\" IS NOT NULL AND\n" +
            "QVIEW3.\"STRAT_UNIT_IDENTIFIER\" IS NOT NULL AND\n" +
            "(QVIEW3.\"STRAT_COLUMN_IDENTIFIER\" =QVIEW4.\"STRAT_COLUMN_IDENTIFIER\") AND\n" +
            "(QVIEW3.\"STRAT_UNIT_IDENTIFIER\" = QVIEW4.\"STRAT_UNIT_IDENTIFIER\") AND\n" +
            "(QVIEW3.\"STRAT_COLUMN_IDENTIFIER\" =QVIEW5.\"STRAT_COLUMN_IDENTIFIER\") AND\n" +
            "(QVIEW3.\"STRAT_UNIT_IDENTIFIER\" = QVIEW5.\"STRAT_UNIT_IDENTIFIER\")AND\n" +
            "(QVIEW3.\"STRAT_COLUMN_IDENTIFIER\" = QVIEW6.\"DATA_COLLECTION_NM\")AND\n" +
            "('stratigraphic hierarchy' = QVIEW6.\"R_DATA_COLL_TY_NM\") ";


        String testSelf = "SELECT \n" +
            "   3 AS \"wellboreQuestType\", NULL AS \"wellboreLang\", QVIEW1.\"WELLBORE_ID\" AS \"wellbore\", \n"
            +
            "   5 AS \"formation_pressureQuestType\", NULL AS \"formation_pressureLang\", CAST(QVIEW2.\"DATA_VALUE\" AS VARCHAR(4000)) AS \"formation_pressure\"\n"
            +
            " FROM \n" +
            "SLEGGE_EPI.wellbore QVIEW1,\n" +
            "SLEGGE_EPI.p_pressure QVIEW2,\n" +
            "SLEGGE_EPI.activity QVIEW3,\n" +
            "SLEGGE_EPI.wellbore QVIEW4,\n" +
            "SLEGGE_EPI.activity_class QVIEW5\n" +
            "WHERE \n" +
            "('actual' = QVIEW1.\"R_EXISTENCE_KD_NM\")  AND\n" +
            "QVIEW1.\"WELLBORE_ID\" IS NOT NULL AND\n" +
            "(QVIEW1.\"WELLBORE_ID\" = QVIEW4.\"WELLBORE_ID\") AND\n" +
            "(('formation pressure depth data' = QVIEW5.\"CLSN_CLS_NAME\") AND ((QVIEW5.\"ACTIVITY_CLASS_S\" = QVIEW3.\"KIND_S\") AND ((QVIEW4.\"WELLBORE_S\" = QVIEW3.\"FACILITY_S\") AND (QVIEW3.\"ACTIVITY_S\" = QVIEW2.\"ACTIVITY_S\"))))  AND\n"
            +
            "QVIEW2.\"P_PRESSURE_S\" IS NOT NULL AND\n" +
            "(('bar' = QVIEW2.\"DATA_VALUE_U\") AND (('formation pressure depth data' = QVIEW6.\"CLSN_CLS_NAME\") AND ((QVIEW6.\"ACTIVITY_CLASS_S\" = QVIEW5.\"KIND_S\") AND (QVIEW5.\"ACTIVITY_S\" = QVIEW2.\"ACTIVITY_S\")))) AND\n"
            +
            "QVIEW2.\"DATA_VALUE\" IS NOT NULL";

        String bsbm = "SELECT *\n" +
            "FROM (\n" +
            "SELECT DISTINCT \n" +
            "   1 AS \"productQuestType\", NULL AS \"productLang\", ('http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"producer\" AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F') || '/Product' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"nr\" AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"product\", \n"
            +
            "   3 AS \"labelQuestType\", NULL AS \"labelLang\", QVIEW1.\"label\" AS \"label\"\n" +
            " FROM \n" +
            "db1_product QVIEW1,\n" +
            "db1_producttypeproduct QVIEW2,\n" +
            "db1_productfeatureproduct QVIEW3,\n" +
            "db1_productfeatureproduct QVIEW4\n" +
            "WHERE \n" +
            "QVIEW1.\"producer\" IS NOT NULL AND\n" +
            "QVIEW1.\"nr\" IS NOT NULL AND\n" +
            "QVIEW1.\"label\" IS NOT NULL AND\n" +
            "(QVIEW2.\"productType\" = '10') AND\n" +
            "((QVIEW2.\"product\" = QVIEW1.\"nr\") OR ((QVIEW2.\"product\" = QVIEW1.\"nr\") OR (QVIEW2.\"product\" = QVIEW1.\"nr\"))) AND\n"
            +
            "(QVIEW3.\"productFeature\" = '414') AND\n" +
            "((QVIEW3.\"product\" = QVIEW1.\"nr\") OR ((QVIEW3.\"product\" = QVIEW1.\"nr\") OR (QVIEW3.\"product\" = QVIEW1.\"nr\"))) AND\n"
            +
            "(QVIEW4.\"productFeature\" = '369') AND\n" +
            "((QVIEW4.\"product\" = QVIEW1.\"nr\") OR ((QVIEW4.\"product\" = QVIEW1.\"nr\") OR (QVIEW4.\"product\" = QVIEW1.\"nr\"))) AND\n"
            +
            "QVIEW1.\"propertyNum1\" IS NOT NULL AND\n" +
            "(QVIEW1.\"propertyNum1\" > 3)\n" +
            ") SUB_QVIEW\n" +
            "ORDER BY SUB_QVIEW.\"label\"\n" +
            "LIMIT 10\n" +
            "OFFSET 0";

        String testMulti = "Select n.name as name from producer p, names n, invoice i "
            + "where p.id=i.id and i.id=n.id " + "union " + "Select n.name as name "
            + "from producer2 p, names n, invoice i " + "where p.id=i.id and i.id=n.id ";

        String testMulti1 = "Select n.name as name from names n, invoice i, producer p "
            + "where  i.id=n.id and i.id=p.id and n.name=p.name and p.cost=9";

        String testMulti2 = "Select n.name as name, n.id as id from names n, invoice i, producer p "
            + "where  i.id=n.id and n.name=p.name and p.cost=9";

        // String q1 = readFile("/home/dimitris/npd-benchmark-master/sql/12test.sql");
        // String q1="select * from test_underscore_wellbore_mud limit 10";
        String q1 = "select * from npd_wellbore_core";

        String slegge = "SELECT *\n" +
            "FROM (\n" +
            "SELECT\n" +
            "   QVIEW1.\"wellboreid\"\n" +
            " FROM\n" +
            "(SELECT substr(IDENTIFIER,4) as WELLBOREID, IDENTIFIER \n" +
            "FROM adp.npd_WELLBORE) QVIEW1\n" +
            "WHERE\n" +
            "QVIEW1.\"wellboreid\" IS NOT NULL\n" +
            ") SUB_QVIEW\n" +
            "LIMIT 100\n" +
            "OFFSET 0";

        String left = " SELECT \n" +
            "   \"Qans4View\".\"v8\"\n" +
            " FROM \n" +
            "                (SELECT \n" +
            "\n" +
            "   3 AS \"v8QuestType\", NULL AS \"v8Lang\", CAST(\"Qmys_db1_productVIEW0\".\"propertyTex1\" AS CHAR) AS \"v8\"\n"
            +
            " FROM \n" +
            "mys_db1_product \"Qmys_db1_productVIEW0\",\n" +
            "mys_db1_producer \"Qmys_db1_producerVIEW1\",\n" +
            "mys_db1_productfeatureproduct \"Qmys_db1_productfeatureproductVIEW2\",\n" +
            "mys_db1_productfeature \"Qmys_db1_productfeatureVIEW3\"\n" +
            "WHERE \n" +
            "(\"Qmys_db1_productVIEW0\".\"nr\" = '4') AND\n" +
            "(\"Qmys_db1_productVIEW0\".\"producer\" = '11') AND\n" +
            "\"Qmys_db1_productVIEW0\".\"label\" IS NOT NULL AND\n" +
            "\"Qmys_db1_productVIEW0\".\"comment\" IS NOT NULL AND\n" +
            "(\"Qmys_db1_producerVIEW1\".\"nr\" = '11') AND\n" +
            "\"Qmys_db1_producerVIEW1\".\"label\" IS NOT NULL AND\n" +
            "(\"Qmys_db1_productfeatureproductVIEW2\".\"product\" = '4') AND\n" +
            "\"Qmys_db1_productfeatureproductVIEW2\".\"productFeature\" IS NOT NULL AND\n" +
            "(\"Qmys_db1_productfeatureproductVIEW2\".\"productFeature\" = \"Qmys_db1_productfeatureVIEW3\".\"nr\") AND\n"
            +
            "\"Qmys_db1_productfeatureVIEW3\".\"label\" IS NOT NULL AND\n" +
            "\"Qmys_db1_productVIEW0\".\"propertyTex1\" IS NOT NULL AND\n" +
            "\"Qmys_db1_productVIEW0\".\"propertyTex2\" IS NOT NULL AND\n" +
            "\"Qmys_db1_productVIEW0\".\"propertyTex3\" IS NOT NULL AND\n" +
            "\"Qmys_db1_productVIEW0\".\"propertyNum1\" IS NOT NULL AND\n" +
            "\"Qmys_db1_productVIEW0\".\"propertyNum2\" IS NOT NULL) \"Qans4View\"\n" +
            "        LEFT OUTER JOIN\n" +
            "        (SELECT \n" +
            "   3 AS \"v0QuestType\", NULL AS \"v0Lang\", CAST(\"Qmys_db1_productVIEW0\".\"propertyTex4\" AS CHAR) AS \"v0\"\n"
            +
            " FROM \n" +
            "mys_db1_product \"Qmys_db1_productVIEW0\"\n" +
            "WHERE \n" +
            "(\"Qmys_db1_productVIEW0\".\"nr\" = '4') AND\n" +
            "(\"Qmys_db1_productVIEW0\".\"producer\" = '1') AND\n" +
            "\"Qmys_db1_productVIEW0\".\"propertyTex4\" IS NOT NULL) \"Qans5View\"         ON\n" +
            "        (1 = 1)";


        System.out.println(limit);
        String t = readFile("/home/dimitris/Desktop/parse-error.sql");
        SQLQuery query = SQLQueryParser.parse(limit);
        QueryDecomposer d = new QueryDecomposer(query, "/tmp/", 1, null);
        ArrayList<SQLQuery> subqueries = d.getSubqueries();
        for (SQLQuery q : subqueries) {
            System.out.println(q.toDistSQL());
        }
        //Node root=d.getPlan();
        // String dot=root.dotPrint();
        // System.out.print(dot);
     /*   String q1=testMulti;
        System.out.println(q1);
        SQLQuery query = SQLQueryParser.parse(q1);
        QueryDecomposer d = new QueryDecomposer(query, false, "/home/dimitris/distsqlDB/", 2, false, true);
        ArrayList<SQLQuery> subqueries = d.getSubqueries();
        for (SQLQuery q : subqueries) {
            System.out.println(q.toDistSQL());
        }*/

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
