/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

import madgik.exareme.worker.art.container.adaptorMgr.AdaptorManagerStatus;
import madgik.exareme.worker.art.container.bufferMgr.BufferManagerStatus;
import madgik.exareme.worker.art.container.diskMgr.DiskManagerStatus;
import madgik.exareme.worker.art.container.netMgr.NetManagerStatus;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManagerStatus;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerStatus;

import java.io.Serializable;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ContainerStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    public final AdaptorManagerStatus adaptorStatus = new AdaptorManagerStatus();
    public final BufferManagerStatus bufferStatus = new BufferManagerStatus();
    public final DiskManagerStatus diskManagerStatus = new DiskManagerStatus();
    public final NetManagerStatus netManagerStatus = new NetManagerStatus();
    public final ConcreteOperatorManagerStatus operatorStatus = new ConcreteOperatorManagerStatus();
    public final StatisticsManagerStatus statisticsStatus = new StatisticsManagerStatus();
}
