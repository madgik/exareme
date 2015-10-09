/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.quantumClock;

import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.registry.ArtRegistry;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author herald kllapi
 */
public class GlobalQuantumClock extends QuantumClock {
    private static final Logger log = Logger.getLogger(GlobalQuantumClock.class);

    public GlobalQuantumClock(long warnTimeBeforeQuantum, long quantumSize) {
        super(warnTimeBeforeQuantum, quantumSize);
        this.setName("Global Quantum Clock");
    }

    @Override protected void clockWarningTick(long timeToTick_ms, long quantumCount) {
        try {
            ArtRegistry artRegistry = ArtRegistryLocator.getArtRegistryProxy().getRemoteObject();
            log.debug(
                "Send warning tick event to exec engines: " + quantumCount + " in " + timeToTick_ms
                    + " ms");
            ExecutionEngineProxy[] execEngines = artRegistry.createProxy().getExecutionEngines();
            for (ExecutionEngineProxy exec : execEngines) {
                try {
                    exec.connect().getClockTickManagerProxy()
                        .globalWarningClockTick(timeToTick_ms, quantumCount);
                } catch (RemoteException e) {
                    log.error("Cannot send tick event: " + exec.getRemoteObject().getRegEntryName(),
                        e);
                }
            }
        } catch (RemoteException e) {
            log.error("Cannot send tick event!", e);
        }
    }

    @Override protected void clockTick(long quantumCount) {
        try {
            ArtRegistry artRegistry = ArtRegistryLocator.getArtRegistryProxy().getRemoteObject();
            log.debug("Send tick event to exec engines: " + quantumCount);
            ExecutionEngineProxy[] execEngines = artRegistry.createProxy().getExecutionEngines();
            for (ExecutionEngineProxy exec : execEngines) {
                try {
                    exec.connect().getClockTickManagerProxy().globalClockTick(quantumCount);
                } catch (RemoteException e) {
                    log.error("Cannot send tick event: " + exec.getRemoteObject().getRegEntryName(),
                        e);
                }
            }
        } catch (RemoteException e) {
            log.error("Cannot send tick event!", e);
        }
    }
}
