package madgik.exareme.worker.art.container.diskMgr;

import madgik.exareme.worker.art.container.diskMgr.simple.SimpleDiskManager;
import madgik.exareme.worker.art.container.diskMgr.sync.SynchronizedDiskManager;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;

/**
 * @author herald
 */
public class DiskManagerInterfaceFactory {

    private DiskManagerInterfaceFactory() {
        throw new RuntimeException("Cannot create instances of this class");
    }

    public static DiskManagerInterface createSimpleDiskManager(DiskManagerStatus status,
                                                               StatisticsManagerInterface statistics) {
        SimpleDiskManager simpleDiskManager = new SimpleDiskManager(status, statistics);
        SynchronizedDiskManager wrapper = new SynchronizedDiskManager(simpleDiskManager);
        simpleDiskManager.setWrapper(wrapper);
        return wrapper;
    }
}
