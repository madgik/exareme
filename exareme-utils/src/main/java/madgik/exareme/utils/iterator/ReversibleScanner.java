/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.iterator;

import org.apache.log4j.Logger;

import java.util.Scanner;

/**
 * @author herald
 */
public class ReversibleScanner {

    private static Logger log = Logger.getLogger(ReversibleScanner.class);
    private int tokenWindow = 0;
    private Scanner scanner = null;
    // TODO(herald): make this right. For now it has just window of 1.
    private String line = null;
    private int cachedLine = 0;
    private int scannerLine = 0;

    public ReversibleScanner(int tokenWindow, Scanner scanner) {
        this.tokenWindow = tokenWindow;
        this.scanner = scanner;
    }

    public boolean hasNext() {
        if (cachedLine < scannerLine) {
            return true;
        }

        return scanner.hasNext();
    }

    public boolean rollBack(int numOfTokens) {
        cachedLine--;
        return true;
    }

    public String nextLine() {
        if (cachedLine < scannerLine) {
            cachedLine++;
            return line;
        }

        line = scanner.nextLine();
        cachedLine++;
        scannerLine++;

        return line;
    }

    public void close() {
        scanner.close();
    }
}
