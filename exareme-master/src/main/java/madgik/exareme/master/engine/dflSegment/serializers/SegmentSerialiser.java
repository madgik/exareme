package madgik.exareme.master.engine.dflSegment.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import madgik.exareme.master.engine.dflSegment.Segment;

import java.lang.reflect.Type;

/**
 *
 * @author Vaggelis Nikolopoulos
 */
public class SegmentSerialiser implements JsonSerializer<Segment> {

    @Override
    public JsonElement serialize(Segment seg, Type type, JsonSerializationContext jsc) {
        final JsonObject jsonObject = new JsonObject();

        final JsonElement jsonSubsegments = jsc.serialize(seg.getSubSegments());
        jsonObject.add("Subsegments", jsonSubsegments);

        final JsonElement jsonQueryScript = jsc.serialize(seg.getQueryScript());
        jsonObject.add("QScript", jsonQueryScript);

        final JsonElement jsonSQLScript = jsc.serialize(seg.getSQLScript());
        jsonObject.add("SQLScript", jsonSQLScript);

          final JsonElement jsonType = jsc.serialize(seg.getType());
          jsonObject.add("Type", jsonType);


        return jsonObject;
    }

}
