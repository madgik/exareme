/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator.manager;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Date;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ExceptionManager {
    private static Logger log = Logger.getLogger(ExceptionManager.class);
    private SessionManager sessionManager = null;

    public ExceptionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public final void reportException(String msg) {
        try {
            log.debug(msg);
            sessionManager.getSessionReportID().reportManagerProxy
                    .operatorError(sessionManager.getOpID(), new ServerException(msg), new Date(),
                            sessionManager.getContainerID());
        } catch (RemoteException e) {
            log.error(e.getMessage());
        }
    }

    public final void reportException(String msg, Exception ex) {
        try {
            log.debug(msg);
            sessionManager.getSessionReportID().reportManagerProxy
                    .operatorError(sessionManager.getOpID(), new ServerException(msg, ex), new Date(),
                            sessionManager.getContainerID());
        } catch (RemoteException e) {
            log.error(e.getMessage());
        }
    }
}
