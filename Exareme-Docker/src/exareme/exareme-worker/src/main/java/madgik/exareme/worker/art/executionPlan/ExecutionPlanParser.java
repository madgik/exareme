/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import madgik.exareme.worker.art.executionPlan.deserializers.*;
import madgik.exareme.worker.art.executionPlan.parser.expression.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author Herald Kllapi <br>
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ExecutionPlanParser {

    public ExecutionPlanParser() {
    }

    public ExecutionPlan parse(InputStream stream) throws IOException {
        String plan = IOUtils.toString(stream, "UTF-8");
        return parse(plan);
    }

    public ExecutionPlan parse(String plan) throws SemanticError {

        final GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(Container.class, new ContainerDeserialiser());
        gsonBuilder.registerTypeAdapter(PlanExpression.class, new ExecutionPlanDeserialiser());
        gsonBuilder.registerTypeAdapter(Operator.class, new OperatorDeserialiser());
        gsonBuilder.registerTypeAdapter(OperatorLink.class, new OperatorLinkDeserialiser());
        gsonBuilder.registerTypeAdapter(Pragma.class, new PragmaDeserialiser());

        final Gson gson = gsonBuilder.create();

        Reader reader = new StringReader(plan);
        final PlanExpression expression = gson.fromJson(reader, PlanExpression.class);

        return ExecutionPlanFactory.createEditableExecutionPlan(expression);
    }

}
