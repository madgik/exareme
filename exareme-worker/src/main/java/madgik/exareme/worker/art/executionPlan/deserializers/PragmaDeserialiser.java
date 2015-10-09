/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.deserializers;

import com.google.gson.*;
import madgik.exareme.worker.art.executionPlan.PlanExpressionConstants;
import madgik.exareme.worker.art.executionPlan.parser.expression.Pragma;

import java.lang.reflect.Type;

/**
 * @author johnchronis
 */
public class PragmaDeserialiser implements JsonDeserializer<Pragma> {

    @Override public Pragma deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
        throws JsonParseException {
        final JsonObject jsonObject = je.getAsJsonObject();

        String name = jsonObject.get(PlanExpressionConstants.NAME).getAsString();
        String value = jsonObject.get(PlanExpressionConstants.VALUE).getAsString();

        final Pragma pragma = new Pragma(name, value);

        return pragma;
    }
}
