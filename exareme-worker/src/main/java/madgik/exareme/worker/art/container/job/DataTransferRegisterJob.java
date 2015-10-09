/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobType;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;
import madgik.exareme.worker.art.parameter.Parameters;

import java.util.LinkedList;
import java.util.Map;

/**
 * @author John Chronis
 */
public class DataTransferRegisterJob implements ContainerJob {

    Map<String, LinkedList<Parameter>> linkMapParameters;
    String operatorName;
    Parameters parameters;
    PlanSessionReportID sessionReportID;

    public DataTransferRegisterJob(String operatorName,
        Map<String, LinkedList<Parameter>> linkMapParameters, Parameters parameters,
        PlanSessionReportID sessionReportID) {
        this.linkMapParameters = linkMapParameters;
        this.operatorName = operatorName;
        this.parameters = parameters;
        this.sessionReportID = sessionReportID;
    }

    @Override public ContainerJobType getType() {
        return ContainerJobType.dataTransferRegister;
    }

}
