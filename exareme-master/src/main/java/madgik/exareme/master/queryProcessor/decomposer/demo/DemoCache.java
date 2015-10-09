/*
 * To change this template, choose Tools | Templates
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
public class DemoCache {

    public static void main(String[] args) throws Exception {
        DBInfoWriterDB.write(
            "addFederatedEndpoint(npd,jdbc:mysql://whale.di.uoa.gr:3306/npd,com.mysql.jdbc.Driver,benchmark,gray769watt724!@#,bookstores)",
            "/tmp/");

        String q1 = "select * from npd_wellbore_core";
        SQLQuery query = SQLQueryParser.parse(q1);
        QueryDecomposer d = new QueryDecomposer(query, "/tmp/", 2, null);
        ArrayList<SQLQuery> subqueries = d.getSubqueries();
        for (SQLQuery q : subqueries) {
            if (q.isFederated()) {
                //call cache
                System.out.print(q.getExecutionStringInFederatedSource());
            }
        }

    }


}
