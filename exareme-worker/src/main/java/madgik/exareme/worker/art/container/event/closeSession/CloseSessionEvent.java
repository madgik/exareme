/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.event.closeSession;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.utils.eventProcessor.Event;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManagerStatus;
import madgik.exareme.worker.art.container.operatorMgr.thread.TCOMPlanSession;

//import madgik.exareme.db.art.container.bufferPoolMgr.BufferPoolManagerInterface;


/**
 * @author herald
 */
public class CloseSessionEvent implements Event {
    private static final long serialVersionUID = 1L;
    public PlanSessionID sID = null;
    public ContainerSessionID cSID = null;
    public TCOMPlanSession session = null;
    //  public BufferPoolManagerInterface bufferPool = null;
    public ConcreteOperatorManagerStatus status = null;

    public CloseSessionEvent(PlanSessionID sID, TCOMPlanSession session,
        //                           BufferPoolManagerInterface bufferPool,
        ConcreteOperatorManagerStatus status) {
        this.sID = sID;
        this.session = session;
        //    this.bufferPool = bufferPool;
        this.status = status;
    }

    public CloseSessionEvent(PlanSessionID sID, ContainerSessionID cSID, TCOMPlanSession session,
        //                           BufferPoolManagerInterface bufferPool,
        ConcreteOperatorManagerStatus status) {
        this.sID = sID;
        this.cSID = cSID;
        this.session = session;
        //    this.bufferPool = bufferPool;
        this.status = status;
    }
}
