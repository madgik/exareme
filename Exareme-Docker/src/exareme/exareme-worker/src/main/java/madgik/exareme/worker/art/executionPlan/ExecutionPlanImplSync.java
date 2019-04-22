/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionPlan.entity.*;
import madgik.exareme.worker.art.executionPlan.parser.expression.*;

/**
 * @author herald
 */
public class ExecutionPlanImplSync implements EditableExecutionPlan {

    private static final long serialVersionUID = 1L;
    private final EditableExecutionPlan plan;

    public ExecutionPlanImplSync() {
        plan = new ExecutionPlanImpl();
    }

    public ExecutionPlanImplSync(PlanExpression expression) throws SemanticError {
        plan = new ExecutionPlanImpl(expression);
    }

    @Override
    public ObjectType getType(String name) throws SemanticError {
        synchronized (plan) {
            return plan.getType(name);
        }
    }

    @Override
    public EntityName addContainer(Container c) throws SemanticError {
        synchronized (plan) {
            return plan.addContainer(c);
        }
    }

    @Override
    public EntityName removeContainer(String containerName) throws SemanticError {
        synchronized (plan) {
            return plan.removeContainer(containerName);
        }
    }

    @Override
    public OperatorEntity addOperator(Operator operator) throws SemanticError {
        synchronized (plan) {
            return plan.addOperator(operator);
        }
    }

    @Override
    public OperatorEntity removeOperator(String operatorName) throws SemanticError {
        synchronized (plan) {
            return plan.removeOperator(operatorName);
        }
    }

    @Override
    public StateEntity addState(State state) throws SemanticError {
        synchronized (plan) {
            return plan.addState(state);
        }
    }

    @Override
    public StateEntity removeState(String stateName) throws SemanticError {
        synchronized (plan) {
            return plan.removeState(stateName);
        }
    }

    @Override
    public StateLinkEntity addStateLink(StateLink statelink) throws SemanticError {
        synchronized (plan) {
            return plan.addStateLink(statelink);
        }
    }

    @Override
    public StateLinkEntity removeStateLink(String operatorName, String stateName)
            throws SemanticError {
        synchronized (plan) {
            return plan.removeStateLink(operatorName, stateName);
        }
    }

    @Override
    public SwitchEntity addSwitch(Switch s) throws SemanticError {
        synchronized (plan) {
            return plan.addSwitch(s);
        }
    }

    @Override
    public SwitchEntity removeSwitch(Switch s) throws SemanticError {
        synchronized (plan) {
            return plan.removeSwitch(s);
        }
    }

    @Override
    public SwitchLinkEntity addSwitchConnect(SwitchLink switchConnect)
            throws SemanticError {
        synchronized (plan) {
            return plan.addSwitchConnect(switchConnect);
        }
    }

    @Override
    public SwitchLinkEntity removeSwitchConnect(SwitchLink switchConnect)
            throws SemanticError {
        synchronized (plan) {
            return plan.removeSwitchConnect(switchConnect);
        }
    }

    @Override
    public void setDataTransferOperatorsCount(int dataTransferOperatorsCount) {
        synchronized (plan) {
            plan.setDataTransferOperatorsCount(dataTransferOperatorsCount);
        }
    }

    @Override
    public int getDataTransferOperatorsCount() {
        synchronized (plan) {
            return plan.getDataTransferOperatorsCount();
        }
    }

    @Override
    public StartEntity createStartEntity(Start start) throws SemanticError {
        synchronized (plan) {
            return plan.createStartEntity(start);
        }
    }

    @Override
    public StopEntity createStopEntity(Stop stop) throws SemanticError {
        synchronized (plan) {
            return plan.createStopEntity(stop);
        }
    }

    @Override
    public DestroyEntity createDestroyEntity(Destroy destroy) throws SemanticError {
        synchronized (plan) {
            return plan.createDestroyEntity(destroy);
        }
    }

    @Override
    public int getContainerCount() {
        synchronized (plan) {
            return plan.getContainerCount();
        }
    }

    @Override
    public int getOperatorCount() {
        synchronized (plan) {
            return plan.getOperatorCount();
        }
    }


    @Override
    public OperatorEntity getOperator(String operatorName) throws SemanticError {
        synchronized (plan) {
            return plan.getOperator(operatorName);
        }
    }

    @Override
    public StateEntity getState(String stateName) throws SemanticError {
        synchronized (plan) {
            return plan.getState(stateName);
        }
    }

    @Override
    public StateLinkEntity getStateLink(String operatorName, String stateName)
            throws SemanticError {
        synchronized (plan) {
            return plan.getStateLink(operatorName, stateName);
        }
    }

    @Override
    public Iterable<OperatorEntity> getFromLinks(OperatorEntity to) throws SemanticError {
        synchronized (plan) {
            return plan.getFromLinks(to);
        }
    }

    @Override
    public Iterable<OperatorEntity> getToLinks(OperatorEntity from) throws SemanticError {
        synchronized (plan) {
            return plan.getToLinks(from);
        }
    }

    @Override
    public Iterable<StateEntity> getConnectedStates(String operatorName)
            throws SemanticError {
        synchronized (plan) {
            return plan.getConnectedStates(operatorName);
        }
    }

    @Override
    public Iterable<OperatorEntity> getConnectedOperators(String stateName)
            throws SemanticError {
        synchronized (plan) {
            return plan.getConnectedOperators(stateName);
        }
    }


    @Override
    public Iterable<OperatorEntity> iterateOperators() {
        synchronized (plan) {
            return plan.iterateOperators();
        }
    }


    @Override
    public Iterable<StateEntity> iterateStates() {
        synchronized (plan) {
            return plan.iterateStates();
        }
    }

    @Override
    public Iterable<StateLinkEntity> iterateStateLinks() {
        synchronized (plan) {
            return plan.iterateStateLinks();
        }
    }


    @Override
    public boolean isDefined(String name) throws SemanticError {
        synchronized (plan) {
            return plan.isDefined(name);
        }
    }

    @Override
    public EntityName getContainer(String containerName) throws SemanticError {
        synchronized (plan) {
            return plan.getContainer(containerName);
        }
    }

    @Override
    public Iterable<String> iterateContainers() {
        synchronized (plan) {
            return plan.iterateContainers();
        }
    }

    @Override
    public PragmaEntity addPragma(Pragma p) throws SemanticError {
        synchronized (plan) {
            return plan.addPragma(p);
        }
    }

    @Override
    public int getPragmaCount() {
        synchronized (plan) {
            return plan.getPragmaCount();
        }
    }

    @Override
    public Iterable<PragmaEntity> iteratePragmas() {
        synchronized (plan) {
            return plan.iteratePragmas();
        }
    }

    @Override
    public PragmaEntity getPragma(String pragmaName) throws SemanticError {
        synchronized (plan) {
            return plan.getPragma(pragmaName);
        }
    }

    @Override
    public PragmaEntity removePragma(String pragmaName) throws SemanticError {
        synchronized (plan) {
            return plan.removePragma(pragmaName);
        }
    }

    @Override
    public int getStateCount() {
        synchronized (plan) {
            return plan.getStateCount();
        }
    }

    @Override
    public int getStateLinkCount() {
        synchronized (plan) {
            return plan.getStateLinkCount();
        }
    }

    @Override
    public int getOperatorLinkCount() {
        synchronized (plan) {
            return plan.getOperatorLinkCount();
        }
    }

    @Override
    public OperatorLinkEntity getOperatorLink(String from, String to)
            throws SemanticError {
        synchronized (plan) {
            return plan.getOperatorLink(from, to);
        }
    }

    @Override
    public Iterable<OperatorLinkEntity> iterateOperatorLinks() {
        synchronized (plan) {
            return plan.iterateOperatorLinks();
        }
    }

    @Override
    public OperatorLinkEntity addOperatorLink(OperatorLink opLink) throws SemanticError {
        synchronized (plan) {
            return plan.addOperatorLink(opLink);
        }
    }

    @Override
    public OperatorLinkEntity removeOperatorLink(String from, String to)
            throws SemanticError {
        synchronized (plan) {
            return plan.removeOperatorLink(from, to);
        }
    }


}
