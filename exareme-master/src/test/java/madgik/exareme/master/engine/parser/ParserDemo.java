package madgik.exareme.master.engine.parser;

import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.common.schema.Select;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.utils.file.FileUtil;

import java.io.File;

/**
 * @author herald
 */
public class ParserDemo {
    private ParserDemo() {
    }

    public static void main(String[] args) throws Exception {
        AdpDBParser parser = new AdpDBParser(args[0]);

        Registry registry = Registry.getInstance(args[0]);
        String queryScript = FileUtil.readFile(new File(args[1]));

        QueryScript script = parser.parse(queryScript, registry);

        for (Select q : script.getSelectQueries()) {
            System.out.println(q.toString() + "\n");
            System.out.println(" --- ");
        }
    }
}
