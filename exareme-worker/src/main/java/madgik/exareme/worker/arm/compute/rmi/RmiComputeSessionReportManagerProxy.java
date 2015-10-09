/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.arm.compute.ComputeSessionReportManager;
import madgik.exareme.worker.arm.compute.ComputeSessionReportManagerProxy;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

/**
 * @author Herald Kllapi <br> University of Athens / Department of Informatics
 *         and Telecommunications.
 * @since 1.0
 */
public class RmiComputeSessionReportManagerProxy extends RmiObjectProxy<ComputeSessionReportManager>
    implements ComputeSessionReportManagerProxy {
    public ArmComputeSessionID sessionID = null;

    public RmiComputeSessionReportManagerProxy(String regEntryName, EntityName regEntityName) {
        super(regEntryName, regEntityName);
    }
}
