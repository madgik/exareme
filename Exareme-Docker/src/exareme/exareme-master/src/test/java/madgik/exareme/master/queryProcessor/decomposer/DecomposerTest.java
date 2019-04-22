package madgik.exareme.master.queryProcessor.decomposer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by dimitris on 4/20/15.
 */
public class DecomposerTest {

    public static void main(String[] args) throws Exception {
        String query2 = " select t1.id as id1, t3.id as id3, " + "count(t1.id) as id1_count, "
                + "        sum(t2.id) as w1, " + "        count(t1.id) as w2, "
                + "        max(t1.id) as w3, " + "        f(t2.id, t1.id, t3.id) as w4 "
                + " from a t1, b t2, c t3, d t4 " + " where t3.id>0 "
                // + "and t1.id >4  "
                + "and t2.id>=0 " + "and t4.id < 100 " + " and t1.id = t2.id " + " and t3.id = t4.id "
                + " and t1.id = t3.id " + " group by t1.id " + " order by t2.id " + " limit 10";

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
                "select\n" + "  nation,\n" + "  o.o_year\n" + // "  sum(amount) as sum_profit\n" +
                        "from\n" + "  (\n" + "    select\n" + "      n.n_name as nation,\n" +
                        // "      strftime('%Y', o.o_orderdate) as o.o_year,\n" +
                        "      l.l_extendedprice * (1 - l.l_discount) - ps.ps_supplycost * l.l_quantity as amount\n"
                        + "    from\n" + "      part p,\n" + "      supplier s,\n" + "      lineitem l,\n"
                        + "      partsupp ps,\n" + "      orders o,\n" + "      nation n\n" + "    where\n"
                        + "      s.s_suppkey = l.l_suppkey\n" + "      and ps.ps_suppkey = l.l_suppkey\n"
                        + "      and ps.ps_partkey = l.l_partkey\n"
                        + "      and p.p_partkey = l.l_partkey\n"
                        + "      and o.o_orderkey = l.l_orderkey\n"
                        + "      and s.s_nationkey = n.n_nationkey\n" +
                        // "      and p.p_name like '%sky%'\n" +
                        "  ) as profit\n" + "group by\n" + "  n.nation,\n" + "  o.o_year\n" + "order by\n"
                        + "  nation,\n" + "  o.o_year desc";


        //        SQLQuery query = SQLQueryParser.parse(query2);
        //        QueryDecomposer d = new QueryDecomposer(query, false, "/tmp/", 4, false,
        //                false, false);
        //        ArrayList<SQLQuery> subqueries = d.getSubqueries();
        //        for (SQLQuery q : subqueries) {
        //            System.out.println(q.toDistSQL());
        //        }
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
