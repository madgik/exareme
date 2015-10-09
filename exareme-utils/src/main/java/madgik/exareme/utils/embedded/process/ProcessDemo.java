/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.embedded.process;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author heraldkllapi
 */
public class ProcessDemo {

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);

        MadisProcess proc = new MadisProcess("");
        proc.start();

        execAndPrintResults("create table a(id, name);", proc);
        execAndPrintResults("insert into a values (1, 'hoho');", proc);
        execAndPrintResults("insert into a values (2, 'haha');", proc);
        for (int i = 0; i < 100; ++i) {
            System.out.println(">>> " + i);
            execAndPrintResults("select * from (pipe 'echo a');", proc);
            execAndPrintResults("select " + i + ";", proc);
        }
        proc.stop();
    }

    private static void execAndPrintResults(String query, MadisProcess proc) throws Exception {
        QueryResultStream stream = proc.execQuery(query);
        System.out.println(stream.getSchema());
        String record = stream.getNextRecord();
        while (record != null) {
            System.out.println(record);
            record = stream.getNextRecord();
        }
    }
}
