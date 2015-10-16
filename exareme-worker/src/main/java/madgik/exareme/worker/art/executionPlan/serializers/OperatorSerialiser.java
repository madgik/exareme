/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.serializers;

import com.google.gson.*;
import madgik.exareme.worker.art.executionPlan.PlanExpressionConstants;
import madgik.exareme.worker.art.executionPlan.parser.expression.Operator;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;

import java.lang.reflect.Type;
import java.net.URL;

/**
 * @author John Chronis
 */
public class OperatorSerialiser implements JsonSerializer<Operator> {

    @Override public JsonElement serialize(Operator op, Type type, JsonSerializationContext jsc) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(PlanExpressionConstants.CONTAINERNAME, op.containerName);
        jsonObject.addProperty(PlanExpressionConstants.OPERATOR, op.operator);
        jsonObject.addProperty(PlanExpressionConstants.OPERATORNAME, op.operatorName);
        if (op.queryString != null) {
            jsonObject.addProperty(PlanExpressionConstants.QUERYSTRING, op.queryString);
        }

        if (op.locations != null) {
            JsonParser parser = new JsonParser();
            JsonArray locations = new JsonArray();
            for (URL url : op.locations) {
                locations.add(parser.parse(url.toString()));
            }
            jsonObject.add(PlanExpressionConstants.LOCATIONS, locations);
        }

        if (op.paramList != null) {
            JsonParser parser = new JsonParser();
            JsonArray params = new JsonArray();
            for (Parameter param : op.paramList) {
                JsonArray p = new JsonArray();
                p.add(parser.parse(param.name));
                p.add(parser.parse(param.value));
                params.add(p);
            }
            jsonObject.add(PlanExpressionConstants.PARAMETERS, params);
        }

        return jsonObject;
    }

}
