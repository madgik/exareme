/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.sort;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class JavaSortPerformance {

    private static Logger log = Logger.getLogger(JavaSortPerformance.class);

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        long[] data = readData("unsortedFile.csv");

        long end = System.currentTimeMillis();
        log.debug(end - start);

        start = end;
        Arrays.sort(data);

        end = System.currentTimeMillis();
        log.debug(end - start);

        start = end;
        writeData(data, "sortedFile.csv");

        end = System.currentTimeMillis();
        log.debug(end - start);
    }

    static long[] readData(String fileName) throws Exception {
        long data[] = new long[100000000];

        BufferedReader r = new BufferedReader(new FileReader(fileName));
        StreamTokenizer stok = new StreamTokenizer(r);
        stok.parseNumbers();
        stok.nextToken();

        int i = 0;
        while (stok.ttype != StreamTokenizer.TT_EOF) {
            if (stok.ttype == StreamTokenizer.TT_NUMBER) {
                data[i] = (long) stok.nval;
            }

            stok.nextToken();
            i++;
        }

        return data;
    }

    static long[] readData() {
        long data[] = new long[100000000];

        Random rand = new Random();
        for (int i = 0; i < data.length; i++) {
            data[i] = rand.nextInt(100);
        }

        return data;
    }

    static void writeData(long[] data, String file) throws Exception {
        BufferedWriter fw = new BufferedWriter(new FileWriter(new File(file)));

        for (int i = 0; i < data.length; i++) {
            fw.write(data[i] + "\n");
        }

        fw.close();
    }
}
