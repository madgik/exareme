/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import madgik.exareme.worker.art.executionPlan.parser.expression.*;
import madgik.exareme.worker.art.executionPlan.serializers.*;

/**
 * @author John Chronis
 * @author Vaggelis Nikolopoulos
 */
public class JsonBuilder {

    final GsonBuilder gsonBuilder;
    final Gson gson;

    public JsonBuilder() {
        gsonBuilder = new GsonBuilder();
        //    gsonBuilder.registerTypeAdapter(Buffer.class, new BufferSerialiser());
        //    gsonBuilder.registerTypeAdapter(BufferLink.class, new BufferLinkSerialiser());
        gsonBuilder.registerTypeAdapter(Container.class, new ContainerSerialiser());
        gsonBuilder.registerTypeAdapter(PlanExpression.class, new ExecutionPlanSerialiser());
        gsonBuilder.registerTypeAdapter(Operator.class, new OperatorSerialiser());
        gsonBuilder.registerTypeAdapter(OperatorLink.class, new OperatorLinkSerialiser());
        gsonBuilder.registerTypeAdapter(Pragma.class, new PragmaSerialiser());
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
    }

    public String parse(PlanExpression ex) {
        return gson.toJson(ex);
    }

}
