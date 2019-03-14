package madgik.exareme.master.queryProcessor.decomposer;

import madgik.exareme.master.queryProcessor.decomposer.federation.NamesToAliases;
import madgik.exareme.master.queryProcessor.decomposer.federation.QueryDecomposer;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQueryParser;

public class DemoDAG {

    public static final String queryExample = "SELECT *\n" +
            "FROM (\n" +
            "SELECT \n" +
            "   7 AS \"wellboreQuestType\", NULL AS \"wellboreLang\", CAST(QVIEW1.\"wlbWellboreName\" AS VARCHAR(10485760)) AS \"wellbore\", \n" +
            "   5 AS \"lenghtMQuestType\", NULL AS \"lenghtMLang\", CAST(QVIEW4.\"lsuCoreLenght\" AS VARCHAR(10485760)) AS \"lenghtM\", \n" +
            "   7 AS \"companyQuestType\", NULL AS \"companyLang\", CAST(QVIEW2.\"wlbDrillingOperator\" AS VARCHAR(10485760)) AS \"company\", \n" +
            "   4 AS \"yearQuestType\", NULL AS \"yearLang\", CAST(QVIEW2.\"wlbCompletionYear\" AS VARCHAR(10485760)) AS \"year\"\n" +
            " FROM \n" +
            "\"wellbore_development_all\" QVIEW1,\n" +
            "\"wellbore_exploration_all\" QVIEW2,\n" +
            "\"company\" QVIEW3,\n" +
            "\"strat_litho_wellbore_core\" QVIEW4,\n" +
            "\"wellbore_npdid_overview\" QVIEW5\n" +
            "WHERE \n" +
            "QVIEW1.\"wlbWellboreName\" IS NOT NULL AND\n" +
            "QVIEW1.\"wlbNpdidWellbore\" IS NOT NULL AND\n" +
            "(QVIEW1.\"wlbNpdidWellbore\" = QVIEW2.\"wlbNpdidWellbore\") AND\n" +
            "QVIEW2.\"wlbCompletionYear\" IS NOT NULL AND\n" +
            "(QVIEW2.\"wlbDrillingOperator\" = QVIEW3.\"cmpLongName\") AND\n" +
            "QVIEW2.\"wlbDrillingOperator\" IS NOT NULL AND\n" +
            "QVIEW3.\"cmpNpdidCompany\" IS NOT NULL AND\n" +
            "(QVIEW1.\"wlbNpdidWellbore\" = QVIEW4.\"wlbNpdidWellbore\") AND\n" +
            "(QVIEW1.\"wlbNpdidWellbore\" = QVIEW5.\"wlbNpdidWellbore\") AND\n" +
            "QVIEW4.\"lsuNpdidLithoStrat\" IS NOT NULL AND\n" +
            "QVIEW4.\"lsuCoreLenght\" IS NOT NULL AND\n" +
            "((QVIEW4.\"lsuCoreLenght\" > 50) AND (QVIEW2.\"wlbCompletionYear\" >= 2008))\n" +
            "UNION\n" +
            "SELECT \n" +
            "   7 AS \"wellboreQuestType\", NULL AS \"wellboreLang\", CAST(QVIEW1.\"wlbWellboreName\" AS VARCHAR(10485760)) AS \"wellbore\", \n" +
            "   5 AS \"lenghtMQuestType\", NULL AS \"lenghtMLang\", CAST(QVIEW6.\"wlbTotalCoreLength\" AS VARCHAR(10485760)) AS \"lenghtM\", \n" +
            "   7 AS \"companyQuestType\", NULL AS \"companyLang\", CAST(QVIEW2.\"wlbDrillingOperator\" AS VARCHAR(10485760)) AS \"company\", \n" +
            "   4 AS \"yearQuestType\", NULL AS \"yearLang\", CAST(QVIEW2.\"wlbCompletionYear\" AS VARCHAR(10485760)) AS \"year\"\n" +
            " FROM \n" +
            "\"wellbore_development_all\" QVIEW1,\n" +
            "\"wellbore_exploration_all\" QVIEW2,\n" +
            "\"company\" QVIEW3,\n" +
            "\"wellbore_core\" QVIEW4,\n" +
            "\"wellbore_npdid_overview\" QVIEW5,\n" +
            "\"wellbore_core\" QVIEW6\n" +
            "WHERE \n" +
            "QVIEW1.\"wlbWellboreName\" IS NOT NULL AND\n" +
            "QVIEW1.\"wlbNpdidWellbore\" IS NOT NULL AND\n" +
            "(QVIEW1.\"wlbNpdidWellbore\" = QVIEW2.\"wlbNpdidWellbore\") AND\n" +
            "QVIEW2.\"wlbCompletionYear\" IS NOT NULL AND\n" +
            "(QVIEW2.\"wlbDrillingOperator\" = QVIEW3.\"cmpLongName\") AND\n" +
            "QVIEW2.\"wlbDrillingOperator\" IS NOT NULL AND\n" +
            "QVIEW3.\"cmpNpdidCompany\" IS NOT NULL AND\n" +
            "(QVIEW1.\"wlbNpdidWellbore\" = QVIEW4.\"wlbNpdidWellbore\") AND\n" +
            "(QVIEW1.\"wlbNpdidWellbore\" = QVIEW5.\"wlbNpdidWellbore\") AND\n" +
            "QVIEW4.\"wlbCoreNumber\" IS NOT NULL AND\n" +
            "(QVIEW4.\"wlbCoreNumber\" = QVIEW6.\"wlbCoreNumber\") AND\n" +
            "(QVIEW1.\"wlbNpdidWellbore\" = QVIEW6.\"wlbNpdidWellbore\") AND\n" +
            "QVIEW6.\"wlbTotalCoreLength\" IS NOT NULL AND\n" +
            "((QVIEW6.\"wlbTotalCoreLength\" > 50) AND (QVIEW2.\"wlbCompletionYear\" >= 2008))) SUB";

    public static void main(String[] args) throws Exception {

        SQLQuery query = SQLQueryParser.parse(queryExample);
        QueryDecomposer d = new QueryDecomposer(query, "/tmp/", 1, null);

        d.setN2a(new NamesToAliases());

        for (SQLQuery s : d.getSubqueries()) {
            System.out.println(s.getHashId() + " : \n" + s.toDistSQL());
        }


    }

}
