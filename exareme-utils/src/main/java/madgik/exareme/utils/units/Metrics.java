package madgik.exareme.utils.units;

/**
 * @author herald
 */
public class Metrics {

    // Capacity - Relative to B.
    public static int KB = 1024;
    public static int MB = 1024 * KB;
    public static int GB = 1024 * MB;
    public static long TB = 1024L * GB;

    // Time - How many in a second.
    public static int MiliSec = 1000;
    public static int MicroSec = 1000 * MiliSec;
    public static int NanoSec = 1000 * MicroSec;

    // Time - Relative to Sec.
    public static int Min = 60;
    public static int Hour = 60 * Min;
    public static int Day = 24 * Hour;

    private Metrics() {
        throw new RuntimeException("Cannot create instances of this class");
    }
}
