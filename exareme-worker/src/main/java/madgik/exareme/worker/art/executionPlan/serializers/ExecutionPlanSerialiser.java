/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import madgik.exareme.worker.art.executionPlan.PlanExpressionConstants;
import madgik.exareme.worker.art.executionPlan.parser.expression.PlanExpression;

import java.lang.reflect.Type;

/**
 * @author John Chronis
 */
public class ExecutionPlanSerialiser implements JsonSerializer<PlanExpression> {

    @Override
    public JsonElement serialize(PlanExpression planexp, Type type, JsonSerializationContext jsc) {
        final JsonObject jsonObject = new JsonObject();

        //    if (!(planexp.getBufferList().isEmpty())) {
        //      final JsonElement jsonBuffers = jsc.serialize(planexp.getBufferList());
        //      jsonObject.add(PlanExpressionConstants.BUFFERS, jsonBuffers);
        //    }
        //    if (!(planexp.getBufferLinkList().isEmpty())) {
        //      final JsonElement jsonBufferLinks = jsc.serialize(planexp.getBufferLinkList());
        //      jsonObject.add(PlanExpressionConstants.BUFFER_LINKS, jsonBufferLinks);
        //    }
        if (!(planexp.getContainerList().isEmpty())) {
            final JsonElement jsonContaines = jsc.serialize(planexp.getContainerList());
            jsonObject.add(PlanExpressionConstants.CONTAINERS, jsonContaines);
        }
        if (!(planexp.getOperatorList().isEmpty())) {
            final JsonElement jsonOperators = jsc.serialize(planexp.getOperatorList());
            jsonObject.add(PlanExpressionConstants.OPERATORS, jsonOperators);
        }
        if (!(planexp.getOperatorLinkList().isEmpty())) {
            final JsonElement jsonOperatorLinks = jsc.serialize(planexp.getOperatorLinkList());
            jsonObject.add(PlanExpressionConstants.OPERATOR_LINKS, jsonOperatorLinks);
        }
        if (!(planexp.getPragmaList().isEmpty())) {
            final JsonElement jsonPragma = jsc.serialize(planexp.getPragmaList());
            jsonObject.add(PlanExpressionConstants.PRAGMA, jsonPragma);
        }
        return jsonObject;
    }

}
