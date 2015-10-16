/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.deserializers;

import com.google.gson.*;
import madgik.exareme.worker.art.executionPlan.PlanExpressionConstants;
import madgik.exareme.worker.art.executionPlan.parser.expression.Operator;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author johnchronis
 */
public class OperatorDeserialiser implements JsonDeserializer<Operator> {

    @Override public Operator deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
        throws JsonParseException {
        final JsonObject jsonObject = je.getAsJsonObject();

        String operatorName = jsonObject.get(PlanExpressionConstants.NAME).getAsString();
        String operator = jsonObject.get(PlanExpressionConstants.OPERATOR).getAsString();
        String containerName = jsonObject.get(PlanExpressionConstants.CONTAINER).getAsString();
        String queryString = null;
        if (jsonObject.has(PlanExpressionConstants.QUERYSTRING)) {
            queryString = jsonObject.get(PlanExpressionConstants.QUERYSTRING).getAsString();
        }
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
        List<URL> locations = null;
        if (jsonObject.has(PlanExpressionConstants.LOCATIONS)) {
            JsonArray jlocations =
                jsonObject.get(PlanExpressionConstants.LOCATIONS).getAsJsonArray();
            for (JsonElement url : jlocations) {
                try {
                    locations.add(new URL(url.getAsString()));
                } catch (MalformedURLException ex) {
                    Logger.getLogger(OperatorDeserialiser.class.getName())
                        .log(Level.SEVERE, null, ex);
                }

            }
        }

        final Operator op =
            new Operator(operatorName, operator, paramList, queryString, containerName, null);
        return op;
    }
}
