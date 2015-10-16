package madgik.exareme.utils.generators;

import madgik.exareme.utils.statistics.Zipf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author herald
 */
public class ZipfGenerator {
    private int diffNumbers = 0;
    private long totalNumbers = 0;
    private double z = 0.0;

    public ZipfGenerator(int diffNumbers, long totalNumbers, double z) {
        this.diffNumbers = diffNumbers;
        this.totalNumbers = totalNumbers;
        this.z = z;
    }

    public void generate(File out) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(out), 1024 * 1024);

        Zipf zipf = new Zipf(diffNumbers, z);
        for (long i = 0L; i < totalNumbers; i += 1L) {
            writer.write("" + zipf.next());
            writer.newLine();
        }

        writer.close();
    }
}
