package madgik.exareme.master.engine.parser;

import java.util.Scanner;

/**
 * @author herald
 */
public class SQLFilterScanner {
    private String script = null;
    private Scanner scanner = null;
    private String nextLine = null;

    public SQLFilterScanner(String queryScript) {
        script = queryScript;
        scanner = new Scanner(script);
    }

    public boolean hasNextLine() {
        if (nextLine != null) {
            return true;
        }

        while (true) {
            if (scanner.hasNextLine() == false) {
                return false;
            }

            nextLine = scanner.nextLine();
            if (nextLine.startsWith("--") == false) {
                break;
            }
            nextLine = null;
        }

        return nextLine != null ? true : false;
    }

    public String nextLine() {
        String next = nextLine;
        nextLine = null;

        return next;
    }

    public void close() {
        scanner.close();
    }
}
