/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.arm.compute.ComputeSessionStatusManager;
import madgik.exareme.worker.arm.compute.ComputeSessionStatusManagerProxy;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiComputeSessionStatusManagerProxy extends RmiObjectProxy<ComputeSessionStatusManager>
        implements ComputeSessionStatusManagerProxy {

    public ArmComputeSessionID sessionID = null;

    public RmiComputeSessionStatusManagerProxy(String regEntryName, EntityName regEntityName) {
        super(regEntryName, regEntityName);
    }
}
