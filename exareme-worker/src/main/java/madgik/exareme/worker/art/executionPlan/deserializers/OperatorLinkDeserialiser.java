/**
 * Copyright MaDgIK Group 2010 - 2015.
 */

package madgik.exareme.worker.art.executionPlan.deserializers;

import com.google.gson.*;
import madgik.exareme.worker.art.executionPlan.PlanExpressionConstants;
import madgik.exareme.worker.art.executionPlan.parser.expression.OperatorLink;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;

import java.lang.reflect.Type;
import java.util.LinkedList;

/**
 * @author Vaggelis Nikolopoulos
 */
public class OperatorLinkDeserialiser implements JsonDeserializer<OperatorLink> {

    @Override
    public OperatorLink deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
        throws JsonParseException {
        final JsonObject jsonObject = je.getAsJsonObject();

        String containerName = null;
        if (jsonObject.has(PlanExpressionConstants.CONTAINERNAME)) {
            containerName = jsonObject.get(PlanExpressionConstants.CONTAINERNAME).getAsString();
        }
        String from = jsonObject.get(PlanExpressionConstants.FROM).getAsString();
        String to = jsonObject.get(PlanExpressionConstants.TO).getAsString();
        LinkedList<Parameter> paramList = null;

        if (jsonObject.has(PlanExpressionConstants.PARAMETERS)) {
            JsonArray parameters =
                jsonObject.get(PlanExpressionConstants.PARAMETERS).getAsJsonArray();
            paramList = new LinkedList<>();
            JsonArray param;
            for (JsonElement jee : parameters) {
                param = jee.getAsJsonArray();
                paramList
                    .add(new Parameter(param.get(0).getAsString(), param.get(1).getAsString()));
            }
        }

        final OperatorLink operatorLink = new OperatorLink(from, to, containerName, paramList);

        return operatorLink;
    }

}
