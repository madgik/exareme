/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator.manager;

import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.ContainerSessionStatistics;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorInfo;
import madgik.exareme.worker.art.container.ContainerID;
import madgik.exareme.worker.art.container.monitor.OperatorStatus;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class SessionManager {
    private String operatorName = null;
    private String operatorCategory = null;
    private OperatorType operatorType = null;
    private ConcreteOperatorID opID = null;
    private PlanSessionReportID sessionReportID = null;
    private OperatorStatus operatorStatus = null;
    private ConcreteOperatorStatistics operatorStatistics = null;
    private ContainerSessionStatistics sessionStatistics = null;
    private ContainerSessionID containerSessionID = null;
    private ConcreteOperatorInfo operatorInfo = null;
    private PlanSessionID sessionID = null;
    private ContainerID containerID = null;

    public SessionManager(String operatorName, String category, OperatorType type,
                          ConcreteOperatorID opID, ContainerSessionID containerSessionID, PlanSessionID sessionID,
                          PlanSessionReportID sessionReportID, OperatorStatus operatorStatus,
                          ContainerID containerID) {
        this.operatorName = operatorName;
        this.operatorCategory = category;
        this.operatorType = type;
        this.opID = opID;
        this.containerSessionID = containerSessionID;
        this.sessionID = sessionID;
        this.sessionReportID = sessionReportID;
        this.operatorStatus = operatorStatus;
        this.operatorInfo = new ConcreteOperatorInfo(operatorName);
        this.containerID = containerID;
    }

    public OperatorType getOperatorType() {
        return operatorType;
    }

    public ConcreteOperatorStatistics getOperatorStatistics() {
        return operatorStatistics;
    }

    public ConcreteOperatorInfo getInfo() {
        return operatorInfo;
    }

    public ContainerSessionStatistics getSessionStatistics() {
        return sessionStatistics;
    }

    public void setSessionStatistics(ContainerSessionStatistics sessionStatistics) {
        this.sessionStatistics = sessionStatistics;
        this.operatorStatistics = sessionStatistics
                .createOperatorStatistics(operatorName, operatorCategory, operatorType);
    }

    public ConcreteOperatorID getOpID() {
        return opID;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public PlanSessionReportID getSessionReportID() {
        return sessionReportID;
    }

    public OperatorStatus getOperatorStatus() {
        return operatorStatus;
    }

    public ContainerSessionID getContainerSessionID() {
        return containerSessionID;
    }

    public PlanSessionID getSessionID() {
        return sessionID;
    }

    public ContainerID getContainerID() {
        return containerID;
    }
}
