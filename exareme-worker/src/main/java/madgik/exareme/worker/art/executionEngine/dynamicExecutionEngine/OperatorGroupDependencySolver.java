/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;

import madgik.exareme.common.optimizer.OperatorBehavior;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperator;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.Resources;
import madgik.exareme.worker.art.executionPlan.SemanticError;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * @author herald
 */
public class OperatorGroupDependencySolver {

    private static final Logger log = Logger.getLogger(OperatorGroupDependencySolver.class);
    private PlanEventSchedulerState state = null;
    private LinkedList<OperatorGroup> groupsToReschedule = null;
    private HashSet<OperatorEntity> operatorsWithActiveResources;

    public OperatorGroupDependencySolver(PlanEventSchedulerState state) {
        this.state = state;
        this.groupsToReschedule = new LinkedList<OperatorGroup>();
        this.operatorsWithActiveResources = new HashSet<>();
    }

    private boolean canBeProcessed(OperatorEntity coe,
        LinkedHashMap<String, OperatorGroup> readyOperatorGroupMap) {
        ActiveOperator activeOperator = state.getActiveOperator(coe.operatorName);
        if (activeOperator != null) {
            return false;
        }
        return !readyOperatorGroupMap.containsKey(coe.operatorName);
    }

    private OperatorGroup createNewGroup() {
        OperatorGroup group = new OperatorGroup(this.state.incrGroupCount(), state);
        return group;
    }

    private void addOperatorToGroup(OperatorEntity op, OperatorGroup group,
        LinkedHashMap<String, OperatorGroup> readyOperatorGroupMap) {
    /* Add the operator to the new group */
        group.operatorMap.put(op.operatorName, op);
        readyOperatorGroupMap.put(op.operatorName, group);
    /* Create and register active operator */
        ActiveOperator operator = new ActiveOperator(op, null, group);
        operator.isActive = true;
        state.addActiveObject(op.operatorName, operator);
    }

    public LinkedHashMap<Long, OperatorGroup> getActivatedGroups() throws SemanticError {
        LinkedHashMap<Long, OperatorGroup> readyGroups = new LinkedHashMap<Long, OperatorGroup>();
        LinkedHashMap<String, OperatorGroup> readyOperatorGroupMap =
            new LinkedHashMap<String, OperatorGroup>();
    /* Add groups that need to be rescheduled */
        boolean notEnoughRes = false;
        for (OperatorGroup g : groupsToReschedule) {
            readyGroups.put(g.groupID, g);
            for (OperatorEntity co : g.operatorMap.values()) {
                readyOperatorGroupMap.put(co.operatorName, g);
            }
        }
        groupsToReschedule.clear();
        // Put all data transfer operators in pairs
        for (OperatorEntity coe : state.getPlan().iterateOperators()) {
            // Process only the data transfer operators
            if (coe.type != OperatorType.dataTransfer) {
                continue;
            }
            if (canBeProcessed(coe, readyOperatorGroupMap) == false) {
                continue;
            }
            LinkedList<OperatorEntity> from = new LinkedList<OperatorEntity>();
            LinkedList<OperatorEntity> to = new LinkedList<OperatorEntity>();
            for (OperatorEntity fromOp : state.getPlan().getFromLinks(coe)) {
                from.add(fromOp);
            }
            for (OperatorEntity tpOp : state.getPlan().getToLinks(coe)) {
                to.add(tpOp);
            }
            //      if (from.size() * to.size() != 1) {
            //        throw new SemanticError("Data transfer links error!");
            //      }
            // The following prevent forming the same group twice
            if (from.get(0).type != OperatorType.processing) {
                continue;
            }
            //      if (to.get(0).type != OperatorType.dataTransfer) {
            //        throw new SemanticError("Data transfer links error! " + to.get(0).operatorName);
            //      }
            ActiveOperator fromActOp = state.getActiveOperator(from.get(0).operatorName);
            if (fromActOp == null) {
                continue;
            }
            OperatorGroup fromGroup = fromActOp.operatorGroup;
            if (fromGroup == null || fromGroup.hasTerminated == false) {
                continue;
            }
            // Check the resources
            Resources r1 = state.resourceManager.getAvailableResources(coe.container);
            // Resources r2 = state.resourceManager.getAvailableResources(to.get(0).container);
            if (r1.accuireIfAvailable(coe)) {// && r2.hasAvailable(to.get(0))) {
                operatorsWithActiveResources.add(coe);
                //  r2.accuireIfAvailable(to.get(0));
                OperatorGroup group = createNewGroup();
                addOperatorToGroup(coe, group, readyOperatorGroupMap);
                //   addOperatorToGroup(to.get(0), group, readyOperatorGroupMap);
                readyGroups.put(group.groupID, group);
            } else {
                log.debug("Not enough available resources, container: " + coe.containerName);
                notEnoughRes = true;
            }
        }
        boolean fixedPoint = false;
        while (!fixedPoint) {
            fixedPoint = true;
            for (OperatorEntity coe : state.getPlan()
                .iterateOperators()) {/////////////////////////////////////////////////////
                // Do not process here the data transfer operators
                if (coe.type == OperatorType.dataTransfer) {
                    continue;
                }
                if (canBeProcessed(coe, readyOperatorGroupMap) == false) {
                    continue;
                }
                if (coe.type == OperatorType.dataMaterialization) {
                    throw new SemanticError("Cannot handle data materialization yet!");
                }
                boolean add = true;
                for (OperatorEntity from : state.getPlan().getFromLinks(coe)) {
                    ActiveOperator fromActiveOperator = state.getActiveOperator(from.operatorName);
                    OperatorGroup fromGroup = null;
                    if (fromActiveOperator != null) {
                        fromGroup = fromActiveOperator.operatorGroup;
                    }
                    if (coe.behavior == OperatorBehavior.store_and_forward) {
            /* The input must come from terminated groups */
                        if (fromGroup == null
                            || fromGroup.hasTerminated == false) {//////////clean these ifs?
                            add = false;
                            break;
                        }
                    } else { // op is PL
                        if (from.behavior == OperatorBehavior.store_and_forward) {
              /* The input must come from terminated groups */
                            if (fromGroup == null || fromGroup.hasTerminated == false) {
                                add = false;
                                break;
                            }
                        } else {
              /* The input must come from terminated groups or from ready PL operators */
                            if ((fromGroup == null || fromGroup.hasTerminated == false)
                                && readyOperatorGroupMap.containsKey(from.operatorName) == false) {
                                add = false;
                                break;
                            }
                        }
                    }
                }
                if (add) {
          /* Check the space shared resources */
                    Resources ac = state.resourceManager.getAvailableResources(coe.container);
                    if (ac.accuireIfAvailable(coe)) {
                        LinkedList<OperatorGroup> mergeWith = new LinkedList<OperatorGroup>();
                        operatorsWithActiveResources.add(coe);
            /* Check if some groups must be merged together */
                        for (OperatorEntity from : state.getPlan()
                            .getFromLinks(coe)) {///////////more perf
                            OperatorGroup g = readyOperatorGroupMap.get(from.operatorName);
                            if (g != null) {
                                mergeWith.add(g);
                            }
                        }
                        OperatorGroup group = createNewGroup();
                        addOperatorToGroup(coe, group, readyOperatorGroupMap);
            /* Merge with other groups */
                        for (OperatorGroup m : mergeWith) {
              /* Add all operators to the new group */
                            for (OperatorEntity op : m.operatorMap.values()) {
                                group.operatorMap.put(op.operatorName, op);
                                readyOperatorGroupMap.put(op.operatorName, group);
                                ActiveOperator acOp = state.getActiveOperator(op.operatorName);
                                acOp.operatorGroup = group;
                            }
                            readyGroups.remove(m.groupID);
                        }
                        readyGroups.put(group.groupID, group);
                        fixedPoint = false;
                    } else {
                        log.debug(
                            "Not enough available resources, container: " + coe.containerName);
                        notEnoughRes = true;
                    }
                }
            }
        }
        state.resourceManager.printUsage("Activated");
        if (readyGroups.isEmpty() && notEnoughRes) {
            return null;
        }
        return readyGroups;
    }

    public void setTerminated(OperatorGroup group) {

        for (OperatorEntity entity : group.operatorMap.values()) {
            Resources ac = state.resourceManager.getAvailableResources(entity.container);
            ac.releaseMemory(entity);
            operatorsWithActiveResources.remove(entity);
            ActiveOperator acOp = state.getActiveOperator(entity.operatorName);
            acOp.isActive = false;
            acOp.hasTerminated = true;
            if (entity.type == OperatorType.processing) {
                this.state.incrTerminatedOperatorCount();
                state.getPlanSession().getPlanSessionStatus()
                    .operatorFinished(entity.operatorName, acOp.exitCode, acOp.exitMessage,
                        acOp.exitDate);
            }
        }
        state.resourceManager.printUsage("Terminated");
    }

    public void rescheduleGroup(OperatorGroup group) {
        for (OperatorEntity op : group.operatorMap.values()) {
            //this map will be updated with new active operators as outputs
            op.clearLinkMap();
        }
        groupsToReschedule.add(group);
    }

    public void rescheduleDTGroup(OperatorGroup group) {
        groupsToReschedule.add(group);
    }

    public HashSet<OperatorEntity> getOperatorsWithActiveResources() {
        return operatorsWithActiveResources;
    }
}
