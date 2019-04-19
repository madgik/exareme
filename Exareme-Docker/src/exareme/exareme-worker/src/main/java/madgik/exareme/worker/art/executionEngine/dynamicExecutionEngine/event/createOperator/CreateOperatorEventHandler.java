/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createOperator;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.entity.OperatorImplementationEntity;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.job.CreateBufferJobResult;
import madgik.exareme.worker.art.container.job.CreateOperatorJob;
import madgik.exareme.worker.art.container.job.CreateOperatorJobResult;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.parameter.Parameter;
import madgik.exareme.worker.art.parameter.Parameters;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class CreateOperatorEventHandler {
    //        implements ExecEngineEventHandler<CreateOperatorEvent> {

    public static final CreateOperatorEventHandler instance = new CreateOperatorEventHandler();
    private static final long serialVersionUID = 1L;

    public CreateOperatorEventHandler() {
    }

    //  @Override
    public void preProcess(CreateOperatorEvent event, PlanEventSchedulerState state)
            throws RemoteException {
        String operatorName;
        if (event.operator != null) {
            operatorName = event.operator.operatorName;
        } else {
            operatorName = event.operatorEntity.operatorName;
        }
        event.activeOperator = state.getActiveOperator(operatorName);
        event.activeGroup = event.activeOperator.operatorGroup.objectNameActiveGroupMap
                .get(event.activeOperator.objectName);

        OperatorEntity operatorEntity = event.operatorEntity;
        if (operatorEntity == null) {
            operatorEntity =
                    event.activeGroup.planSession.getExecutionPlan().addOperator(event.operator);
        }
        Parameters params = new Parameters();
        for (madgik.exareme.worker.art.executionPlan.parser.expression.Parameter param : operatorEntity.paramList) {
            params.addParameter(new Parameter(param.name, param.value));
        }


        OperatorImplementationEntity implEntity =
                new OperatorImplementationEntity(operatorEntity.operator, operatorEntity.locations);

        ContainerSessionID containerSessionID = event.activeOperator.containerSessionID;
        event.session = state.getContainerSession(operatorEntity.containerName, containerSessionID);
        event.jobs = new ContainerJobs();
        event.jobs.addJob(
                new CreateOperatorJob(operatorEntity.operatorName, operatorEntity.category,
                        operatorEntity.type, implEntity, params, operatorEntity.linksparams,
                        operatorEntity.queryString, state.getPlanSessionReportID(), containerSessionID));

    }

    //  @Override
    //  public void handle(CreateOperatorEvent event,
    //                     EventProcessor proc) throws RemoteException {
    //    event.results = event.session.execJobs(event.jobs);
    //    event.messageCount = 1;
    //  }
    //  @Override
    public void postProcess(CreateOperatorEvent event, PlanEventSchedulerState state)
            throws RemoteException {
        ConcreteOperatorID opID =
                ((CreateOperatorJobResult) event.results.getJobResults().get(0)).opID;
        event.activeGroup.planSession.getOperatorIdMap().put(event.operatorEntity, opID);
        event.activeGroup.planSession.getOperatorIdEntityMap().put(opID, event.operatorEntity);
        state.addActiveOperator(opID, event.activeOperator);
        state.getStatistics().incrOperatorsInstantiated();
        if (event.activeOperator.operatorEntity.type == OperatorType.processing) {
            state.getStatistics().IncrProcessingOperatorsInstantiated();
        }
        state.getStatistics().incrControlMessagesCountBy(event.messageCount);

        CreateBufferJobResult bjr =
                ((CreateOperatorJobResult) event.results.getJobResults().get(0)).bufferJobResult;
        if (bjr != null) {
            event.activeGroup.planSession.getBufferIdMap()
                    .put(event.operatorEntity, bjr.operatorIDToBufferId);
            state.getStatistics().incrBuffersCreated();
            state.getStatistics().incrControlMessagesCountBy(event.messageCount);
        }

    }
}
