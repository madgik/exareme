/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.deserializers;

import com.google.gson.*;
import madgik.exareme.worker.art.executionPlan.PlanExpressionConstants;
import madgik.exareme.worker.art.executionPlan.parser.expression.*;

import java.lang.reflect.Type;

/**
 * @author johnchronis
 */
public class ExecutionPlanDeserialiser implements JsonDeserializer<PlanExpression> {

    @Override
    public PlanExpression deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
            throws JsonParseException {
        final JsonObject jsonObject = je.getAsJsonObject();

        //    Buffer[] buffers = jdc.deserialize(
        //      jsonObject.get(PlanExpressionConstants.BUFFERS), Buffer[].class);
        //    BufferLink[] bufferLinks = jdc.deserialize(
        //      jsonObject.get(PlanExpressionConstants.BUFFER_LINKS), BufferLink[].class);
        Container[] containers =
                jdc.deserialize(jsonObject.get(PlanExpressionConstants.CONTAINERS), Container[].class);
        Operator[] operators =
                jdc.deserialize(jsonObject.get(PlanExpressionConstants.OPERATORS), Operator[].class);
        OperatorLink[] operatorLinks =
                jdc.deserialize(jsonObject.get(PlanExpressionConstants.OPERATOR_LINKS),
                        OperatorLink[].class);
        Pragma[] pragma =
                jdc.deserialize(jsonObject.get(PlanExpressionConstants.PRAGMA), Pragma[].class);

        final PlanExpression expression = new PlanExpression();
        //    if (buffers != null) {
        //      for (Buffer bf : buffers) {
        //        expression.addBuffer(bf);
        //      }
        //    }
        //    if (bufferLinks != null) {
        //      for (BufferLink bflk : bufferLinks) {
        //        expression.addBufferConnect(bflk);
        //      }
        //    }
        if (containers != null) {
            for (Container cont : containers) {
                expression.addContainer(cont);
            }
        }
        if (operatorLinks != null) {
            for (OperatorLink bflk : operatorLinks) {
                expression.addOperatorConnect(bflk);
            }
        }
        if (operators != null) {
            for (Operator op : operators) {
                expression.addOperator(op);
            }
        }
        if (pragma != null) {
            for (Pragma pr : pragma) {
                expression.addPragma(pr);
            }
        }
        return expression;
    }

}
