package madgik.exareme.master.engine.parser;

import junit.framework.TestCase;
import madgik.exareme.common.schema.expression.SQLQuery;
import madgik.exareme.common.schema.expression.SQLScript;

import java.io.ByteArrayInputStream;
import java.rmi.ServerException;

public class ParserTest extends TestCase {

    public void test() throws Exception {
        String queryScript = "distributed create table a \n as select 'a', \"aa;\" from a;\n"
                + "distributed create table b \n as select 'b;' from b;\n";
        SQLScript sQLScript = null;
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(queryScript.getBytes());
            AdpDBQueryParser parser = new AdpDBQueryParser(stream);
            sQLScript = parser.parseScript();
        } catch (Exception e) {
            throw new ServerException("Cannot parse script", e);
        }

        for (SQLQuery q : sQLScript.getQueries()) {
            System.out.println(q.getSql());
        }
    }
}
