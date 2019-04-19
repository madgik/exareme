/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.master.queryProcessor.analyzer.stat;

import com.google.gson.Gson;
import madgik.exareme.master.queryProcessor.estimator.db.Schema;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.Types;

/**
 * @author jim
 */
public class StatUtils {

    private static final int HASH_STRING_CHARS = 3;
    private static final int HASH_STRING_BASE = 256;

    public static double hashString(String str) {
        if (str == null)
            return 0;
        // System.out.println("STRING FOR HASH: " + str);
        double hashStringVal = 0.0;

        if (str.length() >= 3) {

            char[] hashChars = new char[HASH_STRING_CHARS];

            for (int i = 0; i < HASH_STRING_CHARS; i++) {
                hashChars[i] = str.charAt(i);
                // System.out.println(hashChars[i]);
            }

            for (int i = 0; i < HASH_STRING_CHARS; i++) {
                // System.out.println(hashChars[i] + " | " +
                // (double)((int)hashChars[i]) + " | " +
                // Math.pow((double)HASH_STRING_BASE, (double)i));

                hashStringVal += (double) ((int) hashChars[i]) * Math
                        .pow((double) HASH_STRING_BASE, (double) (HASH_STRING_CHARS - i));

            }

            return hashStringVal;
        } else {

            char[] hashChars = new char[str.length()];

            for (int i = 0; i < str.length(); i++)
                hashChars[i] = str.charAt(i);

            for (int i = 0; i < str.length(); i++) {
                // System.out.println(hashChars[i] + " | " +
                // (double)((int)hashChars[i]) + " | " +
                // Math.pow((double)HASH_STRING_BASE, (double)i));

                hashStringVal += (double) ((int) hashChars[i]) * Math
                        .pow((double) HASH_STRING_BASE, (double) (HASH_STRING_CHARS - i));

            }

            return hashStringVal;

        }

    }

    public static boolean isTextType(int columnType) {
        if (columnType == Types.CHAR || columnType == Types.DATE || columnType == Types.LONGNVARCHAR
                || columnType == Types.NCHAR || columnType == Types.NVARCHAR || columnType == Types.TIME
                || columnType == Types.TIMESTAMP || columnType == Types.VARCHAR
                || columnType == Types.OTHER)

            return true;
        else
            return false;
    }

    public static boolean isCompatibleType(int columnType) {
        if (columnType == Types.BOOLEAN || columnType == Types.CHAR || columnType == Types.DATE
                || columnType == Types.DECIMAL || columnType == Types.DOUBLE
                || columnType == Types.FLOAT || columnType == Types.INTEGER
                || columnType == Types.NUMERIC || columnType == Types.TIME
                || columnType == Types.TIMESTAMP || columnType == Types.TINYINT
                || columnType == Types.VARCHAR)
            return true;
        else
            return false;

    }

    public static void addSchemaToFile(String filename, Schema s) throws Exception {
        BufferedReader br = null;
        Schema fileSchema = null;
        File f = new File(filename);
        if (f.exists() && !f.isDirectory()) {
            br = new BufferedReader(new FileReader(filename));
            Gson gson = new Gson();
            fileSchema = gson.fromJson(br, Schema.class);
            br.close();
            for (String ri : s.getTableIndex().keySet()) {
                fileSchema.getTableIndex().put(ri, s.getTableIndex().get(ri));
            }

        } else {
            fileSchema = s;
        }
        Gson gson = new Gson();
        String jsonStr = gson.toJson(fileSchema);

        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        writer.println(jsonStr);
        writer.close();

    }

    // int(8) float(8) text(200) varchar(45)

}
