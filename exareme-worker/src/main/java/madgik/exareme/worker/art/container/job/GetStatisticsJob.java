/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobType;

/**
 * @author heraldkllapi
 */
public class GetStatisticsJob implements ContainerJob {

    public static final GetStatisticsJob instance = new GetStatisticsJob();

    public GetStatisticsJob() {
    }

    @Override public ContainerJobType getType() {
        return ContainerJobType.getStatistics;
    }
}
