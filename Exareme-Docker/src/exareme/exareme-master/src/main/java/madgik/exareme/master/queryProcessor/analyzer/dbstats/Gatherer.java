/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.analyzer.dbstats;

import com.google.gson.Gson;
import madgik.exareme.master.queryProcessor.analyzer.stat.Stat;
import madgik.exareme.master.queryProcessor.analyzer.stat.Table;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

/**
 * @author jim
 */
public class Gatherer {

    private Map<String, Table> schema;
    private String connString;
    private String dbName;
    private String sch;

    public Gatherer(String connString, String dbName) {
        this.connString = connString;
        this.dbName = dbName;
        sch = "";
    }

    public Map<String, Table> gather(String dbpath) throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + this.connString);

        Stat stat = new Stat(connection);
        stat.setSch(sch);
        schema = stat.extractStats();

        //

	/*	for (Entry<String, Table> e : schema.entrySet()) {

			System.out.println("TABLE: " + e.getKey() + " TUPLES: "
					+ e.getValue().getNumberOfTuples());
			for (Entry<String, Column> ee : schema.get(e.getKey())
					.getColumnMap().entrySet()) {

				int s = 0;
				for (int i : ee.getValue().getDiffValFreqMap().values())
					s += i;

				System.out.println("COLUMN: " + ee.getKey() + " TUPLES: " + s);
			}

		}*/

        //dataToJson(dbName, dbpath);

        connection.close();
        return schema;

    }

    public void setSch(String s) {
        this.sch = s;
    }

    private void dataToJson(String filename, String dbpath) throws Exception {

        Gson gson = new Gson();
        String jsonStr = gson.toJson(schema);

        PrintWriter writer = new PrintWriter(dbpath + filename + ".json", "UTF-8");
        writer.println(jsonStr);
        writer.close();

    }
}
