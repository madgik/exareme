/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionPlan.entity.*;
import madgik.exareme.worker.art.executionPlan.parser.expression.Destroy;
import madgik.exareme.worker.art.executionPlan.parser.expression.Start;
import madgik.exareme.worker.art.executionPlan.parser.expression.Stop;

import java.io.Serializable;

/**
 * This is the ExecutionPlan interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ExecutionPlan extends Serializable {

    ObjectType getType(String name) throws SemanticError;

    boolean isDefined(String name) throws SemanticError;

    int getPragmaCount();

    PragmaEntity getPragma(String pragmaName) throws SemanticError;

    Iterable<PragmaEntity> iteratePragmas();

    int getContainerCount();

    EntityName getContainer(String containerName) throws SemanticError;

    Iterable<String> iterateContainers();

    int getOperatorCount();

    OperatorEntity getOperator(String operatorName) throws SemanticError;

    Iterable<OperatorEntity> iterateOperators();

    Iterable<OperatorEntity> getFromLinks(OperatorEntity to) throws SemanticError;

    Iterable<OperatorEntity> getToLinks(OperatorEntity from) throws SemanticError;

    int getOperatorLinkCount();

    OperatorLinkEntity getOperatorLink(String from, String to) throws SemanticError;

    Iterable<OperatorLinkEntity> iterateOperatorLinks();

    int getStateCount();

    StateEntity getState(String stateName) throws SemanticError;

    Iterable<StateEntity> iterateStates();

    Iterable<StateEntity> getConnectedStates(String operatorName) throws SemanticError;

    Iterable<OperatorEntity> getConnectedOperators(String stateName) throws SemanticError;

    int getStateLinkCount();

    StateLinkEntity getStateLink(String operatorName, String stateName) throws SemanticError;

    Iterable<StateLinkEntity> iterateStateLinks();

    StartEntity createStartEntity(Start start) throws SemanticError;

    StopEntity createStopEntity(Stop stop) throws SemanticError;

    DestroyEntity createDestroyEntity(Destroy destroy) throws SemanticError;
}
