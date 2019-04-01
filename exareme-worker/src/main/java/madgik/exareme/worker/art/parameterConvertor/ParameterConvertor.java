package madgik.exareme.worker.art.parameterConvertor;

import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;
import madgik.exareme.worker.art.parameter.Parameters;

import java.util.LinkedList;

/**
 * Created by johnchronis on 2/3/2015.
 */
public class ParameterConvertor {
    public static Parameters convert(LinkedList<Parameter> params) {
        madgik.exareme.worker.art.parameter.Parameters paramsret =
                new madgik.exareme.worker.art.parameter.Parameters();
        for (madgik.exareme.worker.art.executionPlan.parser.expression.Parameter p : params) {
            paramsret
                    .addParameter(new madgik.exareme.worker.art.parameter.Parameter(p.name, p.value));
        }
        return paramsret;
    }
}
