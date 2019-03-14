/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.parser.expression;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * University of Athens / Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */

public class PlanExpression implements Serializable {

    private static final long serialVersionUID = 1L;
    public final LinkedList<Pragma> pragmaList;
    public final LinkedList<Container> containersList;
    public final LinkedList<Operator> operatorList;
    public final LinkedList<Switch> switchList;
    public final LinkedList<OperatorLink> operatorConnectList;
    public final LinkedList<SwitchLink> switchConnectList;
    public final LinkedList<State> stateList;
    public final LinkedList<StateLink> stateLinkList;

    public PlanExpression() {
        pragmaList = new LinkedList<Pragma>();
        containersList = new LinkedList<Container>();
        operatorList = new LinkedList<Operator>();
        operatorConnectList = new LinkedList<OperatorLink>();

        switchList = new LinkedList<Switch>();//oxi
        switchConnectList = new LinkedList<SwitchLink>();//oxi
        stateList = new LinkedList<State>();//oxi
        stateLinkList = new LinkedList<StateLink>();//oxi
    }

    public void addPragma(Pragma pragma) {
        pragmaList.add(pragma);
    }

    public void addContainer(Container container) {
        containersList.add(container);
    }

    public void addOperator(Operator instantiate) {
        operatorList.add(instantiate);
    }

    public void addSwitch(Switch s) {
        switchList.add(s);
    }

    public void addSwitchConnect(SwitchLink connect) {
        switchConnectList.add(connect);
    }

    public void addState(State state) {
        stateList.add(state);
    }

    public void addOperatorConnect(OperatorLink opLink) {
        operatorConnectList.add(opLink);
    }

    public void addStateLink(StateLink stateConnect) {
        stateLinkList.add(stateConnect);
    }

    public LinkedList<Pragma> getPragmaList() {
        return pragmaList;
    }

    public LinkedList<Container> getContainerList() {
        return containersList;
    }

    public LinkedList<Operator> getOperatorList() {
        return operatorList;
    }

    public LinkedList<OperatorLink> getOperatorLinkList() {
        return operatorConnectList;
    }

    public LinkedList<Switch> getSwitchList() {
        return switchList;
    }

    public LinkedList<SwitchLink> getSwitchLinkList() {
        return switchConnectList;
    }

    public LinkedList<State> getStateList() {
        return stateList;
    }

    public LinkedList<StateLink> getStateLinkList() {
        return stateLinkList;
    }

    @Override
    public String toString() {
        return "PlanExpression{\n" + "pragmaList=" + pragmaList + ", \ncontainersList="
                + containersList + ", \noperatorList=" + operatorList + ", \nswitchList=" + switchList
                + ", \noperatorConnectList=" + operatorConnectList;
    }

}
