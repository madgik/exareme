/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.utility;

/**
 * @author Christos Mallios <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 */
public class QueryParser {

    public static String escapeSingleQuotes(String query) {
        return query.replace("'", "\\\"");
    }

    public static String retrieveSingleQuotes(String query) {
        return query.replace("\\\"", "'");
    }

    public static String parsing(String query) {

        query = removeQuestionmark(query);

        query = capitalizationOfLetters(query);

        return query;
    }

    private static String removeQuestionmark(String query) {

        if (query.charAt(query.length() - 1) == ';') {
            query = query.replace(query.substring(query.length() - 1), "");
            return query;
        } else {
            return query;
        }

    }

    private static String capitalizationOfLetters(String query) {

        return query;
    }
}
