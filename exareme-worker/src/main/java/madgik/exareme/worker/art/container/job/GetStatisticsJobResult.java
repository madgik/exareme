/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.common.art.ContainerSessionStatistics;
import madgik.exareme.worker.art.container.ContainerJobResult;
import madgik.exareme.worker.art.container.ContainerJobType;

/**
 * @author heraldkllapi
 */
public class GetStatisticsJobResult extends ContainerJobResult {

    private ContainerSessionStatistics stats = null;

    public GetStatisticsJobResult(ContainerSessionStatistics stats) {
        this.stats = stats;
    }

    @Override
    public ContainerJobType getType() {
        return ContainerJobType.getStatistics;
    }

    public ContainerSessionStatistics getStats() {
        return stats;
    }
}
