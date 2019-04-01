/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.deserializers;

import com.google.gson.*;
import madgik.exareme.worker.art.executionPlan.PlanExpressionConstants;
import madgik.exareme.worker.art.executionPlan.parser.expression.Container;

import java.lang.reflect.Type;

/**
 * @author johnchronis
 */
public class ContainerDeserialiser implements JsonDeserializer<Container> {

    @Override
    public Container deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
            throws JsonParseException {
        final JsonObject jsonObject = je.getAsJsonObject();

        String name = jsonObject.get(PlanExpressionConstants.NAME).getAsString();
        String ip = jsonObject.get(PlanExpressionConstants.IP).getAsString();
        int port = jsonObject.get(PlanExpressionConstants.PORT).getAsInt();
        int dataTransferPort = jsonObject.get(PlanExpressionConstants.DATATRANSFERPORT).getAsInt();

        final Container cont = new Container(name, ip, port, dataTransferPort);
        return cont;
    }

}
