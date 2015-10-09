/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.demo;

import com.foundationdb.sql.parser.StatementNode;
import madgik.exareme.master.queryProcessor.decomposer.federation.DistSQLParser;
import madgik.exareme.master.queryProcessor.decomposer.query.Table;
import madgik.exareme.master.queryProcessor.decomposer.query.visitors.BaseTableCrawlerVisitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author dimitris
 */
public class TableImport {

    public static void main(String[] args) throws IOException, Exception {
        String queryDir = args[0];
        String madisOp = args[1];
        String partitions = args[2];
        ArrayList<File> queries = walk(queryDir);
        ArrayList<Table> baseTables = new ArrayList<Table>();
        for (File f : queries) {
            String q = readFile(f.getAbsolutePath());
            if (q == null || q.equals("")) {
                continue;
            }
            DistSQLParser parser = new DistSQLParser();
            StatementNode node = parser.parseStatement(q);

            BaseTableCrawlerVisitor visitor = new BaseTableCrawlerVisitor(baseTables);
            node.accept(visitor);

        }
        for (Table t : baseTables)
            System.out.println("distributed create table " + t.getName().replace("SLEGGE.", "")
                .replace("SLEGGE_EPI.", "") + " to " + partitions + " as select * from " + madisOp
                + t.getName() + ")");
    }

    public static ArrayList<File> walk(String path) {

        File root = new File(path);
        File[] list = root.listFiles();
        ArrayList<File> result = new ArrayList<File>();
        if (list == null)
            return result;

        for (File f : list) {
            if (f.isDirectory()) {
                for (File f2 : walk(f.getAbsolutePath())) {
                    result.add(f2);
                }
            } else {
                result.add(f);
            }
        }
        return result;
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
