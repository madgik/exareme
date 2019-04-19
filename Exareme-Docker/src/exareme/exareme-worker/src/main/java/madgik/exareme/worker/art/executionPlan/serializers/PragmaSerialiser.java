/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import madgik.exareme.worker.art.executionPlan.PlanExpressionConstants;
import madgik.exareme.worker.art.executionPlan.parser.expression.Pragma;

import java.lang.reflect.Type;

/**
 * @author John Chronis
 */
public class PragmaSerialiser implements JsonSerializer<Pragma> {

    @Override
    public JsonElement serialize(Pragma pragma, Type type, JsonSerializationContext jsc) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(PlanExpressionConstants.PRAGMANAME, pragma.pragmaName);
        jsonObject.addProperty(PlanExpressionConstants.PRAGMAVALUE, pragma.pragmaValue);

        return jsonObject;
    }

}
