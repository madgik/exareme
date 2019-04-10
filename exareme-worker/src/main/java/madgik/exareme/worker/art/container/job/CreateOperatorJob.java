/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.entity.OperatorImplementationEntity;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobType;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;
import madgik.exareme.worker.art.parameter.Parameters;

import java.util.LinkedList;
import java.util.Map;

/**
 * @author heraldkllapi
 */
public class CreateOperatorJob implements ContainerJob {
    public final String operatorName;
    public final String category;
    public final OperatorType type;
    public final OperatorImplementationEntity operator;
    public final Parameters parameters;
    public final Map<String, LinkedList<Parameter>> linkMapParameters;
    public final String queryString;
    public final PlanSessionReportID sessionReportID;
    public final ContainerSessionID contSessionID;
    //public final int outputLinks;

    public CreateOperatorJob(String operatorName, String category, OperatorType type,
                             OperatorImplementationEntity operator, Parameters parameters,
                             Map<String, LinkedList<Parameter>> linkMapParameters, String queryString,
                             PlanSessionReportID sessionReportID, ContainerSessionID containerSessionID) {
        this.operatorName = operatorName;
        this.category = category;
        this.type = type;
        this.operator = operator;
        this.parameters = parameters;
        this.linkMapParameters = linkMapParameters;
        this.queryString = queryString;
        this.sessionReportID = sessionReportID;
        this.contSessionID = containerSessionID;
    }

    @Override
    public ContainerJobType getType() {
        return ContainerJobType.createOperator;
    }
}
