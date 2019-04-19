package madgik.exareme.utils.encoding;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author herald
 */
public class JSONTest {
    private static long bytes = 0;

    public static void main(String[] args) throws Exception {
        Gson gson = new Gson();
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        map.put("key", new HashMap<String, String>());
        map.get("key").put("another_key", "value");

        System.out.println(gson.toJson(new ArrayList<String>()));

        System.exit(1);

        ObjectOutputStream outStream = new ObjectOutputStream(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                bytes++;
            }
        });

        long times = 10000000;

        long start = System.currentTimeMillis();
        for (long i = 0; i < times; ++i) {
            bytes += gson.toJson(map).length();
            //      outStream.writeUnshared(map);
        }
        long end = System.currentTimeMillis();

        System.out.println((times / ((end - start) / 1000.0)) + " objects / sec");
        System.out.println(bytes + " bytes");
    }
}
