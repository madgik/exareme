/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.http;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * @author heraldkllapi
 */
public class HttpUtils {

    public static void getValues(String content, Map<String, String> dict)
        throws UnsupportedEncodingException {
        if (!content.isEmpty()) {
            try {
                getValuesFromJDBC(content, dict);
            } catch (Exception e) {
                getValuesFromWeb(content, dict);
            }
        }
    }

    private static void getValuesFromJDBC(String content, Map<String, String> dict)
        throws UnsupportedEncodingException, IllegalStateException {
        Gson g = new Gson();
        Map<String, String> values = g.fromJson(content, Map.class);
        dict.putAll(values);
    }

    private static void getValuesFromWeb(String content, Map<String, String> dict)
        throws UnsupportedEncodingException {
        String[] parts = content.split("&");
        for (String p : parts) {
            int split = p.indexOf("=");
            String key = p.substring(0, split);
            String value = p.substring(split + 1, p.length());
            dict.put(key, normalize(value));
        }
    }

    private static String normalize(String in) throws UnsupportedEncodingException {
        return URLDecoder.decode(in, "UTF-8");
    }
}
