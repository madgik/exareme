/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.eventProcessor;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * @param <T>
 * @author herald
 */
public interface EventListener<T extends Event> extends Serializable {

    void processed(T event, RemoteException exception, EventProcessor processor);
}
