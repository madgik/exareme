/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.event.closeSession;

import madgik.exareme.utils.eventProcessor.EventHandler;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class CloseSessionEventHandler implements EventHandler<CloseSessionEvent> {
    public static CloseSessionEventHandler instance = new CloseSessionEventHandler();
    private static Logger log = Logger.getLogger(CloseSessionEventHandler.class);

    @Override public void handle(CloseSessionEvent event, EventProcessor proc)
        throws RemoteException {
        if (event.cSID != null) {
            log.debug("Destroying container session : " + event.sID.getLongId());
            //TODO(Vag): uncomment and find bug...
            //      int count = event.session.destroySession(event.cSID);
            ////      event.bufferPool.destroyContainerSession(event.cSID, event.sID, true);
            //      event.status.getOperatorMeasurement().changeActiveValue(-count);
        } else {
            log.debug("Destroying plan session : " + event.sID.getLongId());
            int count = event.session.destroySession();
            //      event.bufferPool.destroySessions(event.sID, true);
            event.status.getOperatorMeasurement().changeActiveValue(-count);
        }
    }
}
