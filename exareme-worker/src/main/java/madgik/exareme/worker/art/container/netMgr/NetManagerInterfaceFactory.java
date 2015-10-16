/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.netMgr;

import madgik.exareme.worker.art.container.netMgr.simple.SimpleNetManager;
import madgik.exareme.worker.art.container.netMgr.sync.SynchronizedNetManager;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;

/**
 * @author heraldkllapi
 */
public class NetManagerInterfaceFactory {

    private NetManagerInterfaceFactory() {
        throw new RuntimeException("Cannot create instances of this class");
    }

    public static NetManagerInterface createSimpleNetManager(NetManagerStatus status,
        StatisticsManagerInterface statistics) {
        SimpleNetManager simpleDiskManager = new SimpleNetManager(status, statistics);
        SynchronizedNetManager wrapper = new SynchronizedNetManager(simpleDiskManager);
        simpleDiskManager.setWrapper(wrapper);
        return wrapper;
    }
}
