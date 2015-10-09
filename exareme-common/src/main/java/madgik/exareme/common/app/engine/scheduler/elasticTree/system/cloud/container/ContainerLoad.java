/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud.container;

/**
 * @author heraldkllapi
 */
public class ContainerLoad {

    public final double cpuLoadDelta_sec;
    public final double dataLoadDelta_mb;
    public final double wallTime_sec;

    public ContainerLoad(double cpuLoad_sec, double dataLoad_MB, double wallTime_sec) {
        this.cpuLoadDelta_sec = cpuLoad_sec;
        this.dataLoadDelta_mb = dataLoad_MB;
        this.wallTime_sec = wallTime_sec;
    }
}
