package madgik.exareme.master.engine.dflSegment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author Vaggelis Nikolopoulos
 */
public class JsonBuilder {
    public static String toJson(Segment seg) {
        GsonBuilder gsonBuilder;
        Gson gson;

        gsonBuilder = new GsonBuilder();
        //gsonBuilder.registerTypeAdapter(Segment.class, new SegmentSerialiser());
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();

        return gson.toJson(seg);

    }

}
