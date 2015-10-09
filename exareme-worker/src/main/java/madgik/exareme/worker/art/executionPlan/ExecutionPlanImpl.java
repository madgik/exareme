/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.graph.DirectedGraph;
import madgik.exareme.utils.graph.HashDirectedGraph;
import madgik.exareme.utils.graph.edge.Edge;
import madgik.exareme.utils.graph.edge.UnweightedEdge;
import madgik.exareme.worker.art.executionPlan.entity.*;
import madgik.exareme.worker.art.executionPlan.parser.expression.*;
import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Herald Kllapi <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ExecutionPlanImpl implements EditableExecutionPlan {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ExecutionPlanImpl.class);
    private Map<String, ObjectType> nameTypeMap = new LinkedHashMap<String, ObjectType>();
    private Map<String, PragmaEntity> pragmaEntityName = new LinkedHashMap<String, PragmaEntity>();
    private Map<String, EntityName> containerEntityNames = new LinkedHashMap<String, EntityName>();
    private Map<String, OperatorEntity> concreteOpMap = new LinkedHashMap<String, OperatorEntity>();
    private Map<String, OperatorLinkEntity> operatorLinkMap =
        new LinkedHashMap<String, OperatorLinkEntity>();
    private Map<String, StateEntity> stateMap = new LinkedHashMap<String, StateEntity>();
    private Map<String, StateLinkEntity> stateLinkMap =
        new LinkedHashMap<String, StateLinkEntity>();
    private Map<String, SwitchEntity> switchMap = new LinkedHashMap<String, SwitchEntity>();
    private Map<String, SwitchLinkEntity> switchLinkMap =
        new LinkedHashMap<String, SwitchLinkEntity>();
    private DirectedGraph<String, Edge<String>> operatorEntityGraph =
        new HashDirectedGraph<String, Edge<String>>();

    // Operator to state map
    private Map<String, Map<String, StateEntity>> opStateMap =
        new LinkedHashMap<String, Map<String, StateEntity>>();
    // State to operator map
    private Map<String, Map<String, OperatorEntity>> stateOpMap =
        new LinkedHashMap<String, Map<String, OperatorEntity>>();

    public ExecutionPlanImpl() {
    }

    public ExecutionPlanImpl(PlanExpression expression) throws SemanticError {
        for (Pragma prag : expression.pragmaList) {
            addPragma(prag);
        }
        for (Container cont : expression.containersList) {
            addContainer(cont);
        }
        for (Operator op : expression.operatorList) {
            addOperator(op);
        }

        for (Switch s : expression.switchList) {
            addSwitch(s);
        }

        for (State state : expression.stateList) {
            addState(state);
        }
        for (OperatorLink opLink : expression.operatorConnectList) {
            addOperatorLink(opLink);
        }
        for (StateLink stateLink : expression.stateLinkList) {
            addStateLink(stateLink);
        }
    }

    @Override public ObjectType getType(String name) throws SemanticError {
        ObjectType type = nameTypeMap.get(name);
        if (type == null) {
            throw new SemanticError("Name not found: " + name);
        }
        return type;
    }

    @Override public boolean isDefined(String name) throws SemanticError {
        return nameTypeMap.containsKey(name);
    }

    private void checkExistence(String name) throws SemanticError {
        ObjectType type = nameTypeMap.get(name);
        if (type != null) {
            throw new SemanticError("Name already defined as " + type + ": " + name);
        }
    }

    private void checkExistence(String name, ObjectType ofType) throws SemanticError {
        ObjectType type = nameTypeMap.get(name);
        if (type == null) {
            throw new SemanticError("Name not found: " + name);
        }

        if (type.equals(ofType) == false) {
            throw new SemanticError(
                "Name defined as " + type + " and not as " + ofType + ": " + name);
        }
    }

    @Override public final EntityName addContainer(Container cont) throws SemanticError {
        checkExistence(cont.name);
        nameTypeMap.put(cont.name, ObjectType.Container);
        EntityName entityName =
            new EntityName(cont.ip, cont.ip, cont.port, cont.getDataTransferPort());
        containerEntityNames.put(cont.name, entityName);
        return entityName;
    }

    @Override public EntityName removeContainer(String containerName) throws SemanticError {
        checkExistence(containerName, ObjectType.Container);
        nameTypeMap.remove(containerName);
        EntityName entityName = containerEntityNames.remove(containerName);
        return entityName;
    }

    @Override public final OperatorEntity addOperator(Operator operator) throws SemanticError {
        checkExistence(operator.operatorName);
        checkExistence(operator.containerName, ObjectType.Container);
        nameTypeMap.put(operator.operatorName, ObjectType.Operator);
        EntityName container = containerEntityNames.get(operator.containerName);

        OperatorEntity opEntity =
            new OperatorEntity(operator.operatorName, operator.operator, operator.paramList,
                operator.queryString, operator.locations, operator.containerName, container,
                operator.linksparams);

        concreteOpMap.put(operator.operatorName, opEntity);
        operatorEntityGraph.addVertex(opEntity.operatorName);

        return opEntity;
    }

    @Override public OperatorEntity removeOperator(String operatorName) throws SemanticError {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override public final OperatorLinkEntity addOperatorLink(OperatorLink opLink)
        throws SemanticError {
        log.trace(" ~~~~ ADD: " + opLink.from + "->" + opLink.to);
        String linkName = opLink.from + ":" + opLink.to;
        checkExistence(opLink.containerName, ObjectType.Container);
        checkExistence(linkName);

        checkExistence(opLink.from, ObjectType.Operator);
        checkExistence(opLink.to, ObjectType.Operator);

        nameTypeMap.put(linkName, ObjectType.OperatorConnect);

        EntityName container = containerEntityNames.get(opLink.containerName);
        OperatorEntity fromOperator = concreteOpMap.get(opLink.from);
        OperatorEntity toOperator = concreteOpMap.get(opLink.to);

        OperatorLinkEntity opLinkEntity = null;
        OperatorLinkEntity.ConnectType linkType = null;

        if (fromOperator.container.equals(toOperator.container)) {
            linkType = OperatorLinkEntity.ConnectType.local;
        } else {
            linkType = OperatorLinkEntity.ConnectType.remote;
        }

        opLinkEntity =
            new OperatorLinkEntity(fromOperator, toOperator, linkType, opLink.containerName,
                container, opLink.paramList);

        operatorLinkMap.put(linkName, opLinkEntity);
        Pair<OperatorEntity, OperatorEntity> inputOutput = new Pair<>(fromOperator, toOperator);

        OperatorEntity from = inputOutput.a;
        OperatorEntity to = inputOutput.b;
        String fromToName = from.operatorName + ":" + to.operatorName;

        operatorEntityGraph.addEdge(new UnweightedEdge<String>(from.operatorName, to.operatorName));
        return opLinkEntity;
    }


    @Override public OperatorLinkEntity removeOperatorLink(String fromName, String toName)
        throws SemanticError {
        log.trace(" ~~~~ REMOVE: " + fromName + "->" + toName);
        String linkName = fromName + ":" + toName;
        checkExistence(linkName, ObjectType.OperatorConnect);
        nameTypeMap.remove(linkName);
        OperatorLinkEntity link = operatorLinkMap.remove(linkName);

        String fromToName = link.fromOperator.operatorName + ":" + link.toOperator.operatorName;
        if (operatorEntityGraph
            .removeEdge(link.fromOperator.operatorName, link.toOperator.operatorName) == null) {
            throw new SemanticError("Edge not found: " + link.fromOperator.operatorName + " -> "
                + link.toOperator.operatorName);
        }
        operatorLinkMap.remove(linkName);
        return link;
    }


    @Override public Iterable<OperatorEntity> iterateOperators() {
        return concreteOpMap.values();
    }

    @Override public int getOperatorCount() {
        return concreteOpMap.size();
    }

    public int getOperatorLink() {
        return this.operatorLinkMap.size();
    }

    @Override public int getContainerCount() {
        return containerEntityNames.size();
    }

    @Override public OperatorEntity getOperator(String operatorName) throws SemanticError {
        checkExistence(operatorName, ObjectType.Operator);
        return concreteOpMap.get(operatorName);
    }

    public OperatorLinkEntity getOperatorLink(String from, String to) throws SemanticError {
        String linkName = from + ":" + to;
        checkExistence(linkName, ObjectType.OperatorConnect);
        return operatorLinkMap.get(linkName);
    }

    public EntityName[] getContainerEntities() {
        return containerEntityNames.values().toArray(new EntityName[] {});
    }

    @Override public StartEntity createStartEntity(Start start) throws SemanticError {
        checkExistence(start.containerName, ObjectType.Container);
        checkExistence(start.operatorName, ObjectType.Operator);
        EntityName container = containerEntityNames.get(start.containerName);
        OperatorEntity operatorEntity = concreteOpMap.get(start.operatorName);
        return new StartEntity(start.operatorName, operatorEntity, start.containerName, container);
    }

    @Override public StopEntity createStopEntity(Stop stop) throws SemanticError {
        checkExistence(stop.containerName, ObjectType.Container);
        checkExistence(stop.operatorName, ObjectType.Operator);
        EntityName container = containerEntityNames.get(stop.containerName);
        OperatorEntity operatorEntity = concreteOpMap.get(stop.operatorName);
        return new StopEntity(stop.operatorName, operatorEntity, stop.containerName, container);
    }

    @Override public DestroyEntity createDestroyEntity(Destroy destroy) throws SemanticError {
        checkExistence(destroy.containerName, ObjectType.Container);
        ObjectType type = nameTypeMap.get(destroy.objectName);
        if (type == null) {
            throw new SemanticError("Object not found!");
        }
        EntityName container = containerEntityNames.get(destroy.containerName);
        DestroyEntity destroyEntity = null;
        switch (type) {
            case Operator: {
                OperatorEntity operatorEntity = getOperator(destroy.objectName);

                destroyEntity =
                    new DestroyEntity(destroy.objectName, operatorEntity, destroy.containerName,
                        container);
                break;
            }
        }

        return destroyEntity;
    }

    @Override public Iterable<OperatorEntity> getFromLinks(OperatorEntity to) throws SemanticError {
        checkExistence(to.containerName, ObjectType.Container);
        checkExistence(to.operatorName, ObjectType.Operator);

        LinkedList<OperatorEntity> fromLinks = new LinkedList<OperatorEntity>();
        for (Edge<String> edge : operatorEntityGraph.incomingEdgesSet(to.operatorName)) {
            String opName = edge.getSourceVertex();
            fromLinks.add(getOperator(opName));
        }
        return fromLinks;
    }

    @Override public Iterable<OperatorEntity> getToLinks(OperatorEntity from) throws SemanticError {
        checkExistence(from.containerName, ObjectType.Container);
        checkExistence(from.operatorName, ObjectType.Operator);

        LinkedList<OperatorEntity> toLinks = new LinkedList<OperatorEntity>();
        for (Edge<String> edge : operatorEntityGraph.outgoingEdgesSet(from.operatorName)) {
            String opName = edge.getTargetVertex();
            toLinks.add(getOperator(opName));
        }
        return toLinks;
    }

    @Override public final StateEntity addState(State state) throws SemanticError {
        checkExistence(state.stateName);
        checkExistence(state.containerName, ObjectType.Container);
        nameTypeMap.put(state.stateName, ObjectType.State);
        EntityName container = containerEntityNames.get(state.containerName);

        StateEntity entity =
            new StateEntity(state.stateName, state.state, state.paramList, state.queryString,
                state.locations, state.containerName, container);

        stateMap.put(state.stateName, entity);
        return entity;
    }

    @Override public StateEntity removeState(String stateName) throws SemanticError {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override public final StateLinkEntity addStateLink(StateLink stateLink) throws SemanticError {
        String stateLinkName = stateLink.operatorName + ":" + stateLink.stateName;
        checkExistence(stateLinkName);
        checkExistence(stateLink.containerName, ObjectType.Container);
        checkExistence(stateLink.operatorName, ObjectType.Operator);
        checkExistence(stateLink.stateName, ObjectType.State);
        nameTypeMap.put(stateLinkName, ObjectType.StateLink);
        EntityName container = containerEntityNames.get(stateLink.containerName);
        OperatorEntity operatorEntity = concreteOpMap.get(stateLink.operatorName);
        StateEntity stateEntity = stateMap.get(stateLink.stateName);

        StateLinkEntity linkEntity =
            new StateLinkEntity(operatorEntity, stateEntity, stateLinkName, container,
                stateLink.paramList);

        stateLinkMap.put(stateLinkName, linkEntity);
        Map<String, StateEntity> sMap = opStateMap.get(stateLink.operatorName);
        if (sMap == null) {
            sMap = new LinkedHashMap<String, StateEntity>();
            opStateMap.put(stateLink.operatorName, sMap);
        }
        sMap.put(stateLink.stateName, stateEntity);
        Map<String, OperatorEntity> opsMap = stateOpMap.get(stateLink.stateName);
        if (opsMap == null) {
            opsMap = new LinkedHashMap<String, OperatorEntity>();
            stateOpMap.put(stateLink.stateName, opsMap);
        }
        opsMap.put(stateLink.operatorName, operatorEntity);
        return linkEntity;
    }

    @Override public StateLinkEntity removeStateLink(String opName, String stateName)
        throws SemanticError {
        String stateLinkName = opName + ":" + stateName;
        checkExistence(stateLinkName, ObjectType.StateLink);
        nameTypeMap.remove(stateLinkName);
        StateLinkEntity linkEntity = stateLinkMap.remove(stateLinkName);
        Map<String, StateEntity> sMap = opStateMap.get(opName);
        sMap.remove(stateName);
        Map<String, OperatorEntity> opsMap = stateOpMap.get(stateName);
        opsMap.remove(opName);
        return linkEntity;
    }

    @Override public StateEntity getState(String stateName) throws SemanticError {
        checkExistence(stateName, ObjectType.State);
        return stateMap.get(stateName);
    }

    @Override public StateLinkEntity getStateLink(String operatorName, String stateName)
        throws SemanticError {
        String stateLinkName = operatorName + ":" + stateName;
        checkExistence(stateLinkName, ObjectType.StateLink);
        return stateLinkMap.get(stateLinkName);
    }

    @Override public Iterable<StateEntity> iterateStates() {
        return stateMap.values();
    }

    @Override public Iterable<StateLinkEntity> iterateStateLinks() {
        return stateLinkMap.values();
    }

    @Override public Iterable<StateEntity> getConnectedStates(String operatorName)
        throws SemanticError {
        checkExistence(operatorName, ObjectType.Operator);
        Map<String, StateEntity> sMap = opStateMap.get(operatorName);
        if (sMap == null) {
            return new LinkedList<StateEntity>();
        } else {
            return sMap.values();
        }
    }

    @Override public Iterable<OperatorEntity> getConnectedOperators(String stateName)
        throws SemanticError {
        checkExistence(stateName, ObjectType.State);
        Map<String, OperatorEntity> opsMap = stateOpMap.get(stateName);
        if (opsMap == null) {
            return new LinkedList<OperatorEntity>();
        } else {
            return opsMap.values();
        }
    }

    @Override public EntityName getContainer(String containerName) throws SemanticError {
        checkExistence(containerName, ObjectType.Container);
        return containerEntityNames.get(containerName);
    }

    @Override public Iterable<String> iterateContainers() {
        return containerEntityNames.keySet();
    }

    @Override public int getPragmaCount() {
        return pragmaEntityName.size();
    }

    @Override public Iterable<PragmaEntity> iteratePragmas() {
        return pragmaEntityName.values();
    }

    @Override public PragmaEntity getPragma(String pragmaName) throws SemanticError {
        return pragmaEntityName.get(pragmaName);
    }

    /* TODO: add switch */
    @Override public final SwitchEntity addSwitch(Switch s) throws SemanticError {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override public SwitchEntity removeSwitch(Switch s) throws SemanticError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public SwitchLinkEntity addSwitchConnect(SwitchLink switchConnect)
        throws SemanticError {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override public SwitchLinkEntity removeSwitchConnect(SwitchLink switchConnect)
        throws SemanticError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public final PragmaEntity addPragma(Pragma p) throws SemanticError {
        PragmaEntity entity = new PragmaEntity(p.pragmaName, p.pragmaValue);
        pragmaEntityName.put(p.pragmaName, entity);
        return entity;
    }

    @Override public PragmaEntity removePragma(String pragmaName) throws SemanticError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public int getStateCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public int getStateLinkCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public int getOperatorLinkCount() {
        return operatorLinkMap.size();
    }

    @Override public Iterable<OperatorLinkEntity> iterateOperatorLinks() {
        return operatorLinkMap.values();
    }

    @Override public String toString() {
        return "ExecutionPlanImpl{" + "\n\tnameTypeMap=" + nameTypeMap + ", \n\tpragmaEntityName="
            + pragmaEntityName + ", \n\tcontainerEntityNames=" + containerEntityNames
            + ", \n\tconcreteOpMap=" + concreteOpMap + ", \n\toperatorLinkMap=" + operatorLinkMap
            + ", \n\tstateMap=" + stateMap + ", \n\tstateLinkMap=" + stateLinkMap
            + ", \n\tswitchMap=" + switchMap + ", \n\tswitchLinkMap=" + switchLinkMap
            + ", \n\toperatorEntityGraph=" + operatorEntityGraph + ", \n\topStateMap=" + opStateMap
            + ", \n\tstateOpMap=" + stateOpMap + '}';
    }

}
