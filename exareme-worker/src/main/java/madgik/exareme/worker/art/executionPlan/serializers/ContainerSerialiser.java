/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import madgik.exareme.worker.art.executionPlan.PlanExpressionConstants;
import madgik.exareme.worker.art.executionPlan.parser.expression.Container;

import java.lang.reflect.Type;

/**
 * @author John Chronis
 * @author Vaggelis Nikolopoulos
 */
public class ContainerSerialiser implements JsonSerializer<Container> {

    @Override
    public JsonElement serialize(Container container, Type type, JsonSerializationContext jsc) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(PlanExpressionConstants.NAME, container.name);
        jsonObject.addProperty(PlanExpressionConstants.IP, container.ip);
        jsonObject.addProperty(PlanExpressionConstants.PORT, container.port);
        jsonObject
                .addProperty(PlanExpressionConstants.DATATRANSFERPORT, container.dataTransferPort);

        return jsonObject;
    }
}
