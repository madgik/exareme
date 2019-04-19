package madgik.exareme.worker.art.executionPlan;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionPlan.entity.*;
import madgik.exareme.worker.art.executionPlan.parser.expression.*;

/**
 * @author herald
 */
public interface EditableExecutionPlan extends ExecutionPlan {

    PragmaEntity addPragma(Pragma p) throws SemanticError;

    PragmaEntity removePragma(String pragmaName) throws SemanticError;

    EntityName addContainer(Container c) throws SemanticError;

    EntityName removeContainer(String containerName) throws SemanticError;

    OperatorEntity addOperator(Operator o) throws SemanticError;

    OperatorEntity removeOperator(String operatorName) throws SemanticError;

    OperatorLinkEntity addOperatorLink(OperatorLink opLink) throws SemanticError;

    OperatorLinkEntity removeOperatorLink(String from, String to) throws SemanticError;

    StateEntity addState(State s) throws SemanticError;

    StateEntity removeState(String stateName) throws SemanticError;

    StateLinkEntity addStateLink(StateLink sl) throws SemanticError;

    StateLinkEntity removeStateLink(String operatorName, String stateName) throws SemanticError;

    SwitchEntity addSwitch(Switch s) throws SemanticError;

    SwitchEntity removeSwitch(Switch s) throws SemanticError;

    SwitchLinkEntity addSwitchConnect(SwitchLink sl) throws SemanticError;

    SwitchLinkEntity removeSwitchConnect(SwitchLink sl) throws SemanticError;

    void setDataTransferOperatorsCount(int dataTransferOperatorsCount);

    int getDataTransferOperatorsCount();
}
