/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobType;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;

/**
 * @author heraldkllapi
 */
public class TableTransferJob implements ContainerJob {
    public final PlanSessionReportID sessionReportID;

    public TableTransferJob(PlanSessionReportID sessionReportID) {
        this.sessionReportID = sessionReportID;
    }

    @Override public ContainerJobType getType() {
        return ContainerJobType.dataTransfer;
    }
}
