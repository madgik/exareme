package madgik.exareme.master.engine.executor.remote.operator.OptiqueStreamQueryEndPoint;

/**
 * @author xrs
 */
public class ParseUtils {

    public static String ArrayToCsv(Object[] line) {
        StringBuilder csvLine = new StringBuilder();
        for (Object obj : line) {
            csvLine.append(obj.toString()).append(",");
        }

        csvLine.setCharAt(csvLine.length() - 1, '\n');

        return csvLine.toString();
    }

    public static String queryRewriting(String sqlQuery) {
        String[] a = sqlQuery.split("\\s+");

        StringBuilder finalQuery = new StringBuilder();
        for (int i = 0; i < a.length - 4; ++i) {
            if (a[i].equalsIgnoreCase("create") && a[i + 1].equalsIgnoreCase("stream")) {
                a[i + 1] = "temp view";
                if (!a[i + 4].equalsIgnoreCase("wcache")) {
                    a[i + 3] = "as wcache";
                }
                i = i + 3;
            }
        }

        for (String word : a) {
            finalQuery.append(word + " ");
        }

        return finalQuery.toString();
    }

}
