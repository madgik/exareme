/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.string;

/**
 * @author heraldkllapi
 */
public class StringUtils {

    public static String concatenateUnique(String... strings) {
        // Return empty string only when the given array is empty
        if (strings.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(s.length()).append("|").append(s);
        }
        return sb.toString();
    }

    public static String normalizeSQLQuery(String query) {
        return query.replaceAll("( )+", " ");
    }
}
