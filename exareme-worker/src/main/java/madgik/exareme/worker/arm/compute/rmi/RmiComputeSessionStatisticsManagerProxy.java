/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.arm.compute.ComputeSessionStatisticsManager;
import madgik.exareme.worker.arm.compute.ComputeSessionStatisticsManagerProxy;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

/**
 * @author herald
 */
public class RmiComputeSessionStatisticsManagerProxy
        extends RmiObjectProxy<ComputeSessionStatisticsManager>
        implements ComputeSessionStatisticsManagerProxy {

    public ArmComputeSessionID sessionID = null;

    public RmiComputeSessionStatisticsManagerProxy(String regEntryName, EntityName regEntityName) {
        super(regEntryName, regEntityName);
    }
}
