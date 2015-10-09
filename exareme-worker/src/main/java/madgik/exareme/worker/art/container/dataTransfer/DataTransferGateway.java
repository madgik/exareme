/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.dataTransfer;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface DataTransferGateway {
    void start() throws RemoteException;

    void stop();

}
