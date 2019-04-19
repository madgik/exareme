package madgik.exareme.worker.throughput.misc;

import java.util.BitSet;

/**
 * @author herald
 */
public class BitSetTest {
    public static void main(String[] args) {
        int numOfBits = 500;
        long times = 500000000;

        BitSet bits = new BitSet(numOfBits);

        long start = System.currentTimeMillis();
        for (int i = 0; i < times; ++i) {
            bits.set(i % numOfBits, true);
        }
        long end = System.currentTimeMillis();

        System.out.println(((double) times * 1000.0 / (end - start)) + " ops / sec");
    }
}
