/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.serializers;

import com.google.gson.*;
import madgik.exareme.worker.art.executionPlan.PlanExpressionConstants;
import madgik.exareme.worker.art.executionPlan.parser.expression.OperatorLink;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;

import java.lang.reflect.Type;

/**
 * @author John Chronis
 * @author Vaggelis Nikolopoulos
 */
public class OperatorLinkSerialiser implements JsonSerializer<OperatorLink> {

    @Override
    public JsonElement serialize(OperatorLink link, Type type, JsonSerializationContext jsc) {
        JsonObject jsonObject = new JsonObject();
        if (link.containerName != null) {
            jsonObject.addProperty(PlanExpressionConstants.CONTAINER, link.containerName);
        }
        jsonObject.addProperty(PlanExpressionConstants.FROM, link.from);
        jsonObject.addProperty(PlanExpressionConstants.TO, link.to);

        if (link.paramList != null) {
            JsonParser parser = new JsonParser();
            JsonArray params = new JsonArray();
            for (Parameter param : link.paramList) {
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
