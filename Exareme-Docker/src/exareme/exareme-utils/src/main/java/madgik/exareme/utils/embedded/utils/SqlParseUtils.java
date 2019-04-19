/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.utils.embedded.utils;

/**
 * @author Christoforos Svingos
 */
public class SqlParseUtils {

    public static int countOfqueries(String query) {
        int numberOfQueries = 0;
        for (String s : query.split(";\\s")) {
            if (!s.trim().isEmpty())
                numberOfQueries++;
        }

        return numberOfQueries;
    }

    public static int countOccurrences(String haystack, char needle) {
        int count = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                count++;
            }
        }

        return count;
    }

}
